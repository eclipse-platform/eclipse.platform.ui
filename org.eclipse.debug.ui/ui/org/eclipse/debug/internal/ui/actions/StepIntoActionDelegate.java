package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
}