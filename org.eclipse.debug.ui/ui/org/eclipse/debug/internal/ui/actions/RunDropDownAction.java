package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.RunAction;

public class RunDropDownAction extends LaunchDropDownAction {

	public RunDropDownAction() {
		super(new RunAction());
	}
	
	protected RunDropDownAction(ExecutionAction action) {
		super(action);
	}

	/**
	 * @see LaunchDropDownAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}

	/**
	 * @see LaunchDropDownAction#getLastLaunchPrefix()
	 */
	protected String getLastLaunchPrefix() {
		return ActionMessages.getString("RunDropDownAction.Run_last_launched_1"); //$NON-NLS-1$
	}
	/**
	 * @see LaunchDropDownAction#getTooltipPrefix()
	 */
	protected String getStaticTooltip() {
		return ActionMessages.getString("RunDropDownAction.Run_1"); //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.debug.internal.ui.actions.LaunchDropDownAction#getLaunchGroupId()
	 */
	public String getLaunchGroupId() {
		return IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
	}

}

