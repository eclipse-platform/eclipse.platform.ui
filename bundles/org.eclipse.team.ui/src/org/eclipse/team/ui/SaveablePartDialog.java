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
package org.eclipse.team.ui;

import org.eclipse.compare.internal.ResizableDialog;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Policy;

/**
 * A dialog that displays a {@link org.eclipse.team.ui.SaveablePartAdapter} and
 * ensures that changes made to the input are saved when the dialog is closed.
 * 
 * @see SaveablePartAdapter
 * @since 3.0
 */
public class SaveablePartDialog extends ResizableDialog {
		
	private ISaveableWorkbenchPart input;
	private Button saveButton;

	/**
	 * Creates a dialog with the given title and input. The input is not created until the dialog
	 * is opened.
	 * 
	 * @param shell the parent shell or <code>null</code> to create a top level shell. 
	 * @param title the shell's title
	 * @param input the compare input to show in the dialog
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
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		saveChanges();
		super.buttonPressed(buttonId);
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
		if (input.isDirty() && MessageDialog.openConfirm(getShell(), Policy.bind("ParticipantCompareDialog.2"), Policy.bind("ParticipantCompareDialog.3"))) {						 //$NON-NLS-1$ //$NON-NLS-2$
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					input.doSave(new NullProgressMonitor());
				}
			});		
		}
	}
}