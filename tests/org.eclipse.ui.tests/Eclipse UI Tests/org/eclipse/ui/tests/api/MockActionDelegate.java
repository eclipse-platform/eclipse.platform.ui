package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.util.CallHistory;

public class MockActionDelegate implements IWorkbenchWindowActionDelegate {

	public CallHistory callHistory;
	public static final String ACTION_SET_ID = "org.eclipse.ui.tests.api.MockActionSet";
	public static MockActionDelegate lastDelegate;
	
	public MockActionDelegate() {
		callHistory = new CallHistory(this);
		lastDelegate = this;
	}
	
	/*
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		callHistory.add("run");
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		callHistory.add("selectionChanged");
	}
	
	/*
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

}

