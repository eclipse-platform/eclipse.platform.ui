package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;
import org.eclipse.ui.IViewPart;

/**
 * This mock is used to test IViewActionDelegate lifecycle.
 */
public class MockViewActionDelegate extends MockActionDelegate 
	implements IViewActionDelegate
{
	public static MockViewActionDelegate lastDelegate;

	/**
	 * Constructor for MockWorkbenchWindowActionDelegate
	 */
	public MockViewActionDelegate() {
		super();
		lastDelegate = this;
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		callHistory.add(this, "init");
	}
}

