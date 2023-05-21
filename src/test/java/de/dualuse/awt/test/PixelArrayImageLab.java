package de.dualuse.awt.test;

import de.dualuse.awt.image.LumaArrayImage;
import de.dualuse.awt.image.PixelArrayImage;
import de.dualuse.awt.test.util.JViewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

public class PixelArrayImageLab {

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        InputStream source = PixelArrayImage.class.getResourceAsStream("IMG_3697.jpg");

        BufferedImage bi = ImageIO.read(source);
//        int width = bi.getWidth()/2, height = bi.getHeight()/2;
        int width = bi.getWidth(), height = bi.getHeight();

        BufferedImage bj = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        BufferedImage bk = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);


        int[] pixels = bi.getRGB(0,0,width,height,new int[width*height],0,width);
        bj.setRGB(0,0,width,height, pixels,0,width);


//        PixelArrayImage pai = new PixelArrayImage(bi.getWidth(),bi.getHeight(), pixels, 0, bi.getWidth(), PixelArrayImage.Format.RGB);
//        Image pai = new BufferedImage(bj.getColorModel(),bj.getRaster(),false,new Hashtable<>());
//        System.out.println(bj.getRaster());
//
        ColorModel modelBGR = new DirectColorModel(24,
                0x000000ff,   // Red
                0x0000ff00,   // Green
                0x00ff0000    // Blue
        );

        int[] masksBGR = {255, 65280, 16711680};

        ColorModel modelRGB = new DirectColorModel(24,
                0x00ff0000,   // Red
                0x0000ff00,   // Green
                0x000000ff,   // Blue
                0x0           // Alpha
        );
        int[] masksRGB = {16711680, 65280, 255};

        DataBufferInt data = new DataBufferInt(width*height);
        WritableRaster rasterRGB = Raster.createPackedRaster(data,width,height,width,masksRGB,null);
        WritableRaster rasterBGR = Raster.createPackedRaster(data,width,height,width,masksBGR,null);

        int[] rgb = data.getData();

//        Field f = DataBufferInt.class.getDeclaredField("data");
//        f.setAccessible(true);
//        int[] rgb = (int[])f.get(data);
//
//        Field f = DataBufferInt.class.getDeclaredField("data");
//        f.setAccessible(true);
//        int[] rgb = (int[])f.get(data);
//
//        Field g = DataBuffer.class.getDeclaredField("theTrackable");
//        g.setAccessible(true);
//        Object theTrackable = g.get(data);
//
//        Method m = theTrackable.getClass().getDeclaredMethod("markDirty");
//        m.invoke(theTrackable);

//        BufferedImage pai = new BufferedImage(modelBGR,rasterBGR,false,new Hashtable<>());
        BufferedImage pai = new BufferedImage(modelRGB,rasterRGB,false,new Hashtable<>());
        pai.setRGB(0,0,width,height, pixels,0,width);


        LumaArrayImage lai = new LumaArrayImage(width,height);

        long start = System.nanoTime();
//        lai.set(new PixelArrayImage(width,height,pixels,0,width, PixelArrayImage.Format.RGB));
        lai.setRGB(0,0,width,height,pixels,0,width);
        long end = System.nanoTime();
        System.out.println((end-start)/1e9+"s");

        LumaArrayImage iai = new LumaArrayImage(width,height);
        iai.set(new PixelArrayImage(width,height,pixels,0,width, PixelArrayImage.Format.RGB));

        JViewer v = new JViewer(new ImageIcon(lai));
        v.setVisible(true);

        v.viewer.addMouseListener(new MouseAdapter() {
            int counter =0 ;
            @Override
            public void mouseClicked(MouseEvent e) {
                switch ((counter+=(e.getButton()==MouseEvent.BUTTON1?+1:-1))%3) {
                    case 1: v.figure.setImage(pai); break;
                    case 2: v.figure.setImage(lai); break;
                    case 0: v.figure.setImage(iai); break;
                }
                v.repaint();


//                if (e.getClickCount()==2) {
//                    for (int y=0,o=0;y<height;y++)
//                        for (int x=0;x<width;x++,o++)
//                            if ((x&y)==0)
//                                rgb[o] = 0xFF000000;
//
//                    System.out.println("höhö");
////                    try {
////                        m.invoke(theTrackable);
////                    } catch (IllegalAccessException ex) {
////                        throw new RuntimeException(ex);
////                    } catch (InvocationTargetException ex) {
////                        throw new RuntimeException(ex);
////                    }
//
//                    v.repaint();
//
//                }
            }
        });


//        IntArrayImage i = new IntArrayImage()

        System.out.println(System.getProperty("java.version"));
    }
}
