package de.dualuse.commons.awt.image;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.util.Arrays;
import java.util.List;

public class FloatPlanesBufferedImage extends BufferedImage {
//	final public float[] planes[];
	
	public final int width, height, scan;
	public final FloatBufferedImage[] planes;
	
	private static int[] indices(float[][] planes) {
		List<float[]> planeList = Arrays.asList(planes); 
		
		int[] indices = new int[planes.length];
		for (int i=0;i<planes.length;i++)
			indices[i]=planeList.indexOf(planes[i]);
		
		return indices;
	}
	
//	private static int[] fill(int[] data, int val) {
//		Arrays.fill(data, val);
//		return data;
//	}
	
	public FloatPlanesBufferedImage(int width, int height, float[] planes[], int offsets[], int scan, ColorModel cm) {
		super(cm, new FloatBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, scan, indices(planes), new int[planes.length]),
						new DataBufferFloat(planes, planes.length, offsets),
						new Point(0,0)
					), false, null);
		
		this.width = width;
		this.height = height;
		this.scan = scan;
		
//		super(width, height, offset[0], scan, cm, 
//				new FloatBandedRaster(
//					new BandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, scan, indices(planes), new int[planes.length]),
//					new DataBufferFloat(planes, planes.length, offsets),
//					new Point(0,0)
//				), false );
		
		this.planes = new FloatBufferedImage[planes.length];
		
		for (int i=0;i<planes.length;i++)
			this.planes[i] = new FloatBufferedImage(width, height, planes[i], offsets[i], scan, cm);
		
	}
	
}



