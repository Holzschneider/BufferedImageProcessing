package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ByteBufferedImage extends CustomBufferedImage implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(offset);
		out.writeInt(scan);
		out.writeObject(pixels);
	}
	
	@SuppressWarnings("restriction") private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		try {
			Field widthField = ByteBufferedImage.class.getField("width"); widthField.setAccessible(true); widthField.setInt(this, in.readInt());
			Field heightField = ByteBufferedImage.class.getField("height"); heightField.setAccessible(true); heightField.setInt(this, in.readInt());
			Field offsetField = ByteBufferedImage.class.getField("offset"); offsetField.setAccessible(true); offsetField.setInt(this, in.readInt());
			Field scanField = ByteBufferedImage.class.getField("scan"); scanField.setAccessible(true); scanField.setInt(this, in.readInt());
			Field pixelsField = ByteBufferedImage.class.getField("pixels"); pixelsField.setAccessible(true); pixelsField.set(this, in.readObject());
			Field dataField = ByteBufferedImage.class.getField("data"); dataField.setAccessible(true); dataField.set(this, this.pixels);
		
			Field typeField = BufferedImage.class.getDeclaredField("imageType"); typeField.setAccessible(true); typeField.set(this, BufferedImage.TYPE_CUSTOM);
			Field colorModelField = BufferedImage.class.getDeclaredField("colorModel"); colorModelField.setAccessible(true); colorModelField.set(this, ByteColorModel);
			Field rasterField = BufferedImage.class.getDeclaredField("raster"); rasterField.setAccessible(true); rasterField.set(this, new sun.awt.image.ByteBandedRaster(
					new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 1),
					new DataBufferByte(data, height*scan+offset, offset),
					new Point(0,0)
				));
			
		} catch (Exception ex) {
			throw new Error(ex);
		}

		
	}


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
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	
	static ColorModel ByteColorModel = new ComponentColorModel(Y_COLOR_SPACE, new int[] { 8 }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE) {
		public int getRGB(Object inData) {
			byte[] rgb = (byte[])inData;
			
			final int raw = rgb[0]&0xFF;
			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = (byte)clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | absed;
		}
	};
	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public final byte[] data;
	public final byte[] pixels;
	
//	public ByteBufferedImage clone() {
//		return new ByteBufferedImage(width, height, Arrays.copyOf(data, data.length), offset, scan);
//	}
	
	public ByteBufferedImage(int width, int height) {
		this(width, height, new byte[width*height], 0, width);
	}
	
	public ByteBufferedImage(int width, int height, byte data[], int offset, int scan) {
		this(width, height, data, offset, scan, ByteColorModel);
	}
	
	protected ByteBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster wr) {
		super(width, height, offset, scan, cm, wr, false);
		
		this.pixels = this.data = ((DataBufferByte)wr.getDataBuffer()).getData(); 
	}
	
	protected ColorModel colorModel;
	@SuppressWarnings("restriction") 
	protected ByteBufferedImage(int width, int height, byte[] data, int offset, int scan, ColorModel cm) {
		super(width, height, offset, scan, cm,
				new sun.awt.image.ByteBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 1),
						new DataBufferByte(data, height*scan+offset, offset),
						new Point(0,0)
					),
				true);
		
		this.colorModel = cm;
		this.pixels = this.data = data;
	}
	
	public ByteBufferedImage getSubimage(int x, int y, int w, int h) {
		return new ByteBufferedImage(w, h, data, this.offset+x+y*this.scan, this.scan, this.colorModel);
	}
	
	public float getValue(float x, float y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;
	
//		x-=0.5f;
//		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan;
		final int ul = data[o], ur = data[++o], lr = data[o+=scan], ll = data[--o];
		
		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
		return (ul*omxr+ur*xr)*omyr+(ll*omxr+lr*xr)*yr;
	}
	
	public int getValue(int x, int y) {
		if (x>0 && y>0 && x<width && y<height)
			return data[x+y*scan];
		else
			return 0;
	}

	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	
