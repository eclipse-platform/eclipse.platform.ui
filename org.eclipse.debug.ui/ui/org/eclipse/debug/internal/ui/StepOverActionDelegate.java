package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class StepOverActionDelegate extends StepActionDelegate {
	
	private static final String PREFIX= "step_over_action.";

	/**
	 * @see StepActionDelegate
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepOver();
	}

	/**
	 * @see StepActionDelegate
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepOver();
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.STEP_OVER_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_STEPOVER));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEPOVER));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEPOVER));
	}
}