package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class StepIntoActionDelegate extends StepActionDelegate {
	
	private static final String PREFIX= "step_into_action.";

	/**
	 * @see StepActionDelegate
	 */
	protected boolean checkCapability(IStep element) {
		return element.canStepInto();
	}

	/**
	 * @see StepActionDelegate
	 */
	protected void stepAction(IStep element) throws DebugException {
		element.stepInto();
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.STEP_INTO_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_STEPINTO));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEPINTO));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEPINTO));
	}
}