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
package org.eclipse.ui.tests.internal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.ActionUtil;

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
    protected MenuManager getActionMenuManager(ListView view) throws Throwable {
        return view.getMenuManager();
    }

    /**
     * Tests the visibility of an action.
     */
    protected void testAction(MenuManager mgr, String action, boolean expected)
            throws Throwable {
        if (expected)
            assertNotNull(action, ActionUtil.getActionWithLabel(mgr, action));
        else
            assertNull(action, ActionUtil.getActionWithLabel(mgr, action));
    }

    public void testExpressionEnabledAction() throws Throwable {
        // Setup.
        ListView view = showListView();
        MenuManager mgr = getActionMenuManager(view);

        // Test null selection.
        selectAndUpdateMenu(view, null, mgr);
        testAction(mgr, "expressionEnablementAction_v2", false);

        // Test red selection.
        selectAndUpdateMenu(view, red, mgr);
        testAction(mgr, "expressionEnablementAction_v2", true);

        // Test blue selection.
        selectAndUpdateMenu(view, blue, mgr);
        testAction(mgr, "expressionEnablementAction_v2", false);
    }

}
