package de.dualuse.commons.awt.image;

import java.awt.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;


public class IntPlanesBufferedImage extends BufferedImage {
	
	public final int width, height, scan;
	public final IntBufferedImage planes[];
	
	
	private static int[] indices(int[][] planes) {
		List<int[]> planeList = Arrays.asList(planes); 
		
		int[] indices = new int[planes.length];
		for (int i=0;i<planes.length;i++)
			indices[i]=planeList.indexOf(planes[i]);
		
		return indices;
	}
	
	public IntPlanesBufferedImage(int width, int height, int[] planes[], int offsets[], int scan, ColorModel cm) {
		super(cm, new IntBandedRaster(
						new BandedSampleModel(DataBuffer.TYPE_INT, width, height, scan, indices(planes), new int[planes.length]),
						new DataBufferInt(planes, planes.length, offsets),
						new Point(0,0)
					), false, null);
		
		this.width = width;
		this.height = height;
		this.scan = scan;
		
		this.planes = new IntBufferedImage[planes.length];
		
		for (int i=0;i<planes.length;i++)
			this.planes[i] = new IntBufferedImage(width, height, planes[i], offsets[i], scan);
		
	}


}