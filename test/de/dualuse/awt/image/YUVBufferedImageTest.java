package de.dualuse.awt.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import de.dualuse.commons.swing.JMicroscope;

public class YUVBufferedImageTest {
	public static void main(String[] args) throws IOException {

		final PixelBufferedImage pbi = new PixelBufferedImage(
				ImageIO.read(new File("/Library/Desktop Pictures/Sky.jpg")).getSubimage(600, 200, 500, 500),
				BufferedImage.TYPE_INT_RGB);

		final YUVBufferedImage ypbi = new YUVBufferedImage(pbi.width, pbi.height);

		// for (int y=0,o=pbi.offset, O=ypbi.offset, r = pbi.scan-pbi.width, R =
		// ypbi.scan-ypbi.width;y<pbi.height;y++,o+=r, O+=R)
		// for (int x=0;x<pbi.width;x++,o++,R++) {
		//// pbi.pixels[o] = ((x&y)>0)?0xFF00FF00:0xFFFF00FF;
		//
		// int ARGB = pbi.pixels[o];
		// int red = (ARGB>>16)&0xFF;
		// int green = (ARGB>>8)&0xFF;
		// int blue = (ARGB>>0)&0xFF;
		//
		// float Y_ = .299f*red+.587f*green+0.114f*blue;
		// float Cb = -0.1687f*red-0.3313f*green+.5f*blue;
		// float Cr = .5f*red-0.4186f*green-0.0813f*blue;
		//
		// ypbi.Y.data[o] = (int)Y_;
		// ypbi.U.data[o] = (int)Cb;
		// ypbi.V.data[o] = (int)Cr;
		//
		// }

		ypbi.set(0, 0, pbi.width, pbi.height, pbi, 0, 0);

		JFrame f = new JFrame();
		f.setContentPane(new JMicroscope() {
			private static final long serialVersionUID = 1L;

			public void paintCanvas(Graphics g) {
				g.drawImage(ypbi, 0, 0, null);
			}
		});

		f.setBounds(400, 150, 800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
