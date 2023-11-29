/*******************************************************************************
 * Copyright (c) 2006, 2008 Tom Schindl and others.
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
 *******************************************************************************/
package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A snippet to demonstrate a dialog with image buttons.
 */
public class Snippet012DialogWithImageButtons {
	private ImageRegistry registry;

	public Snippet012DialogWithImageButtons(final Shell shell) {

		Dialog dia = new Dialog(shell) {
			private ImageDescriptor getImageDescriptor(String path) {
				if (registry == null) {
					registry = new ImageRegistry(shell.getDisplay());
				}

				ImageDescriptor desc = registry.getDescriptor(path);
				if (desc == null) {
					desc = ImageDescriptor.createFromFile(Snippet012DialogWithImageButtons.class, path);
					registry.put(path, desc);
				}

				return desc;
			}

			@Override
			protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
				Button b = super.createButton(parent, id, label, defaultButton);
				if (id == IDialogConstants.OK_ID) {
					b.setImage(getImageDescriptor("filesave.png").createImage()); //$NON-NLS-1$
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
				WidgetFactory.label(SWT.CENTER).text("Update Snippet012DialogWithImageButtons...")
						.layoutData(new GridData(GridData.FILL_HORIZONTAL)).create(parent);
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

		new Snippet012DialogWithImageButtons(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}