package de.dualuse.commons.awt.image;

import java.util.Arrays;

public class SeparableKernel {

	final public int coefficients[];
	final public int offset, length;
	final public int norm;
	
	@SuppressWarnings("hiding")
	public<IntBufferedImage extends de.dualuse.commons.awt.image.IntBufferedImage> IntBufferedImage convolve(
			IntBufferedImage to, int toX, int toY, 
			IntBufferedImage from, int fromX, int fromY,
			
			int width, int height, int scanX, int scanY)
	{
		convolve(
			to.data, to.offset+toX+toY*to.scan, to.scan,
			from.data, from.offset+fromX+fromY*from.scan, from.scan,
			
			width, height,
			coefficients, offset, length, norm,
			scanX+scanY*from.scan);
			
		
		return to;
	}

	@SuppressWarnings("hiding")
	public<FloatBufferedImage extends de.dualuse.commons.awt.image.FloatBufferedImage> FloatBufferedImage convolve(
			FloatBufferedImage to, int toX, int toY, 
			FloatBufferedImage from, int fromX, int fromY,
			
			int width, int height, int scanX, int scanY)
	{
		convolve(
			to.data, to.offset+toX+toY*to.scan, to.scan,
			from.data, from.offset+fromX+fromY*from.scan, from.scan,
			
			width, height,
			coefficients, offset, length, norm,
			scanX+scanY*from.scan);
			
		
		return to;
	}
	
	public int[] convolve(
			final int[] to, final int toOffset, final int toScan, 
			final int[] from, final int fromOffset, final int fromScan,
			final int width, final int height, final int scanStep) {
		
		convolve(
				to,toOffset, toScan, 
				from, fromOffset, fromScan, 
				
				width,height, 
				coefficients, offset, length, norm, scanStep);
		
		return to;
	}


	public double[] convolve(
			final double[] to, final int toOffset, final int toScan, 
			final double[] from, final int fromOffset, final int fromScan,
			final int width, final int height, final int scanStep) {
		
		convolve(
				to,toOffset, toScan, 
				from, fromOffset, fromScan, 
				
				width,height, 
				coefficients, offset, length, norm, scanStep);
		
		return to;
	}
	
	public float[] convolve(
			final float[] to, final int toOffset, final int toScan, 
			final float[] from, final int fromOffset, final int fromScan,
			final int width, final int height, final int scanStep) {
		
		convolve(
				to,toOffset, toScan, 
				from, fromOffset, fromScan, 
				
				width,height, 
				coefficients, offset, length, norm, scanStep);
		
		return to;
	}
	
