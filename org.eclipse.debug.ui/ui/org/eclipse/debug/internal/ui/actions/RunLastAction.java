package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;

/**
 * Relaunches the last run-mode launch
 */
public class RunLastAction extends RelaunchLastAction {

	/**
	 * @see RelaunchLastAction#getHistory()
	 */
	public LaunchConfigurationHistoryElement[] getHistory() {
		return DebugUIPlugin.getLaunchConfigurationManager().getRunHistory();
	}

}
