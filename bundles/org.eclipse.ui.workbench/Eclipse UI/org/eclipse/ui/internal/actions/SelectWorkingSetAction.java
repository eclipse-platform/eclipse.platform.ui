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

package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Displays an IWorkingSetSelectionDialog and sets the selected 
 * working set in the action group.
 * 
 * @since 2.1
 */
public class SelectWorkingSetAction extends Action {
	private Shell shell;
	private WorkingSetFilterActionGroup actionGroup;

	/**
	 * Creates a new instance of the receiver.
	 * 
	 * @param actionGroup the action group this action is created in
	 * @param shell shell to use for opening working set selection dialog.
	 */
	public SelectWorkingSetAction(WorkingSetFilterActionGroup actionGroup, Shell shell) {
		super(WorkbenchMessages.getString("SelectWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkbenchMessages.getString("SelectWorkingSetAction.toolTip")); //$NON-NLS-1$
		
		this.shell = shell;
		this.actionGroup = actionGroup;
		WorkbenchHelp.setHelp(this, IHelpContextIds.SELECT_WORKING_SET_ACTION);
	}
	/**
	 * Overrides method from Action
	 * 
	 * @see org.eclipse.jface.Action#run
	 */
	public void run() {
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog = manager.createWorkingSetSelectionDialog(shell, false);
		IWorkingSet workingSet = actionGroup.getWorkingSet();
		
		if (workingSet != null)
			dialog.setSelection(new IWorkingSet[]{workingSet});

		if (dialog.open() == Window.OK) {
			IWorkingSet[] result = dialog.getSelection();
			if (result != null && result.length > 0) {
				actionGroup.setWorkingSet(result[0]);
				manager.addRecentWorkingSet(result[0]);
			}
			else {
				actionGroup.setWorkingSet(null);
			}
		}
		else
			actionGroup.setWorkingSet(workingSet);
	}
}
