package org.eclipse.ui.tests.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;

import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.util.ActionUtil;


/**
 * This class contains tests for action set enablement
 */
public class ActionSetExpressionTest extends ActionExpressionTest {
		
	public ActionSetExpressionTest(String testName) {
		super(testName);
	}
	
 	/**
 	 * Opens the action set.  Returns the menu manager containing it.
 	 */
 	protected MenuManager getActionMenuManager(ListView view) 
 		throws Throwable 
 	{
 		fPage.showActionSet("org.eclipse.ui.tests.internal.ListElementActions");
 		WorkbenchWindow win = (WorkbenchWindow)fWindow;
 		IContributionItem item = win.getMenuBarManager().find("org.eclipse.ui.tests.internal.ListElementMenu");
 		while (item instanceof SubContributionItem) {
 			item = ((SubContributionItem)item).getInnerItem();
 			if (item instanceof MenuManager)
 				return (MenuManager)item;
 		}
 		fail("Unable to find menu manager");
 		return null;
 	}
 	
 	/**
 	 * Tests the enablement of an action.
 	 */
 	protected void testAction(MenuManager mgr, String action, boolean expected) 
 		throws Throwable
 	{
		assertEquals(action, expected, ActionUtil.getActionWithLabel(mgr, action).isEnabled());
 	}
}