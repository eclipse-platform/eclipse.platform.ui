package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;


/**
 * Test the lifecycle of an action delegate.
 */
public abstract class IActionDelegateTest extends AbstractTestCase {

	protected IWorkbenchWindow fWindow;
	protected IWorkbenchPage fPage;
	
	/**
	 * Constructor for IActionDelegateTest
	 */
	public IActionDelegateTest(String testName) {
		super(testName);
	}
	
	public void setUp() {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}
	
	public void testRun() throws Throwable {
		// From Javadoc: "This method is called when the delegating 
		// action has been triggered."
		addAction();
		runAction();
		MockWorkbenchWindowActionDelegate delegate = 
			MockWorkbenchWindowActionDelegate.lastDelegate;
		assertNotNull(delegate);
		assert(delegate.callHistory.contains(delegate, "run"));
	}
	
	public void testSelectionChanged() throws Throwable {
		// From Javadoc: "Notifies this action delegate that the selection 
		// in the workbench has changed".
		
		// Load the delegate by running it.
		testRun();
		MockWorkbenchWindowActionDelegate delegate = 
			MockWorkbenchWindowActionDelegate.lastDelegate;
		assertNotNull(delegate);
		
		// Now fire a selection.
		delegate.callHistory.clear();		
		fireSelection();
		assert(delegate.callHistory.contains(delegate, "selectionChanged"));
	}
	
	/**
	 * Adds the action delegate.  Subclasses should override
	 */
	protected abstract void addAction() throws Throwable;
	
	/**
	 * Runs the action delegate.  Subclasses should override
	 */
	protected abstract void runAction() throws Throwable;
	
	/**
	 * Fires a selection from the source.  Subclasses should override
	 */
	protected abstract void fireSelection() throws Throwable;

	/**
	 * Runs an action identified by a label.
	 * This is a sub-optimal way to find actions, but it works when
	 * the id of an action contribution has no relationship to the
	 * xml action id, as it is in 0.9.
	 */
	protected void runAction(IMenuManager mgr, String label) {
		IContributionItem [] items = mgr.getItems();
		for (int nX = 0; nX < items.length; nX ++) {
			IContributionItem item = items[nX];
			if (item instanceof SubContributionItem)
				item = ((SubContributionItem)item).getInnerItem();
			if (item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem)item).getAction();
				if (label.equals(action.getText())) {
					action.run();
					return;
				}
			}
		}
		fail("Unable to find action: " + label);
	}
		
	/**
	 * Runs an action contribution.
	 */
	protected void runAction(IContributionItem item) {
		assert(item instanceof ActionContributionItem);
		((ActionContributionItem)item).getAction().run();
	}
}

