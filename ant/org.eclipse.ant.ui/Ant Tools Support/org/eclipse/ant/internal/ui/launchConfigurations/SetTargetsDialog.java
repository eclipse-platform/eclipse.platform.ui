/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SetTargetsDialog extends Dialog {
	
	private ILaunchConfigurationWorkingCopy fConfiguration;
	private AntTargetsTab fTargetsTab;
	
	public SetTargetsDialog(Shell parentShell, ILaunchConfigurationWorkingCopy config) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | getShellStyle());
		fConfiguration= config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		getShell().setText("Set Targets");
		Composite composite = (Composite)super.createDialogArea(parent);
		
		fTargetsTab= new AntTargetsTab();
		fTargetsTab.createControl(composite);
		fTargetsTab.initializeFrom(fConfiguration);
		applyDialogFont(composite);
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fTargetsTab.performApply(fConfiguration);
		super.okPressed();
	}
}
