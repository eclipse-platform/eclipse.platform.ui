package org.eclipse.ui.tests.internal;

import org.eclipse.jface.action.MenuManager;

import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.util.ActionUtil;


/**
 * This class contains tests for popup menu visibility
 */
public class PopupMenuExpressionTest extends ActionExpressionTest {
		
	public PopupMenuExpressionTest(String testName) {
		super(testName);
	}
	
 	/**
 	 * Returns the menu manager containing the actions.
 	 */
 	protected MenuManager getActionMenuManager(ListView view) 
 		throws Throwable 
 	{
 		return view.getMenuManager();
 	}
 	
 	/**
 	 * Tests the visibility of an action.
 	 */
 	protected void testAction(MenuManager mgr, String action, boolean expected) 
 		throws Throwable
 	{
 		if (expected)
 			assertNotNull(action, ActionUtil.getActionWithLabel(mgr, action));
 		else
 			assertNull(action, ActionUtil.getActionWithLabel(mgr, action));
 	}
}