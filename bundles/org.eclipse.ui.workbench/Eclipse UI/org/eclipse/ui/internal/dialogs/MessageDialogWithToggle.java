/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A message dialog which also allows the user to adjust a toggle setting.
 * 
 * This is typically used to allow the user to indicate whether the dialog
 * should be shown in the future.
 */
public class MessageDialogWithToggle extends MessageDialog {
	
	/**
	 * The message displayed to the user, with the toggle button
	 */
	private String toggleMessage = null;
	private boolean toggleState = false;
	private Button toggleButton = null;
	
	/**
	 * The preference store which will be affected by the toggle button
	 */
	IPreferenceStore fStore = null;

	/**
	 * Creates a message dialog with a toggle.
	 * See the superclass constructor for info on the other parameters.
	 * 
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 * 
	 */
	public MessageDialogWithToggle(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex);
		this.toggleMessage = toggleMessage;
		this.toggleState = toggleState; 
	}

	/**
	 * Returns the toggle state.  This can be called even after the dialog
	 * is closed.
	 * 
	 * @return <code>true</code> if the toggle button is checked, 
	 *   <code>false</code> if not
	 */
	public boolean getToggleState() {
		return toggleState;
	}
	
	/* (non-Javadoc)
	 * Method declared in Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		toggleButton = createToggleButton(dialogArea);
		return dialogArea;
	}

	/**
	 * Creates a toggle button with the toggle message and state.
	 */
	protected Button createToggleButton(Composite parent) {
		final Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		String text = toggleMessage; 
		if (text == null) {
			text = WorkbenchMessages.getString("MessageDialogWithToggle.defaultToggleMessage"); //$NON-NLS-1$
		}
		button.setText(text);
		button.setSelection(toggleState);
		
		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan= 2;
		data.horizontalAlignment= GridData.CENTER;
		button.setLayoutData(data);
		button.setFont(parent.getFont());

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toggleState = button.getSelection();
			}

		});
		return button;
	}
	
	/**
	 * Returns the toggle button.
	 * 
	 * @return the toggle button
	 */
	protected Button getToggleButton() {
		return toggleButton;
	}
	
	/**
	 * Convenience method to open a simple confirm (OK/Cancel) dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 * @return <code>true</code> if the user presses the OK button,
	 *    <code>false</code> otherwise
	 */
	public static boolean openConfirm(Shell parent, String title, String message, String toggleMessage, boolean toggleState) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			QUESTION,
			new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
			0,		// OK is the default
			toggleMessage,
			toggleState);
		return dialog.open() == 0;
	}
	
	/**
	 * Convenience method to open a standard error dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 */
	public static void openError(Shell parent, String title, String message, String toggleMessage, boolean toggleState) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			ERROR,
			new String[] {IDialogConstants.OK_LABEL},
			0,		// ok is the default
			toggleMessage,
			toggleState);
		dialog.open();
	}
	
	/**
	 * Convenience method to open a standard information dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 */
	public static void openInformation(Shell parent, String title, String message, String toggleMessage, boolean toggleState) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent, 
			title, 
			null, // accept the default window icon
			message, 
			INFORMATION, 
			new String[] { IDialogConstants.OK_LABEL }, 
			0,		// ok is the default 
			toggleMessage,
			toggleState);
		dialog.open();
	}
	
	/**
	 * Convenience method to open a simple Yes/No question dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 * @return <code>true</code> if the user presses the OK button,
	 *    <code>false</code> otherwise
	 */
	public static boolean openQuestion(Shell parent, String title, String message, String toggleMessage, boolean toggleState) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			QUESTION,
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
			0,		// yes is the default
			toggleMessage,
			toggleState);
		return dialog.open() == 0;
	}
	
	/**
	 * Convenience method to open a standard warning dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 */
	public static void openWarning(Shell parent, String title, String message, String toggleMessage, boolean toggleState) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(
			parent,
			title,
			null,	// accept the default window icon
			message,
			WARNING,
			new String[] {IDialogConstants.OK_LABEL},
			0,		// ok is the default
			toggleMessage,
			toggleState);
		dialog.open();
	}

}
