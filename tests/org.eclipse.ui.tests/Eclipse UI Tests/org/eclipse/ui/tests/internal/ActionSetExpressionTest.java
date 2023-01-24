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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests for action set enablement
 */
@RunWith(JUnit4.class)
public class ActionSetExpressionTest extends ActionExpressionTest {

	public ActionSetExpressionTest() {
		super(ActionSetExpressionTest.class.getSimpleName());
	}

	/**
	 * Opens the action set.  Returns the menu manager containing it.
	 */
	@Override
	protected MenuManager getActionMenuManager(ListView view) throws Throwable {
		fPage.showActionSet("org.eclipse.ui.tests.internal.ListElementActions");
		WorkbenchWindow win = (WorkbenchWindow) fWindow;
		IContributionItem item = win.getMenuBarManager().find(
				"org.eclipse.ui.tests.internal.ListElementMenu");
		while (item instanceof SubContributionItem) {
			item = ((SubContributionItem) item).getInnerItem();
			if (item instanceof MenuManager mm) {
				return mm;
			}
		}
		fail("Unable to find menu manager");
		return null;
	}

	/**
	 * Tests the enablement of an action.
	 */
	@Override
	protected void testAction(MenuManager mgr, String action, boolean expected)
			throws Throwable {
		assertEquals(action, expected, ActionUtil.getActionWithLabel(mgr,
				action).isEnabled());
	}
}
