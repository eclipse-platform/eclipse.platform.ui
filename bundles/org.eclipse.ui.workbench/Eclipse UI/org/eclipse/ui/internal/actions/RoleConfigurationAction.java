/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.roles.CheckboxActivityHelper;
import org.eclipse.ui.internal.roles.SwapActivityHelper;

/**
 * Activates the Role configuration dialog. Can be replaced by addition to
 * welcome page (new &lt;tag&gt;).
 * 
 * @since 3.0
 */
public class RoleConfigurationAction extends Action {

	protected CheckboxActivityHelper checkboxHelper;
	protected SwapActivityHelper swapHelper;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @since 3.0
	 */
	public RoleConfigurationAction() {
		super(WorkbenchMessages.getString("RoleConfigurationAction.text")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		Dialog d = new Dialog(Display.getCurrent().getActiveShell()) {

			/*
			 * (non-Javadoc) @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
			 */
			protected Control createDialogArea(Composite parent) {
				Composite composite = (Composite) super.createDialogArea(parent);
				GridData data = new GridData(GridData.FILL_BOTH);
				data.widthHint = 600;
				data.heightHint = 240;

				swapHelper = new SwapActivityHelper();
				swapHelper.createControl(composite);
				swapHelper.getControl().setLayoutData(data);

				return composite;
			}

			/*
			 * (non-Javadoc) @see org.eclipse.jface.dialogs.Dialog#okPressed()
			 */
			protected void okPressed() {
				if (checkboxHelper != null) {
					checkboxHelper.updateActivityStates();
				}
				if (swapHelper != null) {
					swapHelper.updateActivityStates();
				}
				super.okPressed();
			}
		};
		d.open();
	}
}
