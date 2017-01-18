package de.dualuse.commons.awt.image;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.dualuse.commons.Hints;
import de.dualuse.commons.awt.Graphics3D;
import de.dualuse.commons.swing.JKnob;
import de.dualuse.commons.swing.JMicroscope;
import de.dualuse.commons.util.Sticky;

public class AffineFeaturePatchTest {
	static Sticky<Double> radius = new Sticky<Double>( 16. );
	static AffineFeaturePatch afp = new AffineFeaturePatch(radius.get().intValue());
 
	
	public static void main(String[] args) throws IOException {
		
		final BufferedImage bi = ImageIO.read( AffineFeaturePatchTest.class.getResource("frame_0088.jpg") );
		final BufferedImage bj = ImageIO.read( AffineFeaturePatchTest.class.getResource("frame_0089-sheared2.jpg") );
//		final BufferedImage bi = ImageIO.read( FeaturePatchTest.class.getResource("frame-001280.jpg") );
//		final BufferedImage bj = ImageIO.read( FeaturePatchTest.class.getResource("frame-001281.jpg") );
		PixelBufferedImage pbi = new PixelBufferedImage(bi);
		PixelBufferedImage pbj = new PixelBufferedImage(bj);
		
		final LumaBufferedImage lbi = new LumaBufferedImage(pbi);	
		final LumaBufferedImage lbj = new LumaBufferedImage(pbj);	
		
		
		////////
		
		final JFrame h = (new JFrame());
		h.setContentPane(new JMicroscope(new Sticky<AffineTransform>(new AffineTransform()).get()) {
			private static final long serialVersionUID = 1L;
			public void paintCanvas(Graphics g) {
				super.paintCanvas(g);
				int size = afp.radius*2+1;
				
				if ((page%2)==1) {
					int ip[] = afp.I.clone();
					int jp[] = afp.J.clone();
					
					int highest = 0;
					
					for (int o=0;o<ip.length;o++)
						highest = max (highest, afp.weights[o]);
					
					System.out.println(highest);
	
					for (int o=0;o<jp.length;o++) {
						int ja = 255*afp.weights[o]/highest;
						int ia = 255*afp.weights[o]/highest;
						
						jp[o] = jp[o]|(jp[o]<<8)|(jp[o]<<16)|(ja<<24);
						ip[o] = ip[o]|(ip[o]<<8)|(ip[o]<<16)|(ia<<24);
						
					}
	
	
					g.drawImage(new PixelBufferedImage(size, size, ip, 0, size, BufferedImage.TYPE_INT_ARGB), 0, 0, this);
					g.drawImage(new PixelBufferedImage(size, size, jp, 0, size, BufferedImage.TYPE_INT_ARGB), 0, size+1, this);
				} else {
					g.drawImage(new LumaBufferedImage(size, size, afp.I, 0, size), 0, 0, this);
					g.drawImage(new LumaBufferedImage(size, size, afp.J, 0, size), 0, size+1, this);
				}
			}

			int page = 0;
			
			MouseAdapter ma = new MouseAdapter() {
				{ addMouseListener(this); }
				
				public void mouseClicked(MouseEvent e) {
					page ++;
					repaint();
				}
			};
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
			
			//////////////
			
			
			JKnob c = new JKnob(Sticky.value(new Point2D.Double(100,100)));
			JKnob a = new JKnob(Sticky.value(new Point2D.Double(200,100)));
			JKnob b = new JKnob(Sticky.value(new Point2D.Double(100,200)));
			
			
			{
				c.addMouseListener(new MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						if (e.getClickCount()==2) {
							a.setCenter(c.getCenterX()+afp.radius, c.getCenterY());
							b.setCenter(c.getCenterX(), c.getCenterY()+afp.radius);
						}
					};
				});
				
				c.addMouseWheelListener(new MouseWheelListener() {
					public void mouseWheelMoved(MouseWheelEvent e) {
						
						double cx = c.getCenterX(), cy = c.getCenterY();
						double ax = a.getCenterX(), ay = a.getCenterY();
						double bx = b.getCenterX(), by = b.getCenterY();
						
//						if ((e.getModifiers()&MouseWheelEvent.ALT_MASK)==0)
						if (!e.isAltDown()) {
							double s = Math.pow(1.05, e.getWheelRotation());
							a.setCenter( cx+(ax-cx)*s, cy+(ay-cy)*s);
							b.setCenter( cx+(bx-cx)*s, cy+(by-cy)*s);
						} else {
							double theta =  e.getWheelRotation()*Math.PI/180;

							double acx = ax-cx, acy = ay-cy, bcx = bx-cx, bcy = by-cy;
							double cost = cos(theta), sint = sin(theta);
							a.setCenter( cx+acx*cost+acy*sint, cy+acx*-sint+acy*cost);
							b.setCenter( cx+bcx*cost+bcy*sint, cy+bcx*-sint+bcy*cost);
						}
					}
				});
				
				c.addKnobListener(new JKnob.Listener() {
					public void knobMoved(JKnob k, double dx, double dy) {
						double s = 1.0/canvasTransform.getScaleX();
						
						a.setCenter(a.getCenterX()+dx*s, a.getCenterY()+dy*s);
						b.setCenter(b.getCenterX()+dx*s, b.getCenterY()+dy*s);
					}
				});
				
				c.setColor(Color.RED);
				a.setColor(Color.GREEN);
				b.setColor(Color.BLUE);
				
				add(c); 
				add(a);
				add(b);
			}
			
			////////////////
			
			
			
			
			
			Ellipse2D.Double e= new Ellipse2D.Double();
			public void paintCanvas(Graphics g) {
				Graphics2D g2 = ((Graphics2D)g.create());
				g2.setRenderingHints(Hints.SMOOTH);
				
				switch (page%2) {
				case 0:	g2.drawImage(bi, 0, 0, this); break; 
				case 1:	g2.drawImage(bj, 0, 0, this); break; 
				}
				
//				g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
//				
//				g2.drawImage(bi, 0, 0, this); 
//				g2.drawImage(bj, 0, 0, this); 
	

				
				Graphics3D g3 = new Graphics3D(g);

				
				g3.setStroke(new BasicStroke((float)(3.0/canvasTransform.getScaleX())));

				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), a.getCenterX(), a.getCenterY()));
				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), b.getCenterX(), b.getCenterY()));
				g3.setColor(Color.ORANGE);
				
				AffineTransform from = new AffineTransform(
						(a.getCenterX()-c.getCenterX())/afp.radius, 
						(a.getCenterY()-c.getCenterY())/afp.radius, 
						(b.getCenterX()-c.getCenterX())/afp.radius, 
						(b.getCenterY()-c.getCenterY())/afp.radius, 
						 c.getCenterX(),c.getCenterY());
				
				AffineTransform to = new AffineTransform(from);
				
				g3.draw( from.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );

				for (int i=0;i<20;i++) {
					afp
					.reset()
					.track(	from, lbi.width, lbi.height, lbi.pixels, lbi.offset, lbi.scan, 
							to  , lbj.width, lbj.height, lbj.pixels, lbj.offset, lbj.scan );
				
					to.concatenate(afp.pt);
					to.concatenate(afp.pt);
					g3.draw( to.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );
				}
				
				
				afp
				.reset()
				.track(	from, lbi.width, lbi.height, lbi.pixels, lbi.offset, lbi.scan, 
						to  , lbj.width, lbj.height, lbj.pixels, lbj.offset, lbj.scan );

				
