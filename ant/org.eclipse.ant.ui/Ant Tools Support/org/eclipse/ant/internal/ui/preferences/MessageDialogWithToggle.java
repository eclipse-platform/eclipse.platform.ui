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
package org.eclipse.ant.internal.ui.preferences;


import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A message dialog which allows the user to set a boolean preference.
 * 
 * This is typically used to set a preference that determines if the dialog
 * should be shown in the future
 */
public class MessageDialogWithToggle extends MessageDialog {
	
	/**
	 * The preference key which is set by the toggle button.
	 * This key must be a boolean preference in the preference store.
	 */
	private String fPreferenceKey = null;
	/**
	 * The message displayed to the user, with the toggle button
	 */
	private String fToggleMessage = null;
	private Button fToggleButton = null;
	/**
	 * The preference store which will be affected by the toggle button
	 */
	IPreferenceStore fStore = null;

	public MessageDialogWithToggle(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex);
		fStore = store;
		fPreferenceKey = preferenceKey;
		fToggleMessage = toggleMessage;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		fToggleButton = createCheckButton(area, fToggleMessage);
		getToggleButton().setSelection(fStore.getBoolean(fPreferenceKey));
		return area;
	}

	/**
	 * Creates a button with the given label and sets the default
	 * configuration data.
	 */
	protected Button createCheckButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);

		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan= 2;
		data.horizontalAlignment= GridData.CENTER;
		button.setLayoutData(data);
		button.setFont(parent.getFont());

		return button;
	}

	/**
	 * When the OK button is pressed, store the preference.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.OK_ID) {  // was the OK button pressed?
			storePreference();
		}
		super.buttonPressed(id);
	}

	/**
	 * Store the preference based on the user's selection
	 */
	protected void storePreference() {
		fStore.setValue(fPreferenceKey, getToggleButton().getSelection());
	}

	/**
	 * Returns the button used to toggle the dialog preference
	 * 
	 * @return Button the preference toggle button
	 */
	protected Button getToggleButton() {
		return fToggleButton;
	}
	
	/**
	 * Convenience method to open a simple confirm (OK/Cancel) dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @return <code>true</code> if the user presses the OK button,
	 *    <code>false</code> otherwise
	 */
	public static boolean openConfirm(Shell parent, String title, String message, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			QUESTION,
			new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
			0,		// OK is the default
			preferenceKey,
			toggleMessage,
			store);
		return dialog.open() == 0;
	}
	/**
	 * Convenience method to open a standard error dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 */
	public static void openError(Shell parent, String title, String message, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			ERROR,
			new String[] {IDialogConstants.OK_LABEL},
			0,		// ok is the default
			preferenceKey,
			toggleMessage,
			store);
		dialog.open();
	}
	/**
	 * Convenience method to open a standard information dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 */
	public static void openInformation(
		Shell parent,
		String title,
		String message, String preferenceKey, String toggleMessage, IPreferenceStore store) {
			MessageDialogWithToggle dialog =
				new MessageDialogWithToggle(parent, title, null, // accept the default window icon
		message, INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0,		// ok is the default 
		preferenceKey, toggleMessage, store);
		dialog.open();
	}
	/**
	 * Convenience method to open a simple Yes/No question dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @return <code>true</code> if the user presses the OK button,
	 *    <code>false</code> otherwise
	 */
	public static boolean openQuestion(Shell parent, String title, String message, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			QUESTION,
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
			0,		// yes is the default
			preferenceKey,
			toggleMessage,
			store);
		return dialog.open() == 0;
	}
	/**
	 * Convenience method to open a standard warning dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 */
	public static void openWarning(Shell parent, String title, String message, String preferenceKey, String toggleMessage, IPreferenceStore store) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			WARNING,
			new String[] {IDialogConstants.OK_LABEL},
			0,		// ok is the default
			preferenceKey,
			toggleMessage,
			store);
		dialog.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, IAntUIHelpContextIds.MESSAGE_WITH_TOGGLE_DIALOG);
	}

}
