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
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Clears the selected working set in the working set action group.
 * 
 * @since 2.1
 */
public class ClearWorkingSetAction extends Action {
	private WorkingSetFilterActionGroup actionGroup;

	/**
	 * Creates a new instance of the receiver.
	 * 
	 * @param actionGroup the action group this action is created in
	 */
	public ClearWorkingSetAction(WorkingSetFilterActionGroup actionGroup) {
		super(WorkbenchMessages.getString("ClearWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkbenchMessages.getString("ClearWorkingSetAction.toolTip")); //$NON-NLS-1$
		setEnabled(actionGroup.getWorkingSet() != null);
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLEAR_WORKING_SET_ACTION);
		this.actionGroup = actionGroup;
	}
	/**
	 * Overrides method from Action
	 * 
	 * @see org.eclipse.jface.Action#run
	 */
	public void run() {
		actionGroup.setWorkingSet(null);
	}
}
