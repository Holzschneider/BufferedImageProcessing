package de.dualuse.commons.awt.image;

import java.awt.image.BufferedImage;


public class MipMapBufferedImage extends PixelBufferedImage {
	
	public final MipMapBufferedImage mipmap;
	
	public int getRGB(float x, float y, float s) {
		MipMapBufferedImage m = this;
		for (;s/2>1;x/=2,y/=2,s/=2) 
			m = m.mipmap;
		
		int argb = m.mipmap.getRGB(x/2f, y/2f);
		int ARGB = m.getRGB(x, y);

		int a = (argb>>>24)&0xFF, r = (argb>>>16)&0xFF, g = (argb>>>8)&0xFF, b = (argb&0xFF);
		int A = (ARGB>>>24)&0xFF, R = (ARGB>>>16)&0xFF, G = (ARGB>>>8)&0xFF, B = (ARGB&0xFF);
		
		float mix = Math.max((float) (Math.log(s)/Math.log(2)), 0.0f);
		float ommix = 1-mix;
		int a_ = (int)(a*mix+A*ommix), r_ = (int)(r*mix+R*ommix), g_ = (int)(g*mix+G*ommix), b_ = (int)(b*mix+B*ommix);
		return (a_<<24)|(r_<<16)|(g_<<8)|b_;
	}
	
	private static int[] fullMipMapBufferForSize(int width, int height) {
		int sum = 0;
		for (;width>=2&&height>=2;width=(width+width%2)/2, height=(height+height%2)/2)
			sum+=width*height;
			
		return new int[sum+2];
	}
	
	//TODO maybe dword / qword align pixel lines here
	private MipMapBufferedImage(int width, int height, int pixels[], int offset, int type) {
		super(width, height, pixels, offset, width, type);
		
		if (width>=2 && height>=2)
			mipmap = new MipMapBufferedImage( (width+width%2)/2, (height+height%2)/2, pixels, offset+=width*height, type);
		else
			mipmap = this;
	}

	public MipMapBufferedImage(PixelBufferedImage copy) {
		this(copy.getWidth(), copy.getHeight(), copy.getType());
		copy.getRGB(0, 0, copy.getWidth(), copy.getHeight(), pixels, this.offset, this.scan);
		generateMipmap();
	}
	

	public MipMapBufferedImage(BufferedImage copy) {
		this(copy, formatForFormat(copy.getType()));
		
	}
	
	public MipMapBufferedImage(BufferedImage copy, int type) {
		this(copy.getWidth(), copy.getHeight(), type);
		copy.getRGB(0, 0, copy.getWidth(), copy.getHeight(), pixels, this.offset, this.scan);
		generateMipmap();
	}
	
	public MipMapBufferedImage(int width, int height, int type) { 
		this(width,height, fullMipMapBufferForSize(width, height), 0, type);
	}
	
//	public MipMapBufferedImage(int width, int height, int pixels[], int offset, int scan, int type) {
//		super(width,height,pixels,offset,scan,type);
//		
//		int mipmapWidth = (width+width%2)/2, mipmapHeight = (height+height%2)/2;
//		
//		if (mipmapWidth>=2 && mipmapHeight>=2)
//			this.mipmap = new MipMapBufferedImage( mipmapWidth, mipmapHeight, new int[mipmapWidth*mipmapHeight], 0, mipmapWidth, type);
//		else
//			this.mipmap = this;
//	}
	
	public void generateMipmap() {
		if (mipmap==this)
			return;
		
//		Graphics2D g2 = mipmap.createGraphics();
//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		g2.drawImage(this, AffineTransform.getScaleInstance(.5, .5), null);
		
//		System.out.println(width+", "+height+" ->  "+mipmap.width+", "+mipmap.height);
		
		superSample(width, height, pixels, offset, scan, mipmap);
		
		mipmap.generateMipmap();
	}
	
	public MipMapBufferedImage getLevel(int l) {
		MipMapBufferedImage mmbi = this;
		for (int i=0;i<l;i++)
			mmbi = mmbi.mipmap;
		
		return mmbi;
	}
	

