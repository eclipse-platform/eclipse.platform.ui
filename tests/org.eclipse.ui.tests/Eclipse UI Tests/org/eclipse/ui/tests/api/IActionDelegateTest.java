package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;


/**
 * Test the lifecycle of an action delegate.
 */
public abstract class IActionDelegateTest extends UITestCase {

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
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.contains("run"));
	}
	
	public void testSelectionChanged() throws Throwable {
		// From Javadoc: "Notifies this action delegate that the selection 
		// in the workbench has changed".
		
		// Load the delegate by running it.
		testRun();
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		
		// Now fire a selection.
		delegate.callHistory.clear();		
		fireSelection();
		assertTrue(delegate.callHistory.contains("selectionChanged"));
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
	 * Returns the action delegate.  Subclasses should override.
	 */
	protected abstract MockActionDelegate getDelegate() throws Throwable;
}

