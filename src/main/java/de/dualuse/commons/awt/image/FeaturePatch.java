package de.dualuse.commons.awt.image;

import java.util.Arrays;


public class FeaturePatch {
	private final static int WEIGHT_FIXED_POINT_RESOLUTION = 64;
	
	//////////// Scratch Space
	// Temporary Pixel Space
	protected int radius = 0;
	protected int[] weights;
	
	protected int[] I;
	protected int[] J;
	
	// Coefficients
	protected long gxx = 0, gxy = 0, gyy = 0;
	protected long e = 0, n = 0;
	protected long ex = 0, ey = 0;
	
	/////////// Results
	public float error = 0;
	public float translateX = 0, translateY = 0;
	
	
	public FeaturePatch(int radius) {
		this(radius, radius/2.345);
	}
	
	// ALWAYS Square!! width/height = radius*2+1 
	public FeaturePatch(int radius, double sigma) {
		this.radius = radius;
		int size = radius*2+1;
		
		double sigmaSq = sigma*sigma;;
		int sizesize = size*size;
		weights = new int[sizesize];
		
		for (int y=0,o=0,v=-radius;y<size;y++,v++)
			for (int x=0,u=-radius;x<size;x++,o++,u++) 
				weights[o] = (int) (Math.exp(-0.5* (u*u+v*v) / sigmaSq)*WEIGHT_FIXED_POINT_RESOLUTION);
		
		I = new int[sizesize];
		J = new int[sizesize];
		
	}
	
	public FeaturePatch reset() {
		translateX = translateY = 0;

		gxx = gxy = gyy = 0;
		e = n = 0;
		ex = ey = 0;
		
		return this;
	}
	

	
	
	public FeaturePatch track(	double fromCenterX, double fromCenterY, 
									int fromWidth, int fromHeight, int[] fromPixels, int fromOffset, int fromScan, 
									
									double toCenterX, double toCenterY, 
									int toWidth, int toHeight, int[] toPixels, int toOffset, int toScan ) 
	{
		final int s = radius*2+1;
		
		//grab pixel patches with sub-pixel accuracy to I and J 
		grab(fromCenterX-radius, fromCenterY-radius, s, s, fromWidth, fromHeight, fromPixels, fromOffset, fromScan,  I);
		grab(toCenterX-radius, toCenterY-radius, s, s, toWidth, toHeight, toPixels, toOffset, toScan,  J);
		
		compute(I, J);
		
		return this;
	}
	
	protected void compute(int[] I, int[] J) {
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
		
		error = e*1f/n;
	}
	

	public static void grab(double startX, double startY, int w, int h, int clipWidth, int clipHeight, int[] pixels, int off, int scan, int[] to) {
		final int sx = (int) Math.floor(startX), sy = (int) Math.floor(startY);
		
		final float ur = (float)startX-sx, vr = (float)startY-sy, uo = 1 - ur, vo = 1 - vr; // FIXED-POINT ARITHMETHIC
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));

		int left = sx<1?-sx:0, top = sy<1?-sy:0, right = sx+w>clipWidth-1?sx+w-(clipWidth-1):0, bottom = sy+h>clipHeight-1?sy+h-(clipHeight-1):0;
		if (left>0 || right>0 || top>0 || bottom>0) Arrays.fill(to, 0);
		
		for (int j=top,J=h-bottom,o=left+top*w,p=left+right,O=off+sx+left+(sy+top)*scan,P=scan-(w-right-left);j<J;j++,O+=P,o+=p) 
			for (int i=left,I=w-right, q=O ;i<I;i++,o++,O++) 
				to[o] = (((((pixels[q=O])&0xFF)*uovo+((pixels[q+=1])&0xFF)*urvo+((pixels[q+=scan])&0xFF)*urvr+((pixels[q-=1])&0xFF)*uovr)>>>8) & 0xFF );
	}

};