//				e.setFrameFromCenter(k.getCenterX(), k.getCenterY(), k.getCenterX()+patch.radius, k.getCenterY()+patch.radius);
//				g2.setStroke(new BasicStroke(3));
//				g2.draw(e);
//				g2.setColor(new Color(Color.ORANGE.getRGB()&0xA0FFFFFF,true));
//				g2.setStroke(new BasicStroke(2));
//				g2.draw(e);
//				g2.setColor(Color.BLUE);
//
//				g2.setStroke(new BasicStroke(1/(float)canvasTransform.getScaleX()));
//
//				for (int i=0;i<10;i++) {
//					patch
//					.reset()
//					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, pbi.pixels, pbi.offset, pbi.scan, 
//							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, pbj.pixels, pbj.offset, pbj.scan );
//					
////					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.R.pixels, rgbI.R.offset, rgbI.R.scan, 
////							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.R.pixels, rgbJ.R.offset, rgbJ.R.scan )
////					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.G.pixels, rgbI.G.offset, rgbI.G.scan, 
////							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.G.pixels, rgbJ.G.offset, rgbJ.G.scan )
////					.track(	k.getCenterX(), k.getCenterY(), rgbI.width, rgbI.height, rgbI.B.pixels, rgbI.B.offset, rgbI.B.scan, 
////							k.getCenterX()+tx, k.getCenterY()+ty, rgbJ.width, rgbJ.height, rgbJ.B.pixels, rgbJ.B.offset, rgbJ.B.scan );
////					.track(	k.getCenterX(), k.getCenterY(), lumaI.width, lumaI.height, lumaI.pixels, lumaI.offset, lumaI.scan, 
////							k.getCenterX()+tx, k.getCenterY()+ty, lumaJ.width, lumaJ.height, lumaJ.pixels, lumaJ.offset, lumaJ.scan );
//	
//					tx += patch.translateX*4;
//					ty += patch.translateY*4;
//					
//					patch.translateX =  patch.translateY = 0;
//					
//					e.setFrameFromCenter(
//							k.getCenterX()+tx, k.getCenterY()+ty, 
//							k.getCenterX()+tx+patch.radius, k.getCenterY()+ty+patch.radius);
//					
//					g2.draw(e);
//					
//					System.out.print(patch.error+"  ");
//				}
//				System.out.println();
				
				h.repaint();
				
				
			}
		});
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 800, 800);
		f.setVisible(true);

	}

}
