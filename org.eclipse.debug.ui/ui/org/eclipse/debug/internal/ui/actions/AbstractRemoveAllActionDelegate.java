package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public abstract class AbstractRemoveAllActionDelegate extends AbstractListenerActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) throws DebugException {
		doAction();
		getAction().setEnabled(false);
	}
	
	protected abstract void doAction() throws DebugException;

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return true;
	}

	/**
	 * @see AbstractDebugActionDelegate#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return null;
	}
	
	protected void update(IAction action, ISelection s) {
		update();
	}
	
	protected abstract void update();
	
	/**
	 * @see ControlActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return null;
	}

	/**
	 * @see ControlActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return null;
	}

	/**
	 * @see ControlActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return null;
	}

	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return null;
	}
	
	/**
	 * @see ControlActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED));
	}
	
	/**
	 * RemoveTerminatedAction cares nothing about the current selection
	 */
	public void selectionChanged(IAction action, ISelection s) {
		if (!fInitialized) {
			action.setEnabled(false);
			setAction(action);
			setActionImages(action);
			fInitialized = true;
		}
		update();
	}
}
