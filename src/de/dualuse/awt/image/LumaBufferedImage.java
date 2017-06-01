package de.dualuse.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;


public class LumaBufferedImage extends IntBufferedImage {
	
	static ColorSpace Y_COLOR_SPACE = new ColorSpace(ColorSpace.CS_GRAY, 1) {
		private static final long serialVersionUID = 1L;
		
		public float[] toRGB(float[] colorvalue) { return REF_COLOR_SPACE.toRGB(colorvalue); }
		public float[] toCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.toCIEXYZ(colorvalue); }
		public float[] fromRGB(float[] rgbvalue) { return REF_COLOR_SPACE.fromRGB(rgbvalue); } 
		public float[] fromCIEXYZ(float[] colorvalue) { return REF_COLOR_SPACE.fromCIEXYZ(colorvalue); }
	};
	
	
	static ColorModel Y_COLOR_MODEL = new ComponentColorModel(Y_COLOR_SPACE, new int[] { 8 }, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT) {
		public int getRGB(Object inData) {
			int[] rgb = (int[])inData;
			
			final int raw = rgb[0];
//			final int absed = raw<0?(-raw)&0xFF:raw;
			final int clamped = raw<0?0:(raw>0xFF?0xFF:raw);
			
			rgb[0] = clamped;
//			final int gray = raw&0xFF;//getRed(rgb); //rgb[0];
			
//			return 0xFF000000 | (gray<< 16) | (gray<< 8) | (raw>0?gray:-gray);
			return 0xFF000000 | (clamped<< 16) | (clamped<< 8) | clamped;
		}
	};
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public LumaBufferedImage(int width, int height, int [] data, int offset, int scan) {
		super(width, height, data, offset, scan, Y_COLOR_MODEL);
	}
	
	public LumaBufferedImage(IntBufferedImage from) { this(from.width,from.height); set(0,0,width,height, from,0,0); };
	public LumaBufferedImage(PixelBufferedImage from) { this(from.width,from.height); set(0,0,width,height, from,0,0); };
	public LumaBufferedImage(RGBBufferedImage from) { this(from.width,from.height); set(0,0,width,height, from,0,0); };
	
	public LumaBufferedImage(int width, int height) {
		super(width, height, new int[width*height], 0, width, Y_COLOR_MODEL);
	}
	
	private LumaBufferedImage(int width, int height, int[] luma, int offset, int scan, ColorModel cm, WritableRaster raster) {
		super(width,height, offset, scan, cm, raster);
//		this.luma = luma;
	}
	
	public LumaBufferedImage getSubimage(int x, int y, int w, int h) {
		return new LumaBufferedImage(w, h, data, x+y*scan, scan, Y_COLOR_MODEL, getRaster().createWritableChild(x, y, w, h, 0, 0, null));
	}
	
	public int[] getRGB(double startX, double startY, int w, int h, int[] rgbArray, int off, int scansize) {
		final int sx = (int) startX, sy = (int) startY;
		
		final float ur = (float)startX-sx, vr = (float)startY-sy;
		final float uo = 1 - ur, vo = 1 - vr;
		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
		
		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
			for (int i=0,q;i<w;i++,o++,O++) { 
				int l = ((((data[q=O])*uovo+(data[q+=1])*urvo+(data[q+=scan])*urvr+(data[q-=1])*uovr)>>>8));
				l=l>255?255:(l<0?0:l);
				rgbArray[o] = 0xFF000000 | (l<<16) | (l<<8) | l;
			}
		
		return rgbArray;
	}
	
	
	public LumaBufferedImage set(int toX, int toY, int width, int height, IntBufferedImage li, int fromX, int fromY) 
	{ super.set(toX, toY, width, height, li, fromX, fromY); return this; }
	
	public LumaBufferedImage add(int toX, int toY, int width, int height, IntBufferedImage li, int fromX, int fromY, int norm, int base)
	{ super.add(toX, toY, width, height, li, fromX, fromY, norm, base); return this; }

	public IntBufferedImage mipmap(int toX, int toY, int width, int height, IntBufferedImage li, int fromX, int fromY) 
	{ super.mipmap(toX, toY, width, height, li, fromX, fromY); return this; }


	
