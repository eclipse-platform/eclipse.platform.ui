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
		// From Javadoc: "Initializes this action delegate"
		
		// Run the action.
		testRun();
		
		// Verify lifecycle.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.contains( 
			new String [] {"init", "selectionChanged", "run"}));
	}
	
	public void testDispose() throws Throwable {
		// From Javadoc: "Disposes this action delegate."
		
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
	protected void runAction() throws Throwable {
		ActionUtil.runActionWithLabel(this, fWindow, "Mock Action");
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

	/**
	 * @see IActionDelegateTest#getDelegate()
	 */
	protected MockActionDelegate getDelegate() throws Throwable {
		MockWorkbenchWindowActionDelegate delegate = 
			MockWorkbenchWindowActionDelegate.lastDelegate;
		assertNotNull(delegate);
		return delegate;
	}

}

