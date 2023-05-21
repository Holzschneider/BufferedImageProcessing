package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import static java.lang.Math.min;

public class FloatArrayImage extends CustomBufferedImage {
	static ColorModel RED_COLOR_MODEL = new ComponentColorModel(VALUE_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			final float r[] = (float[]) inData;
			final float red = r[0];
			final int RED = ((int)(red<1&&red>0?red*255f:red))&0xFF;

			return 0xFF000000 | (RED<<16);
		}
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

	public final float[] values;

	public FloatArrayImage(FloatArrayImage fbi) {
		this(fbi.width, fbi.height, fbi.values, fbi.offset, fbi.scan);
	}

	public FloatArrayImage(FloatArrayImage fbi, ColorModel model) {
		this(fbi.width, fbi.height, fbi.values, fbi.offset, fbi.scan, model);
	}

	public FloatArrayImage(int width, int height) {
		this(width, height, new float[width*height], 0, width);
	}

	public FloatArrayImage(int width, int height, ColorModel model) {
		this(width, height, new float[width*height], 0, width, model);
	}

	public FloatArrayImage(int width, int height, float[] data, int offset, int scan) {
		this(width, height, data, offset, scan, VALUE_COLOR_MODEL);
	}
	
	protected FloatArrayImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster wr) {
		super(width, height, offset, scan, cm, wr, false);
		values = ((DataBufferFloat)wr.getDataBuffer()).getData();
	}
	
	protected FloatArrayImage(int width, int height, float[] values, int offset, int scan, ColorModel cm) {
		super(width, height, offset, scan, cm,
				new FloatBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, scan, new int[] {0}, new int[] {0}),
						new DataBufferFloat(values, height*scan, offset),
						new Point(0,0)
						),
				true);
		
		this.values = values;
	}

	@Override
	public FloatArrayImage crop(int x, int y, int width, int height) {
		return new FloatArrayImage(width,height,values, offset(x,y), scan);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int offset(int x, int y) { return offset+x+y*scan; }
	
	public FloatArrayImage set(int x, int y, int width, int height, float value) {
		if (scan==width)
			Arrays.fill(values, offset(x,y), offset(x+width,y+height), value);
		else
			for (int Y=y+height, o=offset(x,y);y<Y;y++,o+=scan)
				Arrays.fill(values, o, o+width, value);

		return this;
	}

	public FloatArrayImage set(IntArrayImage that) { return set(0,0, min(this.width,that.width), min(this.height,that.height),that,0,0); };
	public FloatArrayImage set(int thisX, int thisY, IntArrayImage that)  { return set(thisX,thisY, min(this.width-thisX,that.width), min(this.height-thisY,that.height),that,0,0);}
	public FloatArrayImage set(int thisX, int thisY, int width, int height, IntArrayImage that, int thatX, int thatY)  {
		width  = min(width ,that.width -thatX);
		height = min(height,that.height-thatY);

		for (int y=0,o=offset+thisY*scan+thisX,r=scan-width,O=thatX+thatY*that.scan,rx=that.scan-width;y<height;y++,O+=rx,o+=r)
			for (int x=0;x<width;x++,O++,o++)
				values[o] = that.values[O];

		return this; 
	}

	public FloatArrayImage set(FloatArrayImage from) { return set(0,0, min(width,from.width), min(height,from.height),from,0,0); };
	public FloatArrayImage set(int thisX, int thisY, FloatArrayImage from) { return set(thisX,thisY, min(width-thisX,from.width), min(height-thisY,from.height),from,0,0); };
	public FloatArrayImage set(int thisX, int thisY, int width, int height, FloatArrayImage that, int thatX, int thatY)  {
		width  = min(width ,that.width -thatX);
		height = min(height,that.height-thatY);

		for (int y=0,thisO=this.offset(thisX,thisY),thatO=that.offset(thatX,thatY);y<height;y++,thisO+=this.scan,thatO+=that.scan)
			System.arraycopy(that.values,thatO, this.values,thisO, width);

		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, float src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] = (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
//
//		return dst;
//	}


//	public static float[] set(float[] dst, int doffset, int dscan, int width, int height, int src[], int soffset, int sscan, double shiftX, double shiftY ) {
//		final int sx = (int) shiftX, sy = (int) shiftY;
//
//		final float ur = (float)shiftX-sx, vr = (float)shiftY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final float uovo = (uo*vo), urvo = (ur*vo), uovr = (uo*vr), urvr = (ur*vr);
//
//		for (int j=0,o=doffset,O=soffset+sx+sy*sscan,P=sscan-width,p=dscan-width;j<height;j++,O+=P,o+=p)
//			for (int i=0,q;i<width;i++,o++,O++)
//				dst[o] = (src[q=O])*uovo+(src[q+=1])*urvo+(src[q+=sscan])*urvr+(src[q-=1])*uovr;
//
//		return dst;
//	}

}
