/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * 
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class Bug231304Test extends UITestCase {

	public Bug231304Test(String testName) {
		super(testName);
	}

	public void testToolTip() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = (IMenuService) window
				.getService(IMenuService.class);
		ToolBarManager manager = new ToolBarManager();
		try {
			// populate contribution
			menus.populateContributionManager(manager,
					"toolbar:org.eclipse.ui.tests.tooltipTest");
			IContributionItem[] items = manager.getItems();
			assertEquals(1, items.length);
			ToolBar toolBar = manager.createControl(window.getShell());
			manager.update(true);

			// now get the tool item and verify the tool tip
			ToolItem[] toolItems = toolBar.getItems();
			assertEquals(1, toolItems.length);
			String keys = KeySequence.getInstance("M2+M3+1").format();
			assertEquals("Testing Tooltip (" + keys + ")", toolItems[0]
					.getToolTipText());

		} finally {
			menus.releaseContributions(manager);
		}
	}

}
