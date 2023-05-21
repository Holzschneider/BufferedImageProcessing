package de.dualuse.awt.image;


public class GradientBufferedImage extends YUVBufferedImage {
	
	public GradientBufferedImage(int width, int height) {
		super(width, height);
	}
	
	public GradientBufferedImage extract(int toX, int toY, int width, int height, IntArrayImage from, int fromX, int fromY, SeparableKernel filter) {
		
		filter.norm(1).convolve(U, toX, toY, from, fromX, fromY, width, height, 1, 0);
		filter.norm(filter.norm*filter.norm).convolve(Y, toX, toY, U, toX, toY, width, height, 0, 1);
		
		SeparableKernel.SOBEL.convolve(U, toX, toY, Y, toX, toY, width, height, 1, 0);
		SeparableKernel.SOBEL.convolve(V, toX, toY, Y, toX, toY, width, height, 0, 1);
		
		return this;
	}
	
	public GradientBufferedImage extract(int toX, int toY, int width, int height, IntArrayImage from, int fromX, int fromY, int R) {
		
		BoxFilter.HORIZONTAL.convolve(U, toX, toY, from, fromX, fromY, width, height, R, 1);
		BoxFilter.VERTICAL.convolve(Y, toX, toY, U, toX, toY, width, height, R, (R*2+1)*(R*2+1));
		
		SeparableKernel.SOBEL.convolve(U, toX, toY, Y, toX, toY, width, height, 1, 0);
		SeparableKernel.SOBEL.convolve(V, toX, toY, Y, toX, toY, width, height, 0, 1);
		
		return this;
	}

}
