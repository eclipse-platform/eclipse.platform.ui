package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.junit.util.*;


/**
 * Tests the lifecycle for a window action delegate.
 */
public class IViewActionDelegateTest extends IActionDelegateTest {
	
	public static String TEST_VIEW_ID = "org.eclipse.ui.tests.api.ActionExtensionTestView";

	/**
	 * Constructor for IWorkbenchWindowActionDelegateTest
	 */
	public IViewActionDelegateTest(String testName) {
		super(testName);
	}

	public void testInit() throws Throwable {
		// From Javadoc: "Initializes this action delegate"
		
		// Run the action.
		testRun();
		
		// Verify lifecycle.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.verifyOrder(
			new String [] {"init", "selectionChanged", "run"}));
	}
	
	/**
	 * @see IActionDelegateTest#runAction()
	 */
	protected void runAction() throws Throwable {
		MockViewPart view = (MockViewPart)fPage.showView(TEST_VIEW_ID);
		IMenuManager mgr = view.getViewSite().getActionBars().getMenuManager();
		ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
	}

	/**
	 * @see IActionDelegateTest#addAction()
	 */
	protected void addAction() throws Throwable {
		// Open up a view with the specific view action extension.
		fPage.showView(TEST_VIEW_ID);
	}

	/**
	 * @see IActionDelegateTest#fireSelection()
	 */
	protected void fireSelection() throws Throwable {
		MockViewPart view = (MockViewPart)fPage.showView(TEST_VIEW_ID);
		view.fireSelection();
	}

	/**
	 * @see IActionDelegateTest#getDelegate()
	 */
	protected MockActionDelegate getDelegate() throws Throwable {
		MockViewActionDelegate delegate = 
			MockViewActionDelegate.lastDelegate;
		assertNotNull(delegate);
		return delegate;
	}

}

