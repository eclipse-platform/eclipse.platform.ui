package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

public class LaunchConfigurationClearWorkingSetAction extends Action {
	
	private LaunchConfigurationWorkingSetActionManager fActionMgr;

	public LaunchConfigurationClearWorkingSetAction(LaunchConfigurationWorkingSetActionManager actionMgr) {
		super(LaunchConfigurationsMessages.getString("LaunchConfigurationClearWorkingSetAction.Deselect_working_set_1")); //$NON-NLS-1$
		setToolTipText(LaunchConfigurationsMessages.getString("LaunchConfigurationClearWorkingSetAction.Remove_the_the_current_working_as_a_filter_for_this_view_2")); //$NON-NLS-1$
		setEnabled(actionMgr.getWorkingSet() != null);
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.CLEAR_WORKING_SET_ACTION);
		fActionMgr = actionMgr;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fActionMgr.setWorkingSet(null, true);
	}

}
