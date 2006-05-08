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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test the lifecycle of an action delegate.
 */
public class IActionBarsTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

    private class MockAction extends Action {
        public boolean hasRun = false;

        public MockAction() {
            super();
        }

        public void run() {
            hasRun = true;
        }
    }

    /**
     * Constructor for IActionDelegateTest
     */
    public IActionBarsTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

    public void testGetMenuManager() throws Throwable {
        // From Javadoc: "Returns the menu manager."

        IViewPart part = fPage.showView(MockViewPart.ID);
        IActionBars bars = part.getViewSite().getActionBars();
        assertNotNull(bars);
        IMenuManager mgr = bars.getMenuManager();
        assertNotNull(mgr);
    }

    public void testGetStatusLineManager() throws Throwable {
        // From Javadoc: "Returns the status line manager."

        IViewPart part = fPage.showView(MockViewPart.ID);
        IActionBars bars = part.getViewSite().getActionBars();
        assertNotNull(bars);
        IStatusLineManager mgr = bars.getStatusLineManager();
        assertNotNull(mgr);
    }

    public void testGetToolBarManager() throws Throwable {
        // From Javadoc: "Returns the tool bar manager."

        IViewPart part = fPage.showView(MockViewPart.ID);
        IActionBars bars = part.getViewSite().getActionBars();
        assertNotNull(bars);
        IToolBarManager mgr = bars.getToolBarManager();
        assertNotNull(mgr);
    }

    public void testGetGlobalActionHandler() throws Throwable {
        // From Javadoc: "Returns the global action handler for 
        // the action with the given id.  

        IViewPart part = fPage.showView(MockViewPart.ID);
        IActionBars bars = part.getViewSite().getActionBars();
        assertNotNull(bars);

        // Get actions.  They should all be null.
        assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.CUT));
        assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.COPY));
        assertNull(bars.getGlobalActionHandler(IWorkbenchActionConstants.UNDO));

        // Create actions.
        MockAction cut = new MockAction();
        MockAction copy = new MockAction();
        MockAction undo = new MockAction();

        // Set actions.
        bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cut);
        bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copy);
        bars.setGlobalActionHandler(IWorkbenchActionConstants.UNDO, undo);
        bars.updateActionBars();

        // Get actions.  They should not be null.
        assertEquals(cut, bars
                .getGlobalActionHandler(IWorkbenchActionConstants.CUT));
        assertEquals(copy, bars
                .getGlobalActionHandler(IWorkbenchActionConstants.COPY));
        assertEquals(undo, bars
                .getGlobalActionHandler(IWorkbenchActionConstants.UNDO));
    }

    public void testSetGlobalActionHandler() throws Throwable {
        // From Javadoc: "Returns the global action handler for 
        // the action with the given id.  

        IViewPart part = fPage.showView(MockViewPart.ID);
        IActionBars bars = part.getViewSite().getActionBars();
        assertNotNull(bars);

        // Create actions.
        MockAction cut = new MockAction();
        MockAction copy = new MockAction();
        MockAction undo = new MockAction();

        // Set actions.
        bars.setGlobalActionHandler(IWorkbenchActionConstants.CUT, cut);
        bars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copy);
        bars.setGlobalActionHandler(IWorkbenchActionConstants.UNDO, undo);
        bars.updateActionBars();

        // Run the real workbench actions.
        // Verify the actions are invoked.
        cut.hasRun = copy.hasRun = undo.hasRun = false;
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.CUT);
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.UNDO);
        assertTrue(cut.hasRun);
        assertTrue(!copy.hasRun);
        assertTrue(undo.hasRun);

        // Now create a second view and run the actions again.
        // Our global actions should not be invoked.
        fPage.showView(MockViewPart.ID2);
        cut.hasRun = copy.hasRun = undo.hasRun = false;
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.CUT);
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.UNDO);
        assertTrue(!cut.hasRun);
        assertTrue(!copy.hasRun);
        assertTrue(!undo.hasRun);

        // Reactivate test view and run actions again.
        // This time our global actions should be invoked.		
        fPage.activate(part);
        cut.hasRun = copy.hasRun = undo.hasRun = false;
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.CUT);
        ActionUtil.runActionUsingPath(this, fWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.UNDO);
        assertTrue(cut.hasRun);
        assertTrue(!copy.hasRun);
        assertTrue(undo.hasRun);
    }
}

