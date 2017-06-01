package de.dualuse.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

public class RGBBufferedImage extends IntPlanesBufferedImage {
	static public final ColorSpace REF_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
	
	
	static ColorSpace COMPONENT_COLOR_SPACE = new ColorSpace(ColorSpace.TYPE_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	
	
	static ColorModel G_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[]) inData;
			final int G = rgb[1];
			return 0xFF000000 | ((G<0?0:G>255?255:G)<<8);
		};		
	};
	
	
	static ColorModel R_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[]) inData;
			final int R = rgb[2];
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16);
		};		
	};
	
	static ColorModel B_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[]) inData;
			final int B = rgb[0];
			return 0xFF000000 | ((B<0?0:B>255?255:B)<<0);
		};		
	};
	
	static ColorModel RGB_COLOR_MODEL =  new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[]) inData;
			
			final int R = rgb[0];
			final int G = rgb[1];
			final int B = rgb[2];
			
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16) | ((G<0?0:G>255?255:G)<<8) | ((B<0?0:B>255?255:B)<<0);
		};
	};
	
	public final IntBufferedImage R;
	public final IntBufferedImage G;
	public final IntBufferedImage B;
	
	
	public RGBBufferedImage(PixelBufferedImage pbi) { this(pbi.width,pbi.height); this.set(0, 0, pbi.width, pbi.height, pbi, 0, 0); }
	public RGBBufferedImage(int width, int height) {
		this(width, height, new int[width*height], new int[width*height], new int[width*height], 0, 0, 0, width);
	}
	
	public RGBBufferedImage(int width, int height, IntBufferedImage r, IntBufferedImage g, IntBufferedImage b) {
		this(width, height, r.data, g.data, b.data, r.offset, g.offset, b.offset, r.scan);
	}

	public RGBBufferedImage(int width, int height, int[] rPlane, int[] gPlane, int[] bPlane, int offsetR, int offsetG, int offsetB, int scan) {
		super(width, height, new int[][] { rPlane, gPlane, bPlane }, new int[] { offsetR, offsetG, offsetB }, scan, RGB_COLOR_MODEL);
		
		R = new IntBufferedImage(width, height, rPlane, offsetR, scan, R_COLOR_MODEL);
		G = new IntBufferedImage(width, height, gPlane, offsetG, scan, G_COLOR_MODEL);
		B = new IntBufferedImage(width, height, bPlane, offsetB, scan, B_COLOR_MODEL);
	}
	
	
	public int getRGB(float x, float y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;

//		x-=0.5f;
//		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan;
		final int ul = 0xFF000000|(R.data[o]<<16)|(G.data[o]<<8)|B.data[o];
		final int ur = 0xFF000000|(R.data[++o]<<16)|(G.data[o]<<8)|B.data[o];
		final int lr = 0xFF000000|(R.data[o+=scan]<<16)|(G.data[o]<<8)|B.data[o];
		final int ll = 0xFF000000|(R.data[--o]<<16)|(G.data[o]<<8)|B.data[o];
		
		final int ulB = (ul>>>0)&0xFF, urB = (ur>>>0)&0xFF, lrB = (lr>>>0)&0xFF, llB = (ll>>>0)&0xFF;
		final int ulG = (ul>>>8)&0xFF, urG = (ur>>>8)&0xFF, lrG = (lr>>>8)&0xFF, llG = (ll>>>8)&0xFF;
		final int ulR = (ul>>>16)&0xFF, urR = (ur>>>16)&0xFF, lrR = (lr>>>16)&0xFF, llR = (ll>>>16)&0xFF;
		final int ulA = (ul>>>24)&0xFF, urA = (ur>>>24)&0xFF, lrA = (lr>>>24)&0xFF, llA = (ll>>>24)&0xFF;
		
		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
		
		final int B = (int)((ulB*omxr+urB*xr)*omyr+(llB*omxr+lrB*xr)*yr);
		final int G = (int)((ulG*omxr+urG*xr)*omyr+(llG*omxr+lrG*xr)*yr);
		final int R = (int)((ulR*omxr+urR*xr)*omyr+(llR*omxr+lrR*xr)*yr);
		final int A = (int)((ulA*omxr+urA*xr)*omyr+(llA*omxr+lrA*xr)*yr);
		
		return ((A&0xFF)<<24) | ((R&0xFF)<<16) | ((G&0xFF)<<8) | (B&0xFF);
	}
	
	
	public RGBBufferedImage set(int toX, int toY, int width, int height, PixelBufferedImage pbi, int fromX, int fromY) {
		for (int y=0,o=pbi.offset, OY=toX+toY*this.B.scan+this.B.offset, OU=toX+toY*this.G.scan+this.G.offset, OV=toX+toY*this.R.scan+this.R.offset, r = pbi.scan-width, R = this.B.scan-width;y<height;y++,o+=r, OY+=R, OU+=R, OV+=R)
			for (int x=0;x<pbi.width;x++,o++,OY++,OU++,OV++) {
				final int ARGB = pbi.pixels[o];
				final int red = (ARGB>>16)&0xFF;
				final int green = (ARGB>>8)&0xFF;
				final int blue = (ARGB>>0)&0xFF;
				
				this.R.data[OV] = (int)red;
				this.B.data[OY] = (int)blue;
				this.G.data[OU] = (int)green;
			}
		
		return this;
	}

	
}