//	
//	
//	public ByteBufferedImage dilate(int fromX, int fromY, int width, int height, ByteBufferedImage li  ) {
//		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
//		
//		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//				
//				int max = li.data[O-0];
//				int a = li.data[O-1-S];
//				int b = li.data[O-0-S];
//				int c = li.data[O+1-S];
//				int d = li.data[O-1];
//				int e = li.data[O+1];
//				int f = li.data[O-1+S];
//				int g = li.data[O-0+S];
//				int h = li.data[O+1+S];
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
//	public ByteBufferedImage erode(int fromX, int fromY, int width, int height, ByteBufferedImage li) {
//		int W = this.width<li.width?this.width:li.width, H = (this.height<li.height?this.height:li.height)-2; 
//		
//		for (int y=2,o=offset+y*scan,r=scan-W,O=y*li.scan+li.offset+fromX+fromY*li.scan,S = li.scan,R=li.scan-W;y<H;y++,o+=r,O+=R)
//			for (int x=0;x<W;x++,o++,O++) {
//				
//				int min = li.data[O-0];
//				int a = li.data[O-1-S];
//				int b = li.data[O-0-S];
//				int c = li.data[O+1-S];
//				int d = li.data[O-1];
//				int e = li.data[O+1];
//				int f = li.data[O-1+S];
//				int g = li.data[O-0+S];
//				int h = li.data[O+1+S];
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
//	
//	
//
//	
//	public ByteBufferedImage set(int toX, int toY, int width, int height, ByteBufferedImage from, double fromX, double fromY)  { set(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset, from.scan, fromX, fromY); return this; }
//	public ByteBufferedImage sub(int toX, int toY, int width, int height, ByteBufferedImage from, double fromX, double fromY) { sub(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
//	public ByteBufferedImage add(int toX, int toY, int width, int height, ByteBufferedImage from, double fromX, double fromY) { add(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
//	public ByteBufferedImage mul(int toX, int toY, int width, int height, ByteBufferedImage from, double fromX, double fromY) { mul(this.data, this.offset+toX+toY*this.scan, this.scan, width, height, from.data, from.offset,from.scan, fromX, fromY); return this; }
//	
//	
//	public ByteBufferedImage threshold(int toX, int toY, int width, int height, int threshold, int positive, int negative) {
//		return this.threshold(toX, toY, width, height, this, toX, toY, threshold, positive, negative);
//	}
//	
//	public ByteBufferedImage threshold(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY, int threshold, int positive, int negative) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o]=li.data[O]>threshold?positive:negative;
//				
//		return this;
//	}
//	
//	public ByteBufferedImage quadrance(int toX, int toY, int width, int height, ByteBufferedImage[] as, int aX, int aY, ByteBufferedImage[] bs, int bX, int bY) {
//		quadrance(toX, toY, width, height, as[0], aX, aY, bs[0], bX, bY);
//		
//		for (int i=1,I=as.length;i<I;i++) 
//			addQuadrance(toX, toY, width, height, as[i], aX, aY, bs[i], bX, bY);
//			
//		return this;
//	}
//
//	public ByteBufferedImage quadrance(int toX, int toY, int width, int height, ByteBufferedImage a, int aX, int aY, ByteBufferedImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb) 
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int A = a.data[oa], B = b.data[ob], AB = B-A;
//				data[o] = AB*AB;
//			}
//		
//		return this;
//	}
//
//	public ByteBufferedImage addQuadrance(int toX, int toY, int width, int height, ByteBufferedImage a, int aX, int aY, ByteBufferedImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb) 
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int A = a.data[oa], B = b.data[ob], AB = B-A;
//				data[o] = AB*AB;
//			}
//		
//		return this;
//	}
//	
//	
//	public ByteBufferedImage quadrance(int toX, int toY, int width, int height, PixelBufferedImage a, int aX, int aY, PixelBufferedImage b, int bX, int bY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width, oa=a.offset+aY*a.scan+aX, ra=a.scan-width, ob=b.offset+bY*b.scan+bX, rb=b.scan-width; y<height; y++, o+=r, oa+=ra, ob+=rb) 
//			for (int x=0;x<width;x++,o++,oa++,ob++) {
//				final int argbA = a.pixels[oa], argbB = b.pixels[ob];
//				final int db = (argbA&0xFF)-(argbB&0xFF), dg=((argbA>>>8)&0xFF)-((argbB>>>8)&0xFF), dr=((argbA>>>16)&0xFF)-((argbB>>>16)&0xFF), da=((argbA>>>24)&0xFF)-((argbB>>>24)&0xFF);
//				data[o] = (db*db  +  dg*dg  +  dr*dr  +  da*da);
//				
//			}
//		
//		return this;
//	}
//	
//	
//	public ByteBufferedImage addSquared(int toX, int toY, int width, int height, ByteBufferedImage gx, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
//			for (int x=0;x<width;x++,ox++,o++) {
//				final int g_x=gx.data[ox];
//				data[o] += g_x*g_x;
//			}
//		
//		return this;
//	}
//
//	public ByteBufferedImage mix(int toX, int toY, int width, int height, ByteBufferedImage gx, int fromX, int fromY, final int dst, final int src) {
////		int a = (int)(alpha*(1<<8)), oma = (int)((1-alpha)*(1<<8));
//		
//		final int norm = src+dst;
//		
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
//			for (int x=0;x<width;x++,ox++,o++) {
//				
//				data[o] = (data[o]*dst+gx.data[ox]*src)/norm;
//			}
//		
//		return this;
//	}
//
//	public ByteBufferedImage mipmap(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,S=li.scan,rx=2*(li.scan-width);y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O+=2,o++) 
//				data[o] = (li.data[O]+li.data[O+1]+li.data[O+S]+li.data[O+S+1])>>2;
//		
//		return this;
//	}
//	
//	public ByteBufferedImage mul(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o] *= li.data[O];
//		
//		return this;
//	}
//
//	public ByteBufferedImage add(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o] += li.data[O];
//		
//		return this;
//	}
//	
//	public ByteBufferedImage sub(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY) {
//		sub(	this.data, this.offset, this.scan, 
//				width, height, 
//				li.data, li.offset, li.scan);
//		
//		return this;
//	}
//
//	public ByteBufferedImage add(int toX, int toY, int width, int height, int value) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width;y<height;y++,o+=r) 
//			for (int x=0;x<width;x++,o++)
//				data[o] += value;
//		
//		return this;
//	}
//
//	public ByteBufferedImage add(int toX, int toY, int width, int height, ByteBufferedImage li, int fromX, int fromY, int norm, int base) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o] += base+li.data[O]/norm;
//		
//		return this;
//	}
//
//	public ByteBufferedImage set(int toX, int toY, int width, int height, ByteBufferedImage from, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan,O=fromX+fromY*from.scan,rx=from.scan;y<height;y++,O+=rx,o+=r)
//			System.arraycopy(from.data, O, data, o, width);
//		
////		for (int y=0,o=this.offset+toX+toY*this.scan,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r) 
////			for (int x=0;x<width;x++,O++,o++)
////				data[o] = from.data[O];
////		
//		return this;
//	}
//	
//
//	public ByteBufferedImage set(int toX, int toY, int width, int height, FloatBufferedImage from, int fromX, int fromY) {
//		for (int y=0,o=this.offset+toX+toY*this.scan,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o] = (int) from.data[O];
//		
//		return this;
//	}
//
//	public ByteBufferedImage set(int toX, int toY, int width, int height, int value) {
//		set(data, offset+toX+toY*scan, scan, width, height, value);
//		return this;
//	}
//
//	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	
//	public static int[] set(int[] dst, int doffset, int dscan, int width, int height, int value) {
//		for (int y=0,o=doffset,r=dscan;y<height;y++,o+=r)
//			Arrays.fill(dst, o, o+width, value);
//		
//		return dst;
//	}
//	
//	public static int[] set(int[] dst, int doffset, int dscan, int width, int height, int[] src, int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan,O=soffset,rx=sscan;y<height;y++,O+=rx,o+=r)
//			System.arraycopy(src, O, dst, o, width);
//		
//		return dst;
//	}
//
//	public static int[] sub(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] -= src[O];
//		
//		return dst;
//	}
//
//	public static int[] add(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] += src[O];
//		
//		return dst;
//	}
//	
//	public static int[] mul(int[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan) {
//		for (int y=0,o=doffset,r=dscan-width,O=soffset,rx=sscan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				dst[o] += src[O];
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
}
