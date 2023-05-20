package de.dualuse.awt.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.dualuse.commons.swing.JMicroscope;

public class MipMapBufferedImageTest {

	public static void main(String...args) throws IOException {
		
//		MipMapBufferedImage mm = new MipMapBufferedImage(320, 200, MipMapBufferedImage.TYPE_INT_ARGB);
		
		final BufferedImage bi = ImageIO.read(new File("/Library/Desktop Pictures/Zebras.jpg"));
		
//		final PixelBufferedImage pbi = new PixelBufferedImage(bi);
//		
//		MipMapBufferedImage.superSample(pbi.width, pbi.height, pbi.pixels, pbi.offset, pbi.scan, pbi);
//		MipMapBufferedImage.superSample(pbi.width, pbi.height, pbi.pixels, pbi.offset, pbi.scan, pbi);
//		MipMapBufferedImage.superSample(pbi.width, pbi.height, pbi.pixels, pbi.offset, pbi.scan, pbi);
//		MipMapBufferedImage.superSample(pbi.width, pbi.height, pbi.pixels, pbi.offset, pbi.scan, pbi);
//		MipMapBufferedImage.superSample(pbi.width, pbi.height, pbi.pixels, pbi.offset, pbi.scan, pbi);
//		
//		
//		final JFrame f =new JFrame();
//		f.setContentPane(new JMicroscope() {
//			public void paintCanvas(Graphics g) {
//				
//				g.drawImage(pbi, 0, 0, this);
//				
//			};
//		});
//		
//		f.setBounds(100, 100, 800, 800);
//		f.setVisible(true);
		
		final MipMapBufferedImage mm = new MipMapBufferedImage(bi.getWidth()+1,bi.getHeight()+1, MipMapBufferedImage.Format.ARGB);
		int pixels[] = bi.getRGB(0,0,bi.getWidth(),bi.getHeight(),new int[(bi.getWidth())*(bi.getHeight())],0,bi.getWidth());
//		mm.setRGB(1, 1, bi.getWidth()-100, bi.getHeight()-100, pixels, 0, bi.getWidth());
		mm.setRGB(1, 1, bi.getWidth(), bi.getHeight(), pixels, 0, bi.getWidth());
		
		mm.generateMipmap();
		
		final JFrame f =new JFrame();
		f.setContentPane(new JMicroscope() {
			int level = 0;
			
			private static final long serialVersionUID = 1L;
//			MouseWheelListener mwl = new MouseWheelListener() {
//				{ addMouseWheelListener(this); }
//				public void mouseWheelMoved(MouseWheelEvent e) {
//					if (!e.isShiftDown())
//						return;
//					level= Math.min((mm.depth-1)*r, Math.max(0,level+e.getWheelRotation()));
//					repaint();
//				}
//			};
			
			@SuppressWarnings("unused")
			KeyListener kl = new KeyAdapter() {
				{ f.addKeyListener(this); }
				
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_UP) 
						level--;
					else
					if (e.getKeyCode()==KeyEvent.VK_DOWN)
						level++;
					
					repaint();
				}
			};

			protected void paintComponent(Graphics g) {
				
				g.setColor(Color.ORANGE);
				g.fillRect(0, 0, getWidth(), getHeight());
				super.paintComponent(g);
				
				
			}
			
			public void paintCanvas(Graphics g) {
				double s = Math.pow(2, level);
				
//				((Graphics2D)g).drawImage(mm,0,0,this);
				
				
				MipMapBufferedImage nn = mm.getLevel(level);
//				((Graphics2D)g).drawImage(nn, AffineTransform.getScaleInstance(mm.getWidth()*1./nn.getWidth(), mm.getHeight()*1./nn.getHeight()), null); 
				((Graphics2D)g).drawImage(nn, AffineTransform.getScaleInstance(s, s), null); 
			}
		});
		
		f.setBounds(600, 400, 600, 600);
		f.setVisible(true);
	}
}
