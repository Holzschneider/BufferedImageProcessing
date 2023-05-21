package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.Serializable;
import java.util.Arrays;

public class IntArrayImage extends CustomBufferedImage implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

//	static ColorSpace VALUE_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
//		private static final long serialVersionUID = 1L;
//		
//		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
//		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
//		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
//		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
//	};
//	
//	
//	static ColorModel VALUE_COLOR_MODEL = new ComponentColorModel(VALUE_COLOR_SPACE, new int[] { 8 }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT) {
//		public int getRGB(Object inData) {
//			int[] rgb = (int[])inData;
//			
//			final int raw = rgb[0];
////			final int absed = raw<0?(-raw)&0xFF:raw;
//			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
//			
//			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
//			
////			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
//			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | ((raw>0?gray:-gray)&0xFF);
//		}
//	};
	
	static ColorSpace Y_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return RGB_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return RGB_COLOR_SPACE.fromRGB(rgbvalue); }
		public float[] fromCIEXYZ(float[] colorvalue) { return RGB_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};


	static ColorModel IntColorModel = new ComponentColorModel(Y_COLOR_SPACE, new int[] { 8 }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[])inData;
			
			final int raw = rgb[0];
			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | absed;
		}
	};
	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public final int[] values;
	
	public IntArrayImage clone() {
		return new IntArrayImage(width, height, new int[width*height], 0, width).set(0, 0, width, height, this, 0, 0);
	}
	
	public IntArrayImage(IntArrayImage ibi) {
		this(ibi.width,ibi.height);
		set(ibi);
	}
	
	public IntArrayImage(int width, int height) {
		this(width, height, new int[width*height], 0, width);
	}
	
	public IntArrayImage(int width, int height, int[] values, int offset, int scan) {
		this(width, height, values, offset, scan, IntColorModel);
	}
	
	protected IntArrayImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster wr) {
		super(width, height, offset, scan, cm, wr, false);
		
		this.values = ((DataBufferInt)wr.getDataBuffer()).getData();
	}
	
	protected ColorModel colorModel;
	protected IntArrayImage(int width, int height, int[] values, int offset, int scan, ColorModel cm) {
		super(width, height, offset, scan, cm,
				new IntBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_INT,  width, height, scan, new int[] {0}, new int[] {0}),
						new DataBufferInt(values, height*scan+offset, offset),
						new Point(0,0)
					),
				true);
		
		this.colorModel = cm;
		this.values = values;
	}

	////////////////////////////////////////// ACCESSORS ///////////////////////////////////////////////////////////////

	public int offset(int x, int y) { return x+y*scan+offset; }

	public IntArrayImage set(IntArrayImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); }
	public IntArrayImage set(int toX, int toY, int width, int height, IntArrayImage from, int fromX, int fromY) { return set(toX,toY,width,height, from.values, from.offset,from.scan, fromX,fromY); }
	public IntArrayImage set(int toX, int toY, int width, int height, int[] fromValues, int fromOffset, int fromScan, int fromX, int fromY) {
		if (fromScan == this.scan && this.width==this.scan)
			System.arraycopy(fromValues,fromOffset+fromX+fromY*fromScan, this.values, this.offset(toX,toY),height*scan);
		else
			for (int y=0,o=offset+toY*scan+toX,O=fromX+fromY*fromScan;y<height;y++,O+=fromScan,o+=this.scan)
				System.arraycopy(fromValues, O, values, o, width);

		return this;
	}

	public IntArrayImage set(int value) { return set(0,0,width,height, value); }
	public IntArrayImage set(int toX, int toY, int width, int height, int value) {
		if (width==scan)
			Arrays.fill(values,offset(toX,toY), offset(toX+width,toY+height), value);
		else
			for (int y=0,o=offset(toX,toY);y<height;y++,o+=scan)
				Arrays.fill(this.values,o,o+width,value);

		return this;
	}



	public IntArrayImage crop(int x, int y, int w, int h) {
		return new IntArrayImage(w, h, values, this.offset(x,y), this.scan, this.colorModel);
	}
	
	public float getValue(float x, float y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;
	
//		x-=0.5f;
//		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan;
		final int ul = values[o], ur = values[++o], lr = values[o+=scan], ll = values[--o];
		
		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
		return (ul*omxr+ur*xr)*omyr+(ll*omxr+lr*xr)*yr;
	}
	
	public int getValue(int x, int y) {
		if (x>0 && y>0 && x<width && y<height)
			return values[x+y*scan];
		else
			return 0;
	}

	
