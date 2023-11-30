/*******************************************************************************
 * Copyright (c) 2020 Rolf Theunissen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rolf Theunissen - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.MenuManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class MenuManagerRendererTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MMenu menu;
	private MTrimmedWindow window;

	@Before
	public void setUp() throws Exception {
		window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);

		menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);
	}

	@Test
	public void testMMenuItem_Visible() {
		MMenuItem menuItem1 = ems.createModelElement(MDirectMenuItem.class);
		menu.getChildren().add(menuItem1);

		MMenuItem menyItem2 = ems.createModelElement(MDirectMenuItem.class);
		menyItem2.setVisible(false);
		menu.getChildren().add(menyItem2);

		contextRule.createAndRunWorkbench(window);
		MenuManager mm = getMenuManager();

		assertEquals(2, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());
		assertFalse(mm.getItems()[1].isVisible());

		menuItem1.setVisible(false);

		assertEquals(2, mm.getSize());
		assertFalse(mm.getItems()[0].isVisible());
		assertFalse(mm.getItems()[1].isVisible());

		menuItem1.setVisible(true);

		assertEquals(2, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());
		assertFalse(mm.getItems()[1].isVisible());
	}

	@Test
	public void testMMenuItem_ToBeRendered() {
		MMenuItem menuItem1 = ems.createModelElement(MDirectMenuItem.class);
		menu.getChildren().add(menuItem1);

		MMenuItem menyItem2 = ems.createModelElement(MDirectMenuItem.class);
		menyItem2.setToBeRendered(false);
		menu.getChildren().add(menyItem2);

		contextRule.createAndRunWorkbench(window);
		MenuManager mm = getMenuManager();

		assertEquals(1, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());

		menuItem1.setToBeRendered(false);

		assertEquals(0, mm.getSize());

		menuItem1.setToBeRendered(true);

		assertEquals(1, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());
	}

	@Test
	@Ignore("Bug 560200")
	public void testMMenu_ToBeRendered() {
		MMenu submenu1 = ems.createModelElement(MMenu.class);
		menu.getChildren().add(submenu1);

		MMenuItem menuItem1 = ems.createModelElement(MDirectMenuItem.class);
		submenu1.getChildren().add(menuItem1);

		MMenu submenu2 = ems.createModelElement(MMenu.class);
		submenu2.setToBeRendered(false);
		menu.getChildren().add(submenu2);

		MMenuItem menyItem2 = ems.createModelElement(MDirectMenuItem.class);
		submenu2.getChildren().add(menyItem2);

		contextRule.createAndRunWorkbench(window);
		MenuManager mm = getMenuManager();

		assertEquals(1, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());

		submenu1.setToBeRendered(false);

		assertEquals(0, mm.getSize());

		submenu1.setToBeRendered(true);

		assertEquals(1, mm.getSize());
		assertTrue(mm.getItems()[0].isVisible());
	}

	private MenuManagerRenderer getMenuManagerRenderer() {
		Object renderer = menu.getRenderer();
		assertEquals(MenuManagerRenderer.class, renderer.getClass());
		return (MenuManagerRenderer) renderer;
	}

	private MenuManager getMenuManager() {
		return (getMenuManagerRenderer()).getManager(menu);
	}
}
