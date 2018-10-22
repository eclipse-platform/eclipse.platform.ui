/*******************************************************************************
 * Copyright (c) 2006, 2013 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A snippet to demonstrate a dialog with image buttons.
 *
 */
public class Snippet060DialogWithURLImageButtons {
	private ImageRegistry registry;

	public Snippet060DialogWithURLImageButtons(final Shell shell) {

		Dialog dia = new Dialog(shell) {
			private ImageDescriptor getImageDescriptor(String path) {
				if (registry == null) {
					registry = new ImageRegistry(shell.getDisplay());
				}

				ImageDescriptor desc = registry.getDescriptor(path);
				if (desc == null) {

					desc = ImageDescriptor.createFromURL(Snippet060DialogWithURLImageButtons.class.getResource(path));
					registry.put(path, desc);
				}

				return desc;
			}

			@Override
			protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
				Button b = super.createButton(parent, id, label, defaultButton);
				if (id == IDialogConstants.OK_ID) {

					URL url;
					try {
						url = new URL("http://www.eclipse.org/home/images/enterprise.gif");
						b.setImage(ImageDescriptor.createFromURL(url).createImage());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}

					// reset the button layout
					setButtonLayoutData(b);
				} else {
					b.setImage(getImageDescriptor("cancel.png").createImage()); //$NON-NLS-1$
					// reset the button layout
					setButtonLayoutData(b);
					return b;
				}

				return b;
			}

			@Override
			protected Control createContents(Composite parent) {
				Label label = new Label(parent, SWT.CENTER);
				label.setText("Dialog with Url Image Buttons");
				label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
				return super.createContents(parent);
			}
		};
		dia.open();
	}

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.open();

		new Snippet060DialogWithURLImageButtons(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
