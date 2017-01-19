package de.dualuse.commons.awt.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import de.dualuse.commons.awt.Graphics3D;
import de.dualuse.commons.swing.JKnob;
import de.dualuse.commons.swing.JMicroscope;
import de.dualuse.commons.util.Sticky;
import de.dualuse.commons.util.Sticky.Retriever;

import static java.lang.Math.*;

public class AffineFeaturePatchDebug {
	public static void main(String... args) throws Exception {
		
		final BufferedImage bi = ImageIO.read( FeaturePatchTest.class.getResource("frame_0088.jpg") );
		final BufferedImage bj = ImageIO.read( FeaturePatchTest.class.getResource("frame_0089.jpg") );
		
		final PixelBufferedImage pbi = new PixelBufferedImage(bi);
		final PixelBufferedImage pbj = new PixelBufferedImage(bj);
		
		final LumaBufferedImage lbi = new LumaBufferedImage(pbi);
		final LumaBufferedImage lbj = new LumaBufferedImage(pbj);
		final AffineFeaturePatch afp = new AffineFeaturePatch(20);
		
		
		
		final JFrame h = new JFrame();
		
		h.setBounds(0, 0, 200, 200);
		h.setBounds(Sticky.value( new Retriever<Rectangle>() {
			public Rectangle get() {
				System.out.println("GET BOUNDS "+h.getBounds());
				return h.getBounds();
			}
		} ));
		
		
		
		
		h.setContentPane(new JMicroscope(Sticky.value(new AffineTransform())) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintCanvas(Graphics g) {
				g.drawImage( new LumaBufferedImage(afp.radius*2+1, afp.radius*2+1, afp.I, 0, afp.radius*2+1), 0,0, this);
			}
		});

		h.setVisible(true);
		
		
		JFrame f = new JFrame();
		
		f.setContentPane(new JMicroscope(Sticky.value(new AffineTransform())) {
			private static final long serialVersionUID = 1L;
			
			
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

			@Override
			protected void paintCanvas(Graphics g) {
				h.getContentPane().repaint();
				
				g.drawImage(lbi, 0, 0, this);

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
				
				AffineTransform to = from;
				
				g3.draw( from.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );
				

				afp.reset().track(
						from, lbi.width, lbi.height, lbi.pixels, lbi.offset, lbi.scan, 
						to, lbj.width, lbj.height, lbj.pixels, lbj.offset, lbj.scan);
				
				
				g3.dispose();
				repaint(100);
			}
			
			
		});
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(300, 200, 1200, 800);
		f.setVisible(true);
		
		
	}
}
