package org.eclipse.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.actions.ExecutionAction;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Action to launch the last launch configuration that was successfully
 * launched, in debug mode. If no configurations have been launched, the launch
 * configuration dialog is opened.
 * <p>
 * This class is not intended to be subclassed. This class may
 * be instantiated.
 * </p>
 * @since 2.0
 */
public final class DebugAction extends ExecutionAction {
	
	public DebugAction() {
		super(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
	}

	/**
	 * @see ExecutionAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
	
}