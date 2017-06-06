package de.dualuse.awt.image;

public class RGBfBufferedImage extends RGBFloatBufferedImage {

	public RGBfBufferedImage(int width, int height, float[] rPlane, float[] gPlane, float[] bPlane, int offsetR,
			int offsetG, int offsetB, int scan) {
		super(width, height, rPlane, gPlane, bPlane, offsetR, offsetG, offsetB, scan);
	}

	public RGBfBufferedImage(int width, int height, FloatBufferedImage r, FloatBufferedImage g, FloatBufferedImage b) {
		super(width, height, r, g, b);
	}

	public RGBfBufferedImage(int width, int height) {
		super(width, height);
	}

	public RGBfBufferedImage(PixelBufferedImage pbi) {
		super(pbi);
	}

}
