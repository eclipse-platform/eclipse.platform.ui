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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MenuManagerRendererTest {

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MMenu menu;
	private MTrimmedWindow window;

	@BeforeEach
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
	@Disabled("Bug 560200")
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

	@Test
	public void testClosedMenuIsFilledWhenShown() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		menu.getChildren().add(fileMenu);
		fileMenu.getChildren().add(createItem("item1"));

		contextRule.createAndRunWorkbench(window);
		Menu swtMenu = getMenuManagerRenderer().getManager(fileMenu).getMenu();
		assertEquals(0, swtMenu.getItemCount());

		swtMenu.notifyListeners(SWT.Show, new Event());
		assertEquals(1, swtMenu.getItemCount());
		swtMenu.notifyListeners(SWT.Hide, new Event());

		fileMenu.getChildren().add(createItem("item2"));
		assertEquals(1, swtMenu.getItemCount());

		swtMenu.notifyListeners(SWT.Show, new Event());
		assertEquals(2, swtMenu.getItemCount());
	}

	@Test
	public void testClosedMenuWithNativeAcceleratorIsUpdated() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		menu.getChildren().add(fileMenu);
		fileMenu.getChildren().add(createItem("item1"));

		contextRule.createAndRunWorkbench(window);
		MenuManager fileManager = getMenuManagerRenderer().getManager(fileMenu);
		Action action = new Action("accelerated") {
		};
		action.setAccelerator(SWT.MOD1 | 'J');
		fileManager.add(action);

		fileMenu.getChildren().add(createItem("item2"));
		assertEquals(3, fileManager.getMenu().getItemCount());
	}

	@Test
	public void testClosedMenuWithAcceleratorInSubmenuIsUpdated() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		menu.getChildren().add(fileMenu);
		fileMenu.getChildren().add(createItem("item1"));
		MMenu subMenu = ems.createModelElement(MMenu.class);
		subMenu.setLabel("Sub");
		fileMenu.getChildren().add(subMenu);
		subMenu.getChildren().add(createItem("subItem"));

		contextRule.createAndRunWorkbench(window);
		Menu swtMenu = getMenuManagerRenderer().getManager(fileMenu).getMenu();
		swtMenu.notifyListeners(SWT.Show, new Event());
		swtMenu.notifyListeners(SWT.Hide, new Event());
		assertEquals(2, swtMenu.getItemCount());

		MenuManager subManager = getMenuManagerRenderer().getManager(subMenu);
		Action action = new Action("accelerated") {
		};
		action.setAccelerator(SWT.MOD1 | 'J');
		subManager.add(action);
		subManager.update(true);

		subMenu.setVisible(false);
		assertEquals(1, swtMenu.getItemCount());
	}

	@Test
	public void testClosedMenuShowsUnrenderedAndHiddenItemsWhenShown() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		menu.getChildren().add(fileMenu);
		MMenuItem item1 = createItem("item1");
		MMenuItem item2 = createItem("item2");
		MMenuItem item3 = createItem("item3");
		fileMenu.getChildren().addAll(List.of(item1, item2, item3));

		contextRule.createAndRunWorkbench(window);
		Menu swtMenu = getMenuManagerRenderer().getManager(fileMenu).getMenu();
		swtMenu.notifyListeners(SWT.Show, new Event());
		swtMenu.notifyListeners(SWT.Hide, new Event());
		assertEquals(3, swtMenu.getItemCount());

		item2.setToBeRendered(false);
		item3.setVisible(false);

		swtMenu.notifyListeners(SWT.Show, new Event());
		assertEquals(1, swtMenu.getItemCount());
		assertEquals("item1", swtMenu.getItem(0).getText());
	}

	private MMenuItem createItem(String label) {
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);
		item.setLabel(label);
		return item;
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
