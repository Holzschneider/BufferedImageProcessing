package de.dualuse.commons.awt.image;

import static java.lang.Math.*;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.dualuse.commons.Hints;
import de.dualuse.commons.awt.Graphics3D;
import de.dualuse.commons.awt.dnd.DropListener;
import de.dualuse.commons.awt.dnd.FileDropTarget;
import de.dualuse.commons.swing.JKnob;
import de.dualuse.commons.swing.JMicroscope;
import de.dualuse.commons.util.Sticky;

public class AffineFeaturePatchTest2 extends JMicroscope {
	private static final long serialVersionUID = 1L;

	public AffineFeaturePatchTest2(AffineTransform externalCanvasTransform) {
		super(externalCanvasTransform);
	}

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
				
//				if ((e.getModifiers()&MouseWheelEvent.ALT_MASK)==0)
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
	
	

	static Sticky<URL> source = new Sticky<URL>( AffineFeaturePatchTest2.class.getResource("frame_0088.jpg") );
	static Sticky<URL> probed = new Sticky<URL>( AffineFeaturePatchTest2.class.getResource("frame_0089-sheared2.jpg") );
	
	static Sticky<Double> radius = new Sticky<Double>( 16. );
	static AffineFeaturePatch afp = new AffineFeaturePatch(radius.get().intValue());
	
