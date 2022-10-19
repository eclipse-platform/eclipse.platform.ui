/*******************************************************************************
 * Copyright (c) 2020, 2022, Alex Blewitt and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Blewitt - initial API and implementation
 *     Daniel Kruegler - #399, #401
 ******************************************************************************/

package org.eclipse.jface.tests.images;

import java.net.URL;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import junit.framework.TestCase;

/**
 * Test loading ImageDescriptors from a URL calculated on demand.
 */
public class DeferredImageDescriptorTest extends TestCase {

	public void testDeferredLoading() {
		ImageData empty = ImageDescriptor.getMissingImageDescriptor().getImageData(100);
		assertEquals(empty, ImageDescriptor.createFromURLSupplier(true, () -> null).getImageData(100));
		assertNull(ImageDescriptor.createFromURLSupplier(false, () -> null).getImageData(100));
		assertNotNull(ImageDescriptor.createFromURLSupplier(false, () -> {
			throw new IllegalArgumentException("Should not happen");
		}));
		assertNotNull(ImageDescriptor
				.createFromURLSupplier(false, () -> DeferredImageDescriptorTest.class.getResource("anything.gif"))
				.getImageData(100));
	}

	public void testCreateImage() {
		assertNotNull(ImageDescriptor
				.createFromURLSupplier(true, () -> DeferredImageDescriptorTest.class.getResource("anything.gif"))
				.createImage());
	}

	public void testAdaptToURL() {
		ImageDescriptor descriptor = ImageDescriptor.createFromURLSupplier(false,
				() -> DeferredImageDescriptorTest.class.getResource("anything.gif"));

		URL url = Adapters.adapt(descriptor, URL.class);
		assertNotNull("DeferredImageDescriptor does not adapt to URL", url);

		ImageDescriptor descriptorFromUrl = ImageDescriptor.createFromURL(url);

		ImageData imageDataOrig = descriptor.getImageData(100);
		assertNotNull("Original URL does not return 100% image data", imageDataOrig);

		ImageData imageDataURL = descriptorFromUrl.getImageData(100);
		assertNotNull("Adapted URL does not return 100% image data", imageDataURL);
		assertEquals(imageDataOrig.width, imageDataURL.width);
		assertEquals(imageDataOrig.height, imageDataURL.height);
	}

}
