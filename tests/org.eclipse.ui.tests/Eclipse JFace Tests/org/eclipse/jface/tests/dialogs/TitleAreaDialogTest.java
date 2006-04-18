/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class TitleAreaDialogTest extends TestCase {

	static ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui.tests", "icons/anything.gif");

	public void testSetTitleImage() {
		final TitleAreaDialog dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		final Image image = descriptor.createImage();
		dialog.setTitleImage(image);
		dialog.create();
		Shell shell = dialog.getShell();
		shell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
				// try again with disposed image
				dialog.open();
			}
		});
		dialog.open();
	}

	public void testSetTitleImageDisposed() {
		TitleAreaDialog dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		Image image = descriptor.createImage();
		dialog.setTitleImage(image);
		image.dispose();
		dialog.open();
		try {
			// try again with disposed image
			dialog.setTitleImage(image);
		} catch (IllegalArgumentException e) {
			assertEquals("Argument not valid", e.getMessage()); //$NON-NLS-1$
			return;
		}
		fail();
	}

	public void testSetTitleImageNull() {
		TitleAreaDialog dialog = new TitleAreaDialog(null);
		dialog.setBlockOnOpen(false);
		dialog.setTitleImage(null);
		dialog.open();
		dialog.setTitleImage(null);
	}

	public void testTexts() {
		Dialog dialog = new TitleAreaDialog(DialogCheck.getShell());
		DialogCheck.assertDialogTexts(dialog, this);
	}
}
