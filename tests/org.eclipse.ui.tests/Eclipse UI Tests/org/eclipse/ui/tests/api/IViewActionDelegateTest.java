package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jdt.junit.util.*;


/**
 * Tests the lifecycle for a view action delegate.
 */
public class IViewActionDelegateTest extends IActionDelegateTest {
	
	public static String TEST_VIEW_ID = "org.eclipse.ui.tests.api.IViewActionDelegateTest";

	/**
	 * Constructor for IWorkbenchWindowActionDelegateTest
	 */
	public IViewActionDelegateTest(String testName) {
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
	
	/**
	 * @see IActionDelegateTest#runAction()
	 */
	protected void addAndRunAction() throws Throwable {
		MockViewPart view = (MockViewPart)fPage.showView(TEST_VIEW_ID);
		IMenuManager mgr = view.getViewSite().getActionBars().getMenuManager();
		ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
	}

	/**
	 * @see IActionDelegateTest#fireSelection()
	 */
	protected void fireSelection() throws Throwable {
		MockViewPart view = (MockViewPart)fPage.showView(TEST_VIEW_ID);
		view.fireSelection();
	}
}

