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
		// When an action delegate is run the
		// selectionChanged and run methods should be called
		// in that order.
		addAndRunAction();
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.verifyOrder(
			new String[] {"selectionChanged", "run" } ));
	}
	
	public void testSelectionChanged() throws Throwable {
		// After an action has been created, if a selection
		// occurs the selectionChanged method should be
		// invoked.
		
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
	 * Returns the last mock action delegate created.
	 */
	protected MockActionDelegate getDelegate() throws Throwable {
		MockActionDelegate delegate = 
			MockActionDelegate.lastDelegate;
		assertNotNull(delegate);
		return delegate;
	}

	/**
	 * Adds and runs the action delegate.  Subclasses should override
	 */
	protected abstract void addAndRunAction() throws Throwable;
	
	/**
	 * Fires a selection from the source.  Subclasses should override
	 */
	protected abstract void fireSelection() throws Throwable;
}

