package de.dualuse.awt.image;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PixelBufferedImage extends CustomBufferedImage {
	static {
		new BufferedImage(1, 1, TYPE_INT_ARGB);
	}

	private static ColorModel colorModelForFormat(int format) {
		switch (format) {
		case TYPE_INT_ARGB: return new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000); 
		case TYPE_INT_RGB: return new DirectColorModel(24,0x00ff0000,0x0000ff00,0x000000ff); 
		default :throw new IllegalArgumentException("Invalid format: "+format);
		}
	}
	
	private static int[] bandsForFormat(int format) {
		switch (format) {
		case TYPE_INT_ARGB: return new int[] {0xFF0000,0xFF00,0xFF,0xFF000000};
		case TYPE_INT_RGB: return new int[] {0xFF0000,0xFF00,0xFF}; 
		default :throw new IllegalArgumentException("Invalid format: "+format);
		}		
	}
	
	protected static int formatForFormat(int format) {
		switch (format) {
		case TYPE_3BYTE_BGR:
		case TYPE_BYTE_BINARY:
		case TYPE_BYTE_GRAY:
		case TYPE_BYTE_INDEXED:
		case TYPE_INT_BGR:
		case TYPE_INT_RGB:
		case TYPE_USHORT_555_RGB:
		case TYPE_USHORT_565_RGB:
		case TYPE_USHORT_GRAY:
			return TYPE_INT_RGB;
			
		default:
			return TYPE_INT_ARGB;
		}
	}
	
	int format = BufferedImage.TYPE_INT_ARGB;

	public PixelBufferedImage(BufferedImage bi) {
		this(bi, formatForFormat(bi.getType()));
	}
	
	public PixelBufferedImage(BufferedImage bi, int format) {
		this(bi.getWidth(),bi.getHeight(),format);
		bi.getRGB(0, 0, width, height, pixels, 0, scan);
		this.format = format;
	}
	

	public PixelBufferedImage(int width, int height) {
		this(width,height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public PixelBufferedImage(int width, int height, int format) { 
		this(width,height,new int[width*height],0,width,format);
	}
	
	public final int[] pixels;
	
	public PixelBufferedImage(int width, int height, int pixels[], int offset, int scan, int format) {
		super(	width, height, offset, scan, 
				colorModelForFormat(format), 
				new ThreadLocalIntegerInterleavedRaster(width, height, pixels, offset, scan, bandsForFormat(format)), 
				false);
		this.format = format;
		
		this.pixels = pixels;
	}
	
	
	@Override
	public int getType() {
		return format;
	}
	
	@Override
	public String toString() {
		return "Pixel"+super.toString();
	}
	
	public PixelBufferedImage read(File f) throws IOException {
		return read(ImageIO.createImageInputStream(f));
	}
	
	public PixelBufferedImage read(InputStream is) throws IOException {
		return read(new MemoryCacheImageInputStream(is));
	}
	
	public PixelBufferedImage read(ImageInputStream iis) throws IOException {
		ImageReader ir = ImageIO.getImageReaders(iis).next();
		
		ir.setInput(iis);
		
		int type = ir.getImageTypes(0).next().getNumBands()==3?BufferedImage.TYPE_INT_RGB:BufferedImage.TYPE_INT_ARGB;
		
		int bands[] = type == BufferedImage.TYPE_INT_RGB?new int[] { 0, 1, 2 }:new int[] { 0, 1, 2, 3 }; 
		
		ImageReadParam irp = new ImageReadParam();
		PixelBufferedImage destination = this;
		irp.setDestination(destination);
		irp.setDestinationBands(bands);
		irp.setSourceBands(bands);
		
		ir.read(0, irp);
		
		return destination;
	}
	
		

	public int getRGB(int x, int y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;
		
		return pixels[x+y*scan+offset];
	}
	
	
	public static final int BLUE = 0;
	public static final int GREEN = 1;
	public static final int RED = 2;
	public static final int ALPHA= 3;
	
//	static class Filter {
//		void loop(int x, int y, int o, int pixel) {
//			
//		}
//	}
//	
//	public void forPixels(int x, int y, int w, int h, Filter f) {
//		
//	}
	
	public PixelBufferedImage getSubimage(int x, int y, int w, int h) { return new PixelBufferedImage(w, h, pixels, this.offset+x+y*this.scan, this.scan, this.getType()); }

	public int getRGB(float x, float y) {
		if (x<0 || y<0 || x>width-2 || y>height-2)
			return 0;

		x-=0.5f;
		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan+offset;
		final int ul = pixels[o], ur = pixels[++o], lr = pixels[o+=scan], ll = pixels[--o];

		final int ulB = (ul>>>0)&0xFF, urB = (ur>>>0)&0xFF, lrB = (lr>>>0)&0xFF, llB = (ll>>>0)&0xFF;
		final int ulG = (ul>>>8)&0xFF, urG = (ur>>>8)&0xFF, lrG = (lr>>>8)&0xFF, llG = (ll>>>8)&0xFF;
		final int ulR = (ul>>>16)&0xFF, urR = (ur>>>16)&0xFF, lrR = (lr>>>16)&0xFF, llR = (ll>>>16)&0xFF;
		final int ulA = (ul>>>24)&0xFF, urA = (ur>>>24)&0xFF, lrA = (lr>>>24)&0xFF, llA = (ll>>>24)&0xFF;
		
		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
		
		final int B = (int)((ulB*omxr+urB*xr)*omyr+(llB*omxr+lrB*xr)*yr);
		final int G = (int)((ulG*omxr+urG*xr)*omyr+(llG*omxr+lrG*xr)*yr);
		final int R = (int)((ulR*omxr+urR*xr)*omyr+(llR*omxr+lrR*xr)*yr);
		final int A = (int)((ulA*omxr+urA*xr)*omyr+(llA*omxr+lrA*xr)*yr);
		
		return ((A&0xFF)<<24) | ((R&0xFF)<<16) | ((G&0xFF)<<8) | (B&0xFF);
	}

	
	public float[] getRGB(float x, float y, float argb[]) {
		if (x<0 || y<0 || x>width-1 || y>height-1) {
			argb[0] = argb[1] = argb[2] = argb[3] = 0;
			return argb;
		}

		x-=0.5f;
		y-=0.5f;
		
		final int xi = (int)x, yi = (int)y;
		int o = xi+yi*scan;
		final int ul = pixels[o], ur = pixels[++o], lr = pixels[o+=scan], ll = pixels[--o];

		final int ulB = (ul>>>0)&0xFF, urB = (ur>>>0)&0xFF, lrB = (lr>>>0)&0xFF, llB = (ll>>>0)&0xFF;
		final int ulG = (ul>>>8)&0xFF, urG = (ur>>>8)&0xFF, lrG = (lr>>>8)&0xFF, llG = (ll>>>8)&0xFF;
		final int ulR = (ul>>>16)&0xFF, urR = (ur>>>16)&0xFF, lrR = (lr>>>16)&0xFF, llR = (ll>>>16)&0xFF;
		final int ulA = (ul>>>24)&0xFF, urA = (ur>>>24)&0xFF, lrA = (lr>>>24)&0xFF, llA = (ll>>>24)&0xFF;
		
		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
		
		argb[0] = (ulB*omxr+urB*xr)*omyr+(llB*omxr+lrB*xr)*yr;
		argb[1] = (ulG*omxr+urG*xr)*omyr+(llG*omxr+lrG*xr)*yr;
		argb[2] = (ulR*omxr+urR*xr)*omyr+(llR*omxr+lrR*xr)*yr;
		argb[3] = (ulA*omxr+urA*xr)*omyr+(llA*omxr+lrA*xr)*yr;
		
		return argb;
	}
	
	
	public int[] getRGB(double startX, double startY, int w, int h, int[] rgbArray, int off, int scansize) {
		final int sx = (int) startX, sy = (int) startY;
		
		final float ur = (float)startX-sx, vr = (float)startY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
		
		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
			for (int i=0, c, r, d, e, q ;i<w;i++,o++,O++) 
				rgbArray[o] = 
					((((c=pixels[q=O])&0xFF)*uovo+((r=pixels[q+=1])&0xFF)*urvo+((e=pixels[q+=scan])&0xFF)*urvr+((d=pixels[q-=1])&0xFF)*uovr)>>>8)
					| (((((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr))&0xFF00)
					| (((((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr)<<8)&0xFF0000)
					| (((((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr)<<16)&0xFF000000);
		
		return rgbArray;
	}
	
	
	public PixelBufferedImage sample(int toX, int toY, int w, int h, PixelBufferedImage from, float fromX, float fromY, float fromW, float fromH) {
		
		float stepX = fromW/w, stepY = fromH/h;
		//TODO IMPLEMENT THIS !!
		
		return this;
	}

	
	public PixelBufferedImage set(PixelBufferedImage from) { return set(0,0,Math.min(width,from.width),Math.min(height,from.height),from,0,0); }
	public PixelBufferedImage set(int toX, int toY, int width, int height, PixelBufferedImage from, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan,O=from.offset+fromX+fromY*from.scan,rx=from.scan;y<height;y++,O+=rx,o+=r)
			System.arraycopy(from.pixels, O, pixels, o, width);
		
//		for (int y=0,o=this.offset+toX+toY*this.scan,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				data[o] = from.data[O];
//		
		return this;
	}

	public PixelBufferedImage set(YUVBufferedImage from) { return this.set(0,0, this.width<from.width?this.width:from.width, this.height<from.height?this.height:from.height, from, 0,0); }
	public PixelBufferedImage set(int toX, int toY, int width, int height, YUVBufferedImage from, int fromX, int fromY) {
		setYUV(	width, height, 
				
				this.pixels, this.offset+toX+toY*this.scan, this.scan, 
				
				from.Y.pixels, from.Y.offset+fromX+fromY*from.Y.scan, from.Y.scan, 
				from.U.pixels, from.U.offset+fromX+fromY*from.U.scan, from.U.scan, 
				from.V.pixels, from.V.offset+fromX+fromY*from.V.scan, from.V.scan);
		
		return this;
	}

	

	public PixelBufferedImage set(IntBufferedImage from) { return this.set(0,0, this.width<from.width?this.width:from.width, this.height<from.height?this.height:from.height, from, 0,0); }
	public PixelBufferedImage set(int toX, int toY, int width, int height, IntBufferedImage from, int fromX, int fromY) {
		setLuma(	width, height, 
					this.pixels, this.offset+toX+toY*this.scan, this.scan, 
					from.pixels, from.offset+fromX+fromY*from.scan, from.scan 
				);
		
		return this;
	}

	
//			{
//				final int c = pixels[O], r = pixels[O+1] , d = pixels[O+scan], e = pixels[O+scan+1];
//				
//				final int cB = (c>>>0)&0xFF, fB = (r>>>0)&0xFF, dB = (d>>>0)&0xFF, eB = (e>>>0)&0xFF;
//				final int cG = (c>>>8)&0xFF, fG = (r>>>8)&0xFF, dG = (d>>>8)&0xFF, eG = (e>>>8)&0xFF;
//				final int cR = (c>>>16)&0xFF, fR = (r>>>16)&0xFF, dR = (d>>>16)&0xFF, eR = (e>>>16)&0xFF;
//				final int cA = (c>>>24)&0xFF, fA = (r>>>24)&0xFF, dA = (d>>>24)&0xFF, eA = (e>>>24)&0xFF;
//				
//				final int B = ((cB * uovo + fB * urvo) + (dB * uovr + eB * urvr))>>>BITS;
//				final int G = ((cG * uovo + fG * urvo) + (dG * uovr + eG * urvr))>>>BITS;
//				final int R = ((cR * uovo + fR * urvo) + (dR * uovr + eR * urvr))>>>BITS;
//				final int A = ((cA * uovo + fA * urvo) + (dA * uovr + eA * urvr))>>>BITS;
//				
//				rgbArray[o] = (B<<0)|(G<<8)|(R<<16)|(A<<24);
//				
//				
//				int B = 0, G = 0, R = 0, A = 0;
//				B = (((c=pixels[O])&0xFF)*uovo+((r=pixels[O+1])&0xFF)*urvo+((e=pixels[O+1+scan])&0xFF)*urvr+((d=pixels[O+scan])&0xFF)*uovr)>>>BITS;
//				G = (((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr)>>>BITS;
//				R = (((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr)>>>BITS;
//				A = (((c>>>=8)&0xFF)*uovo+((r>>>=8)&0xFF)*urvo+((e>>>=8)&0xFF)*urvr+((d>>>=8)&0xFF)*uovr)>>>BITS;
//				
//				rgbArray[o] = (B<<0)|(G<<8)|(R<<16)|(A<<24);
//			}	
		
	
	public static void setYUV(int width, int height, int argb[], int offset, int scan, int Y[], int offsetY, int scanY, int U[], int offsetU, int scanU, int V[], int offsetV, int scanV) {
		for (int y=0,o=offset, OY=offsetY, OU=offsetU, OV=offsetV, r = scan-width, RY = scanY-width, RU = scanU-width, RV = scanV-width;y<height;y++,o+=r, OY+=RY, OU+=RU, OV+=RV)
			for (int x=0;x<width;x++,o++,OY++,OU++,OV++) {
				
				final int Y_ = Y[OY];
				final int Cb = U[OU];
				final int Cr = V[OV];
				
				final int red_ = (Y_+((1435*Cr)>>10)), red = red_<0?0:(red_>255?255:red_);
				final int green_ = (Y_-((352*Cb+731*Cr)>>10)), green = green_<0?0:(green_>255?255:green_);
				final int blue_ = (Y_+((1814*Cb)>>10)), blue = blue_<0?0:(blue_>255?255:blue_);
				
				argb[o] = 0xFF000000 | (red<<16) | (green<<8) | blue;
			}
		
	}

	public static void setLuma(int width, int height, int argb[], int offset, int scan, int Y[], int offsetY, int scanY) {
		for (int y=0,o=offset, OY=offsetY, r = scan-width, RY = scanY-width;y<height;y++,o+=r, OY+=RY)
			for (int x=0;x<width;x++,o++,OY++) {
				final int Y_ = Y[OY];
				
				final int absed = Y_<0?(-Y_)&0xFF:Y_;
				final int clamped = Y_<0?0:(Y_>0xFF?0xFF:Y_);
				
				argb[o] = 0xFF000000 | (clamped<<16) | (clamped<<8) | absed;
			}
	}
	
//	static public PixelBufferedImage read(File f) throws IOException {
//		return read(ImageIO.createImageInputStream(f));
//	}
//	
//	static public PixelBufferedImage read(InputStream is) throws IOException {
//		return read(ImageIO.createImageInputStream(is));
//	}
//	
//	static public PixelBufferedImage read(ImageInputStream iis) throws IOException {
//		ImageReader ir = ImageIO.getImageReaders(iis).next();
//		
//		ir.setInput(iis);
//		
//		int type = ir.getImageTypes(0).next().getNumBands()==3?BufferedImage.TYPE_INT_RGB:BufferedImage.TYPE_INT_ARGB;
//		
//		int bands[] = type ==BufferedImage.TYPE_INT_RGB?new int[] { 0, 1, 2 }:new int[] { 0, 1, 2, 3 }; 
//		
//		ImageReadParam irp = new ImageReadParam();
//		PixelBufferedImage destination = new PixelBufferedImage(ir.getWidth(0), ir.getHeight(0), type);
//		irp.setDestination(destination);
//		irp.setDestinationBands(bands);
//		irp.setSourceBands(bands);
//		
//		ir.read(0, irp);
//		
//		return destination;
//	}
	
	public static void main(String... args) throws IOException {
		
		
		
//		BufferedImage source = ImageIO.read(new File("/Library/Desktop Pictures/Nature/Rocks.jpg"));
//		final BufferedImage test = new PixelBufferedImage(500, 500, source.getRGB(0, 0, source.getWidth(), source.getHeight(), new int[source.getWidth()*source.getHeight()], 0, source.getWidth()), 1000+1000*source.getWidth(), source.getWidth(), BufferedImage.TYPE_INT_ARGB);
		
		final PixelBufferedImage test = new PixelBufferedImage(512,512, BufferedImage.TYPE_INT_RGB).read(new FileInputStream("/Library/Desktop Pictures/Lion.jpg"));
//		final PixelBufferedImage test = PixelBufferedImage.read(new File("/Users/pholzschneider/ticker.png"));
		
		System.out.println(test.getType());
		
		final PixelBufferedImage toast = new PixelBufferedImage(256,256, BufferedImage.TYPE_INT_RGB);
		
		JFrame f = new JFrame();
		f.setBounds(100,100,500,400);
		f.setContentPane(new JComponent() {
			private static final long serialVersionUID = 1L;
			JSlider shiftSliderX = new JSlider(JSlider.HORIZONTAL);
			JSlider shiftSliderY = new JSlider(JSlider.VERTICAL);
			
			ChangeListener updateListener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					repaint();
				}
			};
			
			{
				setLayout(new BorderLayout());

				
				shiftSliderX.addChangeListener(updateListener);
				shiftSliderY.addChangeListener(updateListener);
				
				add(shiftSliderX,BorderLayout.SOUTH);
				add(shiftSliderY,BorderLayout.EAST);
			}
			
			protected void paintComponent(Graphics g) {

				test.getRGB(shiftSliderX.getValue()*.05f, shiftSliderY.getValue()*.05f, toast.width, toast.height, toast.pixels, toast.offset, toast.scan);
				
				g=g.create();
				((Graphics2D)g).scale(10, 10);
				g.drawImage(toast,0,0,null);
				g.dispose();
			}
		});
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
}





