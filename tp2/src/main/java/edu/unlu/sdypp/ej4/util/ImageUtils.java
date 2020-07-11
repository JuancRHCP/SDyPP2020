package edu.unlu.sdypp.ej4.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils extends JFrame {

    public static BufferedImage imageIconToBufferedImage(ImageIcon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        // paint the Icon to the BufferedImage.
        icon.paintIcon(null, g, 0,0);
        g.dispose();
        return bi;
    }

    public static List<Image> cropIntoColumns(Image source, int pieces) {
        List<Image> piecesList = new ArrayList<Image>();
        BufferedImage bufferedImage = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(source, 0, 0, null);
        int width = (bufferedImage.getWidth() / pieces);
        int filledWidth = 0;
        for (int i=0; i<pieces; i++) {
            // El ultimo pedazo es el mas largoooo
            if (i == pieces -1)
                width += (bufferedImage.getWidth() % pieces);
            System.out.printf("Piece %d width: %d \n", i, width);
            BufferedImage copy = bufferedImage.getSubimage(filledWidth,0, width, bufferedImage.getHeight());
            filledWidth += width;
            piecesList.add(copy);
        }
        return piecesList;
    }

    /**
     * Esta funcion le agrega una columna de  pixeles extra a la izq y der de cada pedazo. La razon es que el procesamiento posterior que
     * voy a realizar con Sobel lo necesita para calcular el color del pixel. Estas columnas extras son luego removidas por
     * mergeColumnsIntoImage() al unir las partes.
     * @param imageIcon
     * @param pieces cantidad de columnas en las que se va a dividir la imagen
     * @return
     */
    public static List<ImageIcon> cropIntoColumns(ImageIcon imageIcon, int pieces) {
        BufferedImage source = imageIconToBufferedImage(imageIcon);
        List<ImageIcon> piecesList = new ArrayList<ImageIcon>();
        BufferedImage bufferedImage = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(source, 0, 0, null);
        int width = (bufferedImage.getWidth() / pieces);
        int filledWidth = 0;
        for (int i=0; i<pieces; i++) {
            // El ultimo pedazo es el mas largoooo
            if (i == pieces -1)
                width += (bufferedImage.getWidth() % pieces);
            if (i > 0) {
                filledWidth -= 1;
            }
            if (i == 0 || i == pieces-1) {
                width += 1;
            } else {
                width += 2;
            }
            System.out.printf("Piece %d width: %d \n", i, width);
            BufferedImage copy = bufferedImage.getSubimage(filledWidth,0, width, bufferedImage.getHeight());
            if (i == 0 || i == pieces-1) {
                width -= 1;
            } else {
                width -= 2;
            }
            if (i > 0) {
                filledWidth += 1;
            }
            filledWidth += width;
            ImageIcon result = new ImageIcon(copy);
            result.setDescription(imageIcon.getDescription() + " (piece " + i + ")");
            piecesList.add(result);
        }
        return piecesList;
    }

    public static Image mergeColumnsIntoImage(List<Image> pieces){
        int width = 0;
        int height = 0;
        // Sumo los anchos
        for (int i=0; i<pieces.size(); i++) {
            width += pieces.get(i).getWidth(null);
            if (i == 0 || i == pieces.size()-1) {
                width -= 1;
            } else {
                width -= 2;
            }
        }
        // Me quedo con el mas alto
        for (int i=0; i<pieces.size(); i++) {
            height = Math.max(height, pieces.get(i).getHeight(null));
        }

        System.out.printf("width: %d, height: %d\n", width, height);

        BufferedImage fullImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = fullImage.createGraphics();
        int filledWidth = 0; // Ancho ya dibujado
        for (int i=0; i<pieces.size(); i++) {
            BufferedImage bufferedImage;
            bufferedImage = new BufferedImage(pieces.get(i).getWidth(null), pieces.get(i).getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics pieceGraphics = bufferedImage.createGraphics();
            pieceGraphics.drawImage(pieces.get(i), 0,0,pieces.get(i).getWidth(null), pieces.get(i).getHeight(null), null);
            // Recorto los bordes
            if (i == 0) bufferedImage = bufferedImage.getSubimage(0,0,bufferedImage.getWidth()-1, bufferedImage.getHeight());
            if (i == pieces.size()-1) bufferedImage = bufferedImage.getSubimage(1,0,bufferedImage.getWidth()-1,bufferedImage.getHeight());
            if (i != 0 && i != pieces.size()-1) bufferedImage = bufferedImage.getSubimage(1,0,bufferedImage.getWidth()-2,bufferedImage.getHeight());
            g.drawImage(bufferedImage, filledWidth, 0, null);

            int pieceWidth = 0;
            if (i == 0 || i == pieces.size()-1) {
                pieceWidth = pieces.get(i).getWidth(null) -1;
            } else {
                pieceWidth = pieces.get(i).getWidth(null) -2;
            }
            filledWidth += pieceWidth;
        }
        return fullImage;
    }

    public static boolean hasAlpha(Image image1) {
        // If buffered image, the color model is readily available
        if (image1 instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)image1;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image1, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
    Image copy;

    Insets insets;

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
    public void paint(Graphics g) {
        super.paint(g);
        if (insets == null) {
            insets = getInsets();
        }
        g.drawImage(copy, insets.left, insets.top, this);
    }
/*
    public static void main(String args[]) {
        JFrame f = new Crop();
        f.setSize(800,600);
        f.setVisible(true);
    }

 */
}