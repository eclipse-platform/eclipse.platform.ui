/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import junit.framework.TestCase;

import org.eclipse.jface.resource.ImageCache;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.tests.TestPlugin;

/**
 * Test for the image cache.
 * 
 * 
 * @since 3.1
 */
public class ImageCacheTest extends TestCase {

    //TODO: Find a way to automate the testing of the image
    // cleaning process, even though it is time dependent on when the garbage
    // collector will run.

    /**
     * Image descriptor to mimmic a bad descriptor (where creating an image
     * fails and returns null instead of a missing image).
     */
    private static class BadImageDescriptor extends ImageDescriptor {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.resource.ImageDescriptor#createImage(boolean)
         */
        public Image createImage(boolean returnMissingImageOnError) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
         */
        public ImageData getImageData() {
            return null;
        }

    }

    /**
     * Sample image.
     */
    private final static String anythingImage = "anything.gif";//$NON-NLS-1$

    /**
     * Image cache.
     */
    private static ImageCache imageCache = new ImageCache();

    /**
     * Sample image.
     */
    private final static String viewImage = "view.gif";//$NON-NLS-1$

    /**
     * Image cache test.
     * 
     * @param testName
     *            Test name.
     */
    public ImageCacheTest(String testName) {
        super(testName);
    }

    /**
     * Get a valid image descriptor.
     * 
     * @return the image descriptor.
     */
    private ImageDescriptor getImageDescriptor(String imageName) {
        TestPlugin plugin = TestPlugin.getDefault();
        return plugin.getImageDescriptor(imageName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        imageCache.dispose();
    }

    /**
     * Test that the image cache properly disposes all of its images.
     *  
     */
    public void testDispose() {
        Image image1 = imageCache.getImage(getImageDescriptor(anythingImage));
        Image image2 = imageCache.getImage(getImageDescriptor(viewImage));
        Image grayImage1 = imageCache
                .getGrayImage(getImageDescriptor(anythingImage));
        Image grayImage2 = imageCache
                .getGrayImage(getImageDescriptor(viewImage));
        Image disabledImage1 = imageCache
                .getDisabledImage(getImageDescriptor(anythingImage));
        Image disabledImage2 = imageCache
                .getDisabledImage(getImageDescriptor(viewImage));
        Image missingImage = imageCache.getMissingImage();

        imageCache.dispose();

        assertTrue(image1.isDisposed());
        assertTrue(image2.isDisposed());
        assertTrue(grayImage1.isDisposed());
        assertTrue(grayImage2.isDisposed());
        assertTrue(disabledImage1.isDisposed());
        assertTrue(disabledImage2.isDisposed());
        assertTrue(missingImage.isDisposed());

        // dispose an empty cache
        imageCache.dispose();

        image1 = imageCache.getImage(getImageDescriptor(anythingImage));
        image2 = imageCache.getImage(getImageDescriptor(viewImage));
        grayImage1 = imageCache.getGrayImage(getImageDescriptor(anythingImage));
        grayImage2 = imageCache.getGrayImage(getImageDescriptor(viewImage));
        disabledImage1 = imageCache
                .getDisabledImage(getImageDescriptor(anythingImage));
        disabledImage2 = imageCache
                .getDisabledImage(getImageDescriptor(viewImage));
        missingImage = imageCache.getMissingImage();

        // Manually dispose the images
        image1.dispose();
        image2.dispose();
        grayImage1.dispose();
        grayImage2.dispose();
        disabledImage1.dispose();
        disabledImage2.dispose();
        missingImage.dispose();

        // dispose a cache where the images have already been disposed
        imageCache.dispose();
    }

    /**
     * Test that the cache returns the missing image for a bad image descriptor.
     *  
     */
    public void testGetBadImage() {
        BadImageDescriptor badImageDescriptor = new BadImageDescriptor();
        Image missingImage = imageCache.getMissingImage();

        Image badImage = imageCache.getDisabledImage(badImageDescriptor);
        assertSame(badImage, missingImage);

        badImage = imageCache.getGrayImage(badImageDescriptor);
        assertSame(badImage, missingImage);

        badImage = imageCache.getImage(badImageDescriptor);
        assertSame(badImage, missingImage);
    }

    /**
     * Test retrieving images with equivalent image descriptors. Ensure that the
     * same image is returned in each case.
     */
    public void testGetImageForEquivalentDescriptor() {
        ImageDescriptor imageDescriptor1 = getImageDescriptor(anythingImage);
        ImageDescriptor imageDescriptor2 = getImageDescriptor(anythingImage);

        Image image1 = imageCache.getImage(imageDescriptor1);
        Image image2 = imageCache.getImage(imageDescriptor2);
        assertSame(image1, image2);

        image1 = imageCache.getGrayImage(imageDescriptor1);
        image2 = imageCache.getGrayImage(imageDescriptor1);
        assertSame(image1, image2);

        image1 = imageCache.getDisabledImage(imageDescriptor1);
        image2 = imageCache.getDisabledImage(imageDescriptor1);
        assertSame(image1, image2);

    }

    /**
     * Test retrieving images with null descriptors.
     *  
     */
    public void testGetImageForNullValues() {
        Image image = imageCache.getImage(null);
        assertNull(image);

        image = imageCache.getDisabledImage(null);
        assertNull(image);

        image = imageCache.getGrayImage(null);
        assertNull(image);

    }

    /**
     * Test retrieving images with the same image descriptor. Ensure that the
     * same image is returned in each case.
     */
    public void testGetImageForSameDescriptor() {
        ImageDescriptor imageDescriptor = getImageDescriptor(anythingImage);

        Image image1 = imageCache.getImage(imageDescriptor);
        Image image2 = imageCache.getImage(imageDescriptor);
        assertSame(image1, image2);

        image1 = imageCache.getDisabledImage(imageDescriptor);
        image2 = imageCache.getDisabledImage(imageDescriptor);
        assertSame(image1, image2);

        image1 = imageCache.getGrayImage(imageDescriptor);
        image2 = imageCache.getGrayImage(imageDescriptor);
        assertSame(image1, image2);

    }

    /**
     * Test retrieving multiple non-equivalent images from the image cache.
     *  
     */
    public void testMultipleEquivalenceSets() {
        ImageDescriptor imageDescriptor1 = getImageDescriptor(anythingImage);
        ImageDescriptor imageDescriptor2 = getImageDescriptor(viewImage);

        Image image1 = imageCache.getImage(imageDescriptor1);
        Image image2 = imageCache.getImage(imageDescriptor2);
        assertNotSame(image1, image2);

        image1 = imageCache.getGrayImage(imageDescriptor1);
        image2 = imageCache.getGrayImage(imageDescriptor2);
        assertNotSame(image1, image2);

        image1 = imageCache.getDisabledImage(imageDescriptor1);
        image2 = imageCache.getDisabledImage(imageDescriptor2);
        assertNotSame(image1, image2);

    }

}
