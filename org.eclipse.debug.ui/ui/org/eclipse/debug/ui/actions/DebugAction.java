package org.eclipse.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.actions.ExecutionAction;

/**
 * Action to launch a program in debug mode.
 * <p>
 * This action is selection dependent, and acts on the selection
 * in the current workbench page. If the selected object is
 * a launch configuration, it will be launched in debug mode.
 * Otherwise, the launch configuration dialog will be openned.
 * </p>
 * <p>
 * This class is not intended to be subclassed. This class may
 * be instantiated.
 * </p>
 * @since 2.0
 */
public final class DebugAction extends ExecutionAction /*implements IViewActionDelegate*/ {
	
	public DebugAction() {
		//only used as a delegate
	}

	/**
	 * @see ExecutionAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
	
}