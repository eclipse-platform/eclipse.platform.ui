package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;

public class RunDropDownAction extends LaunchDropDownAction {

	public RunDropDownAction() {
		super(new RunAction());
	}

	/**
	 * @see LaunchDropDownAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}

	/**
	 * @see LaunchDropDownAction#getHistory()
	 */
	public LaunchHistoryElement[] getHistory() {
		return DebugUIPlugin.getDefault().getRunHistory();
	}
}

