package de.dualuse.awt.image;

public class RGBAfBufferedImage extends RGBAFloatBufferedImage {

	public RGBAfBufferedImage(int width, int height, float[] rPlane, float[] gPlane, float[] bPlane, float[] aPlane,
			int offsetR, int offsetG, int offsetB, int offsetA, int scan) {
		super(width, height, rPlane, gPlane, bPlane, aPlane, offsetR, offsetG, offsetB, offsetA, scan);
	}

	public RGBAfBufferedImage(int width, int height, FloatBufferedImage r, FloatBufferedImage g, FloatBufferedImage b,
			FloatBufferedImage a) {
		super(width, height, r, g, b, a);
	}

	public RGBAfBufferedImage(int width, int height) {
		super(width, height);
	}

	public RGBAfBufferedImage(PixelBufferedImage pbi) {
		super(pbi);
	}
	
}
