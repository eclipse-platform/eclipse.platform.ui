/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

public class SelectAllAction extends AbstractListenerActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		doAction();
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		doAction(null);
	}

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
	
	protected void doAction() {
		if (!(getView() instanceof AbstractDebugView)) {
			return;
		}
		Viewer viewer= ((AbstractDebugView)getView()).getViewer();
		if (!(viewer instanceof TableViewer)) {
			return;
		}
		((TableViewer)viewer).getTable().selectAll();
	}
	
	/**
	 * Enable this action if there are any breakpoints to select
	 */
	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true);
	}
	
	/**
	 * @see AbstractDebugActionDelegate#setActionImages(IAction)
	 */
	protected void setActionImages(IAction action) {
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
	
	/**
	 * @see AbstractDebugActionDelegate#initialize(IAction, ISelection)
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			IDebugViewAdapter debugView= (IDebugViewAdapter)getView().getAdapter(IDebugViewAdapter.class);
			if (debugView != null) {
				debugView.setAction(AbstractDebugView.SELECT_ALL_ACTION, action);
			}
			return super.initialize(action, selection);
		}
		return false;
	}

}
