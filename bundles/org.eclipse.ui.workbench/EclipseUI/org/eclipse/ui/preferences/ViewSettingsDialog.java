/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The ViewSettingsDialog is an abstract class that provides some common
 * functionality for view preferences.
 *
 * @since 3.1
 */
public class ViewSettingsDialog extends Dialog {

	private static int DEFAULTS_BUTTON_ID = 25;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param parentShell dialog's parent shell
	 */
	public ViewSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == DEFAULTS_BUTTON_ID) {
			performDefaults();
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Performs special processing when this dialog Defaults button has been
	 * pressed.
	 * <p>
	 * This is a framework hook method for subclasses to do special things when the
	 * Defaults button has been pressed. Subclasses may override, but should call
	 * <code>super.performDefaults</code>.
	 * </p>
	 */
	protected void performDefaults() {
		// Do nothing by default

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, DEFAULTS_BUTTON_ID, JFaceResources.getString("defaults"), false); //$NON-NLS-1$

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns += 2;
		layout.makeColumnsEqualWidth = false;

		super.createButtonsForButtonBar(parent);
	}

}
