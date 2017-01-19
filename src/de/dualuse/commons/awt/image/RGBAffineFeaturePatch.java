package de.dualuse.commons.awt.image;

import java.awt.geom.AffineTransform;

public class RGBAffineFeaturePatch extends AffineFeaturePatch {

	protected int[] G, H;
	protected int[] L, K;

	protected void allocate() {
		//more scratch-space
		G = I.clone();
		H = I.clone();

		L = J.clone();
		K = J.clone();
	}
	
	public RGBAffineFeaturePatch(int radius) {
		super(radius);
		allocate();
	}

	public RGBAffineFeaturePatch(int radius, double sigma) {
		super(radius, sigma);
		allocate();
	}

	
	@Override
	public FeaturePatch track(
			AffineTransform from, int fromWidth, int fromHeight, int[] fromPixels, int fromOffset, int fromScan, 
			AffineTransform to, int toWidth, int toHeight, int[] toPixels, int toOffset, int toScan) {
		
		final int s = radius*2+1;
		
		//grab pixel patches with sub-pixel accuracy to I and J 
		grab(from, s, s, fromWidth, fromHeight, fromPixels, fromOffset, fromScan,  G, H, I);
		grab(to, s, s, toWidth, toHeight, toPixels, toOffset, toScan,  J, K, L);

		//grab pixel patches with sub-pixel accuracy to rI/gI/bI and rJ/gJ/bJ, and alpha-mask to weights
//		grab(fromCenterX-radius, fromCenterY-radius, s, s, fromWidth, fromHeight, fromPixels, fromOffset, fromScan,  G, H, I );
//		grab(toCenterX-radius, toCenterY-radius, s, s, toWidth, toHeight, toPixels, toOffset, toScan,  J, K, L );

		//includes masked "weights"
		computeAffine(G,J);
		computeAffine(H,K);
		computeAffine(I,L);
		
		return this;

	}
	
	public static void grab(
			AffineTransform from, int w, int h,
			int clipWidth, int clipHeight, int[] p, int off, int scan, 
			int[] toR, int[] toG, int[] toB
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
				final int ul = p[O]     , ur = p[O+1];
				final int ll = p[O+scan], lr = p[O+1+scan];
				
				toB[o] = ((ul&0xFF)*omxr+(ur&0xFF)*xr*omyr+((ll&0xFF)*omxr+(lr&0xFF)*xr)*yr)>>>DP;
				toG[o] = (((ul>>>8)&0xFF)*omxr+((ur>>>8)&0xFF)*xr*omyr+(((ll>>>8)&0xFF)*omxr+((lr>>>8)&0xFF)*xr)*yr)>>>DP;
				toR[o] = (((ul>>>16)&0xFF)*omxr+((ur>>>16)&0xFF)*xr*omyr+(((ll>>>16)&0xFF)*omxr+((lr>>>16)&0xFF)*xr)*yr)>>>DP;
			}
		
	}

}



