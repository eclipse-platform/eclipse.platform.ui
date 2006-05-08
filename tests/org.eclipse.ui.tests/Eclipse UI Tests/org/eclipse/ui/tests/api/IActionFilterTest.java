/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test the lifecycle of an action filter.
 * 
 * From Javadoc: "An IActionFilter returns whether the specific attribute
 * 		matches the state of the target object."
 * 
 * Setup: The plugin.xml contains a number of popup menu action 
 * targetted to ListElements 
 * 
 * 		redAction -> (name = red)
 * 		blueAction -> (name = blue)
 * 		trueAction -> (flag = true)
 * 		falseAction -> (flag = false)
 * 		redTrueAction -> (name = red) (flag = true)
 */
public class IActionFilterTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

    protected String STATIC_MENU_VIEW_ID = "org.eclipse.ui.tests.api.IActionFilterTest1";

    protected String DYNAMIC_MENU_VIEW_ID = "org.eclipse.ui.tests.api.IActionFilterTest2";

    public IActionFilterTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

    public void testStaticLifecycle() throws Throwable {
        testLifecycle(STATIC_MENU_VIEW_ID);
    }

    public void testDynamicLifecycle() throws Throwable {
        testLifecycle(DYNAMIC_MENU_VIEW_ID);
    }

    /**
     * Select a list element, popup the menu, and verify that the 
     * action filter is called.
     * 
     * See Setup above.
     */
    private void testLifecycle(String viewId) throws Throwable {
        // Create a list view.  
        ListView view = (ListView) fPage.showView(viewId);

        // Create the test objects.
        ListElement red = new ListElement("red");
        view.addElement(red);
        view.selectElement(red);

        // Get action filter.
        ListElementActionFilter filter = ListElementActionFilter.getSingleton();

        // Open menu.  Verify that action filter is called.			
        MenuManager menuMgr = view.getMenuManager();
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(filter.getCalled());
    }

    public void testDynamicMenuContribution() throws Throwable {
        testMenu(DYNAMIC_MENU_VIEW_ID);
    }

    public void testStaticMenuContribution() throws Throwable {
        testMenu(STATIC_MENU_VIEW_ID);
    }

    /**
     * Select a ListElement, popup a menu and verify that the 
     * ListElementActionFilter.testAttribute method is invoked.  
     * Then verify that the correct actions are added to the
     * popup menu.
     * 
     * See Setup above.
     */
    private void testMenu(String viewId) throws Throwable {
        // Create the test objects.
        ListElement red = new ListElement("red");
        ListElement blue = new ListElement("blue");
        ListElement green = new ListElement("green");
        ListElement redTrue = new ListElement("red", true);

        // Create a list view.  
        ListView view = (ListView) fPage.showView(viewId);
        MenuManager menuMgr = view.getMenuManager();
        view.addElement(red);
        view.addElement(blue);
        view.addElement(green);
        view.addElement(redTrue);

        // Get action filter.
        ListElementActionFilter filter = ListElementActionFilter.getSingleton();

        // Select red, verify popup.
        view.selectElement(red);
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(filter.getCalled());
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction_v1"));
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction_v1"));
        view.verifyActions(this, menuMgr);

        // Select blue, verify popup.
        filter.clearCalled();
        view.selectElement(blue);
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(filter.getCalled());
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction_v1"));
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction_v1"));
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction_v1"));
        view.verifyActions(this, menuMgr);

        // Select green, verify popup.
        filter.clearCalled();
        view.selectElement(green);
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(filter.getCalled());
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction_v1"));
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction_v1"));
        view.verifyActions(this, menuMgr);

        // Select redTrue, verify popup.
        filter.clearCalled();
        view.selectElement(redTrue);
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(filter.getCalled());
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "redAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction_v1"));
        assertNotNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction_v1"));
        assertNotNull(ActionUtil
                .getActionWithLabel(menuMgr, "redTrueAction_v1"));
        view.verifyActions(this, menuMgr);

        // Select nothing, verify popup.
        filter.clearCalled();
        view.selectElement(null);
        ActionUtil.fireAboutToShow(menuMgr);
        assertTrue(!filter.getCalled());
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "blueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "trueAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "falseAction_v1"));
        assertNull(ActionUtil.getActionWithLabel(menuMgr, "redTrueAction_v1"));
        view.verifyActions(this, menuMgr);
    }
}
