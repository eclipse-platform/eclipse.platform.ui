/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;


/**
 * A message dialog which also allows the user to adjust a toggle setting.
 * 
 * This is typically used to allow the user to indicate whether the dialog
 * should be shown in the future.
 */
public class UserPreferencePromptDialog extends MessageDialog {
	public static final String ALWAYS = "always"; //$NON-NLS-1$
	public static final String NEVER = "never"; //$NON-NLS-1$
	public static final String PROMPT = "prompt"; //$NON-NLS-1$
	public static final String OK = "ok"; //$NON-NLS-1$
	
	
	/**
	 * The message displayed to the user, with the toggle button
	 */
	protected String toggleMessage = null;
	protected boolean toggleState = false;
	protected Button toggleButton = null;
	
	protected Button[] buttons;
	protected String[] buttonLabels;
	protected int defaultButtonIndex;
	
	/**
	 * The preference store which will be affected by the toggle button.
	 */
	protected IPreferenceStore prefStore = null;
	
	/**
	 * The preference store key that will be used to store the value.
	 */
	protected String prefKey = null;
	 
	
	/**
	 * Creates a message dialog with a toggle.
	 * See the superclass constructor for info on the other parameters.
	 * 
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Do not show this message again").
	 * @param toggleState the initial state for the toggle 
	 * 
	 */
	public UserPreferencePromptDialog(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex);
		this.toggleMessage = toggleMessage;
		this.toggleState = toggleState; 
		this.buttonLabels = dialogButtonLabels;
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

	/**
	 * @param prefKey The prefKey to set.
	 */
	public void setPrefKey(String prefKey) {
		this.prefKey = prefKey;
	}
	/**
	 * @param prefStore The prefStore to set.
	 */
	public void setPrefStore(IPreferenceStore prefStore) {
		this.prefStore = prefStore;
	}
	
	/* (non-Javadoc)
	 * Method declared in Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		toggleButton = createToggleButton(dialogArea);
		return dialogArea;
	}
	
	/* 
	 * (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		buttons = new Button[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			int id = i;
			String label = buttonLabels[i];
			if (label == IDialogConstants.OK_LABEL) {
				id = IDialogConstants.OK_ID;
			} else if (label == IDialogConstants.YES_LABEL) {
				id = IDialogConstants.YES_ID;
			} else if (label == IDialogConstants.NO_LABEL) {
				id = IDialogConstants.NO_ID;
			} else if (label == IDialogConstants.CANCEL_LABEL) {
				id = IDialogConstants.CANCEL_ID;
			}
			Button button = createButton(parent, id, label, defaultButtonIndex == i);
			buttons[i] = button;
		}
	}
	
	
	/**
	 * Creates a toggle button with the toggle message and state.
	 */
	protected Button createToggleButton(Composite parent) {
		final Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		String text = toggleMessage; 
		if (text == null) {
			text = IDEWorkbenchMessages.getString("MessageDialogWithToggle.defaultToggleMessage"); //$NON-NLS-1$
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
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		
		if (buttonId != IDialogConstants.CANCEL_ID && toggleState && prefStore != null && prefKey != null) {
			if (buttonId == IDialogConstants.YES_ID) {
				prefStore.setValue(prefKey, ALWAYS);
			} else if (buttonId == IDialogConstants.NO_ID) {
				prefStore.setValue(prefKey, NEVER);
			} else if (buttonId == IDialogConstants.OK_ID) {
				prefStore.setValue(prefKey, ALWAYS);
			}
		}
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
	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference
	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openOkCancelConfirm(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
				parent,
				title,
				null,	// accept the default window icon
				message,
				QUESTION,
				new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
				0,		// OK is the default
				toggleMessage,
				toggleState);
		dialog.prefStore = store;
		dialog.prefKey = key;
		dialog.open();
		return dialog;
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
	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference
 	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openError(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
				parent,
				title,
				null,	// accept the default window icon
				message,
				ERROR,
				new String[] {IDialogConstants.OK_LABEL},
				0,		// ok is the default
				toggleMessage,
				toggleState);
		dialog.prefStore = store;
		dialog.prefKey = key;
		dialog.open();
		return dialog;
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
 	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference

	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openInformation(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
				parent, 
				title, 
				null, // accept the default window icon
				message, 
				INFORMATION, 
				new String[] { IDialogConstants.OK_LABEL }, 
				0,		// ok is the default 
				toggleMessage,
				toggleState);
		dialog.prefStore = store;
		dialog.prefKey = key;
		dialog.open();
		return dialog;
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
 	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference
 
	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openYesNoQuestion(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
				parent,
				title,
				null,	// accept the default window icon
				message,
				QUESTION,
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
				0,		// yes is the default
				toggleMessage,
				toggleState);
		dialog.prefStore = store;
		dialog.prefKey = key;
		dialog.open();
		return dialog;
	}

	/**
	 * Convenience method to open a simple question Yes/No/Cancel dialog.
	 *
	 * @param parent the parent shell of the dialog, or <code>null</code> if none
	 * @param title the dialog's title, or <code>null</code> if none
	 * @param message the message
	 * @param toggleMessage the message for the toggle control, or <code>null</code> 
	 *   for the default message ("Don't show me this message again").
	 * @param toggleState the initial state for the toggle 
	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference
	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openYesNoCancelQuestion(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
				parent,
				title,
				null,	// accept the default window icon
				message,
				QUESTION,
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL},
				0,		// YES is the default
				toggleMessage,
				toggleState);
		dialog.prefStore = store;
		dialog.prefKey = key;
		dialog.open();
		return dialog;
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
 	 * @param store the IPreference store in which the user's preference should be persisted
	 * @param key the key to use when persisting the user's preference	  
	 * @return the dialog, after being closed by the user, which the client can
	 * 		only call <code>getReturnCode()</code> or <code>getToggleState()</code>
	 */
	public static UserPreferencePromptDialog openWarning(Shell parent, String title, String message, String toggleMessage, boolean toggleState, IPreferenceStore store, String key) {
		UserPreferencePromptDialog dialog = new UserPreferencePromptDialog(
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
		dialog.prefStore = store;
		dialog.prefKey = key;
		return dialog;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getButton(int)
	 */
//	protected Button getButton(int index) {
//		// TODO Auto-generated method stub
//		return super.getButton(index);
//	}
}