package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ResumeActionDelegate extends ControlActionDelegate {

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object object) throws DebugException {
		IDebugElement element= (IDebugElement)object;
		if (element instanceof ISuspendResume) {
			ISuspendResume suspendResume= (ISuspendResume)element;
			if (suspendResume.canResume()) {
				suspendResume.resume();
			}
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof ISuspendResume && ((ISuspendResume) element).canResume();
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
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.RESUME_ACTION;
	}

	/**
	 * @see ControlActionDelegate
	 */
	protected void setActionImages(IAction action) {	
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_RESUME));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RESUME));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESUME));
	}	
	/*
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.getString("ResumeActionDelegate.Resume_failed._1"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("ResumeActionDelegate.Exceptions_occurred_attempting_to_resume._2"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return getToolTipText();
	}

	/*
	 * @see ControlActionDelegate#getToolTipText()
	 */
	protected String getToolTipText() {
		return ActionMessages.getString("ResumeActionDelegate.Resume_3"); //$NON-NLS-1$
	}

	/*
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return ActionMessages.getString("ResumeActionDelegate.&Resume_4"); //$NON-NLS-1$
	}
}