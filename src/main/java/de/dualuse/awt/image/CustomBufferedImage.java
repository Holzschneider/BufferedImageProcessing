package de.dualuse.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

abstract class CustomBufferedImage extends BufferedImage {
	static public final ColorSpace RGB_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
	static public final ColorSpace VALUE_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;

		public float[] toRGB(float[] colorvalue) { return RGB_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return RGB_COLOR_SPACE.fromRGB(rgbvalue); }
		public float[] fromCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};

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


	public abstract CustomBufferedImage crop(int x, int y, int width, int height);

	@Override
	final public CustomBufferedImage getSubimage(int x, int y, int w, int h) { return crop(x,y,w,h); }

}