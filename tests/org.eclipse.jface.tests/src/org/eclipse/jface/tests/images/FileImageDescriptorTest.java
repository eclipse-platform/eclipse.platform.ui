/*******************************************************************************
 * Copyright (c) 2008, 2020 IBM Corporation and others.
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
 *     Christoph LÃ¤ubrich - Bug 567898 - [JFace][HiDPI] ImageDescriptor support alternative naming scheme for high dpi
 *     Daniel Krügler - #375 - High-DPI: FileImageDescriptor's autoscaling strategy unnecessarily restricted to quadratic images
 ******************************************************************************/

package org.eclipse.jface.tests.images;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import junit.framework.TestCase;

/**
 * Test loading a directory full of images.
 *
 * @since 3.4
 *
 */
public class FileImageDescriptorTest extends TestCase {

	protected static final String IMAGES_DIRECTORY = "/icons/imagetests";

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param name
	 */
	public FileImageDescriptorTest(String name) {
		super(name);
	}

	/**
	 * Test loading the image descriptors.
	 */
	public void testFileImageDescriptorWorkbench() {

		Class<?> missing = null;
		ArrayList<Image> images = new ArrayList<>();

		Bundle bundle = FrameworkUtil.getBundle(FileImageDescriptorTest.class);
		Enumeration<String> bundleEntries = bundle.getEntryPaths(IMAGES_DIRECTORY);

		while (bundleEntries.hasMoreElements()) {
			ImageDescriptor descriptor;
			String localImagePath = bundleEntries.nextElement();
			URL[] files = FileLocator.findEntries(bundle, new Path(localImagePath));

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

		Iterator<Image> imageIterator = images.iterator();
		while (imageIterator.hasNext()) {
			imageIterator.next().dispose();
		}

	}

	/**
	 * Test the file image descriptor.
	 */
	public void testFileImageDescriptorLocal() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "anything.gif");

		Image image = descriptor.createImage();
		assertTrue("Could not find image", image != null);
		image.dispose();

	}

	/**
	 * Test for a missing file image descriptor.
	 */
	public void testFileImageDescriptorMissing() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "missing.gif");

		Image image = descriptor.createImage(false);
		assertTrue("Found an image but should be null", image == null);
	}

	/**
	 * Test for a missing file image descriptor.
	 */
	public void testFileImageDescriptorMissingWithDefault() {

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class, "missing.gif");

		Image image = descriptor.createImage(true);
		assertTrue("Did not find default image", image != null);
	}

	public void testGetxName() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/zoomIn.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	public void testGetxPath() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/16x16/zoomIn.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

	public void testGetxPathRectangular() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(FileImageDescriptorTest.class,
				"/icons/imagetests/rectangular-57x16.png");
		ImageData imageData = descriptor.getImageData(100);
		assertNotNull(imageData);
		ImageData imageDataZoomed = descriptor.getImageData(200);
		assertNotNull(imageDataZoomed);
		assertEquals(imageData.width * 2, imageDataZoomed.width);
	}

}
