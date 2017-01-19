package de.dualuse.commons.awt.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;

public class AffineFeaturePatch extends FeaturePatch {

	public AffineFeaturePatch(int radius) {
		super(radius);
	}
	
	public AffineFeaturePatch(int radius, double sigma) {
		super(radius,sigma);
	}
	

//	protected long gxx = 0, gxy = 0, gyy = 0;
//	protected long e = 0, n = 0;
//	protected long ex = 0, ey = 0;
	
	protected long exgx = 0, eygx = 0;
	protected long exgy = 0, eygy = 0;
	
	
	/// V
	protected long xgxx  ,   xgxy,   ygxx,   ygxy;
	protected long /*xgxy,*/ xgyy, /*ygxy,*/ ygyy;
	
	/// U
	protected long xxgxx;//, xxgxy, xygxx,   xygxy; 
	protected long xxgxy,  xxgyy;//,xygxy,   xygyy; 
	protected long xygxx,/*xygxy,*/ yygxx;//,yygxy; 
	protected long xygxy,  xygyy,   yygxy,   yygyy; 

	
	double S[][] = new double[6][6];
	double T[][] = new double[6][6];
	double L[][] = new double[6][6];
	double U[][] = new double[6][6];
	
	double z[] = new double[6];
	double a[] = new double[6];
	double b[] = new double[6];
	
	AffineTransform pt = new AffineTransform();
	
	public FeaturePatch track(
			AffineTransform from, int fromWidth, int fromHeight, int[] fromPixels,int fromOffset, int fromScan, 
			AffineTransform to, int toWidth, int toHeight, int[] toPixels,	int toOffset, int toScan) {
		
		final int s = radius*2+1;
		
		//grab pixel patches with sub-pixel accuracy to I and J 
		grab(from, s, s, fromWidth, fromHeight, fromPixels, fromOffset, fromScan,  I);
		grab(to, s, s, toWidth, toHeight, toPixels, toOffset, toScan,  J);
		
		computeAffine(I, J);
		
				return this;
	}
	
	
	@Override
	public AffineFeaturePatch reset() {
		super.reset();
		
		for (int row=0;row<6;row++)
			for (int col=0;col<6;col++)
				a[row] = T[row][col] = 0;
		
		return this;		
	}
	
