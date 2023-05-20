package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;


public class ShortPlanesBufferedImage extends ShortBufferedImage {
	
	final public short[] planes[];
	
	
	public ShortPlanesBufferedImage convolve( 
			int toX, int toY, int width, int height, 
			ShortPlanesBufferedImage li, int fromX, int fromY,
			SeparableKernel kernel, int stepX, int stepY) {

		for (int i=0,l=planes.length;i<l;i++)
			convolve(width, height, 
				this.planes[i], this.offset+toX+toY*scan, this.scan, 
				li.planes[i], li.offset+fromX+fromY*li.scan, li.scan, 
				kernel.coefficients, kernel.offset, kernel.length, kernel.norm, stepX+stepY*li.scan);
		
		return this;
	}
	
	
	private static int[] fill(int[] data, int val) {
		Arrays.fill(data, val);
		return data;
	}
	
	@SuppressWarnings("restriction")
	public ShortPlanesBufferedImage(int width, int height, int offset, int scan, ColorModel cm, short[]... planes) {
		super(width, height, offset, scan, cm,
				cm.createCompatibleWritableRaster(width,height)
//				new sun.awt.image.ShortBandedRaster(
//					new BandedSampleModel(DataBuffer.TYPE_USHORT, width, height, planes.length),
//					new DataBufferUShort(planes, planes.length, fill(new int[planes.length],offset)),
//					new Point(0,0)
//				)
				, false );
		
		this.planes = planes.clone();
	}

}
