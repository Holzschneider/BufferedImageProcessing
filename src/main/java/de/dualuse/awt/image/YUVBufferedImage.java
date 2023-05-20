package de.dualuse.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

public class YUVBufferedImage extends IntPlanesBufferedImage {
	static public final ColorSpace REF_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
	
	static ColorSpace COMPONENT_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	
	static ColorModel Y_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] yuv = (int[]) inData;
			final int Y_ = yuv[0], Cr = 0, Cb = 0;
			final int R = (int)(Y_+1.402f*Cr), G = (int)(Y_-.3441f*Cb-.714*Cr), B = (int)(Y_+1.772*Cb);
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16) | ((G<0?0:G>255?255:G)<<8) | ((B<0?0:B>255?255:B)<<0);
		};		
	};
	
	
	static ColorModel U_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] yuv = (int[]) inData;
			final int Y_ = 128, Cr = yuv[0], Cb = 0;
			final int R = (int)(Y_+1.402f*Cr), G = (int)(Y_-.3441f*Cb-.714*Cr), B = (int)(Y_+1.772*Cb);
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16) | ((G<0?0:G>255?255:G)<<8) | B;
		};		
	};
	
	
	static ColorModel V_COLOR_MODEL = new ComponentColorModel(COMPONENT_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] yuv = (int[]) inData;
			final int Y_ = 128, Cr = 0, Cb = yuv[0];
			final int R = (int)(Y_+1.402f*Cr), G = (int)(Y_-.3441f*Cb-.714*Cr), B = (int)(Y_+1.772*Cb);
			return 0xFF000000 | R | ((G<0?0:G>255?255:G)<<8) | ((B<0?0:B>255?255:B)<<0);
		};		
	};
	
	static ColorModel YUV_COLOR_MODEL =  new ComponentColorModel(REF_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] yuv = (int[]) inData;
			
			final int Y_ = yuv[0];
			final int Cb = yuv[1];
			final int Cr = yuv[2];
			
			final int R = (int)(Y_+1.402f*Cr);
			final int G = (int)(Y_-.3441f*Cb-.714*Cr);
			final int B = (int)(Y_+1.772*Cb);
			
			return 0xFF000000 | ((R<0?0:R>255?255:R)<<16) | ((G<0?0:G>255?255:G)<<8) | ((B<0?0:B>255?255:B)<<0);
		};		
	};
	
	public final IntBufferedImage Y;
	public final IntBufferedImage U;
	public final IntBufferedImage V;
	
	public YUVBufferedImage(YUVBufferedImage bi) { this(bi.width,bi.height); this.set(bi); }
	public YUVBufferedImage(PixelBufferedImage bi) { this(bi.width,bi.height); this.set(bi); } 
	public YUVBufferedImage(int width, int height) {
		this(width, height, new int[width*height], new int[width*height], new int[width*height], 0, 0, 0, width);
	}
	
	public YUVBufferedImage(int width, int height, int[] yPlane, int[] uPlane, int[] vPlane, int offsetY, int offsetU, int offsetV, int scan) {
		super(width, height, new int[][] { yPlane, uPlane, vPlane }, new int[] { offsetY, offsetU, offsetV }, scan, YUV_COLOR_MODEL);
		
		Y = new IntBufferedImage(width, height, yPlane, offsetY, scan, Y_COLOR_MODEL);
		U = new IntBufferedImage(width, height, uPlane, offsetU, scan, U_COLOR_MODEL);
		V = new IntBufferedImage(width, height, vPlane, offsetV, scan, V_COLOR_MODEL);
	}
	
	

	public YUVBufferedImage set(YUVBufferedImage yuv) {
		set(0,0, Math.min(width,yuv.width), Math.min(height,yuv.height), yuv, 0, 0);
		return this;
	}
	
	public YUVBufferedImage set(int toX, int toY, int width, int height, YUVBufferedImage yuv, int fromX, int fromY) {
		Y.set(toX, toY, width, height, yuv.Y, fromX, fromY);
		U.set(toX, toY, width, height, yuv.U, fromX, fromY);
		V.set(toX, toY, width, height, yuv.V, fromX, fromY);
		return this;
	}

	public YUVBufferedImage set(PixelBufferedImage pbi) { return this.set(0,0,Math.min(pbi.width, this.width), Math.min(pbi.height, this.height),pbi, 0,0); }
	public YUVBufferedImage set(int toX, int toY, int width, int height, PixelBufferedImage pbi, int fromX, int fromY) {
		set(
				width,height, 
				Y.pixels, Y.offset+toX+toY*scan, scan, 
				U.pixels, U.offset+toX+toY*scan, scan, 
				V.pixels, V.offset+toX+toY*scan, scan, 
				pbi.pixels, pbi.offset+fromX+fromY*pbi.scan, pbi.scan);
		
//		for (int y=0,o=pbi.offset, OY=toX+toY*this.Y.scan+this.Y.offset, OU=toX+toY*this.U.scan+this.U.offset, OV=toX+toY*this.V.scan+this.V.offset, r = pbi.scan-width, R = this.Y.scan-width;y<height;y++,o+=r, OY+=R, OU+=R, OV+=R)
//			for (int x=0;x<width;x++,o++,OY++,OU++,OV++) {
//				int ARGB = pbi.pixels[o];
//				int red = (ARGB>>16)&0xFF;
//				int green = (ARGB>>8)&0xFF;
//				int blue = (ARGB>>0)&0xFF;
//				
////				float Y_ = .299f*red+.587f*green+0.114f*blue;
////				float Cb = -0.1687f*red-0.3313f*green+.5f*blue;
////				float Cr = .5f*red-0.4186f*green-0.0813f*blue;
//				
//				int Y_ = (red*306+green*601+blue*117)>>10;
//				int Cb = (red*-173+green*-339+blue*512)>>10;
//				int Cr = (red*512+green*-429+blue*-83)>>10;
//				
//				Y.data[OY] = (int)Y_;
//				U.data[OU] = (int)Cb;
//				V.data[OV] = (int)Cr;
//			}
		
		return this;
	}
	
	
	public YUVBufferedImage gradients(IntBufferedImage from, SeparableKernel filter) { return this.gradients(0, 0, Math.min(width, from.width), Math.min(height, from.height), from, 0,0, filter); }
	public YUVBufferedImage gradients(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY, SeparableKernel filter) {
		filter.norm(1).convolve(U, toX, toY, from, fromX, fromY, width, height, 1, 0);
		filter.norm(filter.norm*filter.norm).convolve(Y, toX, toY, U, toX, toY, width, height, 0, 1);
		
		SeparableKernel.SOBEL.convolve(U, toX, toY, Y, toX, toY, width, height, 1, 0);
		SeparableKernel.SOBEL.convolve(V, toX, toY, Y, toX, toY, width, height, 0, 1);
		
		return this;
	}
	
	public YUVBufferedImage gradients(IntBufferedImage from, int R) { return this.gradients(0, 0, Math.min(width, from.width), Math.min(height, from.height), from, 0,0, R); }
	public YUVBufferedImage gradients(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY, int R) {
		BoxFilter.HORIZONTAL.convolve(U, toX, toY, from, fromX, fromY, width, height, R, 1);
		BoxFilter.VERTICAL.convolve(Y, toX, toY, U, toX, toY, width, height, R, (R*2+1)*(R*2+1));
		
		SeparableKernel.SOBEL.convolve(U, toX, toY, Y, toX, toY, width, height, 1, 0);
		SeparableKernel.SOBEL.convolve(V, toX, toY, Y, toX, toY, width, height, 0, 1);
		
		return this;
	}

	public YUVBufferedImage gradients(IntBufferedImage from) { return this.gradients(0,0,Math.min(width,from.width),Math.min(height,from.height),from, 0,0); }
	public YUVBufferedImage gradients(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY) {
		Y.set(toX, toY, width, height, from, fromX, fromY);
		SeparableKernel.SOBEL.convolve(U, toX, toY, Y, toX, toY, width, height, 1, 0);
		SeparableKernel.SOBEL.convolve(V, toX, toY, Y, toX, toY, width, height, 0, 1);
		return this;
	}

	
	public static void set(int width, int height, int Y[], int offsetY, int scanY, int U[], int offsetU, int scanU, int V[], int offsetV, int scanV, int argb[], int offset, int scan) {
		for (int y=0,o=offset, OY=offsetY, OU=offsetU, OV=offsetV, r = scan-width, RY = scanY-width, RU = scanU-width, RV = scanV-width;y<height;y++,o+=r, OY+=RY, OU+=RU, OV+=RV)
			for (int x=0;x<width;x++,o++,OY++,OU++,OV++) {
				final int ARGB = argb[o];
				final int red = (ARGB>>16)&0xFF;
				final int green = (ARGB>>8)&0xFF;
				final int blue = (ARGB>>0)&0xFF;
				
				final int Y_ = (red*306+green*601+blue*117)>>10;
				final int Cb = (red*-173+green*-339+blue*512)>>10;
				final int Cr = (red*512+green*-429+blue*-83)>>10;
				
				Y[OY] = (int)Y_;
				U[OU] = (int)Cb;
				V[OV] = (int)Cr;
			}
		
	}

	
//	public static void set(int width, int height, short Y[], int offsetY, int scanY, short U[], int offsetU, int scanU, short V[], int offsetV, int scanV, int argb[], int offset, int scan) {
//		for (int y=0,o=offset, OY=offsetY, OU=offsetU, OV=offsetV, r = scan-width, RY = scanY-width, RU = scanU-width, RV = scanV-width;y<height;y++,o+=r, OY+=RY, OU+=RU, OV+=RV)
//			for (int x=0;x<width;x++,o++,OY++,OU++,OV++) {
//				final int ARGB = argb[o];
//				final int red = (ARGB>>16)&0xFF;
//				final int green = (ARGB>>8)&0xFF;
//				final int blue = (ARGB>>0)&0xFF;
//				
//				final int Y_ = (red*306+green*601+blue*117)>>10;
//				final int Cb = (red*-173+green*-339+blue*512)>>10;
//				final int Cr = (red*512+green*-429+blue*-83)>>10;
//				
//				Y[OY] = (short)Y_;
//				U[OU] = (short)Cb;
//				V[OV] = (short)Cr;
//			}
//	}
}



