package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.core.*;

/**
 * User authentication dialog
 */
public class UserValidationDialog extends Dialog {
	protected Text usernameField;
	protected Text passwordField;

	protected String domain;
	protected String realm;
	protected String defaultUsername;
	protected String password = null;
	protected String userid = null;
	protected boolean isUsernameMutable = true;
/**
 * Creates a new UserValidationDialog.
 */
public UserValidationDialog(Shell parentShell, URL location, String realm, String defaultName) {
	super(parentShell);
	this.defaultUsername = defaultName;
	this.domain = location.getHost();
	this.realm = realm;
}
/**
 * @see Window#configureShell
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText(UpdateManagerStrings.getString("S_Password_Required"));
}
/**
 * @see Window@create
 */
public void create() {
	super.create();
	//add some default values
	usernameField.setText(defaultUsername);

	if (isUsernameMutable) {
		//give focus to username field
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

	Label label = new Label(main, SWT.WRAP);
	String text = UpdateManagerStrings.getString("S_Enter_a_user_name_and_password_for")+": " + realm;
	text += "\n" + UpdateManagerStrings.getString("S_At_the_following_domain")+": "+domain;
	label.setText(text);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.horizontalSpan = 3;
	label.setLayoutData(data);
	
	createUsernameFields(main);
	createPasswordFields(main);

	return main;
}
/**
 * Creates the three widgets that represent the user name entry
 * area.
 */
protected void createPasswordFields(Composite parent) {
	new Label(parent, SWT.NONE).setText(UpdateManagerStrings.getString("Password") + ":");
	
	passwordField = new Text(parent, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
	passwordField.setLayoutData(data);
	passwordField.setEchoChar('*');
	
	new Label(parent, SWT.NONE);//spacer
}
/**
 * Creates the three widgets that represent the user name entry
 * area.
 */
protected void createUsernameFields(Composite parent) {
	new Label(parent, SWT.NONE).setText(UpdateManagerStrings.getString("S_User_name") + ":");
	
	usernameField = new Text(parent, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
	usernameField.setLayoutData(data);
	
	new Label(parent, SWT.NONE);//spacer
}
/**
 * Returns the password entered by the user, or null
 * if the user canceled.
 */
public String getPassword() {
	return password;
}
/**
 * Returns the username entered by the user, or null
 * if the user canceled.
 */
public String getUserid() {
	return userid;
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
	userid = usernameField.getText();
	super.okPressed();
}
/**
 * Sets whether or not the username field should be mutable.
 * This method must be called before create(), otherwise it
 * will be ignored.
 */
public void setUsernameMutable(boolean value) {
	isUsernameMutable = value;
}
}
