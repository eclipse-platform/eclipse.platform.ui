/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 * 
 */
public class MenuBaseTests extends MenuTestCase {
	String[] expectedIds = {
			"MenuTest.BasicCmdItem",
			"MenuTest.BasicMenu",
			"MenuTest.BeforeSeparator",
			"MenuTest.Separator",
			"MenuTest.AfterSeparator",
			"MenuTest.ParameterItem",
			"MenuTest.DynamicItem",
			"MenuTest.DynamicMenu",
			"MenuTest.ItemX1"
		};
		Class[] expectedClasses = {
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.jface.action.MenuManager.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.jface.action.Separator.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.menus.CommandContributionItem.class,
			org.eclipse.ui.internal.menus.DynamicContributionItem.class,
			org.eclipse.jface.action.MenuManager.class,
			org.eclipse.ui.menus.CommandContributionItem.class
		};
		String[] expectedMenuItemLabels = {
			"Basic Cmd",
			"Basic Menu",
			"Inserted Before",
			"",
			"Inserted After",
			"Dynamic Item 1",
			"Dynamic Item 2",
			"Dynamic Menu"
		};

	/**
	 * @param testName
	 */
	public MenuBaseTests(String testName) {
		super(testName);
	}

	public void testBasicPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);
		IContributionItem[] items = manager.getItems();
		
		// Correct number of items?
		assertTrue("Bad count", items.length == expectedIds.length);

		int diffIndex = checkContribIds(items, expectedIds);
		assertTrue("Id mismatch at index " + diffIndex , diffIndex == ALL_OK);

		diffIndex = checkContribClasses(items, expectedClasses);
		assertTrue("Class mismatch at index " + diffIndex , diffIndex == ALL_OK);

		menuService.releaseContributions(manager);
		manager.dispose();
	}

	public void testBasicMenuPopulation() throws Exception {
		MenuManager manager = new MenuManager("Test Menu", TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);

		IContributionItem[] items = manager.getItems();
		
		Shell shell = window.getShell();

		// Test the initial menu creation
		manager.createMenuBar((Decorations)shell);
		MenuItem[] menuItems = manager.getMenu().getItems();
		
		// NOTE: Uncomment to print the info needed to update the 'expected'
		// arrays
		printIds(items);
		printClasses(items);
		printMenuItemLabels(menuItems);
		
		// Correct number of items?
		assertTrue("createMenuBar: Bad count", menuItems.length == expectedMenuItemLabels.length);

		int diffIndex = checkMenuItemLabels(menuItems, expectedMenuItemLabels);
		assertTrue("createMenuBar: Index mismatch at index " + diffIndex , diffIndex == ALL_OK);

		// Test the update mechanism
		
		// KLUDGE!! Test commented out until bug 170353 is fixed...
//		manager.update(true);
//		menuItems = manager.getMenu().getItems();
//		
//		// Correct number of items?
//		assertTrue("manager.update(true): Bad count", menuItems.length == expectedMenuItemLabels.length);
//
//		diffIndex = checkMenuItemLabels(menuItems, expectedMenuItemLabels);
//		assertTrue("manager.update(true): Index mismatch at index " + diffIndex , diffIndex == ALL_OK);
		
		menuService.releaseContributions(manager);
		manager.dispose();
	}
}
