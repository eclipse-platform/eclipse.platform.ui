/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A dialog to show an error while giving the user the option to continue.
 */
public class PreferenceErrorDialog extends ErrorDialog {
	private IStatus status;
	/**
	 * Create a new instance of the dialog
	 */
	public PreferenceErrorDialog(
		Shell parentShell,
		String dialogTitle,
		String message,
		IStatus status,
		int displayMask) {
		super(parentShell, dialogTitle, message, status, displayMask);
		this.status = status;
	}
	/**
	 * Opens an error dialog to display the given error.  
	 */
	public static int openError(Shell parentShell, String title, String message, IStatus status) {
		int displayMask = IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR;
		ErrorDialog dialog = new PreferenceErrorDialog(parentShell, title, message, status, displayMask);
		return dialog.open();
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.YES_ID == buttonId) 
			okPressed();
		else if (IDialogConstants.NO_ID == buttonId) 
			cancelPressed();
		else
			super.buttonPressed(buttonId);	
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create Yes, No, and Details buttons
		createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
		super.createButtonsForButtonBar(parent);
		// get rid of the unwanted ok button
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null && !okButton.isDisposed()) {
			okButton.dispose();
			((GridLayout)parent.getLayout()).numColumns--;
		}
	}
	/* (non-Javadoc)
	 * Method declared on ErrorDialog.
	 */
	protected Image getImage() {
		return JFaceResources.getImageRegistry().get(DLG_IMG_WARNING);
	}
}

