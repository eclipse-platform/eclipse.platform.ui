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
import org.eclipse.jface.resource.ImageCache.ImageCacheValue;
import org.eclipse.swt.graphics.Image;

/**
 * Test for the image cache.
 * 
 * @since 3.1
 */
public class ImageCacheTest extends TestCase {

	private static ImageCache imageCache = new ImageCache();

	private static final String imageName = "anything.gif";

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
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
	 * Image cache test.
	 * 
	 * @param testName
	 *            Test name.
	 */
	public ImageCacheTest(String testName) {
		super(testName);
	}

	/**
	 * Get the image descriptor for the specified image name.
	 * 
	 * @param imageName
	 *            Image name.
	 * @return the image descriptor.
	 */
	private ImageDescriptor getImageDescriptor(String imageName) {

		return ImageDescriptor.createFromFile(ImageCacheTest.class, "images/"
				+ imageName);
	}

	/**
	 * Test that the image cache properly disposes images.
	 *  
	 */
	public void testDispose() {
		Image normalImage = imageCache.getImage(getImageDescriptor(imageName))
				.getImage();
		Image grayImage = imageCache
				.getGrayImage(getImageDescriptor(imageName)).getImage();
		Image disabledImage = imageCache.getDisabledImage(
				getImageDescriptor(imageName)).getImage();
		
		imageCache.dispose();

		assertTrue(normalImage.isDisposed());
		assertTrue(grayImage.isDisposed());
		assertTrue(disabledImage.isDisposed());
	}

	/**
	 * Test bad image descriptors.
	 *  
	 */
	public void testGetBadImageDescriptor() {
		ImageDescriptor badImageDescriptor = getImageDescriptor("bad.gif");
		Image badImage = imageCache.getImage(badImageDescriptor, false)
				.getImage();
		assertNull(badImage);
		badImage = imageCache.getImage(badImageDescriptor, true).getImage();
		assertNotNull(badImage);
	}

	/**
	 * Test null values passed to the image cache.
	 *  
	 */
	public void testGetNullDescriptors() {
		assertNull(imageCache.getImage(null));
		assertNull(imageCache.getGrayImage(null));
		assertNull(imageCache.getDisabledImage(null));
		assertNull(imageCache.getDisabledImage(null, null));
		assertNull(imageCache.getGrayImage(null, null));
		assertNull(imageCache.getImage(null, null));

	}

	/**
	 * Test get missing image.
	 *  
	 */
	public void testGetMissingImage() {
		Image missingImage1 = imageCache.getMissingImage();
		Image missingImage2 = imageCache.getMissingImage();
		assertSame(missingImage1, missingImage2);
	}

	/**
	 * Test retrieving a normal image with "equal" image descriptors. Ensure
	 * that the same image cache value (same image and image descriptor objects)
	 * is returned in both cases.
	 */
	public void testGetNormalImageForSameDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(imageName);
		ImageDescriptor imageDescriptor2 = getImageDescriptor(imageName);

		ImageCacheValue imageCacheValue1 = imageCache
				.getImage(imageDescriptor1);
		ImageCacheValue imageCacheValue2 = imageCache
				.getImage(imageDescriptor2);

		Image image1 = imageCacheValue1.getImage();
		Image image2 = imageCacheValue2.getImage();

		assertSame(imageCacheValue1.getImage(), imageCacheValue2.getImage());
		assertSame(imageCacheValue1.getImageDescriptor(), imageCacheValue2
				.getImageDescriptor());
	}

	/**
	 * Test retrieving a disabled image with "equal" image descriptors. Ensure
	 * that the same image cache value (same image and image descriptor objects)
	 * is returned in both cases.
	 */
	public void testGetDisabledImageForSameDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(imageName);
		ImageDescriptor imageDescriptor2 = getImageDescriptor(imageName);

		ImageCacheValue imageCacheValue1 = imageCache
				.getDisabledImage(imageDescriptor1);
		ImageCacheValue imageCacheValue2 = imageCache
				.getDisabledImage(imageDescriptor2);

		Image image1 = imageCache.getDisabledImage(imageDescriptor1).getImage();
		Image image2 = imageCache.getDisabledImage(imageDescriptor2).getImage();

		assertSame(imageCacheValue1.getImage(), imageCacheValue2.getImage());
		assertSame(imageCacheValue1.getImageDescriptor(), imageCacheValue2
				.getImageDescriptor());

	}

	/**
	 * Test retrieving a gray image with "equal" image descriptors. Ensure that
	 * the same image cache value (same image and image descriptor objects) is
	 * returned in both cases.
	 */
	public void testGetGrayedImageForSameDescriptor() {
		ImageDescriptor imageDescriptor1 = getImageDescriptor(imageName);
		ImageDescriptor imageDescriptor2 = getImageDescriptor(imageName);

		ImageCacheValue imageCacheValue1 = imageCache
				.getGrayImage(imageDescriptor1);
		ImageCacheValue imageCacheValue2 = imageCache
				.getGrayImage(imageDescriptor2);

		assertSame(imageCacheValue1.getImage(), imageCacheValue2.getImage());
		assertSame(imageCacheValue1.getImageDescriptor(), imageCacheValue2
				.getImageDescriptor());

	}

	/**
	 * Test retrieving "equal" normal images for the same image descriptor.
	 *  
	 */
	public void testGetUniqueImageForDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(imageName);

		Image image1 = imageCache.getImage(imageDescriptor).getImage();
		Image image2 = imageCache.getImage(imageDescriptor).getImage();

		assertSame(image1, image2);

	}

	/**
	 * Test retrieving "equal" disabled images for the same image descriptor.
	 *  
	 */
	public void testGetUniqueDisabledImageForDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(imageName);

		Image image1 = imageCache.getDisabledImage(imageDescriptor).getImage();
		Image image2 = imageCache.getDisabledImage(imageDescriptor).getImage();

		assertSame(image1, image2);

	}

	/**
	 * Test retrieving "equal" gray images for the same image descriptor.
	 *  
	 */
	public void testGetUniqueGrayedImageForDescriptor() {
		ImageDescriptor imageDescriptor = getImageDescriptor(imageName);

		Image image1 = imageCache.getGrayImage(imageDescriptor).getImage();
		Image image2 = imageCache.getGrayImage(imageDescriptor).getImage();

		assertSame(image1, image2);

	}

}
