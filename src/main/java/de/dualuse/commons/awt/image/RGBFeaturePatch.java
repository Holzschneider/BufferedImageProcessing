package de.dualuse.commons.awt.image;

import java.util.Arrays;

public class RGBFeaturePatch extends FeaturePatch {

	protected int[] G, H;
	protected int[] L, K;
	
	
	public RGBFeaturePatch(int radius) {
		this(radius, radius/2.345);
	}
	
	public RGBFeaturePatch(int radius, double sigma) {
		super(radius, sigma);
		
		//more scratch-space
		G = I.clone();
		H = I.clone();

		L = J.clone();
		K = J.clone();
	}
	
	
	@Override
	public FeaturePatch track(	double fromCenterX, double fromCenterY, 
								int fromWidth, int fromHeight, int[] fromPixels, int fromOffset, int fromScan, 
								
								double toCenterX, double toCenterY, 
								int toWidth, int toHeight, int[] toPixels, int toOffset, int toScan ) 
	{
		final int s = radius*2+1;
		
		//grab pixel patches with sub-pixel accuracy to rI/gI/bI and rJ/gJ/bJ, and alpha-mask to weights
		grab(fromCenterX-radius, fromCenterY-radius, s, s, fromWidth, fromHeight, fromPixels, fromOffset, fromScan,  G, H, I );
		grab(toCenterX-radius, toCenterY-radius, s, s, toWidth, toHeight, toPixels, toOffset, toScan,  J, K, L );

		//includes masked "weights"
		compute(G,J);
		compute(H,K);
		compute(I,L);
		
		return this;
	}
	
	
	public static void grab(double startX, double startY, int w, int h, int clipWidth, int clipHeight, int[] pixels, int off, int scan, int[] toR, int[] toG, int[] toB ) {
		final int sx = (int) Math.floor(startX), sy = (int) Math.floor(startY);
				
		final float ur = (float)startX-sx, vr = (float)startY-sy, uo = 1 - ur, vo = 1 - vr; // FIXED-POINT ARITHMETHIC
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
		
		int left = sx<1?-sx:0, top = sy<1?-sy:0, right = sx+w>clipWidth-1?sx+w-(clipWidth-1):0, bottom = sy+h>clipHeight-1?sy+h-(clipHeight-1):0;
		if (left>0 || right>0 || top>0 || bottom>0) { Arrays.fill(toR, 0); Arrays.fill(toG, 0); Arrays.fill(toB, 0); }
		
		for (int j=top,J=h-bottom,o=left+top*w,p=left+right,O=off+sx+left+(sy+top)*scan,P=scan-(w-right-left);j<J;j++,O+=P,o+=p) 
			for (int i=left,I=w-right, q=O, c=0,r=0,e=0,d=0 ;i<I;i++,o++,O++) {
				toB[o] = ((((c=pixels[q=O])&0xFF)*uovo+((r=pixels[q+=1])&0xFF)*urvo+((e=pixels[q+=scan])&0xFF)*urvr+((d=pixels[q-=1])&0xFF)*uovr)>>>8);
				toG[o] = (((((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr))>>>8);
				toR[o] = (((((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr))>>>8);
			}
	}
}
