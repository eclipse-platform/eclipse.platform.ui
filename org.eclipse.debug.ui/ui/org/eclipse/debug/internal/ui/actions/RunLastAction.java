package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;

/**
 * Relaunches the last run-mode launch
 */
public class RunLastAction extends RelaunchLastAction {

	/**
	 * @see RelaunchLastAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}
	
}
