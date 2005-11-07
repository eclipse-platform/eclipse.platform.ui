/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.actions.ActionMessages;

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
		return ActionMessages.StepReturnActionDelegate_Exceptions_occurred_attempting_to_run_to_return_of_the_frame__2; 
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.StepReturnActionDelegate_Run_to_return_failed__1; 
	}

	/**
	 * @see org.eclipse.debug.internal.ui.actions.
	 * StepActionDelegate#getActionDefinitionId()
	 */
	protected String getActionDefinitionId() {
		return "org.eclipse.debug.internal.ui.actions.StepReturnActionDelegate"; //$NON-NLS-1$
	}
}
