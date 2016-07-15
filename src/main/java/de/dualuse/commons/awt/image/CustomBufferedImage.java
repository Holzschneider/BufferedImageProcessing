package de.dualuse.commons.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

class PlanarComponentBufferedImage extends CustomBufferedImage {

	public PlanarComponentBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
		super(width, height, offset, scan, cm, raster, isRasterPremultiplied);
	}

	
}

abstract class CustomBufferedImage extends BufferedImage {
	static public final ColorSpace REF_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

	public final int width, height, scan, offset;

	protected CustomBufferedImage() {
		super(1,1,BufferedImage.TYPE_INT_RGB);
		width = height = scan = 1;
		offset = 0;
	}
	
	protected CustomBufferedImage(int width, int height, int type) {
		super(width,height,type);
		this.width = width;
		this.height = height;
		this.offset = 0;
		this.scan = width;
	}
	
	public CustomBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
		super(cm, raster, isRasterPremultiplied, new Hashtable<Object,Object>());
		
		this.width = width;
		this.height = height;
		this.offset = offset;
		this.scan = scan;
	}
	
	
//	public CustomBufferedImage set(int toX, int toY, int width, int height, PixelBufferedImage li, int fromX, int fromY) { return null; }
	
}