package org.eclipse.debug.internal.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public class StepIntoActionDelegate extends StepActionDelegate {
	
	/**
	 * @see StepActionDelegate#checkCapability(IStep)
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepInto();
	}

	/**
	 * @see StepActionDelegate#stepAction(IStep)
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepInto();
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("StepIntoActionDelegate.Exceptions_occurred_attempting_to_step_into_the_frame_2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("StepIntoActionDelegate.Step_into_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("StepIntoActionDelegate.Step_Into_3"); //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.debug.internal.ui.actions.StepActionDelegate#getActionDefinitionId()
	 */
	protected String getActionDefinitionId() {
		return "org.eclipse.debug.internal.ui.actions.StepIntoActionDelegate";
	}
}