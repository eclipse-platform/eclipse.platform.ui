package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;

/**
 * Relaunches the last debug-mode launch
 */
public class DebugLastAction extends RelaunchLastAction {
	
	/**
	 * @see RelaunchLastAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
	
}
