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
package org.eclipse.jface.tests.viewers;

/**
 * Test with missing images.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.graphics.Image;

public class CheckboxTreeViewerMissingTest extends CheckboxTreeViewerTest {
	private static Image testImage;

	public static Image getMissingImage() {
		if (testImage == null) {
			testImage = ImageDescriptor.createFromFile(TestLabelProvider.class,
					"images/missing.gif").createImage();
		}
		return testImage;
	}

	public static class CheckboxMissingTableTestLabelProvider extends
			CheckboxTreeViewerTest.CheckboxTableTestLabelProvider {
		@Override
		public Image getImage(Object element) {
			return getMissingImage();
		}

	}

	public CheckboxTreeViewerMissingTest(String name) {
		super(name);
	}

	@Override
	public IBaseLabelProvider getTestLabelProvider() {
		return new CheckboxMissingTableTestLabelProvider();
	}

	@Override
	public void tearDown() {
		super.tearDown();
		if (testImage != null) {
			testImage.dispose();
			testImage = null;
		}
	}

	@Override
	public void testLabelProvider() {
		super.testLabelProvider();
	}

}
