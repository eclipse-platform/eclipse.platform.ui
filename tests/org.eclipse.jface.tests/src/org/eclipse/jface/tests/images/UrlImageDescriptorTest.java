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
 *     Daniel Kruegler - #396 - [jface] Certain ImageDescriptor classes should be adaptable for some internal properties
 ******************************************************************************/
package org.eclipse.jface.tests.images;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageFileNameProvider;

import junit.framework.TestCase;

public class UrlImageDescriptorTest extends TestCase {

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

		assertTrue("URLImageDescriptor does not implement IAdaptable", descriptor instanceof IAdaptable);
		IAdaptable adaptable = (IAdaptable) descriptor;
		ImageFileNameProvider fileNameProvider = adaptable.getAdapter(ImageFileNameProvider.class);
		assertNotNull("URLImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
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

		assertTrue("URLImageDescriptor does not implement IAdaptable", descriptor instanceof IAdaptable);
		IAdaptable adaptable = (IAdaptable) descriptor;
		ImageFileNameProvider fileNameProvider = adaptable.getAdapter(ImageFileNameProvider.class);
		assertNotNull("URLImageDescriptor does not adapt to ImageFileNameProvider", fileNameProvider);
		String imagePath100 = fileNameProvider.getImagePath(100);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 100% path", imagePath100);
		assertEquals(Path.fromOSString(imagePath100).lastSegment(), "zoomIn.png");
		String imagePath200 = fileNameProvider.getImagePath(200);
		assertNotNull("URLImageDescriptor ImageFileNameProvider does not return the 200% path", imagePath200);
		assertEquals(Path.fromOSString(imagePath200).lastSegment(), "zoomIn@2x.png");
		String imagePath150 = fileNameProvider.getImagePath(150);
		assertNull("URLImageDescriptor's ImageFileNameProvider does return a 150% path", imagePath150);
	}

}
