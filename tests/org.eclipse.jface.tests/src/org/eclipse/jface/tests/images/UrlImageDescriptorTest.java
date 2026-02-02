/*******************************************************************************
 * Copyright (c) 2020, 2026 Christoph Läubrich and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UrlImageDescriptorTest {

	@TempDir
	public File tempFolder;

	/**
	 * Test that individually created images of a given descriptor are not equal
	 * (See issue #682).
	 */
	@Test
	public void testDifferentImagesPerUrlImageDescriptor() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/anything.gif"));
		Image image1 = descriptor.createImage();
		assertNotNull(image1, "Could not find first image");
		Image image2 = descriptor.createImage();
		assertNotNull(image2, "Could not find second image");
		assertNotEquals(image1, image2, "Found equal images for URLImageDescriptor");
		image1.dispose();
		image2.dispose();
	}

	@Test
	public void testGetxName() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	@Test
	public void testGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/16x16/zoomIn.png"));
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	@Test
	public void testImageFileNameProviderGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png"));

		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");
		ImageFileNameProvider fileNameProvider2nd = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		// Issue #679: The returned ImageFileNameProvider must be different each time,
		// because Image#equals depends on this non-uniqueness:
		assertNotSame(fileNameProvider, fileNameProvider2nd, "URLImageDescriptor does return identical ImageFileNameProvider");
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull(imagePath100, "URLImageDescriptor ImageFileNameProvider does not return the 100% path");
		assertEquals(IPath.fromOSString(imagePath100).lastSegment(), "rectangular-57x16.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the 200% path");
		assertEquals(IPath.fromOSString(imagePath200).lastSegment(), "rectangular-114x32.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNotNull(imagePath150, "URLImageDescriptor ImageFileNameProvider does not return the 150% path");
		assertEquals(IPath.fromOSString(imagePath150).lastSegment(), "rectangular-86x24.png");
		String imagePath250 = fileNameProvider.getImagePath(250);
		assertNull(imagePath250, "URLImageDescriptor's ImageFileNameProvider does return a 250% path");
	}

	@Test
	public void testImageFileNameProviderGetxName() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));

		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull(imagePath100, "URLImageDescriptor ImageFileNameProvider does not return the 100% path");
		assertEquals(IPath.fromOSString(imagePath100).lastSegment(), "zoomIn.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the @2x path");
		assertEquals(IPath.fromOSString(imagePath200).lastSegment(), "zoomIn@2x.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNull(imagePath150, "URLImageDescriptor's ImageFileNameProvider does return a @1.5x path");
	}

	@Test
	public void testImageFileNameProviderGetxName_forFileURL() throws IOException {
		testImageFileNameProviderGetxName_forFileURL(true);
	}

	@Test
	public void testImageFileNameProviderGetxName_forFileURL_noOSGi() throws IOException {
		testImageFileNameProviderGetxName_forFileURL(false);
	}

	private void testImageFileNameProviderGetxName_forFileURL(boolean osgiAvailable) throws IOException {
		boolean oldOsgiAvailable = InternalPolicy.OSGI_AVAILABLE;
		InternalPolicy.OSGI_AVAILABLE = osgiAvailable;
		try {
			File file = new File(tempFolder, "image.png");
			file.createNewFile();
			URL imageFileURL = file.toURI().toURL();
			new File(tempFolder, "image@2x.png").createNewFile();
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageFileURL);

			ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
			assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");
			String imagePath100 = fileNameProvider.getImagePath(100);
			assertNotNull(imagePath100, "URLImageDescriptor ImageFileNameProvider does not return the 100% path");
			assertEquals(IPath.fromOSString(imagePath100).lastSegment(), "image.png");
			String imagePath200 = fileNameProvider.getImagePath(200);
			assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the @2x path");
			assertEquals(IPath.fromOSString(imagePath200).lastSegment(), "image@2x.png");
			String imagePath150 = fileNameProvider.getImagePath(150);
			assertNull(imagePath150, "URLImageDescriptor's ImageFileNameProvider does return a @1.5x path");
		} finally {
			InternalPolicy.OSGI_AVAILABLE = oldOsgiAvailable;
		}
	}

	@Test
	public void testImageFileNameProviderGetxName_forFileURL_WhiteSpace() throws IOException {
		File imageFolder = new File(tempFolder, "folder with spaces");
		imageFolder.mkdir();
		File imageFile = new File(imageFolder, "image with spaces.png");
		imageFile.createNewFile();

		// This is an invalid URL because the whitespace characters are not properly
		// encoded
		@SuppressWarnings("deprecation")
		URL imageFileURL = new URL("file", null, imageFile.getPath());
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageFileURL);

		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");

		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull(imagePath100, "URLImageDescriptor ImageFileNameProvider does not return the 100% path");
	}

	@Test
	public void testAdaptToURL() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png"));

		URL url = Adapters.adapt(descriptor, URL.class);
		assertNotNull(url, "URLImageDescriptor does not adapt to URL");

		ImageDescriptor descriptorFromUrl = ImageDescriptor.createFromURL(url);

		ImageData imageDataOrig = descriptor.getImageData(100);
		assertNotNull(imageDataOrig, "Original URL does not return 100% image data");

		ImageData imageDataURL = descriptorFromUrl.getImageData(100);
		assertNotNull(imageDataURL, "Adapted URL does not return 100% image data");
		assertEquals(imageDataOrig.width, imageDataURL.width);
		assertEquals(imageDataOrig.height, imageDataURL.height);

		ImageData imageDataOrig200 = descriptor.getImageData(200);
		assertNotNull(imageDataOrig200, "Original URL does not return 200% image data");

		ImageData imageDataURL200 = descriptorFromUrl.getImageData(200);
		assertEquals(imageDataOrig200.width, imageDataURL200.width);
		assertEquals(imageDataOrig200.height, imageDataURL200.height);
	}

}