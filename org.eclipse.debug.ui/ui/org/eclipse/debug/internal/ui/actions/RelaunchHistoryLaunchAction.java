package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Re-launches a previous launch.
 */
public class RelaunchHistoryLaunchAction extends Action {

	private ILaunchConfiguration fConfiguration;
	private String fMode;
	
	public RelaunchHistoryLaunchAction(ILaunchConfiguration configuration, String mode) {
		super();
		fConfiguration = configuration;
		fMode = mode;
		setText(configuration.getName());
		ImageDescriptor descriptor= null;
		descriptor = DebugUITools.getDefaultImageDescriptor(configuration);
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
		DebugUITools.launch(fConfiguration, fMode);
	}
}
