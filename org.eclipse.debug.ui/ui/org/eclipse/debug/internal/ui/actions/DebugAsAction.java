package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.LaunchAsAction;

/**
 * "Debug As" action in the top level "Run" menu.
 */
public class DebugAsAction extends LaunchAsAction {

	public DebugAsAction() {
		super(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
	}

}
