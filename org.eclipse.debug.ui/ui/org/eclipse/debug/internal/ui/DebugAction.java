package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An execution action that uses launchers in
 * debug mode.
 */
public class DebugAction extends ExecutionAction {
	
	public DebugAction() {
		setText(DebugUIMessages.getString("DebugAction.&Debug@F9_1")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG));
		setToolTipText(DebugUIMessages.getString("DebugAction.Debug_2")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.DEBUG_ACTION });
	}

	/**
	 * @see ExecutionAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
}

