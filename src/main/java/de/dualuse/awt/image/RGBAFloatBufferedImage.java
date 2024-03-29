package de.dualuse.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

public class RGBAFloatBufferedImage extends FloatPlanesBufferedImage {
	static public final ColorSpace REF_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
	
	
	static ColorSpace COMPONENT_COLOR_SPACE = new ColorSpace(ColorSpace.TYPE_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	

	static ColorModel G_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			float[] rgb = (float[]) inData;
			final int G = (int)rgb[1];
			return 0xFF000000 | ((G<0?0:G>255?255:G)<<8);
		};		
	};
	
	
	static ColorModel R_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			float[] rgb = (float[]) inData;
			final int R = (int)rgb[2];
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16);
		};		
	};
	
	static ColorModel B_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			float[] rgb = (float[]) inData;
			final int B = (int)rgb[0];
			return 0xFF000000 | ((B<0?0:B>255?255:B)<<0);
		};		
	};
	
	
	static ColorModel RGB_COLOR_MODEL =  new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), true, false, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			float[] rgb = (float[]) inData;
			
			final int R = (int)rgb[0];
			final int G = (int)rgb[1];
			final int B = (int)rgb[2];
			final int A = (int)rgb[3];
			
			return ((A<0?0:A>255?255:A)<<24) | ((R<0?0:R>255?255:R)<<16) | ((G<0?0:G>255?255:G)<<8) | ((B<0?0:B>255?255:B)<<0);
		};
	};
	
	public final FloatArrayImage R;
	public final FloatArrayImage G;
	public final FloatArrayImage B;
	public final FloatArrayImage A;
	
	
	public RGBAFloatBufferedImage(PixelArrayImage pbi) { this(pbi.width,pbi.height); this.set(0, 0, pbi.width, pbi.height, pbi, 0, 0); }
	public RGBAFloatBufferedImage(int width, int height) {
		this(width, height, new float[width*height], new float[width*height], new float[width*height], new float[width*height], 0, 0, 0,0, width);
	}
	
	public RGBAFloatBufferedImage(int width, int height, FloatArrayImage r, FloatArrayImage g, FloatArrayImage b, FloatArrayImage a) {
		this(width, height, r.values, g.values, b.values, a.values, r.offset, g.offset, b.offset, a.offset, r.scan);
	}

	public RGBAFloatBufferedImage(int width, int height, float[] rPlane, float[] gPlane, float[] bPlane, float[] aPlane, int offsetR, int offsetG, int offsetB, int offsetA, int scan) {
		super(width, height, new float[][] { rPlane, gPlane, bPlane, aPlane }, new int[] { offsetR, offsetG, offsetB, offsetA }, scan, RGB_COLOR_MODEL);
		
		R = new FloatArrayImage(width, height, rPlane, offsetR, scan, R_COLOR_MODEL);
		G = new FloatArrayImage(width, height, gPlane, offsetG, scan, G_COLOR_MODEL);
		B = new FloatArrayImage(width, height, bPlane, offsetB, scan, B_COLOR_MODEL);
		A = new FloatArrayImage(width, height, aPlane, offsetA, scan, FloatArrayImage.VALUE_COLOR_MODEL);
	}
	
	
	public int getRGB(float x, float y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;

//		x-=0.5f;
//		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan;
		final int ul = 0xFF000000|((int)R.values[o]<<16)|((int)G.values[o]<<8)|(int)B.values[o];
		final int ur = 0xFF000000|((int)R.values[++o]<<16)|((int)G.values[o]<<8)|(int)B.values[o];
		final int lr = 0xFF000000|((int)R.values[o+=scan]<<16)|((int)G.values[o]<<8)|(int)B.values[o];
		final int ll = 0xFF000000|((int)R.values[--o]<<16)|((int)G.values[o]<<8)|(int)B.values[o];
		
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
	
	
	public RGBAFloatBufferedImage set(int toX, int toY, int width, int height, PixelArrayImage pbi, int fromX, int fromY) {
		for (int y=0,o=pbi.offset, OY=toX+toY*this.B.scan+this.B.offset, OU=toX+toY*this.G.scan+this.G.offset, OV=toX+toY*this.R.scan+this.R.offset, r = pbi.scan-width, R = this.B.scan-width;y<height;y++,o+=r, OY+=R, OU+=R, OV+=R)
			for (int x=0;x<pbi.width;x++,o++,OY++,OU++,OV++) {
				final int ARGB = pbi.pixels[o];
				final int red = (ARGB>>16)&0xFF;
				final int green = (ARGB>>8)&0xFF;
				final int blue = (ARGB>>0)&0xFF;
				
				this.R.values[OV] = (int)red;
				this.B.values[OY] = (int)blue;
				this.G.values[OU] = (int)green;
			}
		
		return this;
	}

	
}