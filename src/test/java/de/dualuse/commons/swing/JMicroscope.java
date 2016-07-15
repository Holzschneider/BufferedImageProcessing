package de.dualuse.commons.swing;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class JMicroscope extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private int moveButtonMask = MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK;
	
	public void setMoveButtonMask(int moveButtonMask) {
		this.moveButtonMask = moveButtonMask;
	}
	
	public int getMoveButtonMask() {
		return moveButtonMask;
	}
	

	private boolean relative = true;
	private boolean widthPinned = true;
	
	
	private CopyOnWriteArrayList<ChangeListener> cls = new CopyOnWriteArrayList<ChangeListener>();
	public void addChangeListener(ChangeListener cl) { cls.add(cl); }
	public void removeChangeListener(ChangeListener cl) { cls.remove(cl); }
	protected void fireStateChanged() { for (ChangeListener cl: cls) cl.stateChanged(new ChangeEvent(this)); }
	
	Dimension lastSize = null;
	final private ComponentAdapter resizeController = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			
			Dimension currentSize = JMicroscope.this.getSize(); 
			
			if (lastSize!=null && isRelative()) {
				double dx = (currentSize.getWidth()-lastSize.getWidth())/2., dy = (currentSize.getHeight()-lastSize.getHeight())/2.;
				double dsw = currentSize.getWidth()/lastSize.getWidth(), dsh = currentSize.getHeight()/lastSize.getHeight();
//	
				AffineTransform at = new AffineTransform();
				double cx = getWidth()/2, cy = getHeight()/2;
				
				if (isWidthPinned()) {
					at.translate(0,cy+dy*dsw);
					at.scale(dsw, dsw);
					at.translate(0,-cy);
				} else {
					at.translate(cx+dx+dsh,0);
					at.scale(dsh, dsh);
					at.translate(-cx,0);
				}
				
				canvasTransform.preConcatenate(at);
				fireStateChanged();
			}
			
			lastSize = currentSize;
		}
	};
	
	final private MouseAdapter viewportController = new MouseAdapter() {
		private Point2D p = new Point2D.Double(), q = new Point2D.Double();
		final public void mouseDragged(MouseEvent e) {
			
			if (e.isConsumed())
				return;
			
			if ((e.getModifiers() & moveButtonMask)==0)
				return;
			
//			if ((e.getModifiers() & MouseEvent.BUTTON3_MASK)==0) return;
			q.setLocation(e.getPoint());
			
			canvasTransform.translate((q.getX()-p.getX())/Math.hypot(canvasTransform.getScaleX(),canvasTransform.getShearY()), (q.getY()-p.getY())/Math.hypot(canvasTransform.getScaleY(),canvasTransform.getShearX()));
			fireStateChanged();
			
			p.setLocation(q);
			repaint();
		}
		
		final public void mouseMoved(MouseEvent e) {
			p.setLocation(e.getPoint());
		}
		
		@Override
		final public void mousePressed(MouseEvent e) {
			p.setLocation(e.getPoint());
		}
		
		final public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isConsumed())
				return;

			if (e.getModifiers()!=0)
				return;

			q.setLocation(e.getPoint());
			try {
				canvasTransform.inverseTransform(q, q);
			} catch (Exception ex) {};
			
			canvasTransform.translate(q.getX(), q.getY());
			
			double s = Math.pow(1.04,-e.getWheelRotation());
			canvasTransform.scale(s, s);
			
			canvasTransform.translate(-q.getX(), -q.getY());
			
			fireStateChanged();
			
			repaint();
		}
	};
	
	final public AffineTransform canvasTransform;
	
	public JMicroscope() {
		this.canvasTransform = new AffineTransform();
	}
	
	public JMicroscope(AffineTransform externalCanvasTransform) {
		this.canvasTransform = externalCanvasTransform;
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		this.addMouseListener(viewportController);
		this.addMouseMotionListener(viewportController);
		this.addMouseWheelListener(viewportController);
		this.addComponentListener(resizeController);
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		this.removeMouseListener(viewportController);
		this.removeMouseMotionListener(viewportController);
		this.removeMouseWheelListener(viewportController);
		this.removeComponentListener(resizeController);
	}
	
//	public JMicroscope(AffineTransform defaultTransform, boolean flipY, boolean normalizeStrokeSize) {
//		this();
//		
////		this.defaultTransform = new AffineTransform(defaultTransform);
//		this.canvasTransform.setTransform(defaultTransform);
//		
//		this.normalizeStrokeSize = normalizeStrokeSize;
//		this.flipY = flipY;
//	}
	
//	private AffineTransform defaultTransform = null;
	private boolean normalizeStrokeSize = false;
	private boolean flipY = false;
	
	
	public float getZoom() {
		return (float) Math.hypot(canvasTransform.getScaleX(), canvasTransform.getShearY());
	}
	
	
	
	final public Point2D getPointOnCanvas(Point onComponent) {
		Point2D onCanvas = new Point2D.Double();
		try {
			canvasTransform.inverseTransform(onComponent, onCanvas);
		} catch (Exception ex) {};
		return onCanvas;
	}
	
	


	public void paintCanvas(Graphics g) {
		
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = ((Graphics2D)g.create());
		g2.transform(canvasTransform);
		
		if (flipY) {
			g2.translate(0, getHeight());
			g2.scale(1, -1);
		}
		
		double scaleX = canvasTransform.getScaleX();
		
//		final AffineTransform at = defaultTransform;
//		if (at!=null) {
//			g2.transform(at);
//			scaleX*= at.getScaleX();
//		}
		
		
		if (normalizeStrokeSize)
			g2.setStroke(new BasicStroke(1f/(float)(scaleX)));
		
		paintCanvas(g2);
	}

	public void setRelative(boolean relative) {
		this.relative = relative;
	}

	public boolean isRelative() {
		return relative;
	}

	public void setWidthPinned(boolean widthPinned) {
		this.widthPinned = widthPinned;
	}

	public boolean isWidthPinned() {
		return widthPinned;
	}
	
	
	
	
//	
	public static void main(String args[]) throws Exception {
		final BufferedImage bi = ImageIO.read(new File("/Library/Desktop Pictures/Nature/Cirques.jpg"));
		
		JFrame f = new JFrame();
		f.setContentPane(new JMicroscope() {
			private static final long serialVersionUID = 1L;

			public void paintCanvas(Graphics g) {
				g.drawImage(bi,0,0,null);
			}
		});

		f.setBounds(100,100,500,500);
		f.setVisible(true);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


}