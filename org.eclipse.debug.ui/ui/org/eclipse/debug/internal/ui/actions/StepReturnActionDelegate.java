package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public class StepReturnActionDelegate extends StepActionDelegate {

	/**
	 * @see StepActionDelegate#checkCapability(IStep)
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepReturn();
	}

	/**
	 * @see StepActionDelegate#stepAction(IStep)
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepReturn();
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("StepReturnActionDelegate.Run_to_return_failed._1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("StepReturnActionDelegate.Exceptions_occurred_attempting_to_run_to_return_of_the_frame._2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("StepReturnActionDelegate.Run_to_Return_3"); //$NON-NLS-1$
	}	
}