/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * Abstract base class for image descriptors that synthesize 
 * an image from other images in order to simulate the effect
 * of custom drawing. For example, this could be used to
 * superimpose a red bar dexter symbol across an image to indicate
 * that something was disallowed.
 * <p>
 * Subclasses must implement the <code>getSize</code> and
 * <code>fill</code> methods. Little or no work happens
 * until the image descriptor's image is actually requested
 * by a call to <code>createImage</code> (or to
 * <code>getImageData</code> directly).
 * </p>
 */
public abstract class CompositeImageDescriptor extends ImageDescriptor {

    /**
     * The image data for this composite image.
     */
    private ImageData imageData;

    /**
     * Constructs an uninitialized composite image.
     */
    protected CompositeImageDescriptor() {
    }

    /**
     * Returns the index of a RGB entry in the given map which matches
     * the specified RGB color. If no such entry exists, a new RGB is
     * allocated. If the given array is full, the value 0 is returned
     * (which maps to the transparency value).
     */
    private static int alloc(RGB[] map, int red, int green, int blue) {
        int i;
        RGB c;

        // this loops starts at index 1 because index 0 corresponds to the transparency value
        for (i = 1; i < map.length && (c = map[i]) != null; i++)
            if (c.red == red && c.green == green && c.blue == blue)
                return i;

        if (i < map.length - 1) {
            map[i] = new RGB(red, green, blue);
            return i;
        }
        return 0;
    }

    /**
     * Draw the composite images.
     * <p>
     * Subclasses must implement this framework method
     * to paint images within the given bounds using
     * one or more calls to the <code>drawImage</code>
     * framework method.
     * </p>
     *
     * @param width the width
     * @param height the height
     */
    protected abstract void drawCompositeImage(int width, int height);

    /**
     * Draws the given source image data into this composite
     * image at the given position.
     * <p>
     * Call this internal framework method to superimpose another 
     * image atop this composite image.
     * </p>
     *
     * @param src the source image data
     * @param ox the x position
     * @param oy the y position
     */
    final protected void drawImage(ImageData src, int ox, int oy) {

        RGB[] out = imageData.getRGBs();

        PaletteData palette = src.palette;
        if (palette.isDirect) {

            ImageData mask = src.getTransparencyMask();

            for (int y = 0; y < src.height; y++) {
                for (int x = 0; x < src.width; x++) {
                    if (mask.getPixel(x, y) != 0) {
                        int xx = x + ox;
                        int yy = y + oy;
                        if (xx >= 0 && xx < imageData.width && yy >= 0
                                && yy < imageData.height) {
                            int pixel = src.getPixel(x, y);

                            int r = pixel & palette.redMask;
                            /* JM: Changed operators from >> to >>> to shift sign bit right */
                            r = (palette.redShift < 0) ? r >>> -palette.redShift
                                    : r << palette.redShift;
                            int g = pixel & palette.greenMask;
                            g = (palette.greenShift < 0) ? g >>> -palette.greenShift
                                    : g << palette.greenShift;
                            int b = pixel & palette.blueMask;
                            b = (palette.blueShift < 0) ? b >>> -palette.blueShift
                                    : b << palette.blueShift;

                            pixel = alloc(out, r, g, b);

                            imageData.setPixel(xx, yy, pixel);
                        }
                    }
                }
            }

            return;
        }

        // map maps src pixel values to dest pixel values
        int map[] = new int[256];
        for (int i = 0; i < map.length; i++)
            map[i] = -1;

        /* JM: added code to test if the image is an icon */
        if (src.getTransparencyType() == SWT.TRANSPARENCY_MASK) {
            ImageData mask = src.getTransparencyMask();
            for (int y = 0; y < src.height; y++) {
                for (int x = 0; x < src.width; x++) {
                    if (mask.getPixel(x, y) != 0) {
                        int xx = x + ox;
                        int yy = y + oy;
                        if (xx >= 0 && xx < imageData.width && yy >= 0
                                && yy < imageData.height) {
                            int pixel = src.getPixel(x, y);
                            int newPixel = map[pixel];
                            if (newPixel < 0) {
                                RGB c = palette.getRGB(pixel);
                                map[pixel] = newPixel = alloc(out, c.red,
                                        c.green, c.blue);
                            }

                            imageData.setPixel(xx, yy, newPixel);
                        }
                    }
                }
            }
            return;
        }

        int maskPixel = src.transparentPixel;
        for (int y = 0; y < src.height; y++) {
            for (int x = 0; x < src.width; x++) {
                int pixel = src.getPixel(x, y);
                if (maskPixel < 0 || pixel != maskPixel) {
                    int xx = x + ox;
                    int yy = y + oy;
                    if (xx >= 0 && xx < imageData.width && yy >= 0
                            && yy < imageData.height) {

                        int newPixel = map[pixel];
                        if (newPixel < 0) {
                            RGB c = palette.getRGB(pixel);
                            map[pixel] = newPixel = alloc(out, c.red, c.green,
                                    c.blue);
                        }

                        imageData.setPixel(xx, yy, newPixel);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on ImageDesciptor.
     */
    public ImageData getImageData() {

        Point size = getSize();

        RGB black = new RGB(0, 0, 0);
        RGB[] rgbs = new RGB[256];
        rgbs[0] = black; // transparency
        rgbs[1] = black; // black

        PaletteData dataPalette = new PaletteData(rgbs);
        imageData = new ImageData(size.x, size.y, 8, dataPalette);
        imageData.transparentPixel = 0;

        drawCompositeImage(size.x, size.y);

        for (int i = 0; i < rgbs.length; i++)
            if (rgbs[i] == null)
                rgbs[i] = black;

        return imageData;

    }

    /**
     * Return the size of this composite image.
     * <p>
     * Subclasses must implement this framework method.
     * </p>
     *
     * @return the x and y size of the image expressed as a 
     *   point object
     */
    protected abstract Point getSize();
}