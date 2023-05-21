package de.dualuse.awt.test;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

//import de.dualuse.commons.swing.JMicroscope;

public class BoxFilterTest {

	public static void main(String[] args) throws IOException {
//
//		int W = 512, H = 512;
//
//		PixelBufferedImage pbi = new PixelBufferedImage(ImageIO.read(new File("/Library/Desktop Pictures/Nature/Cirques.jpg")), BufferedImage.TYPE_INT_RGB);
//		final IntBufferedImage lbi = new LumaBufferedImage(W,H).set(0, 0, W, H, pbi, 0, 0);
//
//		int R = 10;
////		int M = 50
////		final IntBufferedImage boxed = BoxFilter.HORIZONTAL.convolve(new IntBufferedImage(W,H), M, 0,    lbi, M, 0, W-2*M, H, R, R*2+1);
////		final IntBufferedImage blurd = BoxFilter.VERTICAL.convolve(new IntBufferedImage(W,H), 0, M,    boxed, 0, M, W, H-2*M, R, R*2+1);
//		final IntBufferedImage boxed = BoxFilter.HORIZONTAL.convolve(new IntBufferedImage(W,H), 0, 0,    lbi, 0, 0, W, H, R, R*2+1);
//		final IntBufferedImage blurd = BoxFilter.VERTICAL.convolve(new IntBufferedImage(W,H), 0, 0,    boxed, 0, 0, W, H, R, R*2+1);
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
