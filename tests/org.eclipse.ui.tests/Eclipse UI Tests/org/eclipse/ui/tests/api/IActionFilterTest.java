package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;

/**
 * Test the lifecycle of an action filter.
 * 
 * From Javadoc: "An IActionFilter returns whether the specific attribute
 * 		matches the state of the target object."
 */
public class IActionFilterTest extends UITestCase {

	protected IWorkbenchWindow fWindow;
	protected IWorkbenchPage fPage;
	protected String VIEW_ID = "org.eclipse.ui.tests.api.ActionFilterTestView";
		
	public IActionFilterTest(String testName) {
		super(testName);
	}
	
	public void setUp() {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}
	
	/**
	 * Select a ListElement, popup a menu and verify that the 
	 * ListElementActionFilter.testAttribute method is invoked.  
	 * Then verify that the correct actions are added to the
	 * popup menu.
	 * 
	 * Setup: The plugin.xml contains a number of popup menu action 
	 * targetted to ListElements 
	 * 
	 * redAction -> (name = red)
	 * blueAction -> (name = blue)
	 * trueAction -> (flag = true)
	 * falseAction -> (flag = false)
	 * redTrueAction -> (name = red) (flag = true)
	 */
	public void testAttribute() throws Throwable {
		// Create the test objects.
		ListElement red = new ListElement("red");
		ListElement blue = new ListElement("blue");
		ListElement green = new ListElement("green");
		ListElement redTrue = new ListElement("red", true);
		
		// Create a list view.  
		ListView view = (ListView)fPage.showView(VIEW_ID);
		MenuManager menuMgr = view.getMenuManager();
		view.addElement(red);
		view.addElement(blue);
		view.addElement(green);
		view.addElement(redTrue);
		
		// Get action filter.
		ListElementActionFilter	filter = ListElementActionFilter.getSingleton();
			
		// Select red, verify popup.
		view.selectElement(red);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction"));
		
		// Select blue, verify popup.
		filter.clearCalled();
		view.selectElement(blue);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction"));
		
		// Select green, verify popup.
		filter.clearCalled();
		view.selectElement(green);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction"));
		
		// Select redTrue, verify popup.
		filter.clearCalled();
		view.selectElement(redTrue);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction"));
 	}	
}
