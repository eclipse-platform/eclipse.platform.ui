package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

public class DebugDropDownAction extends LaunchDropDownAction {

	public DebugDropDownAction() {
		super(new DebugAction());
	}

	/**
	 * @see LaunchDropDownAction
	 */
	public String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}

	/**
	 * @see LaunchDropDownAction
	 */
	public ILaunch[] getHistory() {
		return DebugUIPlugin.getDefault().getDebugHistory();
	}
}

