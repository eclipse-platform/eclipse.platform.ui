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
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
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

public class RemoveICDialog extends Dialog implements IShellProvider {

	private Group group;

	private Label removeLabel;

	Point shellSize;

	Point shellLocation;

	private String infoCenterName = ""; //$NON-NLS-1$

	public RemoveICDialog(Shell parentShell, String infoCenterName) {

		super(parentShell);
		this.infoCenterName = infoCenterName;
	}

	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"org.eclipse.help.ui.prefPageHelpContent"); //$NON-NLS-1$

		Composite composite = (Composite) super.createDialogArea(parent);
		//add controls to composite as necessary

		createGroup(parent);

		//Create Button Bar
		this.createButtonBar(parent);

		return composite;

	}

	public void initializeBounds() {
		shellSize = getInitialSize();
		shellLocation = getInitialLocation(shellSize);

		this.getShell().setBounds(shellLocation.x, shellLocation.y,
				shellSize.x + 130, shellSize.y - 55);
		this.getShell().setText(NLS.bind(Messages.RemoveICDialog_2 ,infoCenterName));
	}

	/*
	 * Create the "Location" group.
	 */
	private void createGroup(Composite parent) {
		group = new Group(parent, SWT.NONE);
		group.setText(Messages.RemoveICDialog_4);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		createRemoveSection(group);

	}

	/*
	 * Create the "Host:" label and text field.
	 */
	private void createRemoveSection(Composite parent) {
		removeLabel = new Label(parent, SWT.NONE);
		removeLabel.setText(NLS.bind(Messages.RemoveICDialog_5 , infoCenterName)
				+ " ?"); //$NON-NLS-1$

	}

	protected void okPressed() {
		this.setReturnCode(OK);
		this.close();
	}

}
