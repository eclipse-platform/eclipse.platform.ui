/* -*-mode:java; c-basic-offset:2; -*- */
/**********************************************************************
Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
**********************************************************************/
package org.eclipse.team.ccvs.ssh2;

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

public class PasswordDialog extends Dialog {

  protected Text passwordField;
  protected String password=null;
	
  protected String message=null;

  public PasswordDialog(Shell parentShell, String message) {
    super(parentShell);
    this.message=message;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(message);
  }

  public void create() {
    super.create();
    passwordField.setFocus();
  }

  protected Control createDialogArea(Composite parent) {
    Composite main=new Composite(parent, SWT.NONE);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    main.setLayout(layout);
    main.setLayoutData(new GridData(GridData.FILL_BOTH));
	
    if (message!=null) {
      Label messageLabel=new Label(main, SWT.WRAP);
      messageLabel.setText(message);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      messageLabel.setLayoutData(data);
    }
		
    createPasswordFields(main);
    return main;
  }

  protected void createPasswordFields(Composite parent) {
    new Label(parent, SWT.NONE).setText("Password:");
		
    passwordField=new Text(parent, SWT.BORDER);
    GridData data=new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint=convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
    passwordField.setLayoutData(data);
    passwordField.setEchoChar('*');
		
    new Label(parent, SWT.NONE);
  }

  public String getPassword() {
    return password;
  }

  protected void okPressed() {
    String _password=passwordField.getText();
    if(_password==null || _password.length()==0){
      //messageLabel.setText("password is not given.");
      //messageLabel.setForeground(messageLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
      return;
    }
    password=_password;
    super.okPressed();
  }

  protected void cancelPressed() {
    password=null;
    super.cancelPressed();
  }
}
