/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ui;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to alter the triggers of an ICommand that represents a builder.
 */
public class EditCommandDialog extends Dialog {

	private Button fFullButton;
	private Button fIncrementalButton;
	private Button fAutoButton;
	private Button fCleanButton;
	
	private ICommand fCommand;
	
	public EditCommandDialog(Shell parentShell, ICommand command) {
		super(parentShell);
		fCommand= command;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		getShell().setText(ExternalToolsUIMessages.EditCommandDialog_0);
		Composite composite = (Composite)super.createDialogArea(parent);
		
		Label label= new Label(composite, SWT.NONE);
		label.setText(ExternalToolsUIMessages.EditCommandDialog_1);
		
		fFullButton = new Button(composite, SWT.CHECK);
		fFullButton.setText(ExternalToolsUIMessages.EditCommandDialog_2);
		fFullButton.setSelection(fCommand.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		fIncrementalButton = new Button(composite, SWT.CHECK);
		fIncrementalButton.setText(ExternalToolsUIMessages.EditCommandDialog_3);
		fIncrementalButton.setSelection(fCommand.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		fAutoButton = new Button(composite, SWT.CHECK);
		fAutoButton.setText(ExternalToolsUIMessages.EditCommandDialog_4);
		fAutoButton.setSelection(fCommand.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		
		fCleanButton = new Button(composite, SWT.CHECK);
		fCleanButton.setText(ExternalToolsUIMessages.EditCommandDialog_5);
		fCleanButton.setSelection(fCommand.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		applyDialogFont(composite);
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fCommand.setBuilding(IncrementalProjectBuilder.FULL_BUILD, fFullButton.getSelection());
		fCommand.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, fIncrementalButton.getSelection());
		fCommand.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, fAutoButton.getSelection());
		fCommand.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, fCleanButton.getSelection());
		
		super.okPressed();
	}
}
