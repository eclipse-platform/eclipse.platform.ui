package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.ui.actions.DebugAction;

public class DebugDropDownAction extends LaunchDropDownAction {


	public DebugDropDownAction() {
		super(new DebugAction());
	}
	
	protected DebugDropDownAction(ExecutionAction action) {
		super(action);
	}

	/**
	 * @see LaunchDropDownAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
	
	/**
	 * @see LaunchDropDownAction#getHistory()
	 */
	public LaunchConfigurationHistoryElement[] getHistory() {
		return DebugUIPlugin.getLaunchConfigurationManager().getDebugHistory();
	}
	
	/**
	 * @see LaunchDropDownAction#getFavorites()
	 */
	public LaunchConfigurationHistoryElement[] getFavorites() {
		return DebugUIPlugin.getLaunchConfigurationManager().getDebugFavorites();
	}	

	/**
	 * @see LaunchDropDownAction#getLastLaunchPrefix()
	 */
	protected String getLastLaunchPrefix() {
		return ActionMessages.getString("DebugDropDownAction.Debug_last_launched_1"); //$NON-NLS-1$
	}
	
	/**
	 * @see LaunchDropDownAction#getTooltipPrefix()
	 */
	protected String getStaticTooltip() {
		return ActionMessages.getString("DebugDropDownAction.Debug_1"); //$NON-NLS-1$
	}
}