	public static void convolve(
			final int[] toLuma, final int toOffset, final int toScan, 
			final int[] fromLuma, final int fromOffset, final int fromScan, 

			final int width, final int height, 
			final int[] kernel, final int offset, final int len, final int norm, final int scanStep) {
		
		final int px = 100, py = 100;
		final int sscanStep = scanStep + py*fromScan+px;
		final int scanX = (sscanStep%fromScan)-px, scanY = (sscanStep/fromScan)-py;

		final int j = offset, J=j+len, S = scanStep, k=(J/2)*S;
		final int W = width, H = height;//, mx = Math.abs(k%fromScan<len?k%fromScan:fromScan-(k%fromScan)), my = Math.abs((k%fromScan<len?0:1)+k/fromScan);
		final int mx = Math.abs(scanX*(J/2)), my = Math.abs(scanY*(J/2));
		
		int left = (left = mx-(fromOffset%fromScan))>0?left:0, top = (top = my-(fromOffset/fromScan))>0?top:0;
		int right = (right = /*1+*/mx-(fromScan-(((fromOffset+width-1)%fromScan)+1)))>0?right:0, bottom = (bottom = my-(fromLuma.length-(fromOffset+height*fromScan))/fromScan)>0?bottom:0;
		final int Y=H-bottom, X=W-right, r=toScan-(W-right-left),R=fromScan-(W-right-left);
		
		for (int y=0,o=left+toOffset,O=left+fromOffset;y<top;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P>0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		for (int y=top,o=top*toScan+toOffset,O=top*fromScan+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		for (int y=0,o=toOffset,O=fromOffset;y<top;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		for (int y=height-bottom,o=left+y*toScan+toOffset,O=left+y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P<size) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}

		
		for (int y=height-bottom,o=y*toScan+toOffset,O=y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k,u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		//center-right
		for (int y=top,o=top*toScan+width-right+toOffset,O=top*fromScan+width-right+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		//upper-right
		for (int y=0,o=width-right+toOffset,O=width-right+fromOffset;y<top;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>0 && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}

		//lower-right
		for (int y=height-bottom,o=(height-bottom)*toScan+width-right+toOffset,O=(height-bottom)*fromScan+width-right+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum/norm;
			}
		
		
		//center-center
		for (int y=top,o=left+top*toScan+toOffset,O=left+top*fromScan+fromOffset;y<Y;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0;
				for (int i=j,P=O-k;i<J;i++,P+=S) 
					sum += kernel[i]*fromLuma[P];
				
				toLuma[o] = sum/norm;
			}
	}
	
	public static void convolve(
			final float[] toLuma, final int toOffset, final int toScan, 
			final float[] fromLuma, final int fromOffset, final int fromScan, 

			final int width, final int height, 
			final int[] kernel, final int offset, final int len, final int norm, final int scanStep) {

		final int px = 100, py = 100;
		final int sscanStep = scanStep + py*fromScan+px;
		final int scanX = (sscanStep%fromScan)-px, scanY = (sscanStep/fromScan)-py;

		final int j = offset, J=j+len, S = scanStep, k=(J/2)*S;
		final int W = width, H = height;//, mx = Math.abs(k%fromScan<len?k%fromScan:fromScan-(k%fromScan)), my = Math.abs((k%fromScan<len?0:1)+k/fromScan);
		final int mx = Math.abs(scanX*(J/2)), my = Math.abs(scanY*(J/2));
		
		int left = (left = mx-(fromOffset%fromScan))>0?left:0, top = (top = my-(fromOffset/fromScan))>0?top:0;
		int right = (right = /*1+*/mx-(fromScan-(((fromOffset+width-1)%fromScan)+1)))>0?right:0, bottom = (bottom = my-(fromLuma.length-(fromOffset+height*fromScan))/fromScan)>0?bottom:0;
		final int Y=H-bottom, X=W-right, r=toScan-(W-right-left),R=fromScan-(W-right-left);
		
		final float ooNorm = 1f/norm;
		
		for (int y=0,o=left+toOffset,O=left+fromOffset;y<top;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P>0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=top,o=top*toScan+toOffset,O=top*fromScan+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=0,o=toOffset,O=fromOffset;y<top;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=height-bottom,o=left+y*toScan+toOffset,O=left+y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P<size) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}

		
		for (int y=height-bottom,o=y*toScan+toOffset,O=y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k,u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		//center-right
		for (int y=top,o=top*toScan+width-right+toOffset,O=top*fromScan+width-right+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		//upper-right
		for (int y=0,o=width-right+toOffset,O=width-right+fromOffset;y<top;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>0 && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}

		//lower-right
		for (int y=height-bottom,o=(height-bottom)*toScan+width-right+toOffset,O=(height-bottom)*fromScan+width-right+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		
		//center-center
		for (int y=top,o=left+top*toScan+toOffset,O=left+top*fromScan+fromOffset;y<Y;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0;
				for (int i=j,P=O-k;i<J;i++,P+=S) 
					sum += kernel[i]*fromLuma[P];
				
				toLuma[o] = sum*ooNorm;
			}
	}
	
	

	public static void convolve(
			final double[] toLuma, final int toOffset, final int toScan, 
			final double[] fromLuma, final int fromOffset, final int fromScan, 

			final int width, final int height, 
			final int[] kernel, final int offset, final int len, final int norm, final int scanStep) {

		final int px = 100, py = 100; //Modulo padding?? WTF ? correct thiz! 
		final int sscanStep = scanStep + py*fromScan+px;
		final int scanX = (sscanStep%fromScan)-px, scanY = (sscanStep/fromScan)-py;

		final int j = offset, J=j+len, S = scanStep, k=(J/2)*S;
		final int W = width, H = height;//, mx = Math.abs(k%fromScan<len?k%fromScan:fromScan-(k%fromScan)), my = Math.abs((k%fromScan<len?0:1)+k/fromScan);
		final int mx = Math.abs(scanX*(J/2)), my = Math.abs(scanY*(J/2));
		
		int left = (left = mx-(fromOffset%fromScan))>0?left:0, top = (top = my-(fromOffset/fromScan))>0?top:0;
		int right = (right = /*1+*/mx-(fromScan-(((fromOffset+width-1)%fromScan)+1)))>0?right:0, bottom = (bottom = my-(fromLuma.length-(fromOffset+height*fromScan))/fromScan)>0?bottom:0;
		final int Y=H-bottom, X=W-right, r=toScan-(W-right-left),R=fromScan-(W-right-left);
		
		final double ooNorm = 1f/norm;
		
		for (int y=0,o=left+toOffset,O=left+fromOffset;y<top;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P>0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=top,o=top*toScan+toOffset,O=top*fromScan+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=0,o=toOffset,O=fromOffset;y<top;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		for (int y=height-bottom,o=left+y*toScan+toOffset,O=left+y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k;i<J;i++,P+=S) if (P<size) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}

		
		for (int y=height-bottom,o=y*toScan+toOffset,O=y*fromScan+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-left,O+=fromScan-left)
			for (int x=0;x<left;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k,u=((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u>=0) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		//center-right
		for (int y=top,o=top*toScan+width-right+toOffset,O=top*fromScan+width-right+fromOffset,size=fromLuma.length;y<Y;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>=0 && P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		//upper-right
		for (int y=0,o=width-right+toOffset,O=width-right+fromOffset;y<top;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P>0 && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}

		//lower-right
		for (int y=height-bottom,o=(height-bottom)*toScan+width-right+toOffset,O=(height-bottom)*fromScan+width-right+fromOffset,size=fromLuma.length;y<height;y++,o+=toScan-right,O+=fromScan-right)
			for (int x=0;x<right;x++,o++,O++) {
				int sum=0; for (int i=j,P=O-k, u=width+((P+px)%fromScan)-px;i<J;i++,P+=S,u+=scanX) if (P<size && u<width) sum += kernel[i]*fromLuma[P];
				toLuma[o] = sum*ooNorm;
			}
		
		
		//center-center
		for (int y=top,o=left+top*toScan+toOffset,O=left+top*fromScan+fromOffset;y<Y;y++,o+=r,O+=R)
			for (int x=left;x<X;x++,o++,O++) {
				int sum=0;
				for (int i=j,P=O-k;i<J;i++,P+=S) 
					sum += kernel[i]*fromLuma[P];
				
				toLuma[o] = sum*ooNorm;
			}
	}
	

	private SeparableKernel(int ka[], int wa) {
		this.coefficients = ka;
		this.offset = 0;
		this.length = ka.length;
		this.norm = wa;
	}
	
	
	public String toString() {
		String prefix = getClass().getSimpleName()+"( [ ";
		String suffix = "], "+norm+" )";
		StringBuilder body = new StringBuilder();
		for (int i=offset,l=i+length;i<l;i++)
			if (i!=0)
				body.append(", ").append(coefficients[i]);
			else
				body.append(coefficients[i]);
		
		return prefix+body+suffix;
	}
	
	public SeparableKernel norm(int norm) { return new SeparableKernel(coefficients,norm); }
	
	public static SeparableKernel Bar(int r) { return new Bar(r); }
	public static SeparableKernel Bar(int r, int w) { return new Bar(r,w); }
	public static class Bar extends SeparableKernel {
		static int[] kernelForRadius(int radius, int w) {
			int[] kernel = new int[radius*2+1];
			Arrays.fill(kernel, w);
			return kernel;
		}
		
		public Bar(int radius, int weight) {
			super(kernelForRadius(radius, weight), radius*2+1);
		}

		public Bar(int radius) {
			this(radius,1);
		}
	}
	
	public static class Gradient extends SeparableKernel {
		public static int[] kernelForGradient(int radius, double sigma) {
			int ka[] = new int[radius*2+1];
			
			double sa = sigma; 
			
			@SuppressWarnings("unused") int wa = 0;
			for (int i=0,l=ka.length,j=-l/2;i<l;j++,i++) wa += (ka[i] = (int)(-j*Math.exp(-.5*j*j/sa/sa)*0xFF));
			
			return ka;
		}
		
		public static int weightForKernel(int[] kernel) {
			int sum = 0;
			for (int i=0;i<kernel.length/2;i++)
				sum+=kernel[i];
			return sum;
		}
		
		private Gradient(int[] kernel) {
			super(kernel,weightForKernel(kernel));
		}
		
		public Gradient(int radius, double sigma) {
			this(kernelForGradient(radius, sigma));
		}
	}

//	public static SeparableKernel Gaussian(int radius, double sigma) { return new Gaussian(radius, sigma); }
//	public static class Gaussian extends SeparableKernel {
//		public static int[] kernelForGaussian(int radius, double sigma) {
//			int ka[] = new int[radius*2+1];
//			
//			double sa = sigma; 
//			@SuppressWarnings("unused") int wa = 0;
//			for (int i=0,l=ka.length,j=-l/2;i<l;j++,i++) wa += (ka[i] = (int)(Math.exp(-.5*j*j/sa/sa)*0xFF));
//			
//			return ka;
//		}
//		
//		public static int weightForKernel(int[] kernel) {
//			int sum = 0;
//			for (int i=0;i<kernel.length;i++)
//				sum+=kernel[i];
//			return sum;
//		}
//		
//		private Gaussian(int[] kernel) {
//			super(kernel,weightForKernel(kernel));
//		}
//		
//		public Gaussian(int radius, double sigma) {
//			this(kernelForGaussian(radius, sigma));			
//		}
//	}
	
	public static SeparableKernel Gaussian(int radius, double... sigma) { return new Gaussian(radius, sigma); }
	public static class Gaussian extends SeparableKernel {
		public static int[] kernelForDoG(int radius, double... sigmas) {
			int ka[] = new int[radius*2+1];
			
			for (int i=0,l=ka.length,j=-l/2;i<l;j++,i++) {
				double sum = 0, jSq = j*j;
				for (int k=0,m=sigmas.length;k<m;k++)
					sum += /*10./(sigmas[k]*Math.sqrt(2*Math.PI))**/Math.exp(-.5*jSq/(sigmas[k]*sigmas[k]));
					
//				wa += (ka[i] = (int)((1/sa*Math.exp(-.5*j*j/sa/sa)+1/so*Math.exp(-.5*j*j/so/so)*0xFF)));
				
				ka[i] = (int)(sum*0xFF);
			}
			
			return ka;
		}
		
		
		public static int weightForKernel(int[] kernel) {
			int sum = 0;
			for (int i=0;i<kernel.length;i++)
				sum+=kernel[i];
			return sum;
		}
		
		private Gaussian(int[] kernel) {
			super(kernel,weightForKernel(kernel));
		}
		
		public Gaussian(int radius, double... sigmas) {
			this(kernelForDoG(radius, sigmas));			
		}
	}
	
	public static final SeparableKernel SOBEL = new SeparableKernel(new int[]{-1,0,1},1); 
	
//	public static class Sobel extends SeparableKernel {
//		public Sobel() {
//			super(new int[]{-1,0,1},1);
//		}
//	}
	
	
}
