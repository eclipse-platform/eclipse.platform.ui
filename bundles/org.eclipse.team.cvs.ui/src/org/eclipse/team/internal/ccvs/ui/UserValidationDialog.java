package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A dialog for prompting for a username and password
 */
public class UserValidationDialog extends Dialog {
	// widgets
	protected Text usernameField;
	protected Text passwordField;

	protected String domain;
	protected String defaultUsername;
	protected String password = null;
	
	// whether or not the username can be changed
	protected boolean isUsernameMutable = true;
	protected String username = null;
	protected String message = null;

	/**
	 * Creates a new UserValidationDialog.
	 * 
	 * @param parentShell  the parent shell
	 * @param location  the location
	 * @param defaultName  the default user name
	 * @param message  a mesage to display to the user
	 */
	public UserValidationDialog(Shell parentShell, String location, String defaultName, String message) {
		super(parentShell);
		this.defaultUsername = defaultName;
		this.domain = location;
		this.message = message;
	}
	/**
	 * @see Window#configureShell
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Policy.bind("UserValidationDialog.required")); //$NON-NLS-1$
		// set F1 help
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.USER_VALIDATION_DIALOG);	
	}
	/**
	 * @see Window#create
	 */
	public void create() {
		super.create();
		// add some default values
		usernameField.setText(defaultUsername);
	
		if (isUsernameMutable) {
			// give focus to username field
			usernameField.selectAll();
			usernameField.setFocus();
		} else {
			usernameField.setEditable(false);
			passwordField.setFocus();
		}
	}
	/**
	 * @see Dialog#createDialogArea
	 */
	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		if (message != null) {
			Label messageLabel = new Label(main, SWT.WRAP);
			messageLabel.setText(message);
			messageLabel.setForeground(messageLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 3;
			messageLabel.setLayoutData(data);
		}
		
		Label label = new Label(main, SWT.WRAP);
		if (isUsernameMutable) {
			label.setText(Policy.bind("UserValidationDialog.labelUser", domain)); //$NON-NLS-1$
		} else {
			label.setText(Policy.bind("UserValidationDialog.labelPassword", new Object[] {defaultUsername, domain})); //$NON-NLS-1$
		}
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		label.setLayoutData(data);
		
		createUsernameFields(main);
		createPasswordFields(main);
	
		return main;
	}
	/**
	 * Creates the three widgets that represent the password entry area.
	 * 
	 * @param parent  the parent of the widgets
	 */
	protected void createPasswordFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(Policy.bind("UserValidationDialog.password")); //$NON-NLS-1$
		
		passwordField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordField.setLayoutData(data);
		passwordField.setEchoChar('*');
		
		// spacer
		new Label(parent, SWT.NONE);
	}
	/**
	 * Creates the three widgets that represent the user name entry area.
	 * 
	 * @param parent  the parent of the widgets
	 */
	protected void createUsernameFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(Policy.bind("UserValidationDialog.user")); //$NON-NLS-1$
		
		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);
		
		// spacer
		new Label(parent, SWT.NONE);
	}
	/**
	 * Returns the password entered by the user, or null
	 * if the user canceled.
	 * 
	 * @return the entered password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * Returns the username entered by the user, or null
	 * if the user canceled.
	 * 
	 * @return the entered username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The default implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed() {
		password = passwordField.getText();
		username = usernameField.getText();
	
		super.okPressed();
	}
	/**
	 * Sets whether or not the username field should be mutable.
	 * This method must be called before create(), otherwise it
	 * will be ignored.
	 * 
	 * @param value  whether the username is mutable
	 */
	public void setUsernameMutable(boolean value) {
		isUsernameMutable = value;
	}
}
