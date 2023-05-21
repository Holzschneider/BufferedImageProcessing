package de.dualuse.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;


public class LumaArrayImage extends IntArrayImage {
	
	static ColorSpace Y_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return RGB_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return RGB_COLOR_SPACE.fromRGB(rgbvalue); }
		public float[] fromCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	
	static ColorModel Y_COLOR_MODEL = new ComponentColorModel(Y_COLOR_SPACE, new int[] { 8 }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[])inData;
			
			final int raw = rgb[0];
//			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | clamped;
		}
	};
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public LumaArrayImage(int width, int height, int [] data, int offset, int scan) {
		super(width, height, data, offset, scan, Y_COLOR_MODEL);
	}
	
	public LumaArrayImage(IntArrayImage from) {
		this(from.width, from.height, from.values, from.offset, from.scan);
	}

	public LumaArrayImage(int width, int height) {
		super(width, height, new int[width*height], 0, width, Y_COLOR_MODEL);
	}
	
	private LumaArrayImage(int width, int height, int[] luma, int offset, int scan, ColorModel cm, WritableRaster raster) {
		super(width,height, offset, scan, cm, raster);
	}
	
	public LumaArrayImage crop(int x, int y, int w, int h) {
		return new LumaArrayImage(w, h, values, offset(x,y), scan);
	}
	
	public int[] getRGB(double startX, double startY, int w, int h, int[] rgbArray, int off, int scansize) {
		final int sx = (int) startX, sy = (int) startY;
		
		final float ur = (float)startX-sx, vr = (float)startY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
		
		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
			for (int i=0,q;i<w;i++,o++,O++) { 
				int l = ((((values[q=O])*uovo+(values[q+=1])*urvo+(values[q+=scan])*urvr+(values[q-=1])*uovr)>>>8));
				l=l>255?255:(l<0?0:l);
				rgbArray[o] = 0xFF000000 | (l<<16) | (l<<8) | l;
			}
		
		return rgbArray;
	}

	public LumaArrayImage set(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
		super.set(toX, toY, width, height, li, fromX, fromY); return this;
	}

	
//	public int[] getLuma(double startX, double startY, int w, int h, int[] lumaArray, int off, int scansize) {
//		final int sx = (int) startX, sy = (int) startY;
//		
//		final float ur = (float)startX-sx, vr = (float)startY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//		
//		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
//			for (int i=0,q;i<w;i++,o++,O++)  
//				lumaArray[o] = ((((luma[q=O])*uovo+(luma[q+=1])*urvo+(luma[q+=scan])*urvr+(luma[q-=1])*uovr)>>>8));
//			
//		return lumaArray;
//	}
	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		set(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		add(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	public LumaBufferedImage sub(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		sub(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	public float getLuma(float x, float y) {
//		if (x<0 || y<0 || x>width-1 || y>height-1)
//			return 0;
//
//		x-=0.5f;
//		y-=0.5f;
//		
//		final int xi = (int)x, yi = (int)y;
//		int o = xi+yi*scan;
//		final int ul = luma[o], ur = luma[++o], lr = luma[o+=scan], ll = luma[--o];
//		
//		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
//		return (ul*omxr+ur*xr)*omyr+(ll*omxr+lr*xr)*yr;
//	}
//	
//	public int getLuma(int x, int y) {
//		if (x>0 && y>0 && x<width && y<height)
//			return luma[x+y*scan];
//		else
//			return 0;
//	}
//
//	
//	public LumaBufferedImage addSquared(int toX, int toY, int width, int height, LumaBufferedImage gx, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
//			for (int x=0;x<width;x++,ox++,o++) {
//				final int g_x=gx.luma[ox];
//				luma[o] += g_x*g_x;
//			}
//		
//		return this;
//	}
//
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] += li.luma[O];
//		
//		return this;
//	}
//	
//	public LumaBufferedImage sub(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		sub(
//				this.luma, this.offset, this.scan, 
//				width, height, 
//				li.luma, li.offset, li.scan);
//		
//		return this;
//	}
//
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY, int norm, int base) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] += base+li.luma[O]/norm;
//		
//		return this;
//	}
//
//	public LumaBufferedImage set(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan,O=fromX+fromY*li.scan,rx=li.scan;y<height;y++,O+=rx,o+=r)
//			System.arraycopy(li.luma, O, luma, o, width);
//		
//		return this;
//	}
//	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, int[] from, int fromOffset, int fromScan) {
//		set(	this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width, height, 
//				from, fromOffset, fromScan);
//		
//		return this;
//	}
//
//
//	public LumaBufferedImage set(int toX, int toY, int width, int height, FloatBufferedImage from) {
//		for (int y=0,o=this.offset,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] = (int) from.data[O];
//		
//		return this;
//	}

	

	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, float [] from, int fromOffset, int fromScan) {
//		Blit.setLumaWithLuma(
//					this.luma, this.offset+toX+toY*this.scan, this.scan, 
//					width, height, 
//					from, fromOffset, fromScan);
//		
//		return this;
//	}

	public LumaArrayImage set(PixelArrayImage li) { return this.set(0,0, Math.min(width,li.width), Math.min(height, li.height), li, 0,0); }
	public LumaArrayImage set(int toX, int toY, int width, int height, PixelArrayImage li, int fromX, int fromY) {
		
//		for (int y=0,o=lbi.offset,r=lbi.scan-lbi.width,O=mbi.offset,R=mbi.scan-mbi.width;y<lbi.height;y++,o+=r,O+=R)
//			for (int x=0,argb;x<lbi.width;x++,o++,O++)
//				lbi.pixels[o] = (((argb=mbi.pixels[O])&0xFF)*117+((argb>>>8)&0xFF)*601+((argb>>>16)&0xFF)*306)>>10;

		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan+li.offset,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
			for (int x=0,argb;x<width;x++,O++,o++) 
				values[o] = (((argb=li.pixels[O])&0xFF)*117+((argb>>>8)&0xFF)*601+((argb>>>16)&0xFF)*306)>>10;
				
		return this;
	}
	
	public LumaArrayImage set(RGBBufferedImage li) { return this.set(0,0, Math.min(width,li.width), Math.min(height, li.height), li, 0,0); }
	public LumaArrayImage set(int toX, int toY, int width, int height, RGBBufferedImage li, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,OR=li.R.offset+fromX+fromY*li.scan,OG=li.G.offset+fromX+fromY*li.scan,OB=li.B.offset+fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,OR+=rx,OG+=rx,OB+=rx,o+=r) 
			for (int x=0;x<width;x++,OR++,OG++,OB++,o++) 
				values[o] = (li.R.values[OR]*117+li.G.values[OG]*601+li.B.values[OB]*306)>>10;
				
		return this;
	}
	
	public LumaArrayImage set(int toX, int toY, int width, int height, int value) {
		super.set(toX,toY,width,height,value);
		return this;
	}
	
}


