package de.dualuse.awt.test.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static javax.swing.SwingConstants.CENTER;

public class JViewer extends JFrame implements MouseWheelListener, Icon, MouseMotionListener {
    final static int TITLEBAR_HEIGHT = 32;

    public final AffineTransform magnifier = new AffineTransform();
    public final JLabel viewer = new JLabel();
    public ImageIcon figure;

    public JViewer(Image figure) {
        this(new ImageIcon(figure));
    }

    public JViewer(ImageIcon figure) {
        this.figure = figure;

        viewer.setHorizontalAlignment(CENTER);
        viewer.setIcon(this);
        viewer.addMouseWheelListener(this);
        viewer.addMouseMotionListener(this);

        setContentPane(viewer);
        setBounds(800,100,figure.getIconWidth(),figure.getIconHeight()+TITLEBAR_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
        for (int y=0;y<bi.getHeight();y++)
            for (int x=0;x<bi.getWidth();x++)
                bi.setRGB(x,y, (x&y)>0?0xFFFFFFFF:0xFF000000);

        new JViewer(new ImageIcon(bi));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double zoom = Math.pow(1.0337,-e.getWheelRotation());

        magnifier.preConcatenate(AffineTransform.getTranslateInstance(-e.getX(),-e.getY()));
        magnifier.preConcatenate(AffineTransform.getScaleInstance(zoom,zoom));
        magnifier.preConcatenate(AffineTransform.getTranslateInstance(+e.getX(),+e.getY()));

        viewer.repaint();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.transform(magnifier);
        figure.paintIcon(viewer, g2, x,y);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return figure.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return figure.getIconHeight();
    }

    Point l = null;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (l!=null)
            magnifier.preConcatenate(AffineTransform.getTranslateInstance(e.getX()- l.getX(),e.getY()- l.getY()));
        l = e.getPoint();
        viewer.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        l = e.getPoint();
    }
}
