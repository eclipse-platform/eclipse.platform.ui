package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.test.harness.util.*;

public class MockAction implements IWorkbenchWindowActionDelegate {
	public static String SET_ID = "org.eclipse.ui.tests.api.MockActionSet";
	public static String ID = "org.eclipse.ui.tests.api.MockAction";
	protected CallHistory callTrace;
	
	public MockAction() {		
		callTrace = new CallHistory();
		System.out.println( "ooh hoo" );
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
	}	

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		callTrace.add( this, "dispose" );
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		callTrace.add( this, "init" );
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		callTrace.add( this, "run" );
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		callTrace.add( this, "selectionChanged" );
	}
}

