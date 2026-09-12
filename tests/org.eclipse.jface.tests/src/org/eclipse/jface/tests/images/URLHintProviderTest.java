/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
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
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for URLHintProvider functionality that detects desired image sizes from
 * URL paths and query parameters.
 *
 * <p>
 * Note: SVG size hint support requires SWT 3.132+ with native SVG loading
 * capabilities. Tests verify that hints are detected and passed to the loader,
 * but actual rendering depends on platform SVG support.
 * </p>
 */
public class URLHintProviderTest {

	private Display display;

	@BeforeEach
	public void setUp() {
		display = Display.getDefault();
	}

	@AfterEach
	public void tearDown() {
		// Display is shared, don't dispose
	}

	@Test
	public void testPathBasedHintDetection16x16() throws Exception {
		URL url = URLHintProviderTest.class.getResource("/icons/imagetests/16x16/test-icon.svg");
		assertNotNull(url, "Test SVG not found");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		// If SVG size hints are supported, should be 16x16, otherwise native 128x128
		assertEquals(16, imageData.width, "Width should be 16 based on path hint");
		assertEquals(16, imageData.height, "Height should be 16 based on path hint");
	}

	@Test
	public void testPathBasedHintDetection32x32() throws Exception {
		URL url = URLHintProviderTest.class.getResource("/icons/imagetests/32x32/test-icon.svg");
		assertNotNull(url, "Test SVG not found");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(32, imageData.width, "Width should be 32 based on path hint");
		assertEquals(32, imageData.height, "Height should be 32 based on path hint");
	}

	@Test
	public void testPathBasedHintDetectionZoom200() throws Exception {
		URL url = URLHintProviderTest.class.getResource("/icons/imagetests/16x16/test-icon.svg");
		assertNotNull(url, "Test SVG not found");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(200);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(32, imageData.width, "Width should be 32 (16*2) at 200% zoom");
		assertEquals(32, imageData.height, "Height should be 32 (16*2) at 200% zoom");
	}

	@Test
	public void testPathBasedHintDetectionZoom150() throws Exception {
		URL url = URLHintProviderTest.class.getResource("/icons/imagetests/16x16/test-icon.svg");
		assertNotNull(url, "Test SVG not found");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(150);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(24, imageData.width, "Width should be 24 (16*1.5) at 150% zoom");
		assertEquals(24, imageData.height, "Height should be 24 (16*1.5) at 150% zoom");
	}

