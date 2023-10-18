/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package org.eclipse.jface.tests.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for the MenuManager API.
 *
 * @since 3.1
 */
public class MenuManagerTest {
	@Rule
	public JFaceActionRule rule = new JFaceActionRule();

	private int groupMarkerCount = 0;
	private int separatorCount = 0;

	/**
	 * Tests that a menu with no concrete visible items (that is, ignoring
	 * separators and group markers) is hidden.
	 *
	 * @see MenuManager#isVisible()
	 */

	@Test
	public void testMenuWithNoConcreteVisibleItemsIsHidden() {
		MenuManager menuBarMgr = createMenuBarManager();

		MenuManager fileMenu = createMenuManager("File", "gsgn");
		menuBarMgr.add(fileMenu);
		menuBarMgr.updateAll(false);
		assertEquals(0, rule.getShell().getMenuBar().getItems().length);
	}

	/**
	 * Tests that adding a concrete visible item to a menu with no concrete visible
	 * items makes the menu visible. Regression test for bug 54779 [RCP] Problem
	 * updating menus, menu not appearing
	 *
	 * @see MenuManager#isVisible()
	 * @see MenuManager#markDirty()
	 */

	@Test

	public void testAddingConcreteItemToMenuWithNoConcreteVisibleItems() {
		MenuManager menuBarMgr = createMenuBarManager();

		MenuManager fileMenuMgr = createMenuManager("File", "gsgn");
		menuBarMgr.add(fileMenuMgr);
		menuBarMgr.updateAll(false);

		Menu menuBar = rule.getShell().getMenuBar();
		assertEquals(0, menuBar.getItems().length);

		fileMenuMgr.add(createItem('a'));
		menuBarMgr.updateAll(false);
		assertEquals(1, menuBar.getItems().length);

		assertEquals("File", menuBar.getItems()[0].getText());

		Menu fileMenu = menuBar.getItems()[0].getMenu();
		assertEquals(1, fileMenu.getItems().length);
	}

	/**
	 * This is a test case for bug 204788 to ensure that a disposed menu is marked
	 * as being dirty.
	 */

	@Test
	public void testDisposedMenuIsDirty() {
		MenuManager menuBarMgr = createMenuBarManager();

		MenuManager fileMenuMgr = createMenuManager("File", "gsgn");
		menuBarMgr.add(fileMenuMgr);
		menuBarMgr.updateAll(false);

		assertFalse(menuBarMgr.isDirty());

		menuBarMgr.dispose();
		assertTrue(menuBarMgr.isDirty());
	}

	/**
	 * This is a test case for bug 255429 to ensure that a menu manager without any
	 * text set does not throw an NPE.
	 */

	@Test
	public void testEmptyMenuManagerNPE() {
		Menu menu = new Menu(rule.getShell());
		MenuManager manager = new MenuManager();
		manager.fill(menu, -1);
	}

	/**
	 * Creates a menu manager with the given name, adding items based on the given
	 * template.
	 *
	 * @param name     the name
	 * @param template the template
	 *
	 * @return a menu with no concrete visible items
	 */
	private MenuManager createMenuManager(String name, String template) {
		MenuManager menuMgr = new MenuManager(name);
		addItems(menuMgr, template);
		return menuMgr;
	}

	private void addItems(IContributionManager manager, String template) {
		for (int i = 0; i < template.length(); ++i) {
			manager.add(createItem(template.charAt(i)));
		}
	}

	private IContributionItem createItem(char template) {
		switch (template) {
		case 'g':
			return new GroupMarker("testGroup" + groupMarkerCount++);
		case 's':
			return new Separator("testSeparator" + separatorCount++);
		case 'a': {
			IAction action = new DummyAction();
			return new ActionContributionItem(action);
		}
		case 'n': {
			IAction action = new DummyAction();
			ActionContributionItem item = new ActionContributionItem(action);
			item.setVisible(false);
			return item;
		}
		default:
			throw new IllegalArgumentException("Unknown template char: " + template);
		}
	}

	protected MenuManager createMenuBarManager() {
		Decorations shell = rule.getShell();
		MenuManager menuMgr = new MenuManager();
		Menu menuBar = menuMgr.createMenuBar(shell);
		shell.setMenuBar(menuBar);
		return menuMgr;
	}
}
