/*******************************************************************************
 * Copyright (c) 2020, 2022 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *     Daniel Kruegler - #396, #398, #399, #401,
 *                       #682: Add test case to ensure that a descriptor creates different images
 ******************************************************************************/
package org.eclipse.jface.tests.images;

import static org.junit.Assert.assertNotEquals;

import java.net.URL;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;

import junit.framework.TestCase;

public class UrlImageDescriptorTest extends TestCase {

	/**
	 * Test that individually created images of a given descriptor are not equal
	 * (See issue #682).
	 */
	public void testDifferentImagesPerUrlImageDescriptor() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/anything.gif"));
		Image image1 = descriptor.createImage();
		assertNotNull("Could not find first image", image1);
		Image image2 = descriptor.createImage();
		assertNotNull("Could not find second image", image2);
		assertNotEquals("Found equal images for URLImageDescriptor", image1, image2);
		image1.dispose();
		image2.dispose();
	}

	public void testGetxName() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	public void testGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/16x16/zoomIn.png"));
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	public void testImageFileNameProviderGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png"));

		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull("URLImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
		ImageFileNameProvider fileNameProvider2nd = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		// Issue #679: The returned ImageFileNameProvider must be different each time,
		// because Image#equals depends on this non-uniqueness:
		assertNotSame("URLImageDescriptor does return identical ImageFileNameProvider", fileNameProvider,
				fileNameProvider2nd);
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 100% path", imagePath100);
		assertEquals(Path.fromOSString(imagePath100).lastSegment(), "rectangular-57x16.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 200% path", imagePath200);
		assertEquals(Path.fromOSString(imagePath200).lastSegment(), "rectangular-114x32.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 150% path", imagePath150);
		assertEquals(Path.fromOSString(imagePath150).lastSegment(), "rectangular-86x24.png");
		String imagePath250 = fileNameProvider.getImagePath(250);
		assertNull("URLImageDescriptor's ImageFileNameProvider does return a 250% path", imagePath250);
	}

	public void testImageFileNameProviderGetxName() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));

		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull("URLImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 100% path", imagePath100);
		assertEquals(Path.fromOSString(imagePath100).lastSegment(), "zoomIn.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the @2x path", imagePath200);
		assertEquals(Path.fromOSString(imagePath200).lastSegment(), "zoomIn@2x.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNull("URLImageDescriptor's ImageFileNameProvider does return a @1.5x path", imagePath150);
	}

	public void testAdaptToURL() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png"));

		URL url = Adapters.adapt(descriptor, URL.class);
		assertNotNull("URLImageDescriptor does not adapt to URL", url);

		ImageDescriptor descriptorFromUrl = ImageDescriptor.createFromURL(url);

		ImageData imageDataOrig = descriptor.getImageData(100);
		assertNotNull("Original URL does not return 100% image data", imageDataOrig);

		ImageData imageDataURL = descriptorFromUrl.getImageData(100);
		assertNotNull("Adapted URL does not return 100% image data", imageDataURL);
		assertEquals(imageDataOrig.width, imageDataURL.width);
		assertEquals(imageDataOrig.height, imageDataURL.height);

		ImageData imageDataOrig200 = descriptor.getImageData(200);
		assertNotNull("Original URL does not return 200% image data", imageDataOrig200);

		ImageData imageDataURL200 = descriptorFromUrl.getImageData(200);
		assertEquals(imageDataOrig200.width, imageDataURL200.width);
		assertEquals(imageDataOrig200.height, imageDataURL200.height);
	}

}
