package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;

public class TerminateActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "terminate_action.";

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object element) throws DebugException {
		if (element instanceof ITerminate) {
			((ITerminate)element).terminate();
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof ITerminate && ((ITerminate)element).canTerminate();
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.TERMINATE_ACTION;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {		
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TERMINATE));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE));
	}	
}