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
 * run mode.
 */
public class RunAction extends ExecutionAction {
	
	public RunAction() {
		setText(DebugUIMessages.getString("RunAction.R&un@Alt+F9_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RunAction.Run_2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.RUN_ACTION });
	}

	/**
	 * @see ExecutionAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.RUN_MODE;
	}
}