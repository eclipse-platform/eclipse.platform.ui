package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;

public class StepReturnActionDelegate extends StepActionDelegate {
	
	private static final String PREFIX= "step_return_action.";

	/**
	 * @see StepActionDelegate
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepReturn();
	}

	/**
	 * @see StepActionDelegate
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepReturn();
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
}