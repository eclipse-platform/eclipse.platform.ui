/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @since 3.5
 * @author Prakash G.R.
 */
public class Bug231304Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Test
	public void testToolTip() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = window.getService(IMenuService.class);
		ToolBarManager manager = new ToolBarManager();
		try {
			// populate contribution
			menus.populateContributionManager(manager, "toolbar:org.eclipse.ui.tests.tooltipTest");
			IContributionItem[] items = manager.getItems();
			assertEquals(1, items.length);
			ToolBar toolBar = manager.createControl(window.getShell());
			manager.update(true);

			// now get the tool item and verify the tool tip
			ToolItem[] toolItems = toolBar.getItems();
			assertEquals(1, toolItems.length);
			String keys = KeySequence.getInstance("M2+M3+1").format();
			assertEquals("My Test Tooltip Command (" + keys + ")", toolItems[0].getToolTipText());

		} finally {
			menus.releaseContributions(manager);
		}
	}

}
