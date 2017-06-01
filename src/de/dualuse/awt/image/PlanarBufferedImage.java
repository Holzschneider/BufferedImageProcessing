package de.dualuse.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class PlanarBufferedImage extends CustomBufferedImage {

	public PlanarBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
		super(width, height, offset, scan, cm, raster, isRasterPremultiplied);
	}
	
}

//package de.dualuse.common.awt.image;
//
//import java.awt.Point;
//import java.awt.image.*;
//import java.util.Arrays;
//
//public class PlanarBufferedImage extends CustomBufferedImage {
//	
//	private static int[] fill(int[] data, int val) {
//		Arrays.fill(data, val);
//		return data;
//	}
//	
//	@SuppressWarnings("restriction")
//	protected PlanarBufferedImage(int width, int height, int offset, int scan, ColorModel cm, byte[]... planes) {
//		super(width, height, offset, scan, cm, 
//				new sun.awt.image.ByteBandedRaster(
//					new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, planes.length),
//					new DataBufferByte(planes, planes.length, fill(new int[planes.length],offset)),
//					new Point(0,0)
//				), false );
//	}
//
//	@SuppressWarnings("restriction")
//	protected PlanarBufferedImage(int width, int height, int offset, int scan, ColorModel cm, short[]... planes) {
//		super(width, height, offset, scan, cm, 
//				new sun.awt.image.ShortBandedRaster(
//					new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, planes.length),
//					new DataBufferShort(planes, planes.length, fill(new int[planes.length],offset)),
//					new Point(0,0)
//				), false );
//	}
//
////	public PlanarBufferedImage(int width, int height, int offset, int scan, ColorModel cm, float[]... planes) {
////		super(width, height, offset, scan, cm, 
////				new FloatBandedRaster(
////					new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, planes.length),
////					new DataBufferFloat(planes, planes.length, fill(new int[planes.length],offset)),
////					new Point(0,0)
////				), false );
////	}
//
//}
//
////class Component3bPlanarBufferdImage extends PlanarBufferedImage {
////
////	public Component3bPlanarBufferdImage(int width, int height, int offset, int scan, ColorModel cm, byte[] a, byte[] b, byte[] c) {
////		super(width, height, offset, scan, cm, a, b, c);
////	}
////	
////}
//
