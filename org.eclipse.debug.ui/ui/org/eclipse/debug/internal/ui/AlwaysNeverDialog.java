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
package org.eclipse.debug.internal.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog which prompts the user and provides Yes, No, Always, and Never
 * buttons. The user's selection is written to a preference store.
 */
public class AlwaysNeverDialog extends MessageDialog {
	
	/**
	 * Value written to the preference store if the user selects Always
	 */
	public static final String ALWAYS= "always"; //$NON-NLS-1$
	/**
	 * Value written to the preference store if the user selects Never
	 */
	public static final String NEVER= "never"; //$NON-NLS-1$
	/**
	 * Value written to the preference store if the user selects Yes or No
	 */
	public static final String PROMPT= "prompt"; //$NON-NLS-1$

	/**
	 * The preference key which is set by the toggle button.
	 * This key must be a boolean preference in the preference store.
	 */
	private String fPreferenceKey = null;
	
	/**
	 * The preference store which will be affected by the toggle button
	 */
	IPreferenceStore fStore = null;
	
	public AlwaysNeverDialog(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String preferenceKey, IPreferenceStore store) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex);
		fStore = store;
		fPreferenceKey = preferenceKey;
	}

	/**
	 * Convenience method to open the Yes/No/Always/Never question dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @return <code>true</code> if the user presses a YES button,
	 *    <code>false</code> otherwise
	 */
	public static boolean openQuestion(Shell parent, String title, String message, String preferenceKey, IPreferenceStore store) {
		AlwaysNeverDialog dialog = new AlwaysNeverDialog(
				parent,
				title,
				null,	// accept the default window icon
				message,
				QUESTION,
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, DebugUIMessages.getString("AlwaysNeverDialog.3"), DebugUIMessages.getString("AlwaysNeverDialog.4")}, //$NON-NLS-1$ //$NON-NLS-2$
				0,		// yes is the default
				preferenceKey,
				store);
		int button= dialog.open();
		return button == 0 || button == 2;
	}
	
	/**
	 * When a button is pressed, store the preference.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int id) {
		if (id < 2) { // Yes or No
			fStore.setValue(fPreferenceKey, PROMPT);
		} else if (id == 2) { // Always
			fStore.setValue(fPreferenceKey, ALWAYS);
		} else { // Never
			fStore.setValue(fPreferenceKey, NEVER);
		}
		super.buttonPressed(id);
	}
}
