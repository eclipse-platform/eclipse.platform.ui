package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * An execution action that uses launchers in
 * run mode.
 */
public class RunAction extends ExecutionAction {
	
	protected final static String PREFIX= "run_action.";	
	
	public RunAction() {
		setText(DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN));
	}

	/**
	 * @see ExecutionAction
	 */
	protected String getMode() {
		return ILaunchManager.RUN_MODE;
	}
}