	protected void computeAffine(int[] I, int[] J) {
		final int s = radius*2+1;
		
		//accumulate matrix coefficients
		final int start = -radius+1, end = radius, r = s-(end-start);
		
		for (int y=start,o=1+s;y!=end;y++,o+=r)
			for (int x=start;x!=end;x++,o++) {
				final int delta = I[ o ] - J[ o ];
				
				final int gx = ((I[ o+1 ]-I[ o-1 ]) + (J[ o+1 ]-J[ o-1 ])); //gx/gy as in birchfield's symmetric derivation 1997
				final int gy = ((I[ o+s ]-I[ o-s ]) + (J[ o+s ]-J[ o-s ]));
				
//				final int gx = (J[ o+1 ]-J[ o-1 ]); //gx/gy as in original paper 1993
//				final int gy = (J[ o+s ]-J[ o-s ]);

				float weight = weights[o];

				z[0] = x*gx;
				z[1] = x*gy;
				z[2] = y*gx;
				z[3] = y*gy;
				z[4] = gx;
				z[5] = gy;
				
				for (int row=0;row<6;row++)
					for (int col=0;col<6;col++)
						T[row][col] += z[row]*z[col]*weight;
				
				for (int row=0;row<6;row++)
					a[row] += delta*z[row]*weight;
				
				gxx += gx*gx*weight; 
				gxy += gx*gy*weight; 
				gyy += gy*gy*weight;
				
 				egx += delta* gx * weight;
 				egy += delta* gy * weight; 
 				
				n += weight;
				e += delta*delta * weight;
			}
		
		
		for (int row=0;row<6;row++)
			for (int col=0;col<6;col++)
				S[row][col] = T[row][col];
		
		for (int row=0;row<6;row++)
			b[row] = a[row];
		
		decompose(6, S, L, U);
		solve(6, L, U, b, z); // XXX Overwrites b!
		
		
		// solve 2x2 linear equation system using current coefficient's values 
		float ooDet = 1f/(gxx * gyy - gxy * gxy);
		translateX = (gyy * egx - gxy * egy) * ooDet;
		translateY = (gxx * egy - gxy * egx) * ooDet;
		
		pt.setTransform(1+z[0],z[1],z[2],1+z[3], z[4], z[5]);
		
		error = e*1f/n;
	}
	
	
	public static void grab(
			AffineTransform from, int w, int h,
			int clipWidth, int clipHeight, int[] pixels, int off, int scan, 
			int[] to
			)  {

		// the sum of 4 neighboring 8 bit pixels may require 10bit to store (4x 255 = 1020), leaving 22 bit for fixed-point decimals during computations (DP = 22, FP = 11) 
		final int FP = 11, DP = FP*2;
		final int ONE = 1<<FP;
		final int MASK = (ONE)-1;
		
		AffineTransform i = from;
		
		int i00 = (int) (i.getScaleX()*ONE), i01 = (int) (i.getShearX()*ONE), i02 = (int) (i.getTranslateX()*ONE);
		int i10 = (int) (i.getShearY()*ONE), i11 = (int) (i.getScaleY()*ONE), i12 = (int) (i.getTranslateY()*ONE);
		
		final int rx = w/2, ry = h/2;
		
		int x0 = -rx*i00+-ry*i01+i02;
		int y0 = -rx*i10+-ry*i11+i12;
		int _x = x0, _y = y0;
		
		for (int y=-ry,o=0;y<=ry;y++, _x=x0+=i01, _y=y0+=i11)
			for (int x=-rx;x<=rx;x++,o++,_x+=i00,_y+=i10) {
				final int xr = _x&MASK, omxr = ONE-xr, yr = _y&MASK, omyr = ONE-yr;
				
				final int O = (_x>>>FP)+(_y>>>FP)*scan+off;
				final int ul = pixels[O]     , ur = pixels[O+1];
				final int ll = pixels[O+scan], lr = pixels[O+1+scan];
				
				final int u = ul*omxr+ur*xr;
				final int l = ll*omxr+lr*xr;
				final int c = u*omyr+l*yr;				
								
				to[o] = c>>>DP;
			}
		
	}
	
//	public static void grabf(
//			AffineTransform from, int w, int h,
//			int clipWidth, int clipHeight, int[] pixels, int off, int scan, 
//			int[] to
//			)  {
//
//		AffineTransform i = from;
//		
//		float m00 = (float) i.getScaleX(), m01 = (float) i.getShearX(), m02 = (float) i.getTranslateX();
//		float m10 = (float) i.getShearY(), m11 = (float) i.getScaleY(), m12 = (float) i.getTranslateY();
//		
//		final int rx = w/2, ry = h/2;
//		
//		float x0 = -rx*m00+-ry*m01+m02;
//		float y0 = -rx*m10+-ry*m11+m12;
//		float _x = x0, _y = y0;
//		
//		for (int y=-ry,o=0;y<=ry;y++, _x=x0+=m01, _y=y0+=m11)
//			for (int x=-rx;x<=rx;x++,o++,_x+=m00,_y+=m10) {
//				int x_ = (int)(_x), y_ = (int)(_y);
//				float xr = _x-x_, yr = _y-y_;
//				
//				int O = x_+y_*scan+off;
//				int ul = pixels[O]     , ur = pixels[O+1];
//				int ll = pixels[O+scan], lr = pixels[O+1+scan];
//
//				float u = ul*(1-xr)+ur*xr;
//				float l = ll*(1-xr)+lr*xr;
//				
//				float c = u*(1-yr)+l*yr;				
//								
//				to[o] = (int)c;
//			}
//		
//	}


//	 decompose
	protected static void decompose(int n, double[][] M, double[][] L, double[][] U){
		
	    // Code: Cormen et al., page 756
	    int i, j, k;
	    for ( k = 0; k < n; ++k) {
	        U[ k][ k] = M[ k][ k];
	        for ( i = k+1; i < n; ++i) {
	            L[ i][ k] = M[ i][ k] / U[ k][ k];
	            U[ k][ i] = M[ k][ i];
	        }
	        for( i = k+1; i < n; ++i) {
	            for( j = k+1; j < n; ++j) {
	                M[ i][ j] = M[ i][ j] - L[ i][ k]*U[ k][ j];
	            }
	        }
	    }
	}

	// solve
	protected static void solve(int n, double[][] L, double[][] U, double[] y, double[] x) {
		
	    // Code: Cormen et al., page 756
//	    double[] y = b;//new double[n];
	    int i, j;

	    // forward substitution
	    for ( i = 0; i < n; ++i) {
//	        y[ i] = b[ i];
	        for ( j = 0; j < i; ++j) {
	            y[ i] -= L[ i][ j] * y[ j];
	        }
	    }

	    // back substitution
	    for ( i = n-1; i >= 0; --i) {
	        x[ i] = y[ i];
	        for ( j = i+1; j < n; ++j) {
	            x[ i] -= U[ i][ j] * x[ j];
	        }
	        x[ i] /= U[ i][ i];
	    }
	}
	
};

















