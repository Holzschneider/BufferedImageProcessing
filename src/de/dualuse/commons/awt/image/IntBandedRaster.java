package de.dualuse.commons.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class IntBandedRaster extends WritableRaster {

    int[]         dataOffsets;
    int           scanlineStride;
    int[][]      data;

    private int maxX;
    private int maxY;

    /**
     *  Constructs a IntBandedRaster with the given sampleModel. The
     *  Raster's upper left corner is origin and it is the same
     *  size as the SampleModel.  A dataBuffer large
     *  enough to describe the Raster is automatically created. SampleModel
     *  must be of type BandedSampleModel.
     *  @param sampleModel     The SampleModel that specifies the layout.
     *  @param origin          The Point that specifies the origin.
     */
    public IntBandedRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel,
             sampleModel.createDataBuffer(),
             new Rectangle(origin.x,
                           origin.y,
                           sampleModel.getWidth(),
                           sampleModel.getHeight()),
             origin,
             null);
    }

    /**
     *  Constructs a ByteBanded Raster with the given sampleModel
     *  and DataBuffer. The Raster's upper left corner is origin and
     *  it is the same size as the SampleModel.  The DataBuffer is not
     *  initialized and must be a DataBufferShort compatible with SampleModel.
     *  SampleModel must be of type BandedSampleModel.
     *  @param sampleModel     The SampleModel that specifies the layout.
     *  @param dataBuffer      The DataBufferShort that contains the image data.
     *  @param origin          The Point that specifies the origin.
     */
    public IntBandedRaster(SampleModel sampleModel,
                   DataBuffer dataBuffer,
                   Point origin) {
        this(sampleModel, dataBuffer,
         new Rectangle(origin.x , origin.y,
               sampleModel.getWidth(),
               sampleModel.getHeight()),
         origin, null);
    }

    /**
     *  Constructs a IntBandedRaster with the given sampleModel,
     *  DataBuffer, and parent. DataBuffer must be a DataBufferShort and
     *  SampleModel must be of type BandedSampleModel.
     *  When translated into the base Raster's
     *  coordinate system, aRegion must be contained by the base Raster.
     *  Origin is the coordinate in the new Raster's coordinate system of
     *  the origin of the base Raster.  (The base Raster is the Raster's
     *  ancestor which has no parent.)
     *
     *  Note that this constructor should generally be called by other
     *  constructors or create methods, it should not be used directly.
     *  @param sampleModel     The SampleModel that specifies the layout.
     *  @param dataBuffer      The DataBufferShort that contains the image data.
     *  @param aRegion         The Rectangle that specifies the image area.
     *  @param origin          The Point that specifies the origin.
     *  @param parent          The parent (if any) of this raster.
     */
    public IntBandedRaster(SampleModel sampleModel,
                            DataBuffer dataBuffer,
                            Rectangle aRegion,
                            Point origin,
                            IntBandedRaster parent) {

        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.maxX = minX + width;
        this.maxY = minY + height;

        if (!(dataBuffer instanceof DataBufferInt)) {
           throw new RasterFormatException("IntBandedRaster must have int DataBuffers");
        }
        
        DataBufferInt dbi = (DataBufferInt)dataBuffer;

        if (sampleModel instanceof BandedSampleModel) {
            BandedSampleModel bsm = (BandedSampleModel)sampleModel;
            this.scanlineStride = bsm.getScanlineStride();
            int bankIndices[] = bsm.getBankIndices();
            int bandOffsets[] = bsm.getBandOffsets();
            int dOffsets[] = dbi.getOffsets();
            dataOffsets = new int[bankIndices.length];
            data = new int[bankIndices.length][];
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            for (int i = 0; i < bankIndices.length; i++) {
               data[i] = dbi.getData(bankIndices[i]);
               dataOffsets[i] = dOffsets[bankIndices[i]] +
                   xOffset + yOffset*scanlineStride + bandOffsets[i];
            }
        } else {
            throw new RasterFormatException("IntBandedRasters must have BandedSampleModels");
        }
        verify(false);
    }


    /**
     * Returns a copy of the data offsets array. For each band the data
     * offset is the index into the band's data array, of the first sample
     * of the band.
     */
    public int[] getDataOffsets() {
        return (int[])dataOffsets.clone();
    }

    /**
     * Returns data offset for the specified band.  The data offset
     * is the index into the band's data array
     * in which the first sample of the first scanline is stored.
     * @param The band whose offset is returned.
     */
    public int getDataOffset(int band) {
        return dataOffsets[band];
    }

    /**
     * Returns the scanline stride -- the number of data array elements
     * between a given sample and the sample in the same column
     * of the next row in the same band.
     */
    public int getScanlineStride() {
        return scanlineStride;
    }

    /**
     * Returns the pixel stride, which is always equal to one for
     * a Raster with a BandedSampleModel.
     */
    public int getPixelStride() {
        return 1;
    }

    /**
     * Returns a reference to the entire data array.
     */
    public int[][] getDataStorage() {
        return data;
    }

    /**
     * Returns a reference to the specific band data array.
     */
    public int[] getDataStorage(int band) {
        return data[band];
    }

    /**
     * Returns the data elements for all bands at the specified
     * location.  
     * An ArrayIndexOutOfBounds exception will be thrown at runtime
     * if the pixel coordinate is out of bounds.
     * A ClassCastException will be thrown if the input object is non null
     * and references anything other than an array of transferType.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param outData  An object reference to an array of type defined by
     *                 getTransferType() and length getNumDataElements().
     *                 If null an array of appropriate type and size will be
     *                 allocated.
     * @return         An object reference to an array of type defined by
     *                 getTransferType() with the request pixel data.
     */
    public Object getDataElements(int x, int y, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        
        int outData[];
        if (obj == null) {
            outData = new int[numDataElements];
        } else {
            outData = (int[])obj;
        }
        int off = (y-minY)*scanlineStride + (x-minX);

        for (int band = 0; band < numDataElements; band++) {
            outData[band] = data[band][dataOffsets[band] + off];
        }

        return outData;
    }

    /**
     * Returns an  array  of data elements from the specified
     * rectangular region.
     * An ArrayIndexOutOfBounds exception will be thrown at runtime
     * if the pixel coordinates are out of bounds.
     * A ClassCastException will be thrown if the input object is non null
     * and references anything other than an array of transferType.
     * <pre>
     *       byte[] bandData = (byte[])raster.getDataElement(x, y, w, h, null);
     *       int numDataElements = raster.getNumDataElements();
     *       byte[] pixel = new byte[numDataElements];
     *       // To find a data element at location (x2, y2)
     *       System.arraycopy(bandData, ((y2-y)*w + (x2-x))*numDataElements,
     *                        pixel, 0, numDataElements);
     * </pre>
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param width    Width of the pixel rectangle.
     * @param height   Height of the pixel rectangle.
     * @param outData  An object reference to an array of type defined by
     *                 getTransferType() and length w*h*getNumDataElements().
     *                 If null an array of appropriate type and size will be
     *                 allocated.
     * @return         An object reference to an array of type defined by
     *                 getTransferType() with the request pixel data.
     */
    public Object getDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int outData[];
        if (obj == null) outData = new int[numDataElements*w*h];
        else outData = (int[])obj;
        
        int yoff = (y-minY)*scanlineStride + (x-minX);

        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            int[] bank = data[c];
            int dataOffset = dataOffsets[c];

            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    outData[off] = bank[xoff++];
                    off += numDataElements;
                }
            }
        }

        return outData;
    }

    /**
     * Stores the data elements for all bands at the specified location.
     * An ArrayIndexOutOfBounds exception will be thrown at runtime
     * if the pixel coordinate is out of bounds.
     * A ClassCastException will be thrown if the input object is non null
     * and references anything other than an array of transferType.
     * @param x        The X coordinate of the pixel location.
     * @param y        The Y coordinate of the pixel location.
     * @param inData   An object reference to an array of type defined by
     *                 getTransferType() and length getNumDataElements()
     *                 containing the pixel data to place at x,y.
     */
    public void setDataElements(int x, int y, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x >= this.maxX) || (y >= this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int inData[] = (int[])obj;
        int off = (y-minY)*scanlineStride + (x-minX);
        for (int i = 0; i < numDataElements; i++) {
            data[i][dataOffsets[i] + off] = inData[i];
        }
    }

    /**
     * Stores the Raster data at the specified location.
     * An ArrayIndexOutOfBounds exception will be thrown at runtime
     * if the pixel coordinate is out of bounds.
     * @param x          The X coordinate of the pixel location.
     * @param y          The Y coordinate of the pixel location.
     * @param inRaster   Raster of data to place at x,y location.
     */
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = inRaster.getMinX() + x;
        int dstOffY = inRaster.getMinY() + y;
        int width  = inRaster.getWidth();
        int height = inRaster.getHeight();
        if ((dstOffX < this.minX) || (dstOffY < this.minY) ||
            (dstOffX + width > this.maxX) || (dstOffY + height > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }

        setDataElements(dstOffX, dstOffY, width, height, inRaster);
    }

   /**
     * Stores the Raster data at the specified location.
     * @param dstX The absolute X coordinate of the destination pixel
     * that will receive a copy of the upper-left pixel of the
     * inRaster
     * @param dstY The absolute Y coordinate of the destination pixel
     * that will receive a copy of the upper-left pixel of the
     * inRaster
     * @param width      The number of pixels to store horizontally
     * @param height     The number of pixels to store vertically
     * @param inRaster   Raster of data to place at x,y location.
     */
    private void setDataElements(int dstX, int dstY,
                                 int width, int height,
                                 Raster inRaster) {
        // Assume bounds checking has been performed previously
        if (width <= 0 || height <= 0) {
            return;
        }

        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        Object tdata = null;

//      // REMIND: Do something faster!
//      if (inRaster instanceof IntBandedRaster) {
//      }

        for (int startY=0; startY < height; startY++) {
            // Grab one scanline at a time
            tdata = inRaster.getDataElements(srcOffX, srcOffY+startY, width, 1, tdata);
            setDataElements(dstX, dstY+startY, width, 1, tdata);
        }

    }

    /**
     * Stores an array of data elements into the specified rectangular
     * region.
     * An ArrayIndexOutOfBounds exception will be thrown at runtime
     * if the pixel coordinates are out of bounds.
     * A ClassCastException will be thrown if the input object is non null
     * and references anything other than an array of transferType.
     * The data elements in the
     * data array are assumed to be packed.  That is, a data element
     * for the nth band at location (x2, y2) would be found at:
     * <pre>
     *      inData[((y2-y)*w + (x2-x))*numDataElements + n]
     * </pre>
     * @param x        The X coordinate of the upper left pixel location.
     * @param y        The Y coordinate of the upper left pixel location.
     * @param w        Width of the pixel rectangle.
     * @param h        Height of the pixel rectangle.
     * @param inData   An object reference to an array of type defined by
     *                 getTransferType() and length w*h*getNumDataElements()
     *                 containing the pixel data to place between x,y and
     *                 x+h, y+h.
     */
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if ((x < this.minX) || (y < this.minY) ||
            (x + w > this.maxX) || (y + h > this.maxY)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        byte inData[] = (byte[])obj;
        int yoff = (y-minY)*scanlineStride + (x-minX);

        for (int c = 0; c < numDataElements; c++) {
            int off = c;
            int[] bank = data[c];
            int dataOffset = dataOffsets[c];

            int yoff2 = yoff;
            for (int ystart=0; ystart < h; ystart++, yoff2 += scanlineStride) {
                int xoff = dataOffset + yoff2;
                for (int xstart=0; xstart < w; xstart++) {
                    bank[xoff++] = inData[off];
                    off += numDataElements;
                }
            }
        }

    }

     public WritableRaster createWritableChild (int x, int y,
                                               int width, int height,
                                               int x0, int y0,
                                               int bandList[]) {

        if (x < this.minX) {
            throw new RasterFormatException("x lies outside raster");
        }
        if (y < this.minY) {
            throw new RasterFormatException("y lies outside raster");
        }
        if ((x+width < x) || (x+width > this.width + this.minX)) {
            throw new RasterFormatException("(x + width) is outside raster") ;
        }
        if ((y+height < y) || (y+height > this.height + this.minY)) {
            throw new RasterFormatException("(y + height) is outside raster");
        }

//        SampleModel sm;
//
//        if (bandList != null)
//            sm = sampleModel.createSubsetSampleModel(bandList);
//        else
//            sm = sampleModel;
//
//        int deltaX = x0 - x;
//        int deltaY = y0 - y;

//        return new FloatBandedRaster(sm,
//                                    dataBuffer,
//                                    new Rectangle(x0,y0,width,height),
//                                    new Point(sampleModelTranslateX+deltaX,
//                                              sampleModelTranslateY+deltaY),
//                                    this);
        
        return null;
    }

    /**
     * Creates a subraster given a region of the raster.  The x and y
     * coordinates specify the horizontal and vertical offsets
     * from the upper-left corner of this raster to the upper-left corner
     * of the subraster.  A subset of the bands of the parent Raster may
     * be specified.  If this is null, then all the bands are present in the
     * subRaster. A translation to the subRaster may also be specified.
     * Note that the subraster will reference the same
     * DataBuffers as the parent raster, but using different offsets.
     * @param x               X offset.
     * @param y               Y offset.
     * @param width           Width (in pixels) of the subraster.
     * @param height          Height (in pixels) of the subraster.
     * @param x0              Translated X origin of the subraster.
     * @param y0              Translated Y origin of the subraster.
     * @param bandList        Array of band indices.
     * @exception RasterFormatException
     *            if the specified bounding box is outside of the parent raster.
     */
    public Raster createChild (int x, int y,
                                   int width, int height,
                                   int x0, int y0,
                                   int bandList[]) {
        return createWritableChild(x, y, width, height, x0, y0, bandList);
    }

    /**
     * Creates a Raster with the same layout but using a different
     * width and height, and with new zeroed data arrays.
     */
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <=0) {
            throw new RasterFormatException("negative "+
                                          ((w <= 0) ? "width" : "height"));
        }

        SampleModel sm = sampleModel.createCompatibleSampleModel(w,h);

        return new IntBandedRaster(sm, new Point(0,0));
    }

    /**
     * Creates a Raster with the same layout and the same
     * width and height, and with new zeroed data arrays.  If
     * the Raster is a subRaster, this will call
     * createCompatibleRaster(width, height).
     */
    public WritableRaster createCompatibleWritableRaster() {
        return createCompatibleWritableRaster(width, height);
    }

    /**
     * Verify that the layout parameters are consistent with
     * the data.  If strictCheck
     * is false, this method will check for ArrayIndexOutOfBounds conditions.  If
     * strictCheck is true, this method will check for additional error
     * conditions such as line wraparound (width of a line greater than
     * the scanline stride).
     * @return   String   Error string, if the layout is incompatible with
     *                    the data.  Otherwise returns null.
     */
    private void verify (boolean strictCheck) {
        // Make sure data for Raster is in a legal range
        for (int i=0; i < dataOffsets.length; i++) {
            if (dataOffsets[i] < 0) {
                throw new RasterFormatException("Data offsets for band "+i+
                                                "("+dataOffsets[i]+
                                                ") must be >= 0");
            }
        }

        int maxSize = 0;
        int size;

        for (int i=0; i < numDataElements; i++) {
            size = (height-1)*scanlineStride + (width-1) + dataOffsets[i];
            if (size > maxSize) {
                maxSize = size;
            }
        }

        if (data.length == 1) {
            if (data[0].length < maxSize*numDataElements) {
                throw new RasterFormatException("Data array too small "+
                                                "(it is "+data[0].length+
                                                " and should be "+
                                                (maxSize*numDataElements)+
                                                " )");
            }
        }
        else {
            for (int i=0; i < numDataElements; i++) {
                if (data[i].length < maxSize) {
                    throw new RasterFormatException("Data array too small "+
                                                    "(it is "+data[i].length+
                                                    " and should be "+
                                                    maxSize+" )");
                }
            }
        }
    }

    public String toString() {
        return new String ("IntBandedRaster: width = "+width+" height = "
                           + height
                           +" #bands "+numDataElements
                           +" minX = "+minX+" minY = "+minY);
    }

}