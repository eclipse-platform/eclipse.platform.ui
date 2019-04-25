/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.internal.workbench.addons.CommandProcessingAddon;
import org.eclipse.e4.ui.internal.workbench.addons.HandlerProcessingAddon;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MMenuItemTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private IEclipseContext appContext;

	@Inject
	private MApplication application;

	@Before
	public void setUp() {
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, appContext);
	}

	private void testMMenuItem_Text(String before, String beforeExpected, String after, String afterExpected) {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);

		menuItem.setLabel(before);

		window.setMainMenu(menu);
		menu.getChildren().add(menuItem);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		((MenuManager) ((Widget) menu.getWidget()).getData()).updateAll(true);

		Object widget = menuItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget;

		assertEquals(beforeExpected, menuItemWidget.getText());

		menuItem.setLabel(after);

		assertEquals(afterExpected, menuItemWidget.getText());
	}

	@Test
	public void testMMenuItem_Text_NullNull() {
		testMMenuItem_Text(null, "", null, "");
	}

	@Test
	public void testMMenuItem_Text_NullEmpty() {
		testMMenuItem_Text(null, "", "", "");
	}

	@Test
	public void testMMenuItem_Text_NullString() {
		testMMenuItem_Text(null, "", "label", "label");
	}

	@Test
	public void testMMenuItem_Text_EmptyNull() {
		testMMenuItem_Text("", "", null, "");
	}

	@Test
	public void testMMenuItem_Text_EmptyEmpty() {
		testMMenuItem_Text("", "", "", "");
	}

	@Test
	public void testMMenuItem_Text_EmptyString() {
		testMMenuItem_Text("", "", "label", "label");
	}

	@Test
	public void testMMenuItem_Text_StringNull() {
		testMMenuItem_Text("label", "label", null, "");
	}

	@Test
	public void testMMenuItem_Text_StringEmpty() {
		testMMenuItem_Text("label", "label", "", "");
	}

	@Test
	public void testMMenuItem_Text_StringStringUnchanged() {
		testMMenuItem_Text("label", "label", "label", "label");
	}

	@Test
	public void testMMenuItem_Text_StringStringChanged() {
		testMMenuItem_Text("label", "label", "label2", "label2");
	}

	@Test
	public void testMMenuItem_RadioItems() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem menuItem1 = ems.createModelElement(MDirectMenuItem.class);
		MMenuItem menuItem2 = ems.createModelElement(MDirectMenuItem.class);

		menuItem1.setType(ItemType.RADIO);
		menuItem2.setType(ItemType.RADIO);

		menu.getChildren().add(menuItem1);
		menu.getChildren().add(menuItem2);
		window.setMainMenu(menu);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		((MenuManager) ((Widget) menu.getWidget()).getData()).updateAll(true);

		Object widget1 = menuItem1.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		Object widget2 = menuItem2.getWidget();
		assertNotNull(widget2);
		assertTrue(widget2 instanceof MenuItem);

		MenuItem menuItemWidget1 = (MenuItem) widget1;
		MenuItem menuItemWidget2 = (MenuItem) widget2;

		// test that 'clicking' on the item updates the model
		menuItemWidget1.setSelection(false);
		menuItemWidget2.setSelection(true);
		menuItemWidget1.notifyListeners(SWT.Selection, new Event());
		menuItemWidget2.notifyListeners(SWT.Selection, new Event());

		assertFalse(menuItem1.isSelected());
		assertTrue(menuItem2.isSelected());

		menuItemWidget2.setSelection(false);
		menuItemWidget1.setSelection(true);
		menuItemWidget2.notifyListeners(SWT.Selection, new Event());
		menuItemWidget1.notifyListeners(SWT.Selection, new Event());

		assertTrue(menuItem1.isSelected());
		assertFalse(menuItem2.isSelected());

		// Check that model changes are reflected in the items
		menuItem1.setSelected(false);
		assertFalse(menuItemWidget1.getSelection());
		menuItem2.setSelected(true);
		assertTrue(menuItemWidget2.getSelection());
	}

	@Test
	public void testMDirectMenuItem_Check_Bug316752() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);

		menuItem.setType(ItemType.CHECK);
		menuItem.setSelected(true);

		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		((MenuManager) ((Widget) menu.getWidget()).getData()).updateAll(true);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget1;
		assertTrue(menuItemWidget.getSelection());
	}

	@Test
	public void testMHandledMenuItem_Check_Bug316752() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu menu = ems.createModelElement(MMenu.class);
		MHandledMenuItem menuItem = ems.createModelElement(MHandledMenuItem.class);
		MCommand command = ems.createModelElement(MCommand.class);

		command.setElementId("commandId");

		menuItem.setCommand(command);
		menuItem.setType(ItemType.CHECK);
		menuItem.setSelected(true);

		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MenuManager barManager = (MenuManager) ((Menu) menu.getWidget()).getData();
		barManager.updateAll(true);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget1;
		assertTrue(menuItemWidget.getSelection());
	}

	@Test
	public void testMHandledMenuItem_Check_Bug463280() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu menu = ems.createModelElement(MMenu.class);
		MHandledMenuItem menuItem = ems.createModelElement(MHandledMenuItem.class);
		MCommand command = ems.createModelElement(MCommand.class);

		command.setElementId("commandId");

		menuItem.setCommand(command);
		menuItem.setType(ItemType.CHECK);
		menuItem.setSelected(true);
		menuItem.setLabel("&Test Xxx");
		menuItem.setMnemonics("");

		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MenuManager barManager = (MenuManager) ((Menu) menu.getWidget()).getData();
		barManager.updateAll(true);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget1;
		assertFalse(menuItemWidget.getText().startsWith("&&"));
	}

	@Test
	public void testSubMenuCreation() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);
		MenuManager manager = renderer.getManager(mainMenu);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(3, fileManager.getSize());
	}

	@Test
	public void testTbrItem() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);
		item2.setToBeRendered(false);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);
		MenuManager manager = renderer.getManager(mainMenu);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(2, fileManager.getSize());
	}

	@Test
	public void testInvisibleItem() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);
		item2.setVisible(false);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);
		MenuManager manager = renderer.getManager(mainMenu);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(3, fileManager.getSize());

		assertEquals(false, fileManager.getItems()[2].isVisible());
	}

	@Test
	public void testMenuContribution() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		application.getChildren().add(window);
		application.getMenuContributions().add(createContribution(false));
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);

		MenuManager fileManager = renderer.getManager(fileMenu);
		assertNotNull("No file menu?", fileManager);

		assertEquals(4, fileManager.getSize());

		assertEquals("mmc.item1", fileManager.getItems()[3].getId());
	}

	@Test
	public void testWithVisible() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		application.getChildren().add(window);
		application.getMenuContributions().add(createContribution(true));
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);

		MenuManager fileManager = renderer.getManager(fileMenu);
		assertNotNull("No file menu?", fileManager);

		assertEquals(4, fileManager.getSize());

		IContributionItem mmcItem = fileManager.getItems()[3];
		assertEquals("mmc.item1", mmcItem.getId());
		assertEquals("before the first show, we have no context to evaluate", true, mmcItem.isVisible());

		MenuManager manager = renderer.getManager(mainMenu);
		manager.updateAll(true);
		Menu fileWidget = fileManager.getMenu();
		assertNotNull(fileWidget);

		Event show = new Event();
		show.widget = fileWidget;
		show.type = SWT.Show;

		Event hide = new Event();
		hide.widget = fileWidget;
		hide.type = SWT.Hide;

		fileWidget.notifyListeners(SWT.Show, show);

		assertEquals("after the first show, it should not be visible", false, mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Hide, hide);

		appContext.set("mmc1", Boolean.TRUE);

		assertEquals("Change should not show up until next show", false,
				mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Show, show);

		assertEquals(true, mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Hide, hide);

		appContext.remove("mmc1");

		fileWidget.notifyListeners(SWT.Show, show);

		assertEquals(false, mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Hide, hide);
	}

	@Test
	public void testVisibilityOfMenuItemChangesBasedOnCoreExpression() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		application.getChildren().add(window);
		createMenuContributionWithCoreExpression(application);
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);
		MenuManager manager = renderer.getManager(mainMenu);
		manager.updateAll(true);

		assertEquals(2, manager.getSize());

		MenuManager vanishManager = (MenuManager) manager.getItems()[1];
		assertEquals("vanish", vanishManager.getId());
		// core expression evaluates to false, hence menu should not be visible
		assertFalse(vanishManager.isVisible());
		assertNull(vanishManager.getMenu());

		// ensure core expression now evaluates to true
		appContext.set("mmc1", Boolean.TRUE);

		// menu should be visible now
		assertTrue(vanishManager.isVisible());
		assertNotNull(vanishManager.getMenu());

		// again, ensure that coreexpression evaluates to false
		appContext.remove("mmc1");

		assertFalse(vanishManager.isVisible());
		Menu vanishMenu = vanishManager.getMenu();
		if (vanishMenu != null) {
			assertTrue(vanishMenu.isDisposed());
		}

		appContext.set("mmc1", Boolean.TRUE);

		assertTrue(vanishManager.isVisible());
		assertNotNull(vanishManager.getMenu());
		assertFalse(vanishManager.getMenu().isDisposed());
	}

	@Test
	public void testVisibilityOfMenuItemChangesBasedOnImperativeExpression() {
		MWindow window = ems.createModelElement(MWindow.class);
		MMenu mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(mainMenu);

		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = ems.createModelElement(MMenuSeparator.class);
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		application.getChildren().add(window);
		createMenuContributionWithImperativeExpression(application);
		contextRule.createAndRunWorkbench(window);

		MenuManagerRenderer renderer = getRenderer(appContext, mainMenu);
		MenuManager manager = renderer.getManager(mainMenu);
		manager.updateAll(true);

		assertEquals(2, manager.getSize());

		MenuManager vanishManager = (MenuManager) manager.getItems()[1];
		assertEquals("vanish", vanishManager.getId());
		// core expression evaluates to false, hence menu should not be visible
		assertFalse(vanishManager.isVisible());
		assertNull(vanishManager.getMenu());

		// ensure core expression now evaluates to true
		appContext.set("mmc1", Boolean.TRUE);

		// menu should be visible now
		assertTrue(vanishManager.isVisible());
		assertNotNull(vanishManager.getMenu());

		// again, ensure that coreexpression evaluates to false
		appContext.remove("mmc1");

		assertFalse(vanishManager.isVisible());
		Menu vanishMenu = vanishManager.getMenu();
		if (vanishMenu != null) {
			assertTrue(vanishMenu.isDisposed());
		}

		appContext.set("mmc1", Boolean.TRUE);

		assertTrue(vanishManager.isVisible());
		assertNotNull(vanishManager.getMenu());
		assertFalse(vanishManager.getMenu().isDisposed());
	}

	@Test
	public void testElementHierarchyInContext_DirectItem() {
		MWindow window = ems.createModelElement(MWindow.class);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		final MPart activePart = ems.createModelElement(MPart.class);
		final MPart inactivePart = ems.createModelElement(MPart.class);
		stack.getChildren().add(activePart);
		stack.getChildren().add(inactivePart);
		stack.setSelectedElement(activePart);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MMenu menu = ems.createModelElement(MMenu.class);
		MDirectMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);
		final boolean executed[] = { false };
		menuItem.setObject(new Object() {
			@Execute
			public void execute(MUIElement uiElement, MMenuItem menuItem,
					MDirectMenuItem directMenuItem, MPart part,
					@Named("key") String key) {
				// items should be resolved in the leaf tab, so the
				// MPart should be activePart, but all menu item things
				// should be the menuItem
				assertEquals(menuItem, directMenuItem);
				assertEquals(menuItem, menuItem);
				assertEquals(menuItem, uiElement);

				assertEquals(part, activePart);
				assertEquals(key, "active");
				executed[0] = true;
			}
		});
		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		// force the part activation to ensure they have a context
		EPartService eps = window.getContext().get(EPartService.class);
		eps.activate(inactivePart);
		eps.activate(activePart);
		assertEquals(activePart, eps.getActivePart());

		activePart.getContext().set("key", "active");
		inactivePart.getContext().set("key", "inactive");

		assertFalse(executed[0]);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);
		((MenuItem) widget1).notifyListeners(SWT.Selection, new Event());

		assertTrue(executed[0]);
	}

	@Test
	public void testElementHierarchyInContext_HandledItem() {
		MWindow window = ems.createModelElement(MWindow.class);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		final MPart activePart = ems.createModelElement(MPart.class);
		final MPart inactivePart = ems.createModelElement(MPart.class);
		stack.getChildren().add(activePart);
		stack.getChildren().add(inactivePart);
		stack.setSelectedElement(activePart);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MCommand command = ems.createModelElement(MCommand.class);
		command.setElementId("testElementHierarchyInContext_HandledItem");
		command.setCommandName("Test HandledItem");

		MMenu menu = ems.createModelElement(MMenu.class);
		MHandledMenuItem menuItem = ems.createModelElement(MHandledMenuItem.class);
		menuItem.setCommand(command);

		MHandler handler = ems.createModelElement(MHandler.class);
		handler.setCommand(command);
		final boolean executed[] = { false };
		handler.setObject(new Object() {
			@Execute
			public void execute(MUIElement uiElement, MMenuItem menuItem,
					MHandledMenuItem handledMenuItem, MPart part,
					@Named("key") String key) {
				// items should be resolved in the leaf tab, so the
				// MPart should be activePart, but all menu item things
				// should be the menuItem
				assertEquals(menuItem, handledMenuItem);
				assertEquals(menuItem, menuItem);
				assertEquals(menuItem, uiElement);
				assertEquals(part, activePart);
				assertEquals(key, "active");
				executed[0] = true;
			}
		});

		window.getHandlers().add(handler);
		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		application.getCommands().add(command);
		application.getChildren().add(window);
		// The handler processing addon cannot run until the context
		// contains the MApplication
		ContextInjectionFactory.make(CommandProcessingAddon.class, appContext);
		ContextInjectionFactory.make(HandlerProcessingAddon.class, appContext);

		contextRule.createAndRunWorkbench(window);

		// force the part activation to ensure they have a context
		EPartService eps = window.getContext().get(EPartService.class);
		eps.activate(inactivePart);
		eps.activate(activePart);
		assertEquals(activePart, eps.getActivePart());

		activePart.getContext().set("key", "active");
		inactivePart.getContext().set("key", "inactive");

		assertFalse(executed[0]);
		assertEquals(activePart, window.getContext().get(EPartService.class)
				.getActivePart());

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);
		((MenuItem) widget1).notifyListeners(SWT.Selection, new Event());

		assertTrue(executed[0]);
	}

	private MMenuContribution createContribution(boolean withVisibleWhen) {
		MMenuContribution mmc = ems.createModelElement(MMenuContribution.class);
		mmc.setElementId("test.contrib1");
		mmc.setParentId("file");
		mmc.setPositionInParent("after=additions");

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("mmc.item1");
		item1.setLabel("mmc.item1");
		mmc.getChildren().add(item1);

		if (withVisibleWhen) {
			MCoreExpression exp = ems.createModelElement(MCoreExpression.class);
			exp.setCoreExpressionId("org.eclipse.e4.ui.tests.withMmc1");
			mmc.setVisibleWhen(exp);
		}

		return mmc;
	}

	private void createMenuContributionWithCoreExpression(MApplication application) {
		MMenuContribution mmc = ems.createModelElement(MMenuContribution.class);
		mmc.setElementId("test.contrib2");
		mmc.setParentId("org.eclipse.ui.main.menu");
		mmc.setPositionInParent("after=additions");

		MMenu menu = ems.createModelElement(MMenu.class);
		menu.setElementId("vanish");
		menu.setLabel("Vanish");
		mmc.getChildren().add(menu);

		MCoreExpression exp = ems.createModelElement(MCoreExpression.class);
		exp.setCoreExpressionId("org.eclipse.e4.ui.tests.withMmc1");
		mmc.setVisibleWhen(exp);

		application.getMenuContributions().add(mmc);

		mmc = ems.createModelElement(MMenuContribution.class);
		mmc.setElementId("test.contrib3");
		mmc.setParentId("vanish");
		mmc.setPositionInParent("after=additions");

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("mmc.item2");
		item1.setLabel("mmc.item2");
		mmc.getChildren().add(item1);

		application.getMenuContributions().add(mmc);
	}

	private void createMenuContributionWithImperativeExpression(MApplication application) {
		MMenuContribution mmc = ems.createModelElement(MMenuContribution.class);
		mmc.setElementId("test.contrib2");
		mmc.setParentId("org.eclipse.ui.main.menu");
		mmc.setPositionInParent("after=additions");

		MMenu menu = ems.createModelElement(MMenu.class);
		menu.setElementId("vanish");
		menu.setLabel("Vanish");
		mmc.getChildren().add(menu);

		MImperativeExpression exp = ems.createModelElement(MImperativeExpression.class);
		exp.setTracking(true);
		exp.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.ImperativeExpressionTestEvaluation");
		mmc.setVisibleWhen(exp);
		menu.setVisibleWhen(exp);

		application.getMenuContributions().add(mmc);

		mmc = ems.createModelElement(MMenuContribution.class);
		mmc.setElementId("test.contrib3");
		mmc.setParentId("vanish");
		mmc.setPositionInParent("after=additions");

		MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("mmc.item2");
		item1.setLabel("mmc.item2");
		mmc.getChildren().add(item1);

		application.getMenuContributions().add(mmc);
	}

	private MenuManagerRenderer getRenderer(IEclipseContext context, MUIElement element) {
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		AbstractPartRenderer renderer = rendererFactory.getRenderer(element, null);
		assertEquals(MenuManagerRenderer.class, renderer.getClass());
		return (MenuManagerRenderer) renderer;
	}

}
