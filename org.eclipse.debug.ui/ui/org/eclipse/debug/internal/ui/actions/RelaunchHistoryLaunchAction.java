package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Re-launches a previous launch.
 */
public class RelaunchHistoryLaunchAction extends Action {

	protected LaunchConfigurationHistoryElement fLaunch;
	
	public RelaunchHistoryLaunchAction(LaunchConfigurationHistoryElement launch) {
		super();
		fLaunch= launch;
		setText(launch.getLabel());
		ImageDescriptor descriptor= null;
		if (launch.getLaunchConfiguration() != null) {
			descriptor = DebugUITools.getDefaultImageDescriptor(launch.getLaunchConfiguration());
		} else {
			if (launch.getMode().equals(ILaunchManager.DEBUG_MODE)) {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
			} else {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
			}
		}

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.RELAUNCH_HISTORY_ACTION);
	}

	/**
	 * @see IAction
	 */
	public void run() {
		if (!DebugUITools.saveAndBuildBeforeLaunch()) {
			return;
		}
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				RelaunchActionDelegate.relaunch(fLaunch);
			}
		});
	}
}
