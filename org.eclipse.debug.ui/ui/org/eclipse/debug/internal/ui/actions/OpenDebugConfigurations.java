package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.OpenLaunchDialogAction;

/**
 * Opens the launch config dialog on the debug launch group.
 */
public class OpenDebugConfigurations extends OpenLaunchDialogAction {

	public OpenDebugConfigurations() {
		super(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
	}
	
}
