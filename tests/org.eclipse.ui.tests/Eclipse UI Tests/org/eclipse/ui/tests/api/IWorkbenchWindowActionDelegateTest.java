package org.eclipse.ui.tests.api;

import org.eclipse.ui.junit.util.*;

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
		// When an action delegate is run the
		// init, selectionChanged, and run methods should
		// be called, in that order.
		
		// Run the action.
		testRun();
		
		// Verify lifecycle.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.verifyOrder( 
			new String [] {"init", "selectionChanged", "run"}));
	}
	
	public void testDispose() throws Throwable {
		// When an action delegate is removed from the window
		// the dispose method should be called.
		
		// Run the action.
		testRun();
		
		// Get the action.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		delegate.callHistory.clear();
		
		// Dispose action.
		removeAction();
		
		// Verify lifecycle.
		assertTrue(delegate.callHistory.contains("dispose"));
	}
	
	/**
	 * @see IActionDelegateTest#runAction()
	 */
	protected void addAndRunAction() throws Throwable {
		fPage.showActionSet("org.eclipse.ui.tests.api.MockActionSet");
		ActionUtil.runActionWithLabel(this, fWindow, "Mock Action");
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

