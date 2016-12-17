package de.dualuse.commons.awt.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class AffineFeaturePatch extends FeaturePatch {

	public AffineFeaturePatch(int radius) {
		super(radius);
	}


//	protected long gxx = 0, gxy = 0, gyy = 0;
//	protected long e = 0, n = 0;
//	protected long ex = 0, ey = 0;
	
	/// V
	protected long xgxx  ,   xgxy,   ygxx,   ygxy;
	protected long /*xgxy,*/ xgyy, /*ygxy,*/ ygyy;
	
	/// U
	protected long xxgxx;//, xxgxy, xygxx,   xygxy; 
	protected long xxgxy,  xxgyy;//,xygxy,   xygyy; 
	protected long xygxx,/*xygxy,*/ yygxx;//,yygxy; 
	protected long xygxy,  xygyy,   yygxy,   yygyy; 

	
	double T[][] = new double[6][6];
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
		
		//accumulate matrix coefficients
		final int start = -radius+1, end = radius, r = s-(end-start);
		for (int y=start,o=1+s;y!=end;y++,o+=r)
			for (int x=start;x!=end;x++,o++) {
				final int delta = I[ o ] - J[ o ];
				
				final int gxSum = (I[ o+1 ]-I[ o-1 ])+ (J[ o+1 ]-J[ o-1 ]);
				final int gySum = (I[ o+s ]-I[ o-s ])+ (J[ o+s ]-J[ o-s ]);
				
				float weight = weights[o];
				gxx += gxSum*gxSum * weight;
				gxy += gxSum*gySum * weight;
				gyy += gySum*gySum * weight;
				
				ex += delta* gxSum * weight;
				ey += delta* gySum * weight; 
				
				n += weight;
				e += delta*delta * weight;
			}
		
		// solve 2x2 linear equation system using current coefficient's values 
		float ooDet = 1f/(gxx * gyy - gxy * gxy);
		translateX = (gyy * ex - gxy * ey) * ooDet;
		translateY = (gxx * ey - gxy * ex) * ooDet;
		
		
		System.out.println("### "+translateX+", "+translateY);
		pt.setTransform(1,0,0,1, translateX, translateY);
		
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


};

















