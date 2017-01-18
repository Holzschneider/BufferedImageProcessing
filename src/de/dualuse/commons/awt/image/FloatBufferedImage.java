package de.dualuse.commons.awt.image;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class FloatBufferedImage extends CustomBufferedImage {

	static ColorSpace VALUE_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};

	static ColorModel VALUE_COLOR_MODEL = new ComponentColorModel(VALUE_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			final float r[] = (float[]) inData;
			final float red = r[0];
			final int RED = ((int)(red<1&&red>0?red*255f:red))&0xFF;
			
//			return 0xFF000000 | (RED<<16);
			final int raw = RED;
//			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | ((raw>0?gray:-gray)&0xFF);
//			final int rd = (RED<0?0:RED>255?255:RED);
//			return 0xFF000000 | (rd<<16) | (rd<<8) | (rd<<0); 
		};
	};
	
	
	
	public final float[] data, pixels;

	public FloatBufferedImage(IntBufferedImage ibi) { 
		this(ibi.width, ibi.height);
		set(ibi);
	}

	public FloatBufferedImage(FloatBufferedImage fbi) { 
		this(fbi.width, fbi.height);
		set(fbi);
	}

	public FloatBufferedImage(int width, int height) {
		this(width, height, new float[width*height], 0, width);
	}
	
	public FloatBufferedImage(int width, int height, float[] data, int offset, int scan) {
		this(width, height, data, offset, scan, VALUE_COLOR_MODEL);
	}
	
	protected FloatBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster wr) {
		super(width, height, offset, scan, cm, wr, false);
		this.pixels = data = ((DataBufferFloat)wr.getDataBuffer()).getData();
	}
	
	protected FloatBufferedImage(int width, int height, float[] data, int offset, int scan, ColorModel cm) {
		super(width, height, offset, scan, cm,
				new FloatBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, scan, new int[] {0}, new int[] {0}),
						new DataBufferFloat(data, height*scan, offset),
						new Point(0,0)
						),
				true);
		
		this.pixels = this.data = data;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public FloatBufferedImage set(int toX, int toY, int width, int height, float value) {
		set(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, value);
		return this;
	}

	public FloatBufferedImage set(IntBufferedImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); };
	public FloatBufferedImage set(int toX, int toY, int width, int height, IntBufferedImage li, int fromX, int fromY)  {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
			for (int x=0;x<width;x++,O++,o++)
				data[o] = li.data[O];
		
		return this; 
	}

	public FloatBufferedImage set(FloatBufferedImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); };
	public FloatBufferedImage set(int toX, int toY, int width, int height, FloatBufferedImage from, int fromX, int fromY)  { set(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset+fromX+fromY*from.scan, from.scan); return this; }

	public FloatBufferedImage set(int toX, int toY, int width, int height, FloatBufferedImage from, double fromX, double fromY)  { set(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset, from.scan, fromX, fromY); return this; }
	public FloatBufferedImage sub(int toX, int toY, int width, int height, FloatBufferedImage from, double fromX, double fromY) { sub(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	public FloatBufferedImage add(int toX, int toY, int width, int height, FloatBufferedImage from, double fromX, double fromY) { add(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	public FloatBufferedImage mul(int toX, int toY, int width, int height, FloatBufferedImage from, double fromX, double fromY) { mul(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	
	public FloatBufferedImage set(int toX, int toY, int width, int height, IntBufferedImage from, double fromX, double fromY)  { set(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset, from.scan, fromX, fromY); return this; }
	public FloatBufferedImage sub(int toX, int toY, int width, int height, IntBufferedImage from, double fromX, double fromY) { sub(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	public FloatBufferedImage add(int toX, int toY, int width, int height, IntBufferedImage from, double fromX, double fromY) { add(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	public FloatBufferedImage mul(int toX, int toY, int width, int height, IntBufferedImage from, double fromX, double fromY) { mul(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
	
	
	
	public FloatBufferedImage addSquared(FloatBufferedImage gx) { return this.addSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
	public FloatBufferedImage addSquared(int toX, int toY, int width, int height, FloatBufferedImage gx, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++) {
				final float g_x = gx.pixels[ox];
				data[o] += g_x*g_x;
			}
		
		return this;
	}

	public FloatBufferedImage addSquared(IntBufferedImage gx) { return this.addSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
	public FloatBufferedImage addSquared(int toX, int toY, int width, int height, IntBufferedImage gx, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++) {
				final int g_x = gx.pixels[ox];
				data[o] += g_x*g_x;
			}
		
		return this;
	}

	public FloatBufferedImage subSquared(FloatBufferedImage gx) { return this.subSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
	public FloatBufferedImage subSquared(int toX, int toY, int width, int height, FloatBufferedImage gx, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++) {
				final float g_x = gx.pixels[ox];
				data[o] += g_x*g_x;
			}
		
		return this;
	}

	public FloatBufferedImage subSquared(IntBufferedImage gx) { return this.subSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
	public FloatBufferedImage subSquared(int toX, int toY, int width, int height, IntBufferedImage gx, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++) {
				final int g_x = gx.pixels[ox];
				data[o] += g_x*g_x;
			}
		
		return this;
	}


	public FloatBufferedImage addLogged(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY) { 
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*from.scan,rx=from.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++)
				data[o] += Math.log1p(from.data[ox]);
		
		return this;
	}

	public FloatBufferedImage addSqrt(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY) { 
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*from.scan,rx=from.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++)
				data[o] += Math.sqrt(from.data[ox]);
		
		return this;
	}
	
	
	public FloatBufferedImage smoothstep(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY, int lower, int upper) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*from.scan,rx=from.scan-width;y<height;y++,ox+=rx,o+=r) 
			for (int x=0;x<width;x++,ox++,o++) {
				final float s = (from.data[ox]-lower)*1f/(upper-lower), t= s<0f?0f:(s>1f?1:s), tt = t*t;
				data[o] = (3*tt-2*tt*t)*.999999f;
			}
		
		return this;
	}
	
	public FloatBufferedImage mul(int value) { return this.mul(0,0, width, height, value); }
	public FloatBufferedImage mul(int toX, int toY, int width, int height, int value) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r) 
			for (int x=0;x<width;x++,o++)
				data[o] *= value;
		
		return this;
	}

//	
//	public FloatBufferedImage dilate(int fromX, int fromY, int width, int height, FloatBufferedImage li  ) {
//		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
//		
//		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//				
//				float max = li.data[O-0];
//				float a = li.data[O-1-S];
//				float b = li.data[O-0-S];
//				float c = li.data[O+1-S];
//				float d = li.data[O-1];
//				float e = li.data[O+1];
//				float f = li.data[O-1+S];
//				float g = li.data[O-0+S];
//				float h = li.data[O+1+S];
//				
//				max = max>a?max:a;
//				max = max>b?max:b;
//				max = max>c?max:c;
//				max = max>d?max:d;
//				max = max>e?max:e;
//				max = max>f?max:f;
//				max = max>g?max:g;
//				max = max>h?max:h;
//				
//				data[o] = max;
//			}
//		
//		return this;
//	}
//
//	public FloatBufferedImage erode(int fromX, int fromY, int width, int height, FloatBufferedImage li) {
//		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
//		
//		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//				
//				float min = li.data[O-0];
//				float a = li.data[O-1-S];
//				float b = li.data[O-0-S];
//				float c = li.data[O+1-S];
//				float d = li.data[O-1];
//				float e = li.data[O+1];
//				float f = li.data[O-1+S];
//				float g = li.data[O-0+S];
//				float h = li.data[O+1+S];
//				
//				min = min<a?min:a;
//				min = min<b?min:b;
//				min = min<c?min:c;
//				min = min<d?min:d;
//				min = min<e?min:e;
//				min = min<f?min:f;
//				min = min<g?min:g;
//				min = min<h?min:h;
//				
//				data[o] = min;
//			}
//		
//		return this;
//	}
	
	
	public FloatBufferedImage dilate(FloatBufferedImage li) { return this.dilate(0, 0, Math.min(width,li.width), Math.min(height,li.height), li); }
	public FloatBufferedImage dilate(int fromX, int fromY, int width, int height, FloatBufferedImage li  ) {
		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
		
		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
			for (int x=0;x<W;x++,o++,O++) {
				
				float max = li.data[O-0];
//				float a = li.data[O-1-S];
				float b = li.data[O-0-S];
//				float c = li.data[O+1-S];
				float d = li.data[O-1];
				float e = li.data[O+1];
//				float f = li.data[O-1+S];
				float g = li.data[O-0+S];
//				float h = li.data[O+1+S];
				
//				max = max>a?max:a;
				max = max>b?max:b;
//				max = max>c?max:c;
				max = max>d?max:d;
				max = max>e?max:e;
//				max = max>f?max:f;
				max = max>g?max:g;
//				max = max>h?max:h;
				
				data[o] = max;
			}
		
		return this;
	}

	public FloatBufferedImage erode(FloatBufferedImage li) { return this.erode(0, 0, Math.min(width,li.width), Math.min(height,li.height), li); }
	public FloatBufferedImage erode(int fromX, int fromY, int width, int height, FloatBufferedImage li) {
		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
		
		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
			for (int x=0;x<W;x++,o++,O++) {
				
				float min = li.data[O-0];
//				float a = li.data[O-1-S];
				float b = li.data[O-0-S];
//				float c = li.data[O+1-S];
				float d = li.data[O-1];
				float e = li.data[O+1];
//				float f = li.data[O-1+S];
				float g = li.data[O-0+S];
//				float h = li.data[O+1+S];
				
//				min = min<a?min:a;
				min = min<b?min:b;
//				min = min<c?min:c;
				min = min<d?min:d;
				min = min<e?min:e;
//				min = min<f?min:f;
				min = min<g?min:g;
//				min = min<h?min:h;
				
				data[o] = min;
			}
		
		return this;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, float[] src, int soffset, int sscan) {
		for (int y=0,o=doffset,r=dscan,O=soffset,rx=sscan;y<height;y++,O+=rx,o+=r)
			System.arraycopy(src, O, dst, o, width);
		
		return dst;
	}

	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, float value) {
		for (int y=0,o=doffset,r=dscan;y<height;y++,o+=r)
			Arrays.fill(dst, o, o+width, value);
		
		return dst;
	}
	

	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, float src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] = (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	

	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] = (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	
	public static float[] sub(float dst[], int doffset, int dscan, int width, int height, float src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] -= (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	
	
	public static float[] sub(float dst[], int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] -= (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	
	
	
	public static float[] mul(float dst[], int doffset, int dscan, int width, int height, float src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] *= (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	
	
	
	public static float[] mul(float dst[], int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] *= (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
	
	public static float[] add(float[] dst, int doffset, int dscan, int width, int height, float src[], int soffset, int sscan, double shiftX, double shiftY) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] += (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}

	public static float[] add(float[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY) {
		final int sx = (int) shiftX, sy = (int) shiftY;
		
		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
		
		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p) 
			for (int i=0,q;i<width;i++,o++,O++)  
				dst[o] += (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
		
		return dst;
	}
}
