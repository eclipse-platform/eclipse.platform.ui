package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
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
	
	protected void update(IAction action, ISelection s) {
		update();
	}
	
	protected abstract void update();
	
	/**
	 * Remove all actions do care nothing about the current selection
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		if (!isInitialized()) {
			action.setEnabled(false);
			setAction(action);
			setInitialized(true);
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
