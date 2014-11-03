/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 271339
 *          [FieldAssist] Add CC text field content assist doesn't work as expected when narrowing suggestions
 *******************************************************************************/
package org.eclipse.ui.tests.fieldassist;

import junit.framework.TestCase;

import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;

/**
 * Tests the Operations Framework API.
 *
 * @since 3.1
 */
public class FieldAssistAPITest extends TestCase {

	public FieldAssistAPITest() {
		super();
	}

	/**
	 * @param testName
	 */
	public FieldAssistAPITest(String name) {
		super(name);
	}

	public void testFieldDecorationRegistry() {
		int originalMaxHeight = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight();
		int originalMaxWidth = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		// System.out.println(new Rectangle(0, 0, originalMaxWidth,
		// originalMaxHeight));
		Image imageLarge = IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ)
				.createImage();
		// System.out.println(imageLarge.getBounds());
		// This image is known to be larger than the default images
		// Test that the maximum increases
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID",
				"Test image", imageLarge);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == imageLarge.getBounds().height);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == imageLarge.getBounds().width);

		// This image is known to be smaller. Test that the maximum decreases
		Image imageSmall = IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED)
				.createImage();
		// System.out.println(imageSmall.getBounds());
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID",
				"Test image", imageSmall);
		int currentMaxHeight = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight();
		assertTrue(currentMaxHeight < imageLarge.getBounds().height);
		int currentMaxWidth = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		assertTrue(currentMaxWidth < imageLarge.getBounds().width);

		// Registering another small one shouldn't change things
		FieldDecorationRegistry.getDefault().registerFieldDecoration("TESTID2",
				"Test image",
				"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_CONTENT_PROPOSAL");
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == currentMaxHeight);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == currentMaxWidth);

		// After we unregister the new decoration2, the maximums should be their
		// original values.
		FieldDecorationRegistry.getDefault()
				.unregisterFieldDecoration("TESTID");
		FieldDecorationRegistry.getDefault().unregisterFieldDecoration(
				"TESTID2");
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationHeight() == originalMaxHeight);
		assertTrue(FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth() == originalMaxWidth);
	}

}
