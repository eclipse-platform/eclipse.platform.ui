/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import org.eclipse.compare.internal.ResizableDialog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.TeamUIMessages;

/**
 * A dialog that displays a {@link org.eclipse.team.ui.ISaveableWorkbenchPart} and
 * ensures that changes made to the input are saved when the dialog is closed.
 * 
 * @see ISaveableWorkbenchPart
 * @see SaveablePartAdapter
 * @since 3.0
 */
public class SaveablePartDialog extends ResizableDialog {
		
	private ISaveableWorkbenchPart input;

	/**
	 * Creates a dialog with the given title and input. The input is not created until the dialog
	 * is opened.
	 * 
	 * @param shell the parent shell or <code>null</code> to create a top level shell. 
	 * @param input the part to show in the dialog.
	 */
	public SaveablePartDialog(Shell shell, ISaveableWorkbenchPart input) {
		super(shell, null);
		this.input = input;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent2) {
		Composite parent = (Composite) super.createDialogArea(parent2);
		input.createPartControl(parent);
		Shell shell = getShell();
		shell.setText(input.getTitle());
		shell.setImage(input.getTitleImage());
		Dialog.applyDialogFont(parent2);
		return parent;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.compare.internal.ResizableDialog#close()
	 */
	public boolean close() {
		saveChanges();
		return super.close();
	}
	
	/**
	 * Save any changes to the compare editor.
	 */
	private void saveChanges() {
		MessageDialog dialog = new MessageDialog(
				getShell(), TeamUIMessages.ParticipantCompareDialog_2, null,  
				TeamUIMessages.ParticipantCompareDialog_3, MessageDialog.QUESTION, new String[]{IDialogConstants.YES_LABEL, 
				IDialogConstants.NO_LABEL}, 0); // YES is the default
			
		if (input.isDirty() && dialog.open() == IDialogConstants.OK_ID) {
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					input.doSave(new NullProgressMonitor());
				}
			});		
		}
	}
}
