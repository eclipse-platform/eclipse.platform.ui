/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *******************************************************************************/
package org.eclipse.jface.snippets.resources;

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
public class Snippet057FileImageDescriptors {
	private ImageRegistry registry;

	public Snippet057FileImageDescriptors(final Shell shell) {

		Dialog dia = new Dialog(shell) {
			private ImageDescriptor getImageDescriptorFromClass(String path) {
				ImageDescriptor desc = getDescriptorBasedOn(shell, path);
				if (desc == null) {
					desc = ImageDescriptor.createFromFile(
							Snippet057FileImageDescriptors.class, path);
					registry.put(path, desc);
				}

				return desc;
			}

			private ImageDescriptor getImageDescriptorFromFile(String path) {
				ImageDescriptor desc = getDescriptorBasedOn(shell, path);
				if (desc == null) {
					URL classPath = Snippet057FileImageDescriptors.class
							.getResource(path);
					Class<?> bogus = null;
					desc = ImageDescriptor.createFromFile(bogus,
							classPath.getFile());

					registry.put(path, desc);
				}
				return desc;
			}

			private ImageDescriptor getDescriptorBasedOn(final Shell shell,
					String path) {
				if (registry == null) {
					registry = new ImageRegistry(shell.getDisplay());
				}
				ImageDescriptor desc = registry.getDescriptor(path);
				return desc;
			}

			@Override
			protected Button createButton(Composite parent, int id,
					String label, boolean defaultButton) {
				Button b = super.createButton(parent, id, label, defaultButton);
				if (id == IDialogConstants.OK_ID) {
					b.setImage(getImageDescriptorFromClass("filesave.png").createImage()); //$NON-NLS-1$
					// reset the button layout
					setButtonLayoutData(b);
				} else {
					b.setImage(getImageDescriptorFromFile("cancel.png").createImage()); //$NON-NLS-1$
					// reset the button layout
					setButtonLayoutData(b);
					return b;
				}

				return b;
			}

			@Override
			protected Control createContents(Composite parent) {
				Label label = new Label(parent, SWT.CENTER);
				label.setText("Snippet057FileImageDescriptors");
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

		new Snippet057FileImageDescriptors(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
