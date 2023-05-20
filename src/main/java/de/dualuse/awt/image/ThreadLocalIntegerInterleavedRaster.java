package de.dualuse.awt.image;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

//
//@SuppressWarnings("restriction")
//class ThreadLocalIntegerInterleavedRaster extends IntegerInterleavedRaster {
//	private static ThreadLocal<int[]> buffer = new ThreadLocal<int[]>();
//
//	public ThreadLocalIntegerInterleavedRaster(int width, int height, int[] pixels, int offset, int scan, int bands[]) {
//		super(
//			new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, scan, bands),
//			new DataBufferInt(pixels,pixels.length,offset),
//			new Point(0,0)
//		);
//	}
//
//	public void setRect(int dx, int dy, Raster srcRaster) {
//        int width  = srcRaster.getWidth();
//        int height = srcRaster.getHeight();
//        int srcOffX = srcRaster.getMinX();
//        int srcOffY = srcRaster.getMinY();
//        int dstOffX = dx+srcOffX;
//        int dstOffY = dy+srcOffY;
//
//        if (dstOffX < this.minX) {
//            int skipX = this.minX - dstOffX;
//            width -= skipX;
//            srcOffX += skipX;
//            dstOffX = this.minX;
//        }
//        if (dstOffY < this.minY) {
//            int skipY = this.minY - dstOffY;
//            height -= skipY;
//            srcOffY += skipY;
//            dstOffY = this.minY;
//        }
//        if (dstOffX+width > this.minX+this.width) {
//            width = this.minX + this.width - dstOffX;
//        }
//        if (dstOffY+height > this.minY+this.height) {
//            height = this.minY + this.height - dstOffY;
//        }
//
//        if (width <= 0 || height <= 0) {
//            return;
//        }
//
//        int[] iData = buffer.get();
//        if (iData==null || iData.length<width*4)
//        	buffer.set(iData = new int[width*4]);
//
//        for (int startY=0; startY < height; startY++) {
//            iData = srcRaster.getPixels(srcOffX, srcOffY+startY, width, 1, iData);
//            setPixels(dstOffX, dstOffY+startY, width, 1, iData);
//        }
//	}
//
//}