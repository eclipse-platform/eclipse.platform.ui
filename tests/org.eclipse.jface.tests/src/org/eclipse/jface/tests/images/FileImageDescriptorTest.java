/*******************************************************************************
 * Copyright (c) 2008, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Karsten Stoeckmann <ngc2997@gmx.net> - Test case for Bug 220766
 *     		[JFace] ImageRegistry.get does not work as expected (crashes with NullPointerException)
 *     Christoph Läubrich - Bug 567898 - [JFace][HiDPI] ImageDescriptor support alternative naming scheme for high dpi
 *     Daniel Kruegler - #375, #378, #396, #398, #399, #401,
 *                       #682: Add test case to ensure that a descriptor creates different images
 ******************************************************************************/

package org.eclipse.jface.tests.images;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Test loading a directory full of images.
 *
 * @since 3.4
 */
public class FileImageDescriptorTest {

	protected static final String IMAGES_DIRECTORY = "/icons/imagetests";

	/**
	 * Test loading the image descriptors.
	 */
	@Test
	public void testFileImageDescriptorWorkbench() {

		Class<?> missing = null;
		ArrayList<Image> images = new ArrayList<>();

		Bundle bundle = FrameworkUtil.getBundle(FileImageDescriptorTest.class);
		Enumeration<String> bundleEntries = bundle.getEntryPaths(IMAGES_DIRECTORY);

		while (bundleEntries.hasMoreElements()) {
			ImageDescriptor descriptor;
			String localImagePath = bundleEntries.nextElement();
			URL[] files = FileLocator.findEntries(bundle, IPath.fromOSString(localImagePath));

			for (URL file : files) {

				// Skip any subdirectories added by version control
				if (file.getPath().lastIndexOf('.') < 0) {
					continue;
				}

				try {
					descriptor = ImageDescriptor.createFromFile(missing, FileLocator.toFileURL(file).getFile());
				} catch (IOException e) {
					fail(e.getLocalizedMessage());
					continue;
				}

				Image image = descriptor.createImage();
				images.add(image);

			}

		}

		for (Image image : images) {
			image.dispose();
		}

	}

	/**
	 * Test the file image descriptor.
	 */
	@Test
	public void testFileImageDescriptorLocal() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "anything.gif");

		Image image = descriptor.createImage();
		assertNotNull("Could not find image", image);
		image.dispose();

	}

	/**
	 * Test for a missing file image descriptor.
	 */
	@Test
	public void testFileImageDescriptorMissing() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "missing.gif");

		Image image = descriptor.createImage(false);
		assertNull("Found an image but should be null", image);
	}

	/**
	 * Test for a missing file image descriptor.
	 */
	@Test
	public void testFileImageDescriptorMissingWithDefault() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "missing.gif");

		Image image = descriptor.createImage(true);
		assertNotNull("Did not find default image", image);
	}

	/**
	 * Test that individually created images of a given descriptor are not equal
	 * (See issue #682).
	 */
	@Test
	public void testDifferentImagesPerFileImageDescriptor() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "anything.gif");
		Image image1 = descriptor.createImage();
		assertNotNull("Could not find first image", image1);
		Image image2 = descriptor.createImage();
		assertNotNull("Could not find second image", image2);
		assertNotEquals("Found equal images for FileImageDescriptor", image1, image2);
		image1.dispose();
		image2.dispose();
	}

	@Test
	public void testGetxName() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/zoomIn.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	@Test
	public void testGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/16x16/zoomIn.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	@Test
	public void testGetxPathRectangular() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/rectangular-57x16.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
		assertEquals(imageData.height * 2, imageDataZoomed.height);
	}

	@Test
	public void testGetxPath150() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/rectangular-57x16.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(150);
		assertNotNull(imageDataZoomed);
		assertEquals(Math.round(imageData.width * 1.5), imageDataZoomed.width);
		assertEquals(Math.round(imageData.height * 1.5), imageDataZoomed.height);
	}

	@Test
	public void testImageFileNameProviderGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/rectangular-57x16.png");
		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull("FileImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
		ImageFileNameProvider fileNameProvider2nd = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		// Issue #679: The returned ImageFileNameProvider must be different each time,
		// because Image#equals depends on this non-uniqueness:
		assertNotSame("FileImageDescriptor does return identical ImageFileNameProvider", fileNameProvider,
				fileNameProvider2nd);
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull("FileImageDescriptor's ImageFileNameProvider does not return the 100% path", imagePath100);
		assertEquals(IPath.fromOSString(imagePath100).lastSegment(), "rectangular-57x16.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull("FileImageDescriptor's ImageFileNameProvider does not return the 200% path", imagePath200);
		assertEquals(IPath.fromOSString(imagePath200).lastSegment(), "rectangular-114x32.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNotNull("FileImageDescriptor's ImageFileNameProvider does not return the 150% path", imagePath150);
		assertEquals(IPath.fromOSString(imagePath150).lastSegment(), "rectangular-86x24.png");
		String imagePath250 = fileNameProvider.getImagePath(250);
		assertNull("FileImageDescriptor's ImageFileNameProvider does return a 250% path", imagePath250);
	}

	@Test
	public void testImageFileNameProviderGetxName() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/zoomIn.png");
		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull("FileImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull("FileImageDescriptor's ImageFileNameProvider does not return the 100% path", imagePath100);
		assertEquals(IPath.fromOSString(imagePath100).lastSegment(), "zoomIn.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull("FileImageDescriptor's ImageFileNameProvider does not return the @2x path", imagePath200);
		assertEquals(IPath.fromOSString(imagePath200).lastSegment(), "zoomIn@2x.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNull("FileImageDescriptor's ImageFileNameProvider does return a @1.5x path", imagePath150);
	}

	@Test
	public void testAdaptToURL() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/rectangular-57x16.png");

		URL url = Adapters.adapt(descriptor, URL.class);
		assertNotNull("FileImageDescriptor does not adapt to URL", url);

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
