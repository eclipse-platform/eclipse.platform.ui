package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.WorkbenchWindow;


/**
 * Tests the lifecycle for a window action delegate.
 */
public class IWorkbenchWindowActionDelegateTest extends IActionDelegateTest {

	/**
	 * Constructor for IWorkbenchWindowActionDelegateTest
	 */
	public IWorkbenchWindowActionDelegateTest(String testName) {
		super(testName);
	}

	public void testInit() throws Throwable {
		// From Javadoc: "Initializes this action delegate"
		
		// Run the action.
		testRun();
		
		// Verify lifecycle.
		MockWorkbenchWindowActionDelegate delegate = 
			MockWorkbenchWindowActionDelegate.lastDelegate;
		assertNotNull(delegate);
		assert(delegate.callHistory.contains(delegate, 
			new String [] {"init", "selectionChanged", "run"}));
	}
	
	public void testDispose() throws Throwable {
		// From Javadoc: "Disposes this action delegate."
		
		// Run the action.
		testRun();
		
		// Get the action.
		MockWorkbenchWindowActionDelegate delegate = 
			MockWorkbenchWindowActionDelegate.lastDelegate;
		assertNotNull(delegate);
		delegate.callHistory.clear();
		
		// Dispose action.
		removeAction();
		
		// Verify lifecycle.
		assert(delegate.callHistory.contains(delegate, "dispose"));
	}
	
	/**
	 * @see IActionDelegateTest#runAction()
	 */
	protected void runAction() throws Throwable {
		WorkbenchWindow win = (WorkbenchWindow)fWindow;
		IMenuManager mgr = win.getMenuManager();
		runAction(mgr, "Mock Action");
	}

	/**
	 * @see IActionDelegateTest#addAction()
	 */
	protected void addAction() throws Throwable {
		fPage.showActionSet("org.eclipse.ui.tests.api.MockActionSet");
	}

	/**
	 * Removes the action.
	 */
	protected void removeAction() {
		fPage.hideActionSet("org.eclipse.ui.tests.api.MockActionSet");
	}
	
	/**
	 * @see IActionDelegateTest#fireSelection()
	 */
	protected void fireSelection() throws Throwable {
		MockViewPart view = (MockViewPart)fPage.showView(MockViewPart.ID);
		view.fireSelection();
	}

}

