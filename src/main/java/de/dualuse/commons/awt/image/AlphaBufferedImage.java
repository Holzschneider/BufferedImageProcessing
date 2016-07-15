package de.dualuse.commons.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;


public class AlphaBufferedImage extends IntBufferedImage {

	static class AlphaColorSpace extends ColorSpace {
		final float[] toRGB;
		
		protected AlphaColorSpace(float red, float green, float blue) {
			super(ColorSpace.CS_GRAY, 0);
			this.toRGB = new float[] { red, green, blue };
		}
		
		private static final long serialVersionUID = 1L;
		public float[] toRGB(float[] colorvalue) { 
			return toRGB;//REF_COLOR_SPACE.toRGB(colorvalue); 
		}
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
		
	}
	
	static class AlphaColorModel extends ComponentColorModel {

		public AlphaColorModel(float red, float green, float blue) {
			super(new AlphaColorSpace(red,green,blue), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_INT);
		}
		
		public int getRGB(Object inData) {
			int[] rgb = (int[])inData;
			
			final int raw = rgb[0];
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
			return (clamped << 24) | 0xFFFFFF; 
		}
	}
	
	
	
	
	static ColorSpace A_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 0) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { 
			return new float[] { 1, 1, 1 };//REF_COLOR_SPACE.toRGB(colorvalue); 
		}
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	static ColorModel A_COLOR_MODEL = new ComponentColorModel(A_COLOR_SPACE, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[])inData;
			
			final int raw = rgb[0];
//			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return (clamped << 24) | 0xFFFFFF; 
//			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | clamped;
		}
	};
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public AlphaBufferedImage(IntBufferedImage copy) {
		super(copy.width,copy.height,set( new int[copy.width*copy.height], 0, copy.width, copy.width, copy.height, copy.data, copy.offset, copy.scan),0,copy.width);
	}

	public AlphaBufferedImage(int width, int height, int [] data, int offset, int scan) {
		super(width, height, data, offset, scan, A_COLOR_MODEL);
	}
	
	public AlphaBufferedImage(int width, int height, int [] data, int offset, int scan, int rgb) {
		this(width,height, data, offset, scan, ((rgb>>>16)&0xFF)/255f, ((rgb>>>8)&0xFF)/255f, ((rgb>>>0)&0xFF)/255f);
	}
	
	public AlphaBufferedImage(int width, int height, int [] data, int offset, int scan, float r, float g, float b) {
		super(width, height, data, offset, scan, new AlphaColorModel(r, g, b));
	}
	
	public AlphaBufferedImage(int width, int height) {
		super(width, height, new int[width*height], 0, width, A_COLOR_MODEL);
	}
	
	private AlphaBufferedImage(int width, int height, int[] alpha, int offset, int scan, ColorModel cm, WritableRaster raster) {
		super(width,height, offset, scan, cm, raster);
	}
	
	public AlphaBufferedImage getSubimage(int x, int y, int w, int h) {
		return new AlphaBufferedImage(w, h, data, x+y*scan, scan, A_COLOR_MODEL, getRaster().createWritableChild(x, y, w, h, 0, 0, null));
	}
	
	public int[] getRGB(double startX, double startY, int w, int h, int[] rgbArray, int off, int scansize) {
		final int sx = (int) startX, sy = (int) startY;
		
		final float ur = (float)startX-sx, vr = (float)startY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
		
		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
			for (int i=0,q;i<w;i++,o++,O++) { 
				int l = ((((data[q=O])*uovo+(data[q+=1])*urvo+(data[q+=scan])*urvr+(data[q-=1])*uovr)>>>8));
				l=l>255?255:(l<0?0:l);
				rgbArray[o] = (l<<24) | 0xFFFFFF;
			}
		
		return rgbArray;
	}
	
	


}


