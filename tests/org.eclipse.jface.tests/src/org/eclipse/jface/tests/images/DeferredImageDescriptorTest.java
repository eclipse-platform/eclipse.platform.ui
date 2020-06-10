/*******************************************************************************
 * Copyright (c) 2020, Alex Blewitt and others
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
 ******************************************************************************/

package org.eclipse.jface.tests.images;

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
}
