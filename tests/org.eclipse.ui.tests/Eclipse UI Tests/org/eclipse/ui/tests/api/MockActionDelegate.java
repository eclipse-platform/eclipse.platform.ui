package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.junit.util.*;

public class MockActionDelegate implements IActionDelegate {

	public CallHistory callHistory;
	public static final String ACTION_SET_ID = "org.eclipse.ui.tests.api.MockActionSet";
	public static MockActionDelegate lastDelegate;
	
	public MockActionDelegate() {
		callHistory = new CallHistory(this);
		lastDelegate = this;
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

