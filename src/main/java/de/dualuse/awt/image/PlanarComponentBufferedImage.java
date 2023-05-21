package de.dualuse.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

abstract class PlanarComponentBufferedImage extends CustomBufferedImage {

    public PlanarComponentBufferedImage(int width, int height, int offset, int scan, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied) {
        super(width, height, offset, scan, cm, raster, isRasterPremultiplied);
    }


}