	@Test
	public void testQueryParameterHintDetection() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		// Create URL with query parameter - using jar: protocol from class loader
		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=16x16").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(16, imageData.width, "Width should be 16 based on query parameter");
		assertEquals(16, imageData.height, "Height should be 16 based on query parameter");
	}

	@Test
	public void testQueryParameterHintDetection64x64() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=64x64").toURL();
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(64, imageData.width, "Width should be 64 based on query parameter");
		assertEquals(64, imageData.height, "Height should be 64 based on query parameter");
	}

	@Test
	public void testQueryParameterWithZoom200() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=16x16").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(200);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(32, imageData.width, "Width should be 32 (16*2) at 200% zoom");
		assertEquals(32, imageData.height, "Height should be 32 (16*2) at 200% zoom");
	}

	@Test
	public void testQueryParameterRectangularSize() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=48x32").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(48, imageData.width, "Width should be 48 based on query parameter");
		assertEquals(32, imageData.height, "Height should be 32 based on query parameter");
	}

	@Test
	public void testNoHintDefaultSize() throws Exception {
		URL url = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(url, "Test SVG not found");

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		// Without hint, SVG should render at its native viewBox size (128x128)
		assertEquals(128, imageData.width, "Width should be 128 (native SVG size)");
		assertEquals(128, imageData.height, "Height should be 128 (native SVG size)");
	}

	@Test
	public void testQueryParameterPrecedenceOverPath() throws Exception {
		// Query parameter should take precedence over path hint
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/16x16/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=64x64").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(64, imageData.width, "Width should be 64 from query parameter, not 16 from path");
		assertEquals(64, imageData.height, "Height should be 64 from query parameter, not 16 from path");
	}

	@Test
	public void testQueryParameterWithMultipleParams() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		String urlString = baseUrl.toExternalForm();
		// Test with multiple query parameters
		URL url = URI.create(urlString + "?foo=bar&size=24x24&other=value").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		ImageData imageData = descriptor.getImageData(100);

		assertNotNull(imageData, "ImageData should not be null");
		assertEquals(24, imageData.width, "Width should be 24 from query parameter");
		assertEquals(24, imageData.height, "Height should be 24 from query parameter");
	}

	/**
	 * Tests that file: URLs can have query parameters for size hints. This is
	 * important because file URLs need special handling - the query parameter
	 * should be used for size detection but stripped when accessing the actual
	 * file.
	 */
	@Test
	public void testFileURLWithQueryParameter() throws IOException {
		// Copy test SVG to a temporary file
		URL resourceUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(resourceUrl, "Test SVG not found");

		File tempSvg = File.createTempFile("test-icon", ".svg");
		try (InputStream openStream = resourceUrl.openStream()) {
			Files.copy(openStream, tempSvg.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Create file: URL with query parameter
			URL fileUrl = tempSvg.toURI().toURL();
			String fileUrlWithQuery = fileUrl.toExternalForm() + "?size=16x16";
			URL fileUrlQuery = URI.create(fileUrlWithQuery).toURL();

			// Verify URL has file protocol and query parameter
			assertEquals("file", fileUrlQuery.getProtocol(), "Should be a file URL");
			assertEquals("size=16x16", fileUrlQuery.getQuery(), "Query parameter should be preserved");

			// Test that ImageDescriptor can load the image with query parameter
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(fileUrlQuery);
			ImageData imageData = descriptor.getImageData(100);

			assertNotNull(imageData, "ImageData should not be null for file URL with query parameter");
			assertEquals(16, imageData.width, "Width should be 16 based on query parameter");
			assertEquals(16, imageData.height, "Height should be 16 based on query parameter");
		} finally {
			tempSvg.delete();
		}
	}

	/**
	 * Tests that file: URLs with query parameters work at different zoom levels.
	 */
	@Test
	public void testFileURLWithQueryParameterZoom() throws IOException {
		// Copy test SVG to a temporary file
		URL resourceUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(resourceUrl, "Test SVG not found");

		File tempSvg = File.createTempFile("test-icon", ".svg");
		try (InputStream openStream = resourceUrl.openStream()) {
			Files.copy(openStream, tempSvg.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Create file: URL with query parameter
			URL fileUrl = tempSvg.toURI().toURL();
			String fileUrlWithQuery = fileUrl.toExternalForm() + "?size=16x16";
			URL fileUrlQuery = URI.create(fileUrlWithQuery).toURL();

			// Test at 200% zoom
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(fileUrlQuery);
			ImageData imageData = descriptor.getImageData(200);

			assertNotNull(imageData, "ImageData should not be null");
			assertEquals(32, imageData.width, "Width should be 32 (16*2) at 200% zoom");
			assertEquals(32, imageData.height, "Height should be 32 (16*2) at 200% zoom");
		} finally {
			tempSvg.delete();
		}
	}

	/**
	 * Tests that file: URLs with query parameters work when creating Image objects
	 * (not just ImageData). This tests the ImageFileNameProvider code path.
	 */
	@Test
	public void testFileURLWithQueryParameterCreateImage() throws IOException {
		// Copy test SVG to a temporary file
		URL resourceUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(resourceUrl, "Test SVG not found");

		File tempSvg = File.createTempFile("test-icon", ".svg");
		try (InputStream openStream = resourceUrl.openStream()) {
			Files.copy(openStream, tempSvg.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Create file: URL with query parameter
			URL fileUrl = tempSvg.toURI().toURL();
			String fileUrlWithQuery = fileUrl.toExternalForm() + "?size=32x32";
			URL fileUrlQuery = URI.create(fileUrlWithQuery).toURL();

			// Test that ImageDescriptor can create Image (not just ImageData) with query parameter
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(fileUrlQuery);
			Image image = descriptor.createImage(display);

			try {
				assertNotNull(image, "Image should not be null for file URL with query parameter");
				assertEquals(32, image.getBounds().width, "Width should be 32 based on query parameter");
				assertEquals(32, image.getBounds().height, "Height should be 32 based on query parameter");
			} finally {
				if (image != null) {
					image.dispose();
				}
			}
		} finally {
			tempSvg.delete();
		}
	}

	/**
	 * Tests that jar: URLs with query parameters work when creating Image objects.
	 * This is the most common use case in Eclipse plugins.
	 */
	@Test
	public void testJarURLWithQueryParameterCreateImage() throws Exception {
		URL baseUrl = URLHintProviderTest.class.getResource("/icons/imagetests/test-icon.svg");
		assertNotNull(baseUrl, "Test SVG not found");

		// Add query parameter to jar: URL
		String urlString = baseUrl.toExternalForm();
		URL url = URI.create(urlString + "?size=48x48").toURL();

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		Image image = descriptor.createImage(display);

		try {
			assertNotNull(image, "Image should not be null");
			assertEquals(48, image.getBounds().width, "Width should be 48 based on query parameter");
			assertEquals(48, image.getBounds().height, "Height should be 48 based on query parameter");
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}
}
