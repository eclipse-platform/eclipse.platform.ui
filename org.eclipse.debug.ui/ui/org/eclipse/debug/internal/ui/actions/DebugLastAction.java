package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;

/**
 * Relaunches the last debug-mode launch
 */
public class DebugLastAction extends RelaunchLastAction {
	
	/**
	 * Returns the debug launch history.
	 */
	public LaunchConfigurationHistoryElement[] getHistory() {
		return DebugUIPlugin.getLaunchConfigurationManager().getDebugHistory();
	}

}
