package de.dualuse.awt.image;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class R32fBufferedImage extends FloatBufferedImage {
	
	static ColorSpace RED_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};

	static ColorModel RED_COLOR_MODEL = new ComponentColorModel(RED_COLOR_SPACE, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_FLOAT) {
		public int getRGB(Object inData) {
			final float r[] = (float[]) inData;
			final float red = r[0];
			final int RED = ((int)(red<1&&red>0?red*255f:red))&0xFF;
			
			return 0xFF000000 | (RED<<16);
			
//			final int raw = RED;
////			final int absed = raw<0?(-raw)&0xFF:raw;
//			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
////			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
//			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | ((raw>0?gray:-gray)&0xFF);
//			final int rd = (RED<0?0:RED>255?255:RED);
//			return 0xFF000000 | (rd<<16) | (rd<<8) | (rd<<0); 
		};
	};
	
	
	public R32fBufferedImage(int width, int height) {
		super(width, height, 0, width, RED_COLOR_MODEL, RED_COLOR_MODEL.createCompatibleWritableRaster(width, height));
	}
	
	public R32fBufferedImage(int width, int height, float[] data, int offset, int scan) {
		super(width,height, data,offset,scan, RED_COLOR_MODEL);
	}
	
	protected R32fBufferedImage(int width, int height, float[] data, int offset, int scan, ColorModel cm) {
		super(width, height, data, offset, scan, cm);
	}
	

	public R32fBufferedImage set(int toX, int toY, int width, int height, IntBufferedImage li, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) for (int x=0;x<width;x++,O++,o++)
			data[o] = li.data[O];
		
		return this;
	}


//	public R32fBufferedImage set(int toX, int toY, int width, int height, float[] from, int fromOffset, int fromScan) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromOffset,rx=fromScan-width;y<height;y++,O+=rx,o+=r) for (int x=0;x<width;x++,O++,o++)
//			data[o] = from[O];
//		
//		return this;
//	}
	
	
//	public static void main(String[] args) throws IOException {
//		JFrame f = new JFrame();
//		
//		final int W = 512, H = 512;
//
//		final BufferedImage bi = ImageIO.read(new File("/Library/Desktop Pictures/Bristle Grass.jpg"));
//		final R32fBufferedImage bj = new R32fBufferedImage(W, H).set(0, 0, W, H, new LumaBufferedImage(W,H).set(0, 0, W, H, new PixelBufferedImage(bi, BufferedImage.TYPE_INT_RGB), 0, 0), 0, 0);
//		final BufferedImage bk = new R32fBufferedImage(100,100, bj.data, 100*bj.scan+100, bj.scan);
//		
//		f.setContentPane(new JComponent() {
//			private static final long serialVersionUID = 1L;
//
//			protected void paintComponent(Graphics g) {
//				g.drawImage(bk, 0,0, this);
//			}
//		});
//		
//		f.setBounds(500, 200, 600, 600);
//		f.setVisible(true);
//	}

	
}