//
//	//////////////////////////////////////////////// Image Operations //////////////////////////////////////////////////
//
//
//	public IntArrayImage dilate(IntArrayImage li) { return this.dilate(0, 0, Math.min(width,li.width), Math.min(height,li.height), li); }
//	public IntArrayImage dilate(int fromX, int fromY, int width, int height, IntArrayImage li  ) {
//		dilate(this.pixels, this.offset, this.scan, width, height, li.pixels, li.offset+fromX+fromY*li.scan, li.scan);
//		return this;
//	}
//
//	public IntArrayImage erode(IntArrayImage li) { return this.erode(0, 0, Math.min(width,li.width), Math.min(height,li.height), li); }
//	public IntArrayImage erode(int fromX, int fromY, int width, int height, IntArrayImage li) {
//		erode(this.pixels, this.offset, this.scan, width, height, li.pixels, li.offset+fromX+fromY*li.scan, li.scan);
//		return this;
//	}
//
//
//
//	public IntArrayImage mul(IntArrayImage li) { return this.mul(0,0,this.width,this.height,li, 0,0); }
//	public IntArrayImage div(IntArrayImage li) { return this.div(0,0,this.width,this.height,li, 0,0); }
//

//	public IntArrayImage add(int toX, int toY, int width, int height, IntArrayImage from, double fromX, double fromY) { add(this.values, this.offset+toX+toY*this.scan, this.scan, width, height, from.values, from.offset,from.scan, fromX, fromY); return this; }
//	public IntArrayImage sub(int toX, int toY, int width, int height, IntArrayImage from, double fromX, double fromY) { sub(this.values, this.offset+toX+toY*this.scan, this.scan, width, height, from.values, from.offset,from.scan, fromX, fromY); return this; }
//	public IntArrayImage mul(int toX, int toY, int width, int height, IntArrayImage from, double fromX, double fromY) { mul(this.values, this.offset+toX+toY*this.scan, this.scan, width, height, from.values, from.offset,from.scan, fromX, fromY); return this; }
//
//
//
////	public<IntBufferedImage extends de.dualuse.commons.awt.image.IntBufferedImage> IntBufferedImage convolve(
////			IntBufferedImage to, int toX, int toY,
////			IntBufferedImage from, int fromX, int fromY,
////
////			int width, int height, int scanX, int scanY)
////	{
////
////	}
//
//
//	public int max() { return this.max(0,0, width, height); }
//	public int max(int toX, int toY, int width, int height) {
//		return max(pixels, offset+toX+toY*scan, scan, width, height);
//	}
//
//	public int min() { return this.min(0,0, width, height); }
//	public int min(int toX, int toY, int width, int height) {
//		int min = Integer.MAX_VALUE;
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r)
//			for (int x=0,v=0;x<width;x++,o++)
//				min = min<(v=pixels[o])?min:v;
//
//		return min;
//	}
//
//
//	public IntArrayImage convolve(IntArrayImage from, SeparableKernel k, int scanX, int scanY) {
//		k.convolve(this, 0, 0, from, 0, 0, Math.min(from.width,this.width), Math.min(from.height, this.height), scanX, scanY);
//		return this;
//	}
//
//	public IntArrayImage convolve(int toX, int toY, IntArrayImage from, int fromX, int fromY, int width, int height, SeparableKernel k, int scanX, int scanY) {
//		k.convolve(this, toX, toY, from, fromX, fromY, width, height, scanX, scanY);
//		return this;
//	}
//
//	public IntArrayImage threshold(int threshold, int positive, int negative) {
//		return this.threshold(0, 0, width, height, threshold, positive, negative);
//	}
//
//	public IntArrayImage threshold(IntArrayImage li, int threshold, int positive, int negative) {
//		return this.threshold(0, 0, Math.min(width,li.width), Math.min(height,li.height), li, 0, 0, threshold, positive, negative);
//	}
//
//	public IntArrayImage threshold(int toX, int toY, int width, int height, int threshold, int positive, int negative) {
//		return this.threshold(toX, toY, width, height, this, toX, toY, threshold, positive, negative);
//	}
//
//	public IntArrayImage threshold(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY, int threshold, int positive, int negative) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o]=li.values[O]>threshold?positive:negative;
//
//		return this;
//	}
//
////	mehrdeutig
////	public IntBufferedImage quadrance(int toX, int toY, int width, int height, IntBufferedImage[] as, int aX, int aY, IntBufferedImage[] bs, int bX, int bY) {
////		quadrance(toX, toY, width, height, as[0], aX, aY, bs[0], bX, bY);
////
////		for (int i=1,I=as.length;i<I;i++)
////			addQuadrance(toX, toY, width, height, as[i], aX, aY, bs[i], bX, bY);
////
////		return this;
////	}
//
//	public IntArrayImage quadrance(int toX, int toY, int width, int height, IntArrayImage a, int aX, int aY, IntArrayImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb)
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int A = a.values[oa], B = b.values[ob], AB = B-A;
//				values[o] = AB*AB;
//			}
//
//		return this;
//	}
//
//	public IntArrayImage addQuadrance(int toX, int toY, int width, int height, IntArrayImage a, int aX, int aY, IntArrayImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb)
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int A = a.values[oa], B = b.values[ob], AB = B-A;
//				values[o] += AB*AB;
//			}
//
//		return this;
//	}
//
//
//	public IntArrayImage quadrance(int toX, int toY, int width, int height, PixelArrayImage a, int aX, int aY, PixelArrayImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb)
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int argbA = a.pixels[oa], argbB = b.pixels[ob];
//				final int db = (argbA&0xFF)-(argbB&0xFF), dg=((argbA>>>8)&0xFF)-((argbB>>>8)&0xFF), dr=((argbA>>>16)&0xFF)-((argbB>>>16)&0xFF), da=((argbA>>>24)&0xFF)-((argbB>>>24)&0xFF);
//				values[o] = (db*db  +  dg*dg  +  dr*dr  +  da*da);
//			}
//
//		return this;
//	}
//
//
//	public IntArrayImage subSquared(IntArrayImage gx) { return this.subSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
//	public IntArrayImage subSquared(int toX, int toY, int width, int height, IntArrayImage gx, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r)
//			for (int x=0;x<width;x++,ox++,o++) {
//				final int g_x=gx.values[ox];
//				values[o] -= g_x*g_x;
//			}
//
//		return this;
//	}
//
//
//	public IntArrayImage addSquared(IntArrayImage gx) { return this.addSquared(0, 0, Math.min(gx.width,width), Math.min(gx.height,height), gx, 0,0); }
//	public IntArrayImage addSquared(int toX, int toY, int width, int height, IntArrayImage gx, int fromX, int fromY) {
//		addSquared(this.pixels, this.offset+toX+toY*this.scan, this.scan, this.width, this.height, gx.pixels, gx.offset+fromX+fromY*gx.scan, gx.scan);
//		return this;
//	}
//
//	public IntArrayImage mix(int toX, int toY, int width, int height, IntArrayImage gx, int fromX, int fromY, final int dst, final int src) {
////		int a = (int)(alpha*(1<<8)), oma = (int)((1-alpha)*(1<<8));
//
//		final int norm = src+dst;
//
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r)
//			for (int x=0;x<width;x++,ox++,o++) {
//
//				values[o] = (values[o]*dst+gx.values[ox]*src)/norm;
//			}
//
//		return this;
//	}
//
//	public IntArrayImage mipmap(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,S=li.scan,rx=2*(li.scan-width);y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O+=2,o++)
//				values[o] = (li.values[O]+li.values[O+1]+li.values[O+S]+li.values[O+S+1])>>2;
//
//		return this;
//	}
//
//	public IntArrayImage mul(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] *= li.values[O];
//
//		return this;
//	}
//
//	public IntArrayImage div(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] /= li.values[O];
//
//		return this;
//	}
//
//	public IntArrayImage add(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] += li.values[O];
//
//		return this;
//	}
//
//	public IntArrayImage sub(IntArrayImage li) { return sub(0,0,Math.min(li.width, width),Math.min(li.height,height),li,0,0); }
//	public IntArrayImage sub(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY) {
//		sub(	this.values, this.offset, this.scan,
//				width, height,
//				li.values, li.offset, li.scan);
//
//		return this;
//	}
//
//	public IntArrayImage sqrt() { return this.sqrt(0,0,width,height); }
//	public IntArrayImage sqrt(int toX, int toY, int width, int height) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				values[o] = (int) Math.sqrt( values[o] );
//
//		return this;
//	}
//
//	public IntArrayImage div(int value) { return this.div(0,0, width, height, value); }
//	public IntArrayImage div(int toX, int toY, int width, int height, int value) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				values[o] /= value;
//
//		return this;
//	}
//
//	public IntArrayImage mul(int value) { return this.mul(0,0, width, height, value); }
//	public IntArrayImage mul(int toX, int toY, int width, int height, int value) {
//		mul(pixels, offset+toX+toY*scan, scan, width, height, value);
//		return this;
//	}
//
//	public IntArrayImage mul(double value) { return this.mul(0,0, width, height, value); }
//	public IntArrayImage mul(int toX, int toY, int width, int height, double value) {
//		float v = (float)value;
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				values[o] *= v;
//
//		return this;
//	}
//
//
//	public IntArrayImage clamp(int min, int max) { return this.clamp(0,0, width, height, min, max); }
//	public IntArrayImage clamp(int toX, int toY, int width, int height, int min, int max) {
//		clamp(pixels, offset+ toX+toY*scan, scan, width, height, min, max);
//		return this;
//	}
//
//
//	public IntArrayImage sub(int value) { return this.add(0,0, width, height, -value); }
//	public IntArrayImage sub(int toX, int toY, int width, int height, int value) { return this.add(toX,toY,width,height,-value); }
//	public IntArrayImage add(int value) { return this.add(0,0, width, height, value); }
//	public IntArrayImage add(int toX, int toY, int width, int height, int value) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				values[o] += value;
//
//		return this;
//	}
//
//	public IntArrayImage add(IntArrayImage li) { return this.add(0,0,this.width,this.height,li, 0,0,1,0); }
//	public IntArrayImage add(int toX, int toY, int width, int height, IntArrayImage li, int fromX, int fromY, int norm, int base) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] += base+li.values[O]/norm;
//
//		return this;
//	}
//

