package org.eclipse.ui.tests.api;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class MockWorkbenchWindowActionDelegate extends MockActionDelegate 
	implements IWorkbenchWindowActionDelegate
{
	public static MockWorkbenchWindowActionDelegate lastDelegate;
	public static String SET_ID = "org.eclipse.ui.tests.api.MockActionSet";
	public static String ID = "org.eclipse.ui.tests.api.MockWindowAction";

	/**
	 * Constructor for MockWorkbenchWindowActionDelegate
	 */
	public MockWorkbenchWindowActionDelegate() {
		super();
		lastDelegate = this;
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		callHistory.add(this, "init");
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		callHistory.add(this, "dispose");
	}
}

