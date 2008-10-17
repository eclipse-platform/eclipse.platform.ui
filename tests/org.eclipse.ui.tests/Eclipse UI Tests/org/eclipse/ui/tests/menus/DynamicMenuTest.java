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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;

/**
 * 
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class DynamicMenuTest extends MenuTestCase {

	public DynamicMenuTest(String testName) {
		super(testName);
	}

	public void testDynamicMenu() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = (IMenuService) window
				.getService(IMenuService.class);
		MenuManager manager = new MenuManager();
		Menu contextMenu = null;
		try {
			menus.populateContributionManager(manager,
					"popup:org.eclipse.ui.tests.dynamicMenuContribution");
			IContributionItem[] contributionItems = manager.getItems();
			assertEquals(1, contributionItems.length);

			contextMenu = manager.createContextMenu(window.getShell());
			contextMenu.notifyListeners(SWT.Show, null);
			processEvents();

			int itemCount = contextMenu.getItemCount();
			assertEquals(3, itemCount); // we created 3 items in the code

			MenuItem[] menuItems = contextMenu.getItems();
			// check the labels
			assertEquals("something 1", menuItems[0].getText());
			assertEquals("something 2", menuItems[1].getText());
			assertEquals("something 3", menuItems[2].getText());

			contextMenu.notifyListeners(SWT.Hide, null);
			processEvents();

		} finally {
			menus.releaseContributions(manager);
			if (contextMenu != null) {
				contextMenu.dispose();
			}
		}

	}

	public void testDynamicMenuMultiOpen() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = (IMenuService) window
				.getService(IMenuService.class);
		MenuManager manager = new MenuManager();
		Menu contextMenu = null;
		try {
			menus.populateContributionManager(manager,
					"popup:org.eclipse.ui.tests.dynamicMenuContribution");
			IContributionItem[] contributionItems = manager.getItems();
			assertEquals(1, contributionItems.length);
	
			contextMenu = manager.createContextMenu(window.getShell());
			contextMenu.notifyListeners(SWT.Show, null);
			processEvents();
	
			int itemCount = contextMenu.getItemCount();
			assertEquals(3, itemCount); // we created 3 items in the code
	
			MenuItem[] menuItems = contextMenu.getItems();
			// check the labels
			assertEquals("something 1", menuItems[0].getText());
			assertEquals("something 2", menuItems[1].getText());
			assertEquals("something 3", menuItems[2].getText());
	
			contextMenu.notifyListeners(SWT.Hide, null);
			processEvents();
			contextMenu.notifyListeners(SWT.Show, null);
			processEvents();
			menuItems = contextMenu.getItems();
			assertEquals(3, menuItems.length);
			
			assertEquals("something 4", menuItems[0].getText());
			assertEquals("something 5", menuItems[1].getText());
			assertEquals("something 6", menuItems[2].getText());
	
		} finally {
			menus.releaseContributions(manager);
			if (contextMenu != null) {
				contextMenu.dispose();
			}
		}
	
	}

}
