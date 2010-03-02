/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
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
     * @param shell the parent shell
     */
    public EditWorkingSetAction(WorkingSetFilterActionGroup actionGroup,
            Shell shell) {
        super(WorkbenchMessages.EditWorkingSetAction_text);
        Assert.isNotNull(actionGroup);
        setToolTipText(WorkbenchMessages.EditWorkingSetAction_toolTip); 

        this.shell = shell;
        this.actionGroup = actionGroup;
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.EDIT_WORKING_SET_ACTION);
    }

    /**
     * Overrides method from Action
     * 
     * @see Action#run
     */
    public void run() {
        IWorkingSetManager manager = PlatformUI.getWorkbench()
                .getWorkingSetManager();
        IWorkingSet workingSet = actionGroup.getWorkingSet();

        if (workingSet == null) {
            setEnabled(false);
            return;
        }
        IWorkingSetEditWizard wizard = manager
                .createWorkingSetEditWizard(workingSet);
        if (wizard == null) {
            String title = WorkbenchMessages.EditWorkingSetAction_error_nowizard_title; 
            String message = WorkbenchMessages.EditWorkingSetAction_error_nowizard_message;
            MessageDialog.openError(shell, title, message);
            return;
        }
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        if (dialog.open() == Window.OK) {
			actionGroup.setWorkingSet(wizard.getSelection());
		}
    }
}
