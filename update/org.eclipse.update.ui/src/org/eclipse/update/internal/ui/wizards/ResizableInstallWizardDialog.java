/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.operations.*;

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
		super.create();
		
		getShell().setText(title);
		SWTUtil.setDialogSize(this, 600, 500);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(
					getShell(),
					null,
					null,
					status);
			return IDialogConstants.ABORT_ID;
		}
		
		return super.open();
	}
}