	public static void superSample(
			int width, int height, 
			int pixels[], int offset, int scan, 
			PixelBufferedImage mipmap) {
		
		superSample(width, height, pixels, offset, scan, mipmap.pixels, mipmap.offset, mipmap.scan, mipmap.getType()==MipMapBufferedImage.TYPE_INT_ARGB);
	}

	
	public static void superSample(
			int width, int height, 
			int pixels[], int offset, int scan, 
			int mipmappixels[], int mipmapoffset, int mipmapscan,
			boolean alpha) {
		
		for (int y=0,Y=height/2,o=offset,p=mipmapoffset,r=2*scan,s=mipmapscan;y<Y;y++,p+=s,o+=r) {
			for (int x=0,X=width/2,O=o,P=p;x<X;x++,P++,O+=2) {
				final int a = pixels[O];
				final int b = pixels[O+1];
				final int c = pixels[O+scan];
				final int d = pixels[O+1+scan];
								
				mipmappixels[P] = 
					(((((a>>>24)&0xFF)+((b>>>24)&0xFF)+((c>>>24)&0xFF)+((d>>>24)&0xFF))>>>2)<<24) | 
//					((((a&0xFF000000)+(b&0xFF000000)+(c&0xFF000000)+(d&0xFF000000))>>2)&0xFF000000) | 
					((((a&0x00FF0000)+(b&0x00FF0000)+(c&0x00FF0000)+(d&0x00FF0000))>>>2)&0x00FF0000) |
					((((a&0x0000FF00)+(b&0x0000FF00)+(c&0x0000FF00)+(d&0x0000FF00))>>>2)&0x0000FF00) | 
					((((a&0x000000FF)+(b&0x000000FF)+(c&0x000000FF)+(d&0x000000FF))>>>2)&0x000000FF);
				
			}
		}
		
		final int borderMask = alpha?0x00FFFFFF:0x00000000;
		
		int mipmapheight = (height+height%2)/2, mipmapwidth = (width+width%2)/2;
		
		if (height%2==1)
		for (int x=0,p=mipmapoffset+(mipmapheight-1)*mipmapscan,o=offset+(height-1)*scan;x<mipmapwidth-1;x++,p++,o+=2) {
			final int a = pixels[o];
			final int b = pixels[o+1];
			final int c = a&borderMask;
			final int d = b&borderMask;
		
			mipmappixels[p] = 
				(((((a>>>24)&0xFF)+((b>>>24)&0xFF)+((c>>>24)&0xFF)+((d>>>24)&0xFF))>>>2)<<24) | 
//				((((a&0xFF000000)+(b&0xFF000000)+(c&0xFF000000)+(d&0xFF000000))>>2)&0xFF000000) | 
				((((a&0x00FF0000)+(b&0x00FF0000)+(c&0x00FF0000)+(d&0x00FF0000))>>>2)&0x00FF0000) |
				((((a&0x0000FF00)+(b&0x0000FF00)+(c&0x0000FF00)+(d&0x0000FF00))>>>2)&0x0000FF00) | 
				((((a&0x000000FF)+(b&0x000000FF)+(c&0x000000FF)+(d&0x000000FF))>>>2)&0x000000FF);
		}
		
		
		if (width%2==1)
		for (int y=0,p=mipmapoffset+mipmapwidth-1,o=offset+width-1,s=scan*2;y<mipmapheight-1;y++,p+=mipmapscan,o+=s) {
			final int a = pixels[o];
			final int b = a&borderMask;
			final int c = pixels[o+scan];
			final int d = c&borderMask;

			mipmappixels[p] = 
				(((((a>>>24)&0xFF)+((b>>>24)&0xFF)+((c>>>24)&0xFF)+((d>>>24)&0xFF))>>>2)<<24) | 
//				((((a&0xFF000000)+(b&0xFF000000)+(c&0xFF000000)+(d&0xFF000000))>>2)&0xFF000000) | 
				((((a&0x00FF0000)+(b&0x00FF0000)+(c&0x00FF0000)+(d&0x00FF0000))>>>2)&0x00FF0000) |
				((((a&0x0000FF00)+(b&0x0000FF00)+(c&0x0000FF00)+(d&0x0000FF00))>>>2)&0x0000FF00) | 
				((((a&0x000000FF)+(b&0x000000FF)+(c&0x000000FF)+(d&0x000000FF))>>>2)&0x000000FF);
		}
		
		if (width%2==1 && height%2==1) {
			final int a = pixels[offset+height*scan-1];
			final int b = a&borderMask;
			final int c = a&borderMask;
			final int d = a&borderMask;
			
			mipmappixels[mipmapoffset+mipmapheight*mipmapscan-1] = 
				(((((a>>>24)&0xFF)+((b>>>24)&0xFF)+((c>>>24)&0xFF)+((d>>>24)&0xFF))>>>2)<<24) | 
//				((((a&0xFF000000)+(b&0xFF000000)+(c&0xFF000000)+(d&0xFF000000))>>2)&0xFF000000) | 
				((((a&0x00FF0000)+(b&0x00FF0000)+(c&0x00FF0000)+(d&0x00FF0000))>>>2)&0x00FF0000) |
				((((a&0x0000FF00)+(b&0x0000FF00)+(c&0x0000FF00)+(d&0x0000FF00))>>>2)&0x0000FF00) | 
				((((a&0x000000FF)+(b&0x000000FF)+(c&0x000000FF)+(d&0x000000FF))>>>2)&0x000000FF);
		}	
	}
	
}



