package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.ExecutionAction;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to launch a program in debug mode.
 * <p>
 * This action is selection dependent, and acts on the selection
 * in the current workbench page. If the selected object is
 * a launch configuration, it will be launched in debug mode.
 * Otherwise, the launch configuration dialog will be openned.
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 */
public final class DebugAction extends ExecutionAction implements IViewActionDelegate {
	
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

