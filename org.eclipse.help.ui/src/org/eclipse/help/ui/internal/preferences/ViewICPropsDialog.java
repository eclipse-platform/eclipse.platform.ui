/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.internal.base.util.TestConnectionUtility;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


public class ViewICPropsDialog extends StatusDialog implements IShellProvider {

	private Label nameLabel;

	private Label nameText;

	private Label hostLabel;

	private Label hostText;

	private Label pathLabel;

	private Label pathText;
	
	private Label protocolLabel;

	private Label protocolText;

	private Label portLabel;

	private Label portText;

	private Label urlLabel;

	private Label urlValue;

	private Label enabledLabel;

	private Label enabledValue;
	
	Point shellSize;

	Point shellLocation;

	private String infoCenterName = ""; //$NON-NLS-1$

	private String selectedName = ""; //$NON-NLS-1$

	private String selectedHost = ""; //$NON-NLS-1$

	private String selectedPort = ""; //$NON-NLS-1$

	private String selectedPath = ""; //$NON-NLS-1$
	
	private String selectedProtocol = ""; //$NON-NLS-1$

	private boolean selectedEnabled;

	public ViewICPropsDialog(Shell parentShell, String infoCenterName) {

		super(parentShell);
		this.infoCenterName = infoCenterName;
	}

	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.help.ui.prefPageHelpContent"); //$NON-NLS-1$



		Composite topComposite= (Composite) super.createDialogArea(parent);
		topComposite.setSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite topGroup = new Composite(topComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.makeColumnsEqualWidth = false;
		topGroup.setLayout(layout);
		topGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		
		// add controls to composite as necessary
		createNameSection(topGroup);
		createHostSection(topGroup);
		createPathSection(topGroup);
		createProtocolSection(topGroup);
		createPortSection(topGroup);
		createURLValidateSection(topGroup);
		createEnabledSection(topGroup);
		
		Dialog.applyDialogFont(topComposite);	
		return topComposite;

	}
	
	/*
	 * Override createButtonsForButtonBar to allow for:
	 * TestConnection (was OK)
	 * Close (was Cancel)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButton(
				parent,IDialogConstants.OK_ID,Messages.ViewICPropsDialog_6,true);
		super.createButton(
				parent,IDialogConstants.CANCEL_ID,Messages.ViewICPropsDialog_7,false);
	}

	public void initializeBounds() {
		shellSize = getInitialSize();
		shellLocation = getInitialLocation(shellSize);

		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x +150, shellSize.y);
		this.getShell().setText(NLS.bind(Messages.ViewICPropsDialog_8 ,infoCenterName));
	}

	/*
	 * Create the "Name:" label and text field.
	 */
	private void createNameSection(Composite parent) {
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText(Messages.ViewICPropsDialog_10);
		nameText = new Label(parent, SWT.NONE);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameText.setText(selectedName);
	}

	/*
	 * Create the "Host:" label and text field.
	 */
	private void createHostSection(Composite parent) {
		hostLabel = new Label(parent, SWT.NONE);
		hostLabel.setText(Messages.ViewICPropsDialog_11);
		hostText = new Label(parent, SWT.NONE);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostText.setText(selectedHost);
	}

	/*
	 * Create the "Path:" label and text field.
	 */
	private void createPathSection(Composite parent) {
		pathLabel = new Label(parent, SWT.NONE);
		pathLabel.setText(Messages.ViewICPropsDialog_12);
		pathText = new Label(parent, SWT.NONE);
		pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pathText.setText(selectedPath);

	}
	
	/*
	 * Create the "Protocol:" label and text field.
	 */
	private void createProtocolSection(Composite parent) {
		protocolLabel = new Label(parent, SWT.NONE);
		protocolLabel.setText(Messages.ViewICPropsDialog_14);
		protocolText = new Label(parent, SWT.NONE);
		protocolText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		protocolText.setText(selectedProtocol);

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
		urlLabel.setText(Messages.ViewICPropsDialog_URL); 
		urlValue = new Label(parent, SWT.NONE);
		if(selectedPort.equals("80")) //$NON-NLS-1$
		{
			urlValue.setText(selectedProtocol+"://"+selectedHost + selectedPath); //$NON-NLS-1$
		}
		else
		{
			urlValue.setText(selectedProtocol+"://"+selectedHost + ":" + selectedPort + selectedPath); //$NON-NLS-1$ //$NON-NLS-2$
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

	public void setTextValues(String icName, String host, String port,
			String path, String protocol, boolean isEnabled) {

		selectedName = icName;
		selectedHost = host;
		selectedPort = port;
		selectedPath = path;
		selectedProtocol = protocol;
		selectedEnabled = isEnabled;
		
		
	}
	
	public void okPressed() {
		
		StatusInfo status = new StatusInfo(); 
		
		// Check to see if connection is valid
		boolean isConnection = TestConnectionUtility.testConnection(selectedHost, 
				selectedPort, selectedPath, selectedProtocol);
		
		if(isConnection)
			status.setInfo(Messages.ViewICPropsDialog_23);
		else
			status.setError(Messages.ViewICPropsDialog_24);

		updateStatus(status);
	}
	
	public void cancelPressed() {
		
		//Cancel is now OK button since right button is set to OK
		
		this.setReturnCode(OK);

		//Close window
		this.close();
	}
}
