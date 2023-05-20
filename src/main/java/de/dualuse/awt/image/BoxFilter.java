package de.dualuse.awt.image;

public enum BoxFilter {
	HORIZONTAL,
	VERTICAL;
	
	@SuppressWarnings("hiding")
	public<IntBufferedImage extends de.dualuse.awt.image.IntBufferedImage> IntBufferedImage convolve( 
			IntBufferedImage to, int toX, int toY,  
			IntBufferedImage from, int fromX, int fromY, 
			
			int width, int height, 
			int boxRadius, int norm) 
	{
		
		convolve(
				to.data, to.offset+toX+toY*to.scan, to.scan, 
				from.data, from.offset+fromX+fromY*from.scan, from.scan, 
				
				width, height, boxRadius, norm);
		
		return to;
	}
	
	@SuppressWarnings("hiding")
	public<FloatBufferedImage extends de.dualuse.awt.image.FloatBufferedImage> FloatBufferedImage convolve( 
			FloatBufferedImage to, int toX, int toY,  
			FloatBufferedImage from, int fromX, int fromY, 
			
			int width, int height, int boxRadius, int norm) 
	{
		convolve(
				to.data, to.offset+toX+toY*to.scan, to.scan, 
				from.data, from.offset+fromX+fromY*from.scan, from.scan, 
				width, height, boxRadius, norm);
		
		return to;
	}
	
	
	public int[] convolve( 
			final int to[], final int toOffset, final int toScan, 
			final int from[], final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		if (this==HORIZONTAL)
			return horizontal(to, toOffset, toScan, from,fromOffset,fromScan, width,height,  box, norm);
		else
			return vertical(to, toOffset, toScan, from,fromOffset,fromScan, width,height,  box, norm);
		
	}

	public float[] convolve( 
			final float to[], final int toOffset, final int toScan, 
			final float from[], final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		if (this==HORIZONTAL)
			return horizontal(to, toOffset, toScan, from,fromOffset,fromScan, width,height, box, norm);
		else
			return vertical(to, toOffset, toScan, from,fromOffset,fromScan, width,height, box, norm);
		
	}

	
	public static int[] horizontal(
			final int[] to, final int toOffset, final int toScan, 
			final int[] from, final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		int left = box-(fromOffset%fromScan);
		int right = 1+box-(fromScan-(((fromOffset+width-1)%fromScan)+1));

		if (left>0)
			for (int	y=0, o=toOffset, r = toScan-left,   
						O=fromOffset, R = fromScan-left,
						Y = height, X = left;
						y<Y;y++, o+=r, O+=R) {
				
				int run= 0;
				for (int i=-(box-left),P=O+i;i<=box;i++,P++)
					run+=from[P];
				
				for (int x=0;x<X;x++, o++, O++ ) {
					to[o] = run/norm;
					run += from[O+box+1];
				}
			}
		else 
			left = 0;

		if (right>0)
			for (int	y=0, o=toOffset+width-right, r = toScan-right,   
						O=fromOffset+width-right, R = fromScan-right,
						Y = height, X = right;
						y<Y;y++, o+=r, O+=R) {
				
				int run= 0;
				for (int i=-box,P=O+i;i<=box;i++,P++)
					run+=from[P];
				
				for (int x=0;x<X;x++, o++, O++ ) {
					to[o] = run/norm;
					
					run -= from[O-box];
				}
			}
		else
			right = 0;
		
		for (int	y=0, o=toOffset+left, r = toScan-(width-left-right),   
					O=fromOffset+left, R = fromScan-(width-left-right),
					Y = height, X = width-left-right;
					y<Y;y++, o+=r, O+=R) {
			
			int run= 0;
			for (int i=-box,P=O+i;i<=box;i++,P++)
				run+=from[P];
			
			for (int x=0;x<X;x++, o++, O++ ) {
				to[o] = run/norm;
				
				run += from[O+box+1];
				run -= from[O-box];
			}
		}


		return to;
	}
	
	
	
