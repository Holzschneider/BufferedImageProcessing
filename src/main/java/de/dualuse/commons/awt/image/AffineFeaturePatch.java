package de.dualuse.commons.awt.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;

public class AffineFeaturePatch extends FeaturePatch {

	public AffineFeaturePatch(int radius) {
		super(radius);
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

	
	double T[][] = new double[6][6];
	double L[][] = new double[6][6];
	double U[][] = new double[6][6];
	
	double z[] = new double[6];
	double a[] = new double[6];
	
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
		return this;		
	}
	
	protected void computeAffine(int[] I, int[] J) {
		final int s = radius*2+1;
		
		System.out.println("miep");
		//accumulate matrix coefficients
		final int start = -radius+1, end = radius, r = s-(end-start);
		
		for (int row=0;row<6;row++)
			for (int col=0;col<6;col++)
				a[row] = T[row][col] = 0;
		
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
				
				gxx += gx*gx*weight; //ok
				gxy += gx*gy*weight; //ok //ok
				gyy += gy*gy*weight; //ok
				
 				egx += delta* gx * weight;
 				egy += delta* gy * weight; 
 				
				n += weight;
				e += delta*delta * weight;
			}
		
		Arrays.fill(z, 0);
		decompose(6, T, L, U);
		solve(6, L, U, a, z);
		
//		System.out.println( (long)T[4][4]+" "+(long)T[4][5]+" * tx =  "+(long)a[4]);
//		System.out.println( (long)T[5][4]+" "+(long)T[5][5]+" * ty =  "+(long)a[5]);
//		System.out.println( " vs ");
//		System.out.println( gxx+" "+gxy+" * tx =  "+egx);
//		System.out.println( gxy+" "+gyy+" * ty =  "+egy);
		
		// solve 2x2 linear equation system using current coefficient's values 
		float ooDet = 1f/(gxx * gyy - gxy * gxy);
		translateX = (gyy * egx - gxy * egy) * ooDet;
		translateY = (gxx * egy - gxy * egx) * ooDet;
		
		
		System.out.println("### "+translateX+", "+translateY+ " vs "+z[4]+", "+z[5]);
//		pt.setTransform(1,0,0,1, translateX, translateY);
		pt.setTransform(1+z[0],z[1],z[2],1+z[3], z[4], z[5]);
		
		error = e*1f/n;
	}
	
	

	public static void grab(
			AffineTransform from, int w, int h,
			int clipWidth, int clipHeight, int[] pixels, int off, int scan, 
			int[] to
			)  {

		AffineTransform i = from;
		
		int rx = w/2, ry = h/2;
		
		for (int y=-ry,o=0;y<=ry;y++)
			for (int x=-rx;x<=rx;x++,o++) {
				Point2D p = i.transform(new Point2D.Double(x,y), new Point2D.Double());
				
				int x_ = (int) p.getX(), y_ = (int) p.getY();
				double xr = p.getX()-x_, yr = p.getY()-y_;
				
				int O = x_+y_*scan+off;
				int ul = pixels[O]     , ur = pixels[O+1];
				int ll = pixels[O+scan], lr = pixels[O+1+scan];

				double u = ul*(1-xr)+ur*xr;
				double l = ll*(1-xr)+lr*xr;
				
				double c = u*(1-yr)+l*yr;				
								
				to[o] = (int)c;
			}
		
	}


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

















