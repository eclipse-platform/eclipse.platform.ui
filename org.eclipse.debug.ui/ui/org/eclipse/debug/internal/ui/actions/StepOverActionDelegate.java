package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class StepOverActionDelegate extends StepActionDelegate {

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
	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("StepOverActionDelegate.Step_over_failed_1"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("StepOverActionDelegate.Exceptions_occurred_attempting_to_step_over_the_frame_2"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return getToolTipText();
	}

	/**
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return ActionMessages.getString("StepOverActionDelegate.Step_over_3"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return ActionMessages.getString("StepOverActionDelegate.Step_O&ver_4"); //$NON-NLS-1$
	}
}