	static BufferedImage bi, bj;
	static PixelBufferedImage pbi, pbj;
	static LumaBufferedImage lbi, lbj;
	
	
	public static void main(String[] args) throws IOException {
		
		bi = ImageIO.read( source.get() );
		bj = ImageIO.read( probed.get() );
		
		pbi = new PixelBufferedImage(bi);
		pbj = new PixelBufferedImage(bj);
		
		lbi = new LumaBufferedImage(pbi);	
		lbj = new LumaBufferedImage(pbj);	
		
		
		////////


		final AffineTransform from = new AffineTransform();
		final AffineTransform to = new AffineTransform();
		final AffineTransform done = new AffineTransform();
		
		final ArrayList<AffineTransform> trail = new ArrayList<AffineTransform>();
		

		final Runnable tracking = new Runnable() {
			public void run() {

				AffineTransform over = new AffineTransform(to);
				
				synchronized(trail) {
					trail.clear();
					for (int i=0;i<3*20;i++) {
						afp
						.reset()
						.track(	from, lbi.width, lbi.height, lbi.pixels, lbi.offset, lbi.scan, 
								over, lbj.width, lbj.height, lbj.pixels, lbj.offset, lbj.scan );
					
						over.concatenate(afp.pt);
						over.concatenate(afp.pt);
						trail.add(new AffineTransform(over));
					}
					done.setTransform(over);
				}
				
			}
		}; 
		
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
		
		
		
		
		
		
		JFrame g = new JFrame();
		
		g.setContentPane(new AffineFeaturePatchTest2(Sticky.value(new AffineTransform())) {
			
			FileDropTarget ftd = new FileDropTarget(this, new DropListener<File[]>() {
				public boolean drop(Point p, File[] f) throws Exception {
					
					try {
						URL u = f[0].toURI().toURL();
						
						bj = ImageIO.read(u);
						pbj = new PixelBufferedImage(bj);
						lbj = new LumaBufferedImage(pbj);
						
						probed.set(u);

						return true;
					} catch (IOException ex) {
						ex.printStackTrace();
						return false;
					}
					
				}
			} );

			
			JSlider progress = new JSlider(JSlider.VERTICAL, 0, 100, 100);
			JSlider alpha = new JSlider(JSlider.VERTICAL, 0, 100, 100);
			
			private static final long serialVersionUID = 1L;
			
			{	
				ChangeListener repainter = new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						repaint();						
					}
				};
				progress.addChangeListener(repainter);
				alpha.addChangeListener(repainter);
				add(progress);
				add(alpha);
			}
			
			@Override
			public void doLayout() {
				progress.setBounds(0, 0, progress.getPreferredSize().width, getHeight());
				alpha.setBounds(progress.getPreferredSize().width, 0, progress.getPreferredSize().width, getHeight());				
			}
			
			public void paintCanvas(Graphics g) {
				Graphics2D g2 = ((Graphics2D)g.create());
				g2.setRenderingHints(Hints.SMOOTH);
				
				g2.drawImage(bj, 0, 0, this); 

				Graphics3D g3 = new Graphics3D(g);

				
				g3.setStroke(new BasicStroke((float)(1.0/canvasTransform.getScaleX())));

				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), a.getCenterX(), a.getCenterY()));
				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), b.getCenterX(), b.getCenterY()));
				g3.setColor(Color.ORANGE);
				
				to.setTransform(
						(a.getCenterX()-c.getCenterX())/afp.radius, 
						(a.getCenterY()-c.getCenterY())/afp.radius, 
						(b.getCenterX()-c.getCenterX())/afp.radius, 
						(b.getCenterY()-c.getCenterY())/afp.radius, 
						 c.getCenterX(),c.getCenterY());
				

				tracking.run();
				synchronized (trail) {
					int j = trail.size()*progress.getValue()/progress.getMaximum();
					
					AffineTransform over = new AffineTransform(to);
					for (int i=0;i<j;i++) {
						over = trail.get(i);
						g3.draw( over.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );
					};
					
					g3.setStroke(new BasicStroke((float)(3.0/canvasTransform.getScaleX())));
					g3.draw( done.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );

					AffineTransform to2 = new AffineTransform(over);
					to2.translate(-afp.radius, -afp.radius);
					
					g3.setComposite(AlphaComposite.SrcOver.derive(alpha.getValue()*1.f/alpha.getMaximum()));
					g3.drawImage(new LumaBufferedImage(afp.radius*2+1, afp.radius*2+1, afp.I, 0, afp.radius*2+1), to2, this);

				}
				
				
				
				g3.dispose();
				g2.dispose();
				
				h.repaint();
			}

			
		});
		
		g.setBounds(1200, 200, 800, 800);
		g.setVisible(true);
			
		
		
		
		
		JFrame f = (new JFrame());
		
		f.setContentPane(new AffineFeaturePatchTest2(Sticky.value(new AffineTransform())) {
			private static final long serialVersionUID = 1L;		

			FileDropTarget ftd = new FileDropTarget(this, new DropListener<File[]>() {
				public boolean drop(Point p, File[] f) throws Exception {
					
					try {
						URL u = f[0].toURI().toURL();
						bi = ImageIO.read(u);
						pbi = new PixelBufferedImage(bi);
						lbi = new LumaBufferedImage(pbi);
						
						source.set(u);
						
						repaint();
						return true;
					} catch (IOException ex) {
						ex.printStackTrace();
						return false;
					}
					
				}
			} );
			
			
			Ellipse2D.Double e= new Ellipse2D.Double();
			public void paintCanvas(Graphics g) {
				Graphics2D g2 = ((Graphics2D)g.create());
				g2.setRenderingHints(Hints.SMOOTH);
				
				g2.drawImage(bi, 0, 0, this); 

				Graphics3D g3 = new Graphics3D(g);

				
				g3.setStroke(new BasicStroke((float)(3.0/canvasTransform.getScaleX())));

				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), a.getCenterX(), a.getCenterY()));
				g3.draw(new Line2D.Double(c.getCenterX(), c.getCenterY(), b.getCenterX(), b.getCenterY()));
				g3.setColor(Color.ORANGE);
				
				from.setTransform(
						(a.getCenterX()-c.getCenterX())/afp.radius, 
						(a.getCenterY()-c.getCenterY())/afp.radius, 
						(b.getCenterX()-c.getCenterX())/afp.radius, 
						(b.getCenterY()-c.getCenterY())/afp.radius, 
						 c.getCenterX(),c.getCenterY());
				
				tracking.run();
				
				g3.draw( from.createTransformedShape( new Rectangle2D.Double(-afp.radius, -afp.radius,2* afp.radius, 2*afp.radius) ) );

				h.repaint();
			}
		});
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 800, 800);
		f.setVisible(true);

	}

}
