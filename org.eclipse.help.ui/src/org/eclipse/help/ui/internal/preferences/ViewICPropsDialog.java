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

import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class ViewICPropsDialog extends Dialog implements IShellProvider {

	private Group group;

	private Label nameLabel;

	private Label nameText;

	private Label hostLabel;

	private Label hostText;

	private Label pathLabel;

	private Label pathText;

	private Label portLabel;

	private Label portText;

	private Label urlLabel;

	private Label urlValue;

	private Label enabledLabel;

	private Label enabledValue;
	
	private Label connectedLabel;

	Point shellSize;

	Point shellLocation;

	private String infoCenterName = ""; //$NON-NLS-1$

	private String selectedName = ""; //$NON-NLS-1$

	private String selectedHost = ""; //$NON-NLS-1$

	private String selectedPort = ""; //$NON-NLS-1$

	private String selectedPath = ""; //$NON-NLS-1$

	private boolean selectedEnabled;
	
	Color connectedColor;

	public ViewICPropsDialog(Shell parentShell, String infoCenterName) {

		super(parentShell);
		this.infoCenterName = infoCenterName;
	}

	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.help.ui.prefPageHelpContent"); //$NON-NLS-1$

		Composite composite = (Composite) super.createDialogArea(parent);
		// add controls to composite as necessary

		createGroup(parent);

		createConnectedSection(parent);
		// Create Button Bar
		this.buttonBar=this.createButtonBar(parent);
		
		
		//Manipulate Button Bar to add custom buttons
		shellSize = getInitialSize();
				
		Button okButton=this.getButton(IDialogConstants.OK_ID); //Get OK Button
		okButton.setVisible(true);
		okButton.setSize(shellSize.x+100,shellSize.y);
		okButton.setText(Messages.ViewICPropsDialog_6); 
		this.setButtonLayoutData(okButton);
		//
		Button cancelButton=this.getButton(IDialogConstants.CANCEL_ID); //Get Cancel button
		cancelButton.setText(Messages.ViewICPropsDialog_7);
		cancelButton.setSize(shellSize.x-50,shellSize.y);
		this.setButtonLayoutData(cancelButton);
		
		return composite;

	}

	public void initializeBounds() {
		shellSize = getInitialSize();
		shellLocation = getInitialLocation(shellSize);

		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x + 180, shellSize.y - 40);
		this.getShell().setText(NLS.bind(Messages.ViewICPropsDialog_8 ,infoCenterName));
	}

	/*
	 * Create the Infocenter properties group.
	 */
	private void createGroup(Composite parent) {
		group = new Group(parent, SWT.NONE);
		group.setText(Messages.ViewICPropsDialog_9);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createNameSection(group);
		createHostSection(group);
		createPathSection(group);
		createPortSection(group);
		createURLValidateSection(group);
		createEnabledSection(group);
	}

	/*
	 * Create the "Name:" label and text field.
	 */
	private void createNameSection(Composite parent) {
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText(Messages.ViewICPropsDialog_10);
		nameText = new Label(parent, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameText.setText(selectedName);
	}

	/*
	 * Create the "Host:" label and text field.
	 */
	private void createHostSection(Composite parent) {
		hostLabel = new Label(parent, SWT.NONE);
		hostLabel.setText(Messages.ViewICPropsDialog_11);
		hostText = new Label(parent, SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostText.setText(selectedHost);
	}

	/*
	 * Create the "Path:" label and text field.
	 */
	private void createPathSection(Composite parent) {
		pathLabel = new Label(parent, SWT.NONE);
		pathLabel.setText(Messages.ViewICPropsDialog_12);
		pathText = new Label(parent, SWT.BORDER);
		pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pathText.setText(selectedPath);

	}

	/*
	 * Create the port radio buttons, and text field.
	 */
	private void createPortSection(Composite parent) {
		portLabel = new Label(parent, SWT.NONE);
		portLabel.setText(Messages.ViewICPropsDialog_13);
		portText = new Label(parent, SWT.NONE);
		portText.setText(selectedPort);
	}

	private void createURLValidateSection(Composite parent) {
		urlLabel = new Label(parent, SWT.NONE);
		urlLabel.setText("URL: "); //$NON-NLS-1$
		urlValue = new Label(parent, SWT.NONE);
		if(selectedPort.equals("80")) //$NON-NLS-1$
		{
			urlValue.setText("http://"+selectedHost + selectedPath); //$NON-NLS-1$
		}
		else
		{
			urlValue.setText("http://"+selectedHost + ":" + selectedPort + selectedPath); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	private void createEnabledSection(Composite parent) {
		enabledLabel = new Label(parent, SWT.NONE);
		enabledLabel.setText(Messages.ViewICPropsDialog_19);
		enabledValue = new Label(parent, SWT.NONE);

		if (selectedEnabled) {
			enabledValue.setText(Messages.ViewICPropsDialog_20);
		} else {
			enabledValue.setText(Messages.ViewICPropsDialog_21);
		}

	}
	
	private void createConnectedSection(Composite parent) {
		connectedLabel = new Label(parent, SWT.NONE);
		connectedLabel.setText(""); //$NON-NLS-1$
		connectedLabel.setVisible(false);	
		
		connectedLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
	}

	public void setTextValues(String icName, String host, String port,
			String path, boolean isEnabled) {

		selectedName = icName;
		selectedHost = host;
		selectedPort = port;
		selectedPath = path;
		selectedEnabled = isEnabled;
		
		
	}
	
	public void okPressed() {
		
		boolean isConnection=TestConnectionUtility.testConnection(selectedHost,selectedPort, selectedPath);
		// Logic here for setting port values
		if(isConnection)
		{
			connectedLabel.setText(Messages.ViewICPropsDialog_23);
			
			Display thisDisplay=this.getShell().getDisplay();
			connectedColor=thisDisplay.getSystemColor(SWT.COLOR_DARK_GREEN);	
			connectedLabel.setForeground(connectedColor);
			
		}
		else 
		{
			connectedLabel.setText(Messages.ViewICPropsDialog_24);
			Display thisDisplay=this.getShell().getDisplay();
			connectedColor=thisDisplay.getSystemColor(SWT.COLOR_RED);
			connectedLabel.setForeground(connectedColor);
		}
		
		connectedLabel.setVisible(true);
	}
	
	public void cancelPressed() {
		
		//Cancel is now OK button since right button is set to OK
		
		this.setReturnCode(OK);
		
		//Dispose the Color
		if(connectedColor!=null)
		{
			connectedColor.dispose();
		}
		//Close window
		this.close();
		

	}
	
	
}
