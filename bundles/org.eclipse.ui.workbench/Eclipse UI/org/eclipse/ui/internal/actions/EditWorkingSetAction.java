/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Displays an IWorkingSetEditWizard for editing a working set.
 * 
 * @since 2.1
 */
public class EditWorkingSetAction extends Action {
	private Shell shell;
	private WorkingSetFilterActionGroup actionGroup;

	/**
	 * Creates a new instance of the receiver.
	 * 
	 * @param actionGroup the action group this action is created in
	 */
	public EditWorkingSetAction(WorkingSetFilterActionGroup actionGroup, Shell shell) {
		super(WorkbenchMessages.getString("EditWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkbenchMessages.getString("EditWorkingSetAction.toolTip")); //$NON-NLS-1$
		
		this.shell = shell;
		this.actionGroup = actionGroup;
		WorkbenchHelp.setHelp(this, IHelpContextIds.EDIT_WORKING_SET_ACTION);
	}
	/**
	 * Overrides method from Action
	 * 
	 * @see org.eclipse.jface.Action#run
	 */
	public void run() {
		if (shell == null)
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = actionGroup.getWorkingSet();
		
		if (workingSet == null) {
			setEnabled(false);
			return;
		}
		IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard(workingSet);
		if (wizard == null) {
			String title = WorkbenchMessages.getString("EditWorkingSetAction.error.nowizard.title"); //$NON-NLS-1$
			String message = WorkbenchMessages.getString("EditWorkingSetAction.error.nowizard.message"); //$NON-NLS-1$
			MessageDialog.openError(shell, title, message);
			return;
		}
		WizardDialog dialog = new WizardDialog(shell, wizard);
	 	dialog.create();		
		if (dialog.open() == WizardDialog.OK)
			actionGroup.setWorkingSet(wizard.getSelection());
	}
}
