package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
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
	 * @see LaunchDropDownAction#getHistory()
	 */
	public LaunchConfigurationHistoryElement[] getHistory() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getRunHistory();
	}
	
	/**
	 * @see LaunchDropDownAction#getFavorites()
	 */
	public LaunchConfigurationHistoryElement[] getFavorites() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getRunFavorites();
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
}

