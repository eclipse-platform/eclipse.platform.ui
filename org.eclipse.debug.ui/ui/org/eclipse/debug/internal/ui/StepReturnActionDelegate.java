package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

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
	 * @see ControlActionDelegate#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.STEP_RETURN_ACTION;
	}

	/**
	 * @see ControlActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_STEPRETURN));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEPRETURN));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEPRETURN));
	}
	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return DebugUIMessages.getString("StepReturnActionDelegate.Run_to_return_failed._1"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return DebugUIMessages.getString("StepReturnActionDelegate.Exceptions_occurred_attempting_to_run_to_return_of_the_frame._2"); //$NON-NLS-1$
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
		return DebugUIMessages.getString("StepReturnActionDelegate.Run_to_Return_3"); //$NON-NLS-1$
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return DebugUIMessages.getString("StepReturnActionDelegate.R&un_to_return_4"); //$NON-NLS-1$
	}
}