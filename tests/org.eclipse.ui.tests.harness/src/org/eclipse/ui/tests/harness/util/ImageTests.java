/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.junit.Assert;

/**
 * @since 3.1
 */
public final class ImageTests {

	/**
	 *
	 */
	private ImageTests() {
		super();
	}

	public static void assertEquals(Image i1, Image i2) {
		ImageData data1 = i1.getImageData();
		ImageData data2 = i2.getImageData();
		Assert.assertTrue(Arrays.equals(data1.data, data2.data));
	}

	public static void assertNotEquals(Image i1, Image i2) {
		ImageData data1 = i1.getImageData();
		ImageData data2 = i2.getImageData();
		Assert.assertFalse(Arrays.equals(data1.data, data2.data));
	}
}
