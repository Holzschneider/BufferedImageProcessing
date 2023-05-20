package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.util.Arrays;


public class BytePlanesBufferedImage extends CustomBufferedImage {
	
	final public byte[] planes[];
	
	
	private static int[] fill(int[] data, int val) {
		Arrays.fill(data, val);
		return data;
	}
	
	@SuppressWarnings("restriction")
	public BytePlanesBufferedImage(int width, int height, int offset, int scan, ColorModel cm, byte[]... planes) {
		super(width, height, offset, scan, cm,
				cm.createCompatibleWritableRaster(width,height),
//				new sun.awt.image.ByteBandedRaster(
//					new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, planes.length),
//					new DataBufferByte(planes, planes.length, fill(new int[planes.length],offset)),
//					new Point(0,0)
//				),
				false );
		
		this.planes = planes.clone();
	}

}
