package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This mock is used to test IViewActionDelegate lifecycle.
 */
public class MockViewActionDelegate extends MockActionDelegate 
	implements IViewActionDelegate
{
	/**
	 * Constructor for MockWorkbenchWindowActionDelegate
	 */
	public MockViewActionDelegate() {
		super();
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		callHistory.add("init");
	}
}

