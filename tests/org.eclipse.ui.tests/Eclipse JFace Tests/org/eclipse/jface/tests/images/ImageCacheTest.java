/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.images;

import junit.framework.TestCase;

import org.eclipse.jface.resource.ImageCache;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.tests.TestPlugin;

/**
 * Test for the image cache. The ImageCacheTest tests for image retrieval as
 * well as image clean up.
 * 
 * 
 * @since 3.1
 */
public class ImageCacheTest extends TestCase {

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
	 * Anything image.
	 */
	private final static String anythingImage = "anything.gif";//$NON-NLS-1$

	/**
	 * Image cache.
	 */
	private static ImageCache imageCache;

	/**
	 * View image.
	 */
	private final static String viewImage = "view.gif";//$NON-NLS-1$

	/**
	 * Timer to wait for cleaner thread to clean up images.
	 */
	private final int timer = 5;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Test name.
	 */
	public ImageCacheTest(String name) {
		super(name);
	}

	/**
	 * Run the garbage collector and wait for the cleaning thread to dispose
	 * images that require cleaning.
	 * 
	 * @param image
	 *            The image to check for disposal.
	 * @return true if the image was disposed, false otherwise.
	 */
	private boolean checkImageCleaning(Image image) {
		if (image.isDisposed()) {
			return true;
		}
		// Give the cleaning thread time to clean up any images
		// that require disposal
		for (int index = 0; index < timer; index++) {
			// Garbage collect
			System.gc();
			System.runFinalization();
			// process events
			Display display = Display.getCurrent();
			if (display != null) {
				while (display.readAndDispatch())
					;
			}
			Thread.yield();
			if (image.isDisposed()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get an image descriptor for the specified image name.
	 * 
	 * @param imageName
	 *            The image name.
	 * @return the newly created image descriptor for the image specified.
	 */
	protected ImageDescriptor getImageDescriptor(String imageName) {
		TestPlugin plugin = TestPlugin.getDefault();
		return plugin.getImageDescriptor(imageName);
	}

	/**
	 * Shut down the image cache right after each test. This is done in this
	 * method instead of the tearDown to prevent locking in the image cache
	 * disposal method.
	 */
	private void prematureTearDown() {
		// dispose all the images
		imageCache.dispose();
		imageCache = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		imageCache = new ImageCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (imageCache != null) {
			imageCache.dispose();
			imageCache = null;
		}
	}

	/**
	 * Ensure that the cleaning thread disposes the images only when both
	 * equivalent descriptors have been nulled.
	 *  
	 */
	public void testCleanUpForEquivalentDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(anythingImage);
		ImageDescriptor imageDescriptor2 = getImageDescriptor(anythingImage);

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertSame(image1, image2);

		imageDescriptor1 = null;
		assertFalse(checkImageCleaning(image1));

		imageDescriptor2 = null;
		assertTrue(checkImageCleaning(image1));

		prematureTearDown();
	}

	/**
	 * Ensure that the cleaning thread disposes the images for different image
	 * descriptors when their respective descriptors have been nulled.
	 *  
	 */
	public void testCleanUpForMultipleEquivalenceSets() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(anythingImage);
		ImageDescriptor imageDescriptor2 = getImageDescriptor(viewImage);

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertNotSame(image1, image2);

		imageDescriptor1 = null;
		imageDescriptor2 = null;

		assertTrue(checkImageCleaning(image1));
		assertTrue(checkImageCleaning(image2));

		prematureTearDown();

	}

	/**
	 * Ensure that the cleaning thread disposes the image when its image
	 * descriptor has been nulled.
	 *  
	 */
	public void testCleanUpForSameDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(anythingImage);

		Image image1 = imageCache.getImage(imageDescriptor);
		Image image2 = imageCache.getImage(imageDescriptor);
		assertSame(image1, image2);

		imageDescriptor = null;
		assertTrue(checkImageCleaning(image1));

		prematureTearDown();

	}

	/**
	 * Test that the image cache properly disposes all of its images.
	 *  
	 */
	public void testDispose() {
		// Store descriptors to avoid gc interference
		ImageDescriptor anythingImageDescriptor = getImageDescriptor(anythingImage);
		ImageDescriptor viewImageDescriptor = getImageDescriptor(viewImage);

		Image image1 = imageCache.getImage(anythingImageDescriptor);
		Image image2 = imageCache.getImage(viewImageDescriptor);
		Image grayImage1 = imageCache.getImage(anythingImageDescriptor,
				ImageCache.GRAY);
		Image grayImage2 = imageCache.getImage(viewImageDescriptor,
				ImageCache.GRAY);
		Image disabledImage1 = imageCache.getImage(anythingImageDescriptor,
				ImageCache.DISABLE);
		Image disabledImage2 = imageCache.getImage(viewImageDescriptor,
				ImageCache.DISABLE);
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

		image1 = imageCache.getImage(anythingImageDescriptor);
		image2 = imageCache.getImage(viewImageDescriptor);
		grayImage1 = imageCache.getImage(anythingImageDescriptor,
				ImageCache.GRAY);
		grayImage2 = imageCache.getImage(viewImageDescriptor, ImageCache.GRAY);
		disabledImage1 = imageCache.getImage(anythingImageDescriptor,
				ImageCache.DISABLE);
		disabledImage2 = imageCache.getImage(viewImageDescriptor,
				ImageCache.DISABLE);
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

		prematureTearDown();
	}

	/**
	 * Test that the cache returns the missing image for a bad image descriptor.
	 *  
	 */
	public void testGetBadImage() {
		BadImageDescriptor badImageDescriptor = new BadImageDescriptor();
		Image missingImage = imageCache.getMissingImage();

		Image badImage = imageCache.getImage(badImageDescriptor,
				ImageCache.DISABLE);
		assertSame(badImage, missingImage);

		badImage = imageCache.getImage(badImageDescriptor, ImageCache.GRAY);
		assertSame(badImage, missingImage);

		badImage = imageCache.getImage(badImageDescriptor);
		assertSame(badImage, missingImage);

		prematureTearDown();
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

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		assertSame(image1, image2);

		prematureTearDown();

	}

	/**
	 * Test retrieving images with null descriptors.
	 *  
	 */
	public void testGetImageForNullValues() {
		Image image = imageCache.getImage(null);
		assertNull(image);

		image = imageCache.getImage(null, ImageCache.DISABLE);
		assertNull(image);

		image = imageCache.getImage(null, ImageCache.GRAY);
		assertNull(image);

		image = imageCache.getImage(null, 4);
		assertNull(image);

		image = imageCache.getImage(getImageDescriptor(anythingImage), 3);
		assertNull(image);

		image = imageCache.getImage(getImageDescriptor(anythingImage), -1);
		assertNull(image);

		prematureTearDown();

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

		image1 = imageCache.getImage(imageDescriptor, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor, ImageCache.DISABLE);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor, ImageCache.GRAY);
		assertSame(image1, image2);

		prematureTearDown();

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

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor2, ImageCache.GRAY);
		assertNotSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor2, ImageCache.DISABLE);
		assertNotSame(image1, image2);

		prematureTearDown();

	}

}
