package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.actions.RemoveAllTerminatedAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Action to remove all terminated launches that lives in the ConsoleView 
 * drop-down toolbar menu.  This action makes use of static methods in the 
 * RemoveAllTerminated action delegate.
 */
public class ConsoleRemoveAllTerminatedAction extends Action {

	public ConsoleRemoveAllTerminatedAction() {
		ImageDescriptor imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL);
		setImageDescriptor(imageDescriptor);
		setText("Remove All Terminated Launches");
	}

	public void run() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		RemoveAllTerminatedAction.removeTerminatedLaunches(launches);
	}
	
}
