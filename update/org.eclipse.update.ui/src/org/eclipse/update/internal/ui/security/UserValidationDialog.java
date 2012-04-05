/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.security;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.*;

/**
 * User authentication dialog
 */
public class UserValidationDialog extends Dialog {
	protected Text usernameField;
	protected Text passwordField;

	protected String host;
	protected String message;
	protected Authentication userAuthentication = null;
	/**
	 * Gets user and password from a user. May be called from any thread
	 * 
	 * @return UserAuthentication that contains the userid and the password or
	 *         <code>null</code> if the dialog has been cancelled
	 */
	public static Authentication getAuthentication(final String host,
			final String message) {
		class UIOperation implements Runnable {
			public Authentication authentication;
			public void run() {
				authentication = UserValidationDialog.askForAuthentication(
						host, message);
			}
		}

		UIOperation uio = new UIOperation();
		if (Display.getCurrent() != null) {
			uio.run();
		} else {
			Display.getDefault().syncExec(uio);
		}
		return uio.authentication;
	}
	/**
	 * Gets user and password from a user Must be called from UI thread
	 * 
	 * @return UserAuthentication that contains the userid and the password or
	 *         <code>null</code> if the dialog has been cancelled
	 */
	protected static Authentication askForAuthentication(String host,
			String message) {
		UserValidationDialog ui = new UserValidationDialog(null, host, message); 
		ui.open();
		return ui.getAuthentication();
	}
	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell
	 *            parent Shell or null
	 */
	protected UserValidationDialog(Shell parentShell, String host,
			String message) {
		super(parentShell);
		this.host = host;
		this.message = message;
		setBlockOnOpen(true);
	}
	/**
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(UpdateUIMessages.UserVerificationDialog_PasswordRequired); 
	}
	/**
	 */
	public void create() {
		super.create();
		//give focus to username field
		usernameField.selectAll();
		usernameField.setFocus();
	}
	/**
	 */
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(main, SWT.WRAP);
		String text = UpdateUIMessages.UserVerificationDialog_ConnectTo + host; 
		text += "\n\n" + message; //$NON-NLS-1$ 
		label.setText(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		label.setLayoutData(data);

		createUsernameFields(main);
		createPasswordFields(main);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(main,
				"org.eclipse.update.ui.UserValidationDialog"); //$NON-NLS-1$
		return main;
	}
	/**
	 * Creates the three widgets that represent the user name entry area.
	 */
	protected void createPasswordFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(UpdateUIMessages.UserVerificationDialog_Password); 

		passwordField = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordField.setLayoutData(data);

		new Label(parent, SWT.NONE); //spacer
	}
	/**
	 * Creates the three widgets that represent the user name entry area.
	 */
	protected void createUsernameFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(UpdateUIMessages.UserVerificationDialog_UserName); 

		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);

		new Label(parent, SWT.NONE); //spacer
	}
	/**
	 * Returns the UserAuthentication entered by the user, or null if the user
	 * canceled.
	 */
	public Authentication getAuthentication() {
		return userAuthentication;
	}
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 */
	protected void okPressed() {
		userAuthentication = new Authentication(usernameField.getText(),
				passwordField.getText());
		super.okPressed();
	}

}
