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
import org.eclipse.ui.IViewPart;

public abstract class AbstractRemoveAllActionDelegate extends AbstractListenerActionDelegate {

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		doAction();
		getAction().setEnabled(false);
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		doAction(null);
	}
	
	protected abstract void doAction();

	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
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
	 * @see ControlActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {
		action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_TERMINATED));
		action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_TERMINATED));
		action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_TERMINATED));
	}
	
	/**
	 * Remove all actions do care nothing about the current selection
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
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		setWindow(view.getViewSite().getWorkbenchWindow());
		getPage().addPartListener(this);
		getPage().getWorkbenchWindow().addPageListener(this);
	}
}