//	public int[] getLuma(double startX, double startY, int w, int h, int[] lumaArray, int off, int scansize) {
//		final int sx = (int) startX, sy = (int) startY;
//		
//		final float ur = (float)startX-sx, vr = (float)startY-sy;
//		final float uo = 1 - ur, vo = 1 - vr;
//		final int uovo = (int)(uo*vo*(1<<8)), urvo = (int)(ur*vo*(1<<8)), uovr = (int)(uo*vr*(1<<8)), urvr = (int)(ur*vr*(1<<8));
//		
//		for (int j=0,o=off,O=this.offset+sx+sy*this.scan,P=scan-w,p=scansize-w;j<h;j++,O+=P,o+=p) 
//			for (int i=0,q;i<w;i++,o++,O++)  
//				lumaArray[o] = ((((luma[q=O])*uovo+(luma[q+=1])*urvo+(luma[q+=scan])*urvr+(luma[q-=1])*uovr)>>>8));
//			
//		return lumaArray;
//	}
	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		set(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		add(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	public LumaBufferedImage sub(int toX, int toY, int width, int height, LumaBufferedImage li, double fromX, double fromY) {
//		sub(
//				this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width,height, 
//				li.luma, li.offset+(int)fromX+li.scan*(int)fromY, li.scan, fromX-(int)fromX, fromY-(int)fromY);
//		return this;
//	}
//	
//	public float getLuma(float x, float y) {
//		if (x<0 || y<0 || x>width-1 || y>height-1)
//			return 0;
//
//		x-=0.5f;
//		y-=0.5f;
//		
//		final int xi = (int)x, yi = (int)y;
//		int o = xi+yi*scan;
//		final int ul = luma[o], ur = luma[++o], lr = luma[o+=scan], ll = luma[--o];
//		
//		final float xr = x-xi, yr = y-yi, omxr = 1f-xr, omyr = 1f-yr;
//		return (ul*omxr+ur*xr)*omyr+(ll*omxr+lr*xr)*yr;
//	}
//	
//	public int getLuma(int x, int y) {
//		if (x>0 && y>0 && x<width && y<height)
//			return luma[x+y*scan];
//		else
//			return 0;
//	}
//
//	
//	public LumaBufferedImage addSquared(int toX, int toY, int width, int height, LumaBufferedImage gx, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,ox=fromX+fromY*gx.scan,rx=gx.scan-width;y<height;y++,ox+=rx,o+=r) 
//			for (int x=0;x<width;x++,ox++,o++) {
//				final int g_x=gx.luma[ox];
//				luma[o] += g_x*g_x;
//			}
//		
//		return this;
//	}
//
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] += li.luma[O];
//		
//		return this;
//	}
//	
//	public LumaBufferedImage sub(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		sub(
//				this.luma, this.offset, this.scan, 
//				width, height, 
//				li.luma, li.offset, li.scan);
//		
//		return this;
//	}
//
//	public LumaBufferedImage add(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY, int norm, int base) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] += base+li.luma[O]/norm;
//		
//		return this;
//	}
//
//	public LumaBufferedImage set(int toX, int toY, int width, int height, LumaBufferedImage li, int fromX, int fromY) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan,O=fromX+fromY*li.scan,rx=li.scan;y<height;y++,O+=rx,o+=r)
//			System.arraycopy(li.luma, O, luma, o, width);
//		
//		return this;
//	}
//	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, int[] from, int fromOffset, int fromScan) {
//		set(	this.luma, this.offset+toX+toY*this.scan, this.scan, 
//				width, height, 
//				from, fromOffset, fromScan);
//		
//		return this;
//	}
//
//
//	public LumaBufferedImage set(int toX, int toY, int width, int height, FloatBufferedImage from) {
//		for (int y=0,o=this.offset,r=this.scan-width,O=from.offset,rx=from.scan-width;y<height;y++,O+=rx,o+=r) 
//			for (int x=0;x<width;x++,O++,o++)
//				luma[o] = (int) from.data[O];
//		
//		return this;
//	}

	

	
//	public LumaBufferedImage set(int toX, int toY, int width, int height, float [] from, int fromOffset, int fromScan) {
//		Blit.setLumaWithLuma(
//					this.luma, this.offset+toX+toY*this.scan, this.scan, 
//					width, height, 
//					from, fromOffset, fromScan);
//		
//		return this;
//	}

	public LumaBufferedImage set(PixelBufferedImage li) { return this.set(0,0, Math.min(width,li.width), Math.min(height, li.height), li, 0,0); }
	public LumaBufferedImage set(int toX, int toY, int width, int height, PixelBufferedImage li, int fromX, int fromY) {
		
//		for (int y=0,o=lbi.offset,r=lbi.scan-lbi.width,O=mbi.offset,R=mbi.scan-mbi.width;y<lbi.height;y++,o+=r,O+=R)
//			for (int x=0,argb;x<lbi.width;x++,o++,O++)
//				lbi.pixels[o] = (((argb=mbi.pixels[O])&0xFF)*117+((argb>>>8)&0xFF)*601+((argb>>>16)&0xFF)*306)>>10;

		
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,O=fromX+fromY*li.scan+li.offset,rx=li.scan-width;y<height;y++,O+=rx,o+=r) 
			for (int x=0,argb;x<width;x++,O++,o++) 
				data[o] = (((argb=li.pixels[O])&0xFF)*117+((argb>>>8)&0xFF)*601+((argb>>>16)&0xFF)*306)>>10;
				
		return this;
	}
	
	public LumaBufferedImage set(RGBBufferedImage li) { return this.set(0,0, Math.min(width,li.width), Math.min(height, li.height), li, 0,0); }
	public LumaBufferedImage set(int toX, int toY, int width, int height, RGBBufferedImage li, int fromX, int fromY) {
		for (int y=0,o=offset+toY*scan+toX,r=scan-width,OR=li.R.offset+fromX+fromY*li.scan,OG=li.G.offset+fromX+fromY*li.scan,OB=li.B.offset+fromX+fromY*li.scan,rx=li.scan-width;y<height;y++,OR+=rx,OG+=rx,OB+=rx,o+=r) 
			for (int x=0;x<width;x++,OR++,OG++,OB++,o++) 
				data[o] = (li.R.data[OR]*117+li.G.data[OG]*601+li.B.data[OB]*306)>>10;
				
		return this;
	}
	
//	public void clear(int toX, int toY, int width, int height, int value) {
//		for (int y=0,o=offset+toY*scan+toX,r=scan;y<height;y++,o+=r)
//			Arrays.fill(luma, o, o+width, value);
//	}
	
//	public LumaBufferedImage convolve( 
//			int toX, int toY, int width, int height, 
//			LumaBufferedImage li, int fromX, int fromY,
//			SeparableKernel kernel, int stepX, int stepY) {
//
//		kernel.convolve(
//				this.luma, this.offset+toX+toY*scan, this.scan, 
//				li.luma, li.offset+fromX+fromY*li.scan, 
//				
//				width, height, li.scan, stepX+stepY*li.scan);
//		
//		return this;
//	}

	
//	public LumaBufferedImage box( int x, int y, int width, int height, LumaBufferedImage li, int sx, int sy, int boxRadius, int norm, BoxKernel processingDirection) {
//		processingDirection.convolve(
//				this.luma, this.offset+x+y*this.scan, this.scan, 
//				li.luma, li.offset+sx+sy*li.scan, li.scan, 
//				
//				width, height, boxRadius, norm);
//
//		return this;
//	}

	

//	public static void main(String[] args) throws IOException {
//		
////		String fname = "/Library/Desktop Pictures/Nature/Cirques.jpg";
//		String fname = "/Volumes/Ohne Titel 2/Crowd_PETS09/S0/Background/View_001/Time_13-06/00000123.jpg";
//		
//		
//		PixelBufferedImage source = new PixelBufferedImage(512,512, BufferedImage.TYPE_INT_RGB).read(new File(fname));
//		
//		final LumaBufferedImage boxee = new LumaBufferedImage(512, 512);//.set(0, 0, 512, 512, source, 0, 0);
//		boxee.set(0, 0, source.width, source.height, source, 0, 0);
////		boxee.getGraphics().drawImage(source,0,0,null);
//		
//		final LumaBufferedImage boxed = new LumaBufferedImage(512, 512);
//		final IntBufferedImage doubleboxed = new LumaBufferedImage(512, 512);
//		
//		final JSlider boxRadiusSlider = new JSlider(JSlider.VERTICAL, 0, 64, 4);
//		
//		final IntBufferedImage mipmapped = new LumaBufferedImage(256, 256);
//		
//		JFrame f = new JFrame();
//		f.getContentPane().add(new JMicroscope() {
//			private static final long serialVersionUID = 1L;
//			
//			{
//				recalc();
//			}
//			
//			private void recalc() {
//				int R = boxRadiusSlider.getValue();
//				
//				BoxFilter.HORIZONTAL.convolve(
//						boxed, R+1, 0, 
//						boxee, R+1, 0,  
//						
//						boxed.width-R*2-2, boxed.height, 
//						R, 2*R+1);
//
//				BoxFilter.VERTICAL.convolve(
//						doubleboxed, 0, R+1, 
//						boxed, 0, R+1, 
//						
//						boxed.width, boxed.height-R*2-2, 
//						R, 2*R+1);
//						
//				doubleboxed.add(0, 0, boxed.width, boxed.height, boxee, 0, 0, -1, 128);
//
////				BoxKernel.VERTICAL.convolve(to, toOffset, toScan, from, fromOffset, fromScan, width, height, box, norm)
//				
////				boxed.box(R+1, R+1, boxed.getWidth()-(R+1)*2, boxed.getHeight()-(R+1)*2, boxee, R, R, R, 2*R+1, BoxKernel.HORIZONTAL);
////				boxed.set(0, 0, boxed.width/2, boxed.height/2, boxee, R/32., R/32.);
//				
//				mipmapped.mipmap(0, 0, doubleboxed.width/2, doubleboxed.height/2, doubleboxed, 0, 0);
//				
//				repaint();
//			}
//			
//			ChangeListener boxer = new ChangeListener() {
//				{ 
//					boxRadiusSlider.addChangeListener(this);
//				}
//				
//				public void stateChanged(ChangeEvent e) {
//					recalc();
//				}
//			};
//			
//			public void paintCanvas(Graphics g) {
//				g.drawImage(mipmapped, 0,0, this);
//			}
//		});
//		
//		f.getContentPane().add(boxRadiusSlider, BorderLayout.EAST);
//		
//		f.setBounds(300, 100, 600, 600);
//		f.setVisible(true);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		
//	}
}


