/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.util.Arrays;

import junit.framework.Assert;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

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
