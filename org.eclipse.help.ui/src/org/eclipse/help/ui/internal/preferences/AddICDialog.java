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

import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class AddICDialog extends StatusDialog implements IShellProvider {

	private Label nameLabel;

	private Text nameText;

	private Label hostLabel;

	private Text hostText;

	private Label pathLabel;

	private Text pathText;
	
	private Label protocolLabel;

	private Combo protocolCombo;

	private Button radio1;

	private Button radio2;

	private Text portText;
	
	Point shellSize;

	Point shellLocation;

	private String enteredHost;

	private String enteredName;

	private String enteredPort;

	private String enteredPath;
	
	private String enteredProtocol;

	private boolean enteredUseDefault;
	
	private Color errorColor;
	
	private StatusInfo dialogStatus;
	
	private static final String PROTOCOL_HTTPS = "https"; //$NON-NLS-1$
	
	public AddICDialog(Shell parentShell) {

		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Listens for any change in the UI and checks for valid input and correct
	 * enablement.
	 */
	private Listener changeListener = new Listener() {
		public void handleEvent(Event event) {
			
			  updateValidity();
		}
	};

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
		topGroup.setFont(topComposite.getFont());
		topGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		
		// add controls to composite as necessary
		createNameSection(topGroup);
		createHostSection(topGroup);
		createPathSection(topGroup);
		createProtocolSection(topGroup);
		createPortSection(topGroup);
		
		//Initialize validity
		updateValidity();		

		Dialog.applyDialogFont(topComposite);	
		return topComposite;

	}

	public void initializeBounds() {
		shellSize = getInitialSize();
		shellLocation = getInitialLocation(shellSize);

		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x + 150, shellSize.y);
				//shellSize.x + 150, shellSize.y-80);
		this.getShell().setText(Messages.AddICDialog_2);
	}
	
	/*
	 * Create the "Name:" label and text field.
	 */
	private void createNameSection(Composite parent) {
		nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText(Messages.AddICDialog_4);
		nameText = new Text(parent, SWT.BORDER);
		
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (nameText.getOrientation() == SWT.RIGHT_TO_LEFT)
			nameText.setOrientation(SWT.LEFT_TO_RIGHT);
		nameText.addListener(SWT.Modify, changeListener);
	}

	/*
	 * Create the "Host:" label and text field.
	 */
	private void createHostSection(Composite parent) {
		hostLabel = new Label(parent, SWT.NONE);
		hostLabel.setText(Messages.AddICDialog_5);
		hostText = new Text(parent, SWT.BORDER);
		hostText.setText(""); //$NON-NLS-1$
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (hostText.getOrientation() == SWT.RIGHT_TO_LEFT)
			hostText.setOrientation(SWT.LEFT_TO_RIGHT);
		hostText.addListener(SWT.Modify, changeListener);
	}

	/*
	 * Create the "Path:" label and text field.
	 */
	private void createPathSection(Composite parent) {
		pathLabel = new Label(parent, SWT.NONE);
		pathLabel.setText(Messages.AddICDialog_7);
		pathText = new Text(parent, SWT.BORDER);
		pathText.setText("/help"); //$NON-NLS-1$
		pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (pathText.getOrientation() == SWT.RIGHT_TO_LEFT)
			pathText.setOrientation(SWT.LEFT_TO_RIGHT);
		pathText.addListener(SWT.Modify, changeListener);
	}
	
	/*
	 * Create the "Protocol:" label and combo box.
	 */
	private void createProtocolSection(Composite parent) {
		protocolLabel = new Label(parent, SWT.NONE);
		protocolLabel.setText(Messages.AddICDialog_8);
		protocolCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		protocolCombo.add("http"); //$NON-NLS-1$
		protocolCombo.add(PROTOCOL_HTTPS);
		protocolCombo.select(0);
	}

	/*
	 * Create the port radio buttons, and text field.
	 */
	private void createPortSection(Composite parent) {
		Composite portComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		portComposite.setLayout(layout);
		portComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));

		radio1 = new Button(portComposite, SWT.RADIO);
		radio1.setText(Messages.AddICDialog_9);
		radio1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false, 2, 1));
		radio1.addListener(SWT.Selection, changeListener);
		radio1.setSelection(true);

		radio2 = new Button(portComposite, SWT.RADIO);
		radio2.setText(Messages.AddICDialog_10);
		radio2.addListener(SWT.Selection, changeListener);

		portText = new Text(portComposite, SWT.BORDER);
		portText.setLayoutData(new GridData(50, SWT.DEFAULT));
		portText.addListener(SWT.Modify, changeListener);
		portText.setEnabled(false);
	}

	public String getEnteredHost() {

		return enteredHost;
	}

	public String getEnteredPath() {

		return enteredPath;
	}
	
	public String getEnteredProtocol() {

		return enteredProtocol;
	}

	public String getEnteredPort() {

		return enteredPort;
	}

	public String getEnteredName() {
		return enteredName;
	}

	public boolean getEnteredDefault() {
		return enteredUseDefault;
	}

	public void okPressed() {
		enteredHost = hostText.getText();
		enteredName = nameText.getText();
		enteredPath = pathText.getText();
		enteredProtocol = protocolCombo.getText();
		
		// Logic here for setting port values
		if (radio1.getSelection()) // Use Default
		{
			enteredUseDefault = true;
			enteredPort = "80"; //$NON-NLS-1$
		} else // Custom Port
		{
			enteredUseDefault = false;
			enteredPort = portText.getText();

		}
		this.setReturnCode(OK);
		
		//Dispose Color object
		if(errorColor!=null)
		{
			errorColor.dispose();	
		}
		
		//Close window
		this.close();
		

	}
	
	
	/*
	 * Checks for errors in the user input and shows/clears the error message
	 * as appropriate.
	 */
	private void updateValidity() {
		// no checking needed if remote not selected
			String errorMessage=""; //$NON-NLS-1$
			boolean errorFound=false;
			dialogStatus = new StatusInfo();
			
			// check for empty hostname
			if (hostText!=null && hostText.getText().equals(""))  //$NON-NLS-1$
			{
				if(!errorFound)
				{
					errorMessage=Messages.AddICDialog_17;
					dialogStatus.setError(errorMessage);
					errorFound=true;
				}
				
			}
			// check for invalid port
			if (radio2!=null && radio2.getSelection() == true) {
				try {
					portText.setEnabled(true);
					// check port range
					int port = Integer.parseInt(portText.getText());
					if (port < 0 || port > 65535) {
						
						if(!errorFound)
						{
							errorMessage=Messages.AddICDialog_19;
							dialogStatus.setError(errorMessage);
							errorFound=true;
						}
												
											
					}
				}
				catch (NumberFormatException e) {
					// not a number
					if(!errorFound)
					{
						errorMessage=Messages.AddICDialog_19;
						dialogStatus.setError(errorMessage);
						errorFound=true;
					}
					
				}
			}
			else
			{
				if(radio1!=null && radio1.getSelection())
				{
					portText.setEnabled(false);
				}
				
			}
		
		
		if(errorFound)
		{
			this.updateStatus(dialogStatus);
		}
		else
		{
			dialogStatus.setOK();
			this.updateStatus(dialogStatus);
			
		}
	}

}
