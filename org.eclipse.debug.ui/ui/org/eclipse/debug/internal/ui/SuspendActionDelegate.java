package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SuspendActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "suspend_action.";

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof ISuspendResume) {
			 ((ISuspendResume) element).suspend();
		}
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof ISuspendResume && ((ISuspendResume)element).canSuspend();
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean getEnableStateForSelection(IStructuredSelection selection) {	 		
		if (selection.size() == 1) {
			return isEnabledFor(selection.getFirstElement());
		} else {
			 return false;
		}
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SUSPEND_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_SUSPEND));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_SUSPEND));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_SUSPEND));
	}
}