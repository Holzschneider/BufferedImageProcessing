package de.dualuse.awt.test;

import de.dualuse.awt.image.PixelArrayImage;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class GetRGBLab {
    public static void main(String... args) throws IOException {
		final InputStream source = PixelArrayImage.class.getResourceAsStream("IMG_3697.jpg");
        final PixelArrayImage test = PixelArrayImage.read(source).crop(1500,1400,512,512);
        final PixelArrayImage toast = new PixelArrayImage(256,256, PixelArrayImage.Format.RGB);

        JFrame f = new JFrame();
        f.setBounds(100,100,500,400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(new JComponent() {
            private static final long serialVersionUID = 1L;
            JSlider shiftSliderX = new JSlider(JSlider.HORIZONTAL);
            JSlider shiftSliderY = new JSlider(JSlider.VERTICAL);

            ChangeListener updateListener =  e-> repaint();
            {
                setLayout(new BorderLayout());

                shiftSliderX.addChangeListener(updateListener);
                shiftSliderY.addChangeListener(updateListener);

                add(shiftSliderX,BorderLayout.SOUTH);
                add(shiftSliderY,BorderLayout.EAST);
            }

            protected void paintComponent(Graphics g) {
                test.getRGB(shiftSliderX.getValue()*.05f, shiftSliderY.getValue()*.05f, toast.width, toast.height, toast.pixels, toast.offset, toast.scan);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.scale(10, 10);
                g2.drawImage(toast,0,0,null);
                g2.dispose();
            }
        });
        f.setVisible(true);
    }

}
