package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

public abstract class AbstractRemoveActionDelegate extends AbstractListenerActionDelegate {
	
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
			IDebugView debugView= (IDebugView)getView().getAdapter(IDebugView.class);
			if (debugView != null) {
				debugView.setAction(AbstractDebugView.REMOVE_ACTION, action);
			}
			return super.initialize(action, selection);
		}
		return false;
	}
}
