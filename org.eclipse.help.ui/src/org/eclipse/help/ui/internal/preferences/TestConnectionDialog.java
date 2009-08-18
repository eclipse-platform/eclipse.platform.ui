/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TestConnectionDialog extends StatusDialog implements IShellProvider {

	private Label testConnectionLabel;
	private Label urlLabel;
	
	Point shellSize;
	Point shellLocation;
	private String infoCenterName = "", infoCenterHost = "", //$NON-NLS-1$ //$NON-NLS-2$
			infoCenterPath = "", infoCenterPort = ""; //$NON-NLS-1$ //$NON-NLS-2$

	boolean successfullConnection = false;
	Color connectionColor;

	protected TestConnectionDialog(Shell parentShell) {

		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	protected Control createDialogArea(Composite parent) {
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		//		IHelpUIConstants.PREF_PAGE_HELP_CONTENT);
		
		setHelpAvailable(false);

		Composite topComposite= (Composite) super.createDialogArea(parent);
		topComposite.setSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite topGroup = new Composite(topComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.makeColumnsEqualWidth = false;
		topGroup.setLayout(layout);
		topGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		createTestLabelSection(topGroup);
		createURLSection(topGroup);
		Dialog.applyDialogFont(topGroup);

		return topGroup;
	}


	/*
	 * Override createButtonsForButtonBar to have only one button (OK)
	 */

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK button only by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	public void initializeBounds() {
		shellSize = getInitialSize();
		shellLocation = getInitialLocation(shellSize);
		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x + 150, shellSize.y);
		this.getShell().setText(
				Messages.TestConnectionDialog_4);
	}

	/*
	 * Create the "Connection" label.
	 */
	private void createTestLabelSection(Composite parent) {
		testConnectionLabel = new Label(parent, SWT.VERTICAL);
	
		testConnectionLabel.setText(NLS.bind(Messages.TestConnectionDialog_6 ,infoCenterName));
	}

	/*
	 * Create the "URL" label.
	 */
	private void createURLSection(Composite parent) {
		urlLabel = new Label(parent, SWT.VERTICAL);
			
		String urlTemplate = Messages.TestConnectionDialog_URL_With_Param; 
		String urlString="http://" + infoCenterHost; //$NON-NLS-1$
		if (infoCenterPort
				.equals("80")) { //$NON-NLS-1$
			urlString = urlString + infoCenterPath;

		} else {
			urlString = urlString + ":" + infoCenterPort + infoCenterPath; //$NON-NLS-1$
		}
		urlString = NLS.bind(urlTemplate, urlString);
		urlLabel.setText("\n" + urlString + '\n'); //$NON-NLS-1$
	}	
	
	//Override cancel button so it acts as OK.  Then set OK button not visible, because we only need one button
	public void cancelPressed()
	{
		this.setReturnCode(OK);

		if (connectionColor != null) {
			connectionColor.dispose();
		}
		this.close();
	}
	public void setValues(String icName, String icHost, String icPort,
			String icPath) {
		infoCenterName = icName;
		infoCenterHost = icHost;
		infoCenterPath = icPath;
		infoCenterPort = icPort;
	}
	
	

	public void create() {
		// TODO Auto-generated method stub
		super.create();
		setConnectionStatus(successfullConnection);
	}

	public void setConnectionStatus(boolean testStatus) {
		successfullConnection = testStatus;
		StatusInfo status = new StatusInfo();
		
		if(successfullConnection)
			status.setInfo(Messages.TestConnectionDialog_12);
		else
			status.setError(Messages.TestConnectionDialog_13);

		updateStatus(status);
	}
}
