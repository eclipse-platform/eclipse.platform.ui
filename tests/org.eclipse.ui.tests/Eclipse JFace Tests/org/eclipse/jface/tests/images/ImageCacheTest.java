/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.tests.images;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	 * A list of available image names (icons folder in "org.eclipse.ui.tests")
	 */
	private static class ImageNames {
		/**
		 * Image descriptors.
		 */
		private final static String imageAnything = "anything.gif";//$NON-NLS-1$

		private final static String imageBinaryCo = "binary_co.gif";//$NON-NLS-1$

		private final static String imageMockEditor1 = "mockeditorpart1.gif";//$NON-NLS-1$

		private final static String imageMockEditor2 = "mockeditorpart2.gif";//$NON-NLS-1$

		private final static String imageView = "view.gif";//$NON-NLS-1$

		private final static int numberOfImages = 5;

		/**
		 * Return an image name for the specified index.
		 * 
		 * @param index
		 *            The image's index.
		 * @return the image's name.
		 */
		public static String get(int index) {
			switch (index) {
			case 0:
				return imageAnything;
			case 1:
				return imageBinaryCo;
			case 2:
				return imageMockEditor1;
			case 3:
				return imageMockEditor2;
			default:
				return imageView;
			}
		}

		/**
		 * Return the number of image names defined.
		 * 
		 * @return the number of image names defined.
		 */
		public static int size() {
			return numberOfImages;
		}
	}

	/**
	 * Image cache.
	 */
	private static ImageCache imageCache;

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
	 * Fill the array with regular, gray and disabled images.
	 * 
	 * @param imagesInCache
	 *            The array of images.
	 * @param imageDescriptor0
	 *            The image descriptor 0.
	 * @param imageDescriptor1
	 *            The image descriptor 1.
	 */
	private void fillImageArray(List imagesInCache,
			ImageDescriptor imageDescriptor0, ImageDescriptor imageDescriptor1) {
		// Regular images
		imagesInCache.add(imageCache.getImage(imageDescriptor0));
		imagesInCache.add(imageCache.getImage(imageDescriptor1));
		// Gray images
		imagesInCache.add(imageCache
				.getImage(imageDescriptor0, ImageCache.GRAY));
		imagesInCache.add(imageCache
				.getImage(imageDescriptor1, ImageCache.GRAY));
		// Disabled images
		imagesInCache.add(imageCache.getImage(imageDescriptor0,
				ImageCache.DISABLE));
		imagesInCache.add(imageCache.getImage(imageDescriptor1,
				ImageCache.DISABLE));
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
		imageCache.dispose();
		imageCache = null;

	}

	/**
	 * Ensure that the cleaning thread disposes the images only when both
	 * equivalent descriptors have been nulled.
	 * 
	 */
	public void testCleanUpForEquivalentDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(ImageNames.get(0));
		ImageDescriptor imageDescriptor2 = getImageDescriptor(ImageNames.get(0));

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertSame(image1, image2);

		imageDescriptor1 = null;
		assertFalse(checkImageCleaning(image1));

		imageDescriptor2 = null;
		assertTrue(checkImageCleaning(image1));

	}

	/**
	 * Ensure that the cleaning thread disposes the images for different image
	 * descriptors when their respective descriptors have been cleared.
	 * 
	 */
	public void testCleanUpForMultipleEquivalenceSets() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(ImageNames.get(0));
		ImageDescriptor imageDescriptor2 = getImageDescriptor(ImageNames.get(1));

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertNotSame(image1, image2);

		imageDescriptor1 = null;
		imageDescriptor2 = null;

		assertTrue(checkImageCleaning(image1));
		assertTrue(checkImageCleaning(image2));

	}

	/**
	 * Ensure that the cleaning thread disposes the image when its image
	 * descriptor has been nulled.
	 * 
	 */
	public void testCleanUpForSameDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(ImageNames.get(0));

		Image image1 = imageCache.getImage(imageDescriptor);
		Image image2 = imageCache.getImage(imageDescriptor);
		assertSame(image1, image2);

		imageDescriptor = null;
		assertTrue(checkImageCleaning(image1));

	}

	/**
	 * Test that the image cache properly disposes all of its images.
	 * 
	 */
	public void testDispose() {
		// List of images
		List imagesInCache = new ArrayList();

		// Store descriptors to avoid gc interference
		ImageDescriptor imageDescriptor0 = getImageDescriptor(ImageNames.get(0));
		ImageDescriptor imageDescriptor1 = getImageDescriptor(ImageNames.get(1));

		// Fill the array of images
		fillImageArray(imagesInCache, imageDescriptor0, imageDescriptor1);
		Image missingImage = imageCache.getMissingImage();

		imageCache.dispose();

		for (Iterator i = imagesInCache.iterator(); i.hasNext();) {
			assertTrue(((Image) i.next()).isDisposed());
		}
		assertTrue(missingImage.isDisposed());
		imagesInCache.clear();

		// dispose an empty cache
		imageCache.dispose();

		// Regular images
		fillImageArray(imagesInCache, imageDescriptor0, imageDescriptor1);
		missingImage = imageCache.getMissingImage();

		// Manually dispose the images
		for (Iterator i = imagesInCache.iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		missingImage.dispose();

		// dispose a cache where the images have already been disposed
		imageCache.dispose();

	}

	/**
	 * Test that the image cache properly performs image disposal in the case
	 * where the cleaner thread is processing images to dispose while the image
	 * cache's dispose is invoked. This method will create 15 images.
	 * 
	 */
	public void testDisposeWhileCleaning() {
		List imageDescriptors = new ArrayList();
		List imagesInCache = new ArrayList();

		// Add image descriptors
		for (int index = 0; index < ImageNames.size(); index++) {
			imageDescriptors.add(getImageDescriptor(ImageNames.get(index)));
		}

		// Regular Images
		for (Iterator i = imageDescriptors.iterator(); i.hasNext();) {
			imagesInCache.add(imageCache.getImage((ImageDescriptor) i.next()));
		}

		// Gray images
		for (Iterator i = imageDescriptors.iterator(); i.hasNext();) {
			imagesInCache.add(imageCache.getImage((ImageDescriptor) i.next(),
					ImageCache.GRAY));
		}

		// Disabled images
		for (Iterator i = imageDescriptors.iterator(); i.hasNext();) {
			imagesInCache.add(imageCache.getImage((ImageDescriptor) i.next(),
					ImageCache.DISABLE));
		}

		// Clear all descriptors
		imageDescriptors.clear();

		// For the GC to run
		System.gc();
		System.runFinalization();

		// Dispose while the cleaner thread is cleaning up images
		imageCache.dispose();

		// Ensure the images have all been disposed
		for (Iterator i = imagesInCache.iterator(); i.hasNext();) {
			assertTrue(((Image) i.next()).isDisposed());
		}
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

	}

	/**
	 * Test retrieving images with equivalent image descriptors. Ensure that the
	 * same image is returned in each case.
	 */
	public void testGetImageForEquivalentDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(ImageNames.get(0));
		ImageDescriptor imageDescriptor2 = getImageDescriptor(ImageNames.get(0));

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		assertSame(image1, image2);

	}

	/**
	 * Test retrieving images with null descriptors.
	 * 
	 */
	public void testGetImageForInvalidValues() {
		Image image = imageCache.getImage(null);
		assertNull(image);

		image = imageCache.getImage(null, ImageCache.DISABLE);
		assertNull(image);

		image = imageCache.getImage(null, ImageCache.GRAY);
		assertNull(image);

		image = imageCache.getImage(null, 4);
		assertNull(image);

		image = imageCache.getImage(getImageDescriptor(ImageNames.get(0)), 3);
		assertNull(image);

		image = imageCache.getImage(getImageDescriptor(ImageNames.get(0)), -1);
		assertNull(image);

	}

	/**
	 * Test retrieving images with the same image descriptor. Ensure that the
	 * same image is returned in each case.
	 */
	public void testGetImageForSameDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(ImageNames.get(0));

		Image image1 = imageCache.getImage(imageDescriptor);
		Image image2 = imageCache.getImage(imageDescriptor);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor, ImageCache.DISABLE);
		assertSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor, ImageCache.GRAY);
		assertSame(image1, image2);

	}

	/**
	 * Test retrieving multiple non-equivalent images from the image cache.
	 * 
	 */
	public void testMultipleEquivalenceSets() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(ImageNames.get(0));
		ImageDescriptor imageDescriptor2 = getImageDescriptor(ImageNames.get(1));

		Image image1 = imageCache.getImage(imageDescriptor1);
		Image image2 = imageCache.getImage(imageDescriptor2);
		assertNotSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.GRAY);
		image2 = imageCache.getImage(imageDescriptor2, ImageCache.GRAY);
		assertNotSame(image1, image2);

		image1 = imageCache.getImage(imageDescriptor1, ImageCache.DISABLE);
		image2 = imageCache.getImage(imageDescriptor2, ImageCache.DISABLE);
		assertNotSame(image1, image2);

	}

}
