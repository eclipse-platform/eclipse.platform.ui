package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;

public class RunWithConfigurationAction extends LaunchWithConfigurationAction {

	/**
	 * @see LaunchWithConfigurationAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;		
	}
}
