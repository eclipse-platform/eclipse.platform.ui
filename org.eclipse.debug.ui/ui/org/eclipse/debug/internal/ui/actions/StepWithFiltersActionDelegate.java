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
import org.eclipse.debug.core.model.IFilteredStep;
import org.eclipse.debug.core.model.IStep;

public class StepWithFiltersActionDelegate extends StepActionDelegate {
	
	/**
	 * @see StepActionDelegate#checkCapability(IStep)
	 */
	protected boolean checkCapability(IStep element) {
		if (element instanceof IFilteredStep) {
			return ((IFilteredStep)element).canStepWithFilters();
		}
		return false;
	}

	/**
	 * @see StepActionDelegate#stepAction(IStep)
	 */
	protected void stepAction(IStep element) throws DebugException {
		if (element instanceof IFilteredStep) {
			((IFilteredStep)element).stepWithFilters();
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("StepWithFiltersActionDelegate.Exceptions_occurred_attempting_to_step_into_the_frame_with_step_filters_applied._1"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("StepWithFiltersActionDelegate.Step_with_filters_failed._2"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("StepWithFiltersActionDelegate.Step_With_Filters_3"); //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.debug.internal.ui.actions.StepActionDelegate#getActionDefinitionId()
	 */
	protected String getActionDefinitionId() {
		return "org.eclipse.debug.internal.ui.actions.StepWithFiltersActionDelegate"; //$NON-NLS-1$
	}
}