//
//	public IntArrayImage set(ByteArrayImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); }
//	public IntArrayImage set(int toX, int toY, int width, int height, ByteArrayImage from, int fromX, int fromY) {
//		for (int y=0,o=this.offset+toX+toY*this.scan,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] = from.data[O]&0xFF;
//
//		return this;
//	}
//
//
//	public IntArrayImage set(FloatBufferedImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); }
//	public IntArrayImage set(int toX, int toY, int width, int height, FloatBufferedImage from, int fromX, int fromY) {
//		for (int y=0,o=this.offset+toX+toY*this.scan,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				values[o] = (int) from.data[O];
//
//		return this;
//	}
//
//
//	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//
//	public static int[] sub( int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] -= src[O];
//
//		return dst;
//	}
//
//	public static int[] add( int[] dst, int doffset, int dscan,  int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] += src[O];
//
//		return dst;
//	}
//
//
//
//	public static int[] mul( int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] *= src[O];
//
//		return dst;
//	}
//
//
//	public static int[] addSquared( int[] dst, int doffset, int dscan,  int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r)
//			for (int x=0;x<width;x++,O++,o++) {
//				final int v = src[O];
//				dst[o] += v*v;
//			}
//
//		return dst;
//	}
//
//
//
//
////	public static int[] set(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
////		final int sx = (int) shiftX, sy = (int) shiftY;
////
////		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
////		final float uo = 1 - ur, vo = 1 - vr;
////		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
////
////		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
////			for (int i=0,q;i<width;i++,o++,O++)
////				dst[o] = (int)((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr);
////
////		return dst;
////	}
//
//	public static int[] set(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] = ((((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr)>>8));
//
//		return dst;
//	}
//
//
////	public static int[] add(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY) {
////		final int sx = (int) shiftX, sy = (int) shiftY;
////
////		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
////		final float uo = 1 - ur, vo = 1 - vr;
////		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
////
////		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
////			for (int i=0,q;i<width;i++,o++,O++)
////				dst[o] += (int)((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr);
////
////		return dst;
////	}
//
//	public static int[] add(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] += ((((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr)>>8));
//
//		return dst;
//	}
//
////	public static int[] sub(int dst[], int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
////		final int sx = (int) shiftX, sy = (int) shiftY;
////
////		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
////		final float uo = 1 - ur, vo = 1 - vr;
////		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
////
////		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
////			for (int i=0,q;i<width;i++,o++,O++)
////				dst[o] -= (int)((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr);
////
////		return dst;
////	}
//
//	public static int[] sub(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] -= ((((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr)>>8));
//
//		return dst;
//	}
//
//	public static int[] mul(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] *= ((((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr)>>8));
//
//		return dst;
//	}
//
//
//
//	public static int[] div(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] /= ((((src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr)>>8));
//
//		return dst;
//	}
//
//	public static int[] dilate(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		int W = width, H = height-2;
//
//		for (int y=2,o=doffset+y*dscan,r=dscan-W,O=y*sscan+soffset,S = sscan,R=sscan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//
//				int max = src[O-0];
////				int a = src[O-1-S];
//				int b = src[O-0-S];
////				int c = src[O+1-S];
//				int d = src[O-1];
//				int e = src[O+1];
////				int f = src[O-1+S];
//				int g = src[O-0+S];
////				int h = src[O+1+S];
//
////				max = max>a?max:a;
//				max = max>b?max:b;
////				max = max>c?max:c;
//				max = max>d?max:d;
//				max = max>e?max:e;
////				max = max>f?max:f;
//				max = max>g?max:g;
////				max = max>h?max:h;
//
//				dst[o] = max;
//			}
//
//		return dst;
//	}
//
//
//	public static int[] erode(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
////		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2;
//		int W = width, H = height-2;
//
//		for (int y=2,o=doffset+y*dscan,r=dscan-W,O=y*sscan+soffset,S = sscan,R=sscan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//
//				int min = src[O-0];
////				int a = src[O-1-S];
//				int b = src[O-0-S];
////				int c = src[O+1-S];
//				int d = src[O-1];
//				int e = src[O+1];
////				int f = src[O-1+S];
//				int g = src[O-0+S];
////				int h = src[O+1+S];
//
////				min = min<a?min:a;
//				min = min<b?min:b;
////				min = min<c?min:c;
//				min = min<d?min:d;
//				min = min<e?min:e;
////				min = min<f?min:f;
//				min = min<g?min:g;
////				min = min<h?min:h;
//
//				dst[o] = min;
//			}
//
//		return dst;
//	}
//
//
//	public static int[] add(int[] dst, int doffset, int dscan, int width, int height, int value) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				dst[o] += value;
//
//		return dst;
//	}
//
//	public static int[] mul(int[] dst, int doffset, int dscan, int width, int height, int value) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				dst[o] *= value;
//
//		return dst;
//	}
//
//	public static int[] div(int[] dst, int doffset, int dscan, int width, int height, int value) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				dst[o] /= value;
//
//		return dst;
//	}
//
//	public static int[] mul(int[] dst, int doffset, int dscan, int width, int height, float value) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				dst[o] *= value;
//
//		return dst;
//	}
//
//	public static int[] mul(int[] dst, int doffset, int dscan, int width, int height, double value) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++)
//				dst[o] *= value;
//
//		return dst;
//	}
//
//	public static int[] clamp(int[] dst, int doffset, int dscan, int width, int height, int min, int max) {
//		for (int y=0,o=doffset,r=dscan-width;y<height;y++,o+=r)
//			for (int x=0;x<width;x++,o++) {
//				int value = dst[o];
//				dst[o] = value<min?min:(value>max?max:value);
//			}
//
//		return dst;
//	}
//
//	public static int max(int[] src, int soffset, int sscan, int width, int height) {
//		int max = Integer.MIN_VALUE;
//		for (int y=0,o=soffset,r=sscan-width;y<height;y++,o+=r)
//			for (int x=0,v=0;x<width;x++,o++)
//				max = max>(v=src[o])?max:v;
//
//		return max;
//	}
//
//
//
////	static public int[] Image set(int to[], int toffset, int tscan, int width, int height, int from[], int foffset, int fscan) {
////		for (int y=0,o=toffset,r=tscan,O=foffset,rx=fscan;y<height;y++,O+=rx,o+=r)
////			System.arraycopy(from, O, to, o, width);
////		return to;
////	}
	
}
