/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;

public class ResizableInstallWizardDialog extends WizardDialog {
	private String title;
	
	/**
	 * Creates a new resizable wizard dialog.
	 */
	public ResizableInstallWizardDialog(Shell parent, IWizard wizard, String title) {
		super(parent, wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.title = title;
	}	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		// TODO Auto-generated method stub
		super.create();
		
		getShell().setText(title);
		SWTUtil.setDialogSize(this, 600, 500);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		int returnValue = super.open();
		
		if (returnValue == IDialogConstants.OK_ID)
			UpdateUI.requestRestart(((InstallWizard)getWizard()).isRestartNeeded());
		return returnValue;
	}
}