/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public class StepOverActionDelegate extends StepActionDelegate {

	/**
	 * @see StepActionDelegate#checkCapability(IStep)
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepOver();
	}

	/**
	 * @see StepActionDelegate#stepAction(IStep)
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepOver();
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("StepOverActionDelegate.Exceptions_occurred_attempting_to_step_over_the_frame_2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("StepOverActionDelegate.Step_over_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("StepOverActionDelegate.Step_over_3"); //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.debug.internal.ui.actions.StepActionDelegate#getActionDefinitionId()
	 */
	 protected String getActionDefinitionId() {
		return "org.eclipse.debug.internal.ui.actions.StepOverActionDelegate"; //$NON-NLS-1$
	}
}
