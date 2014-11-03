/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TitleAreaDialogTest extends TestCase {

	static ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.tests", "icons/anything.gif");

	private TitleAreaDialog dialog;

	@Override
	protected void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog = null;
		}
		super.tearDown();
	}

	// Test setting the title image before creating the dialog.
	public void testSetTitleImageEarly() {
		dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		final Image image = descriptor.createImage();
		dialog.setTitleImage(image);
		dialog.create();
		Shell shell = dialog.getShell();
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});
		dialog.open();
	}

	public void testSetTitleImageNull() {
		dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		dialog.setTitleImage(null);
		dialog.open();
		dialog.setTitleImage(null);
	}
}
