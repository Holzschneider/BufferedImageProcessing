package de.dualuse.commons.awt.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.dualuse.commons.Hints;
import de.dualuse.commons.swing.JKnob;
import de.dualuse.commons.swing.JMicroscope;
import de.dualuse.commons.util.Sticky;

public class FeaturePatchTest {
	static Sticky<Double> radius = new Sticky<Double>( 16. );
	static RGBFeaturePatch patch = new RGBFeaturePatch(radius.get().intValue());
 
	
	public static void main(String[] args) throws IOException {
		
		final BufferedImage bi = ImageIO.read( FeaturePatchTest.class.getResource("frame_0088.jpg") );
		final BufferedImage bj = ImageIO.read( FeaturePatchTest.class.getResource("frame_0089.jpg") );
//		final BufferedImage bi = ImageIO.read( FeaturePatchTest.class.getResource("frame-001280.jpg") );
//		final BufferedImage bj = ImageIO.read( FeaturePatchTest.class.getResource("frame-001281.jpg") );
		
		final PixelBufferedImage pbi = new PixelBufferedImage(bi);
		final PixelBufferedImage pbj = new PixelBufferedImage(bj);
		
		final LumaBufferedImage lumaI = new LumaBufferedImage(pbi);	
		final LumaBufferedImage lumaJ = new LumaBufferedImage(pbj);	
		
		final RGBBufferedImage rgbI = new RGBBufferedImage(new PixelBufferedImage(bi));	
		final RGBBufferedImage rgbJ = new RGBBufferedImage(new PixelBufferedImage(bj));
		
		////////
		
		final JFrame h = (new JFrame());
		h.setContentPane(new JMicroscope(new Sticky<AffineTransform>(new AffineTransform()).get()) {
			private static final long serialVersionUID = 1L;
			public void paintCanvas(Graphics g) {
				super.paintCanvas(g);
				int size = patch.radius*2+1;
				
				g.drawImage(new RGBBufferedImage(size, size, patch.G, patch.H, patch.I, 0, 0, 0, size),0,0,this);
				g.drawImage(new RGBBufferedImage(size, size, patch.J, patch.K, patch.L, 0, 0, 0, size),0,size+1,this);
				
//				g.drawImage(new IntBufferedImage(size, size, patch.I, 0, size), 0, 0, this);
//				g.drawImage(new IntBufferedImage(size, size, patch.J, 0, size), 0, size+1, this);
			}
		});
		h.setBounds(1000, 100, 400, 400);
		h.setVisible(true);
		
		JFrame f = (new JFrame());
		
		f.setContentPane(new JMicroscope(Sticky.value(new AffineTransform())) {
			private static final long serialVersionUID = 1L;
			
			int page = 0;
			
			MouseAdapter ma = new MouseAdapter() {
				{ addMouseListener(this); }
				
				public void mouseClicked(MouseEvent e) {
					page ++;
					repaint();
				}
			};
			
//			FeaturePatch patch = new FeaturePatch(radius.get().intValue());
//			JStickyKnob k = new JStickyKnob(200, 200);
			JKnob k = new JKnob(Sticky.value(new Point2D.Double()));
			MouseWheelListener mwl = new MouseWheelListener() {
				{ k.addMouseWheelListener(this); }
				public void mouseWheelMoved(MouseWheelEvent e) {
					radius.set( Math.max(2, radius.get()*Math.pow(1.05, e.getWheelRotation())) );
					patch = new RGBFeaturePatch(radius.get().intValue());
					repaint();
				}
			};
			
			{ add (k); } 
			
			Ellipse2D.Double e= new Ellipse2D.Double();
			public void paintCanvas(Graphics g) {
				Graphics2D g2 = ((Graphics2D)g.create());
				g2.setRenderingHints(Hints.SMOOTH);
				
				switch (page%2) {
				case 0:	g2.drawImage(bi, 0, 0, this); break; 
				case 1:	g2.drawImage(bj, 0, 0, this); break; 
				}
	
				g2.setColor(Color.BLUE);
				g2.setStroke(new BasicStroke(0.25f));
				
				float tx = 0, ty = 0;
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(0.5f));
				g2.draw(e);
				
				
				e.setFrameFromCenter(k.getCenterX(), k.getCenterY(), k.getCenterX()+patch.radius, k.getCenterY()+patch.radius);
				g2.setStroke(new BasicStroke(3));
				g2.draw(e);
				g2.setColor(new Color(Color.ORANGE.getRGB()&0xA0FFFFFF,true));
				g2.setStroke(new BasicStroke(2));
				g2.draw(e);
				g2.setColor(Color.BLUE);

				g2.setStroke(new BasicStroke(1/(float)canvasTransform.getScaleX()));

				for (int i=0;i<10;i++) {
					patch
					.reset()
					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, pbi.pixels, pbi.offset, pbi.scan, 
							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, pbj.pixels, pbj.offset, pbj.scan );
					
//					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.R.pixels, rgbI.R.offset, rgbI.R.scan, 
//							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.R.pixels, rgbJ.R.offset, rgbJ.R.scan )
//					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.G.pixels, rgbI.G.offset, rgbI.G.scan, 
//							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.G.pixels, rgbJ.G.offset, rgbJ.G.scan )
//					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.B.pixels, rgbI.B.offset, rgbI.B.scan, 
//							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.B.pixels, rgbJ.B.offset, rgbJ.B.scan );
//					.track(	k.getCenterX(), k.getCenterY(), lumaI.width, lumaI.height, lumaI.pixels, lumaI.offset, lumaI.scan, 
//							k.getCenterX()+tx, k.getCenterY()+ty, lumaJ.width, lumaJ.height, lumaJ.pixels, lumaJ.offset, lumaJ.scan );
	
					tx += patch.translateX*4;
					ty += patch.translateY*4;
					
					patch.translateX =  patch.translateY = 0;
					
					e.setFrameFromCenter(
							k.getCenterX()+tx, k.getCenterY()+ty, 
							k.getCenterX()+tx+patch.radius, k.getCenterY()+ty+patch.radius);
					
					g2.draw(e);
					
					System.out.print(patch.error+"  ");
				}
				System.out.println();
				
				h.repaint();
				
				
			}
		});
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 800, 800);
		f.setVisible(true);

	}

}
