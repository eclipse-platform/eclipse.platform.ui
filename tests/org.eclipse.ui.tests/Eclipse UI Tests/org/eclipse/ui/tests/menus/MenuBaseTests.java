/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.menus;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.menus.DeclaredProgrammaticFactory.MyItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Performs a number of basic tests for the org.eclipse.ui.menus
 * extension point:
 * <ul>
 * <li>MenuManagers can populate from declarative extensions</li>
 * <li>'before' and 'after' insertion tags are correctly handled</li>
 * <li>The Menu produced matches the expected structure based on the
 * contributions</li>
 * <li>The MenuManager's update mechanism works</li>
 * </ul>
 *
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class MenuBaseTests extends MenuTestCase {
	String[] expectedIds = {
			"MenuTest.BasicCmdItem",
			"MenuTest.BasicMenu",
			"MenuTest.BeforeSeparator",
			"MenuTest.Separator",
			"MenuTest.AfterSeparator",
			"MenuTest.ParameterItem",
			null, // "MenuTest.DynamicItem",
			"MenuTest.DynamicMenu",
			"MenuTest.ItemX1",
			MenuPopulationTest.ID_DEFAULT,
			MenuPopulationTest.ID_ALL,
			MenuPopulationTest.ID_TOOLBAR,
			"myitem"
		};
	Class<?>[] expectedClasses = {
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.jface.action.MenuManager.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.jface.action.Separator.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.internal.menus.DynamicMenuContributionItem.class,
			org.eclipse.jface.action.MenuManager.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			MyItem.class
		};
		String[] expectedMenuItemLabels = {
			"&Basic Cmd",
			"Basic Menu",
			"Inserted &Before",
			"",
			"Inserted &After",
			"Parameter &Cmd",
			"Dynamic Item 1",
			"Dynamic Item 2",
			"Dynamic Menu",
			"Icons Default",
			"Icons All",
			"Icons Toolbar Only",
			"MyItem"
		};

	public MenuBaseTests() {
		super(MenuBaseTests.class.getSimpleName());
	}

	@Test
	public void testBasicPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);
		IContributionItem[] items = manager.getItems();

		// Correct number of items?
		assertEquals("Bad count", expectedIds.length, items.length);

		int diffIndex = checkContribIds(items, expectedIds);
		assertTrue("Id mismatch at index " + diffIndex , diffIndex == ALL_OK);

		diffIndex = checkContribClasses(items, expectedClasses);
		assertTrue("Class mismatch at index " + diffIndex , diffIndex == ALL_OK);

		menuService.releaseContributions(manager);
		manager.dispose();
	}

	@Test
	public void testBasicMenuPopulation() throws Exception {
		MenuManager manager = new MenuManager("Test Menu", TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		Shell shell = window.getShell();

		// Test the initial menu creation
		final Menu contextMenu = manager.createContextMenu(shell);
		Event e = new Event();
		e.type = SWT.Show;
		e.widget = contextMenu;
		contextMenu.notifyListeners(SWT.Show, e);

		MenuItem[] menuItems = contextMenu.getItems();

		// NOTE: Uncomment to print the info needed to update the 'expected'
		// arrays
//		IContributionItem[] items = manager.getItems();
//		printIds(items);
//		printClasses(items);
//		printMenuItemLabels(menuItems);

		// Correct number of items?
		assertEquals("createMenuBar: Bad count", expectedMenuItemLabels.length, menuItems.length);

		int diffIndex = checkMenuItemLabels(menuItems, expectedMenuItemLabels);
		assertTrue("createMenuBar: Index mismatch at index " + diffIndex , diffIndex == ALL_OK);

		// Test the update mechanism (While visible)
		manager.update(true);
		menuItems = manager.getMenu().getItems();

		// Correct number of items?
		assertTrue("manager.update(true): Bad count", menuItems.length == expectedMenuItemLabels.length);

		diffIndex = checkMenuItemLabels(menuItems, expectedMenuItemLabels);
		assertTrue("manager.update(true): Index mismatch at index " + diffIndex , diffIndex == ALL_OK);

		// Test second appearance
		contextMenu.notifyListeners(SWT.Hide, new Event());
		contextMenu.notifyListeners(SWT.Show, new Event());

		menuItems = manager.getMenu().getItems();

		// Correct number of items?
		assertTrue("manager.update(true): Bad count", menuItems.length == expectedMenuItemLabels.length);

		diffIndex = checkMenuItemLabels(menuItems, expectedMenuItemLabels);
		assertTrue("manager.update(true): Index mismatch at index " + diffIndex, diffIndex == ALL_OK);

		menuService.releaseContributions(manager);
		manager.dispose();
	}
}
