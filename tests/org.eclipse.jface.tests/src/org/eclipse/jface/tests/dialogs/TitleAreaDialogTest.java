/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.jface.tests.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Test;

public class TitleAreaDialogTest {

	static ImageDescriptor descriptor = ResourceLocator
			.imageDescriptorFromBundle("org.eclipse.jface.tests", "icons/anything.gif").orElse(null);

	private TitleAreaDialog dialog;

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
	}

	// Test setting the title image before creating the dialog.
	@Test
	public void testSetTitleImageEarly() {
		dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		final Image image = descriptor.createImage();
		dialog.setTitleImage(image);
		dialog.create();
		Shell shell = dialog.getShell();
		shell.addDisposeListener(e -> image.dispose());
		dialog.open();
	}

	@Test
	public void testSetTitleImageNull() {
		dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		dialog.setTitleImage(null);
		dialog.open();
		dialog.setTitleImage(null);
	}
}
