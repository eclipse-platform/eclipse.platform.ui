package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.test.harness.util.CallHistory;

public class MockActionDelegate implements IActionDelegate {

	public static final String ACTION_SET_ID = "org.eclipse.ui.tests.api.MockActionSet";
	
	public CallHistory callHistory;
	
	public MockActionDelegate() {
		callHistory = new CallHistory(this);
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
}

