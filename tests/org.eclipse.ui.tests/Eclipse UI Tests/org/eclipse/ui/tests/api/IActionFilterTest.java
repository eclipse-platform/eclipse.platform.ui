package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;

/**
 * Test the lifecycle of an action filter.
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
	
	public void testTestAttribute() throws Throwable {
		// From Javadoc: "Returns whether the specific attribute 
		// matches the state of the target object."

		// Setup: The plugin.xml contains one popup menu action targetted 
		// to ListElements with name="red" and another targetted to
		// ListElements with name="blue".  If we create a view with
		// these elements and open the popup menu the action filter
		// should be invoked and the menu should be populated correctly.

		// Create a list view.  
		ListView view = (ListView)fPage.showView(VIEW_ID);
		MenuManager menuMgr = view.getMenuManager();
		ListElement red = view.addElement("red");
		ListElement blue = view.addElement("blue");
		ListElement green = view.addElement("green");
		
		// Get action filter.
		ListElementActionFilter	filter = ListElementActionFilter.getSingleton();
			
		// Open a popup menu on red.
		view.selectElement(red);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		
		// Open a popup menu on blue.
		filter.clearCalled();
		view.selectElement(blue);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
		
		// Open a popup menu on green.
		filter.clearCalled();
		view.selectElement(green);
		ActionUtil.fireAboutToShow(menuMgr);
		assertTrue(filter.getCalled());
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction"));
		assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction"));
 	}	
}