	public static int[] vertical(
			final int[] to, final int toOffset, final int toScan, 
			final int[] from, final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		int top = box-fromOffset/fromScan;
		int bottom = 1+box-(from.length-(fromOffset+height*fromScan))/fromScan;
				
		if (top>0)
			for (int	y=0, o=toOffset, c=toScan, r = 1-toScan*top,   
						O=fromOffset, C=fromScan, R = 1-fromScan*top, B = box*fromScan,
						X = top;
						y<width;y++, o+=r, O+=R) {
	
				int run = 0;
				for (int i= -(box-top), P = O + i*fromScan; i <= box; i++, P+=C)
					run+=from[P];
		
				for (int x = 0; x < X; x++, o+=c, O+=C) {
					to[o] = run/norm;
		
					run += from[O + B + fromScan];
				}
			}
		else
			top = 0;


		if (bottom>0)
			for (int	y=0, o=toOffset+(height-bottom)*toScan, c=toScan, r = 1-toScan*bottom,   
						O=fromOffset+(height-bottom)*fromScan, C=fromScan, R = 1-fromScan*bottom, B = box*fromScan,
						X = bottom;
						y<width;y++, o+=r, O+=R) {
		
				int run = 0;
				for (int i= -box, P = O + i*fromScan; i <= box; i++, P+=C)
					run+=from[P];
		
				for (int x = 0; x < X; x++, o+=c, O+=C) {
					to[o] = run/norm;
		
					run -= from[O - B];
				}
			}
		else
			bottom = 0;
		
		
		for (int	y=0, o=toOffset+top*toScan, c=toScan, r = 1-toScan*(height-top-bottom),   
					O=fromOffset+top*fromScan, C=fromScan, R = 1-fromScan*(height-top-bottom), B = box*fromScan,
					X = height-top-bottom;
					y<width;y++, o+=r, O+=R) {

			int run = 0;
			for (int i= -box, P = O + i*fromScan; i <= box; i++, P+=C)
				run+=from[P];

			for (int x = 0; x < X; x++, o+=c, O+=C) {
				to[o] = run/norm;

				run += from[O + B + fromScan];
				run -= from[O - B];
			}
		}
		
		return to;
	}
	

	
	public static float[] horizontal(
			final float[] to, final int toOffset, final int toScan, 
			final float[] from, final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		int left = box-(fromOffset%fromScan);
		int right = 1+box-(fromScan-(((fromOffset+width-1)%fromScan)+1));
		float ooNorm = 1f/norm;
		
		if (left>0)
			for (int	y=0, o=toOffset, r = toScan-left,   
						O=fromOffset, R = fromScan-left,
						Y = height, X = left;
						y<Y;y++, o+=r, O+=R) {
				
				int run= 0;
				for (int i=-(box-left),P=O+i;i<=box;i++,P++)
					run+=from[P];
				
				for (int x=0;x<X;x++, o++, O++ ) {
					to[o] = run*ooNorm;
					run += from[O+box+1];
				}
			}
		else 
			left = 0;

		if (right>0)
			for (int	y=0, o=toOffset+width-right, r = toScan-right,   
						O=fromOffset+width-right, R = fromScan-right,
						Y = height, X = right;
						y<Y;y++, o+=r, O+=R) {
				
				int run= 0;
				for (int i=-box,P=O+i;i<=box;i++,P++)
					run+=from[P];
				
				for (int x=0;x<X;x++, o++, O++ ) {
					to[o] = run*ooNorm;
					
					run -= from[O-box];
				}
			}
		else
			right = 0;
		
		for (int	y=0, o=toOffset+left, r = toScan-(width-left-right),   
					O=fromOffset+left, R = fromScan-(width-left-right),
					Y = height, X = width-left-right;
					y<Y;y++, o+=r, O+=R) {
			
			int run= 0;
			for (int i=-box,P=O+i;i<=box;i++,P++)
				run+=from[P];
			
			for (int x=0;x<X;x++, o++, O++ ) {
				to[o] = run*ooNorm;
				
				run += from[O+box+1];
				run -= from[O-box];
			}
		}


		return to;
	}
	
	
	
