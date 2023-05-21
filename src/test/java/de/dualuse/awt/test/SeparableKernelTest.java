package de.dualuse.awt.test;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

//import de.dualuse.commons.swing.JMicroscope;

public class SeparableKernelTest {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
//		int W = 512, H = 512;
//
//		PixelBufferedImage pbi = new PixelBufferedImage(ImageIO.read(new File("/Library/Desktop Pictures/Zebras.jpg")), BufferedImage.TYPE_INT_RGB);
//		final IntBufferedImage lbi = new LumaBufferedImage(W,H).set(0, 0, W, H, pbi, 0, 0);
//
//		int M = 50, R = 20, SX = 0, SY = SX;
////		final IntBufferedImage boxed = BoxFilter.HORIZONTAL.convolve(new IntBufferedImage(W,H), M, 0,    lbi, M, 0, W-2*M, H, R, R*2+1);
////		final IntBufferedImage blurd = BoxFilter.VERTICAL.convolve(new IntBufferedImage(W,H), 0, M,    boxed, 0, M, W, H-2*M, R, R*2+1);
//
//		SeparableKernel ker = new SeparableKernel.Gaussian(20, 2, -10).norm(128);
//
//		final IntBufferedImage boxed = ker.convolve(new IntBufferedImage(W,H), 0, 0, lbi, SX, SY, W-SX, H-SY, 1, -1 );
//		final IntBufferedImage blurd = ker.convolve(new IntBufferedImage(W,H), 0, 0, boxed, 0, 0, W, H, -1, -1);
//
//
//		System.out.println(ker);
//
//
//		JFrame f = new JFrame();
//
//		f.setContentPane(new JMicroscope() {
//			private static final long serialVersionUID = 1L;
//
//			public void paintCanvas(Graphics g) {
//				g.drawImage(blurd,0,0,this);
//			}
//		});
//
//		f.setBounds(200, 200, 800, 600);
//		f.setVisible(true);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
