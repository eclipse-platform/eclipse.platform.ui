/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
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
		setText(ActionMessages.getString("ConsoleRemoveAllTerminatedAction.Remove_All_Terminated_1")); //$NON-NLS-1$
	}

	public void run() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		RemoveAllTerminatedAction.removeTerminatedLaunches(launches);
	}
	
}
