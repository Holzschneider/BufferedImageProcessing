package de.dualuse.commons.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class ShortBufferedImage extends CustomBufferedImage {

	public ShortBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
		super(width, height, offset, scan, cm, raster, isRasterPremultiplied);
	}
	
	public static void convolve(
			final int width, final int height, 
			final short[] toLuma, final int toOffset, final int toScan, 
			final short[] fromLuma, final int fromOffset, final int fromScan, 
			final int[] kernel, final int offset, final int len, final int norm, final int scanStep) {
		
		final int j = offset, J=j+len, S = scanStep, k=(J/2)*S;
		final int W = width, H = height, mx = k%fromScan, my = k/fromScan, Y=H-my, X=W-mx, r=toScan-(W-mx*2),R=fromScan-(W-mx*2);
		
		for (int y=my,o=mx+my*toScan+toOffset,O=mx+my*fromScan+fromOffset;y<Y;y++,o+=r,O+=R)
			for (int x=mx;x<X;x++,o++,O++) {
				int sum=0;
				for (int i=j,P=O-k;i<J;i++,P+=S) 
					sum += kernel[i]*fromLuma[P];
				
				toLuma[o] = (short)(sum/norm);
			}
	}
	
}
