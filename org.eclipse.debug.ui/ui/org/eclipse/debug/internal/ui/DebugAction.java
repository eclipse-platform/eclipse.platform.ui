package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * An execution action that uses launchers in
 * debug mode.
 */
public class DebugAction extends ExecutionAction {
	
	protected final static String PREFIX= "debug_action.";
	
	public DebugAction() {
		setText(DebugUIUtils.getResourceString(PREFIX + TEXT));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
	}

	/**
	 * @see ExecutionAction
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}

}

