/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests for popup menu visibility
 */
@RunWith(JUnit4.class)
public class PopupMenuExpressionTest extends ActionExpressionTest {

	public PopupMenuExpressionTest() {
		super(PopupMenuExpressionTest.class.getSimpleName());
	}

	/**
	 * Returns the menu manager containing the actions.
	 */
	@Override
	protected MenuManager getActionMenuManager(ListView view) throws Throwable {
		return view.getMenuManager();
	}

	/**
	 * Tests the visibility of an action.
	 */
	@Override
	protected void testAction(MenuManager mgr, String action, boolean expected)
			throws Throwable {
		if (expected) {
			assertNotNull(action, ActionUtil.getActionWithLabel(mgr, action));
		} else {
			assertNull(action, ActionUtil.getActionWithLabel(mgr, action));
		}
	}

	@Test
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
