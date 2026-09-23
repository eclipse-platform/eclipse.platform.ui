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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UrlImageDescriptorTest {

	@TempDir
	public Path tempFolder;

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
		assertEquals("rectangular-57x16.png", IPath.fromOSString(imagePath100).lastSegment());
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the 200% path");
		assertEquals("rectangular-114x32.png", IPath.fromOSString(imagePath200).lastSegment());
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNotNull(imagePath150, "URLImageDescriptor ImageFileNameProvider does not return the 150% path");
		assertEquals("rectangular-86x24.png", IPath.fromOSString(imagePath150).lastSegment());
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
		assertEquals("zoomIn.png", IPath.fromOSString(imagePath100).lastSegment());
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the @2x path");
		assertEquals("zoomIn@2x.png", IPath.fromOSString(imagePath200).lastSegment());
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
			Path imagePath = tempFolder.resolve("image.png");
			Files.createFile(imagePath);
			URL imageFileURL = imagePath.toUri().toURL();
			Files.createFile(tempFolder.resolve("image@2x.png"));
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageFileURL);

			ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
			assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");
			String imagePath100 = fileNameProvider.getImagePath(100);
			assertNotNull(imagePath100, "URLImageDescriptor ImageFileNameProvider does not return the 100% path");
			assertEquals("image.png", IPath.fromOSString(imagePath100).lastSegment());
			String imagePath200 = fileNameProvider.getImagePath(200);
			assertNotNull(imagePath200, "URLImageDescriptor ImageFileNameProvider does not return the @2x path");
			assertEquals("image@2x.png", IPath.fromOSString(imagePath200).lastSegment());
			String imagePath150 = fileNameProvider.getImagePath(150);
			assertNull(imagePath150, "URLImageDescriptor's ImageFileNameProvider does return a @1.5x path");
		} finally {
			InternalPolicy.OSGI_AVAILABLE = oldOsgiAvailable;
		}
	}

	@Test
	public void testImageFileNameProviderGetxName_forFileURL_WhiteSpace() throws IOException {
		Path imageFolder = tempFolder.resolve("folder with spaces");
		Files.createDirectories(imageFolder);
		Path imagePath = imageFolder.resolve("image with spaces.png");
		Files.createFile(imagePath);

		// This is an invalid URL because the whitespace characters are not properly
		// encoded
		@SuppressWarnings("deprecation")
		URL imageFileURL = new URL("file", null, imagePath.toString());
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

	@AfterEach
	public void clearURLModifier() {
		ImageDescriptor.setURLModifier(null);
	}

	@Test
	public void testNoURLModifierInstalledByDefault() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));
		assertEquals("zoomIn.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());
	}

	@Test
	public void testURLModifierRedirectsImageData() {
		URL zoomIn = FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png");
		URL rectangular = FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png");
		ImageData original = ImageDescriptor.createFromURL(zoomIn).getImageData(100);
		ImageData replacement = ImageDescriptor.createFromURL(rectangular).getImageData(100);
		assertNotEquals(original.width, replacement.width, "test images are indistinguishable");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(zoomIn);
		ImageDescriptor.setURLModifier(url -> zoomIn.toExternalForm().equals(url.toExternalForm()) ? rectangular : url);

		assertEquals(replacement.width, descriptor.getImageData(100).width);
	}

	@Test
	public void testURLModifierLeavesUnmatchedURLsAlone() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));
		ImageDescriptor.setURLModifier(url -> url);

		assertEquals("zoomIn.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());
	}

	@Test
	public void testURLModifierReturningNullKeepsOriginalURL() {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromURL(FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png"));
		ImageDescriptor.setURLModifier(url -> null);

		assertEquals("zoomIn.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());
	}

	@Test
	public void testURLModifierIsClearedByNull() {
		URL zoomIn = FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png");
		URL rectangular = FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png");
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(zoomIn);

		ImageDescriptor.setURLModifier(url -> rectangular);
		assertEquals("rectangular-57x16.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());

		ImageDescriptor.setURLModifier(null);
		assertEquals("zoomIn.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());
	}

	/**
	 * The @2x variant is derived from the rewritten URL, so an icon pack only
	 * supplies the base name.
	 */
	@Test
	public void testURLModifierIsAppliedBeforeHiDpiLookup() {
		URL rectangular = FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png");
		URL zoomIn = FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png");
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(rectangular);

		ImageDescriptor.setURLModifier(url -> url.getPath().contains("rectangular-57x16") ? zoomIn : url);

		assertEquals("zoomIn.png", IPath.fromOSString(imagePath(descriptor, 100)).lastSegment());
		assertEquals("zoomIn@2x.png", IPath.fromOSString(imagePath(descriptor, 200)).lastSegment());
	}

	/**
	 * The scaled name fallback is derived from the rewritten URL too, so an icon
	 * pack is not mixed with platform icons at higher zoom levels.
	 */
	@Test
	public void testURLModifierIsAppliedBeforeScaledNameLookup() {
		URL zoomIn = FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png");
		URL rectangular = FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png");
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(zoomIn);

		ImageDescriptor.setURLModifier(url -> url.getPath().endsWith("/zoomIn.png") ? rectangular : url);

		assertEquals("rectangular-114x32.png", IPath.fromOSString(imagePath(descriptor, 200)).lastSegment());
		assertEquals("rectangular-86x24.png", IPath.fromOSString(imagePath(descriptor, 150)).lastSegment());
		assertEquals(114, descriptor.getImageData(200).width);
	}

	@Test
	public void testURLModifierIsAppliedToAdaptedURL() {
		URL zoomIn = FileImageDescriptorTest.class.getResource("/icons/imagetests/zoomIn.png");
		URL rectangular = FileImageDescriptorTest.class.getResource("/icons/imagetests/rectangular-57x16.png");
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(zoomIn);

		ImageDescriptor.setURLModifier(url -> rectangular);

		URL adapted = Adapters.adapt(descriptor, URL.class);
		assertNotNull(adapted, "URLImageDescriptor does not adapt to URL");
		assertEquals(rectangular.toExternalForm(), adapted.toExternalForm());
	}

	private static String imagePath(ImageDescriptor descriptor, int zoom) {
		ImageFileNameProvider fileNameProvider = Adapters.adapt(descriptor, ImageFileNameProvider.class);
		assertNotNull(fileNameProvider, "URLImageDescriptor does not adapt to ImageFileNameProvider");
		String path = fileNameProvider.getImagePath(zoom);
		assertNotNull(path, "URLImageDescriptor ImageFileNameProvider does not return the " + zoom + "% path");
		return path;
	}

}
