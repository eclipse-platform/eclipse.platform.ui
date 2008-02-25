/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RestoreDefaultsDialog extends Dialog implements IShellProvider{

	private Group group;
	private Label removeLabel;
	Point shellSize;
	Point shellLocation;
	protected RestoreDefaultsDialog(Shell parentShell) {
		
		super(parentShell);
		// TODO Auto-generated constructor stub
	}


	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP_CONTENT);
	
		
		
		Composite composite = (Composite) super.createDialogArea(parent);
		 //add controls to composite as necessary
		
		
		createGroup(parent);
		
		//Create Button Bar
		this.createButtonBar(parent);
		
		return composite;
	
	}
	
	public void initializeBounds()
	{
		shellSize=getInitialSize();
		shellLocation=getInitialLocation(shellSize);
		
		
		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x + 90, shellSize.y - 40);
		this.getShell().setText(Messages.RestoreDefaultsDialog_1);
	}
	
	
	/*
	 * Create the "Location" group.
	 */
	private void createGroup(Composite parent) {
        group = new Group(parent, SWT.NONE);
        group.setText(Messages.RestoreDefaultsDialog_2);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        createRestoreDefaultsSection(group);
       
	}
	
	/*
	 * Create the "Host:" label and text field.
	 */
	private void createRestoreDefaultsSection(Composite parent) {
        removeLabel = new Label(parent, SWT.NONE);        
        removeLabel.setText(Messages.RestoreDefaultsDialog_3);
      
	}

	protected void okPressed()
	{
		this.setReturnCode(OK);
		this.close();
	}
	
	public void setValues(String icName)
	{
	}
	
}