	public static float[] vertical(
			final float[] to, final int toOffset, final int toScan, 
			final float[] from, final int fromOffset, final int fromScan, 
			
			final int width, final int height, final int box, final int norm ) {
		
		int top = box-fromOffset/fromScan;
		int bottom = 1+box-(from.length-(fromOffset+height*fromScan))/fromScan;
		float ooNorm = 1f/norm;
		
		if (top>0)
			for (int	y=0, o=toOffset, c=toScan, r = 1-toScan*top,   
						O=fromOffset, C=fromScan, R = 1-fromScan*top, B = box*fromScan,
						X = top;
						y<width;y++, o+=r, O+=R) {
	
				int run = 0;
				for (int i= -(box-top), P = O + i*fromScan; i <= box; i++, P+=C)
					run+=from[P];
		
				for (int x = 0; x < X; x++, o+=c, O+=C) {
					to[o] = run*ooNorm;
		
					run += from[O + B + fromScan];
				}
			}
		else
			top = 0;


		if (bottom>0)
			for (int	y=0, o=toOffset+(height-bottom)*toScan, c=toScan, r = 1-toScan*bottom,   
						O=fromOffset+(height-bottom)*fromScan, C=fromScan, R = 1-fromScan*bottom, B = box*fromScan,
						X = bottom;
						y<width;y++, o+=r, O+=R) {
		
				int run = 0;
				for (int i= -box, P = O + i*fromScan; i <= box; i++, P+=C)
					run+=from[P];
		
				for (int x = 0; x < X; x++, o+=c, O+=C) {
					to[o] = run*ooNorm;
		
					run -= from[O - B];
				}
			}
		else
			bottom = 0;
		
		
		for (int	y=0, o=toOffset+top*toScan, c=toScan, r = 1-toScan*(height-top-bottom),   
					O=fromOffset+top*fromScan, C=fromScan, R = 1-fromScan*(height-top-bottom), B = box*fromScan,
					X = height-top-bottom;
					y<width;y++, o+=r, O+=R) {

			int run = 0;
			for (int i= -box, P = O + i*fromScan; i <= box; i++, P+=C)
				run+=from[P];

			for (int x = 0; x < X; x++, o+=c, O+=C) {
				to[o] = run*ooNorm;

				run += from[O + B + fromScan];
				run -= from[O - B];
			}
		}
		
		return to;
	}
	
	

//	public static int[] horizontal(
//			final int[] to, final int toOffset, final int toScan, 
//			final int[] from, final int fromOffset, final int fromScan, 
//			
//			final int width, final int height, final int box, final int norm ) {
//		
//		for (int	y=0, o=toOffset, r = toScan-width,   
//					O=fromOffset, R = fromScan-width,
//					Y = height, X = width;
//					y<Y;y++, o+=r, O+=R) {
//			
//			int run= 0;
//			for (int i=-box,P=O+i;i<=box;i++,P++)
//				run+=from[P];
//			
//			for (int x=0;x<X;x++, o++, O++ ) {
//				to[o] = run/norm;
//				
//				run += from[O+box+1];
//				run -= from[O-box];
//			}
//		}
//		
//		return to;
//	}

//	public static float[] horizontal(
//			
//			final float[] to, final int toOffset, final int toScan, 
//			final float[] from, final int fromOffset, final int fromScan, 
//			
//			final int width, final int height, final int box, final int norm ) {
//		
//		for (int	y=0, o=toOffset, r = toScan-width,   
//					O=fromOffset, R = fromScan-width;
//					y<height;y++, o+=r, O+=R) {
//			
//			int run= 0;
//			for (int i=-box,P=O+i;i<=box;i++,P++)
//				run+=from[P];
//			
//			for (int x=0;x<width;x++, o++, O++ ) {
//				to[o] = run/norm;
//				
//				run += from[O+box+1];
//				run -= from[O-box];
//			}
//		}
//		
//		return to;
//	}
//	
//	
//	public static float[] vertical(
//			
//			final float[] to, final int toOffset, final int toScan, 
//			final float[] from, final int fromOffset, final int fromScan, 
//			
//			final int width, final int height, final int box, final int norm ) {
//		
//		for (int	y=0, o=toOffset, c=toScan, r = 1-toScan*height,   
//					O=fromOffset, C=fromScan, R = 1-fromScan*height, B = box*fromScan;
//					y<width;y++, o+=r, O+=R) {
//
//			int run = 0;
//			for (int i= -box, P = O + i*fromScan; i <= box; i++, P+=C)
//				run+=from[P];
//
//			for (int x = 0; x < height; x++, o+=c, O+=C) {
//				to[o] = run/norm;
//
//				run += from[O + B + fromScan];
//				run -= from[O - B];
//			}
//		}
//		
//		return to;
//	}
	
}




