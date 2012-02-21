/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.inject.Named;
import junit.framework.TestCase;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.ContextServiceAddon;
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

public class MMenuItemTest extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, appContext);
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	private void testMMenuItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menuItem.setLabel(before);

		window.setMainMenu(menu);
		menu.getChildren().add(menuItem);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		((MenuManager) ((Widget) menu.getWidget()).getData()).updateAll(true);

		Object widget = menuItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget;

		assertEquals(beforeExpected, menuItemWidget.getText());

		menuItem.setLabel(after);

		assertEquals(afterExpected, menuItemWidget.getText());
	}

	public void testMMenuItem_Text_NullNull() {
		testMMenuItem_Text(null, "", null, "");
	}

	public void testMMenuItem_Text_NullEmpty() {
		testMMenuItem_Text(null, "", "", "");
	}

	public void testMMenuItem_Text_NullString() {
		testMMenuItem_Text(null, "", "label", "label");
	}

	public void testMMenuItem_Text_EmptyNull() {
		testMMenuItem_Text("", "", null, "");
	}

	public void testMMenuItem_Text_EmptyEmpty() {
		testMMenuItem_Text("", "", "", "");
	}

	public void testMMenuItem_Text_EmptyString() {
		testMMenuItem_Text("", "", "label", "label");
	}

	public void testMMenuItem_Text_StringNull() {
		testMMenuItem_Text("label", "label", null, "");
	}

	public void testMMenuItem_Text_StringEmpty() {
		testMMenuItem_Text("label", "label", "", "");
	}

	public void testMMenuItem_Text_StringStringUnchanged() {
		testMMenuItem_Text("label", "label", "label", "label");
	}

	public void testMMenuItem_Text_StringStringChanged() {
		testMMenuItem_Text("label", "label", "label2", "label2");
	}

	public void testMMenuItem_RadioItems() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem menuItem1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		MMenuItem menuItem2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menuItem1.setType(ItemType.RADIO);
		menuItem2.setType(ItemType.RADIO);

		menu.getChildren().add(menuItem1);
		menu.getChildren().add(menuItem2);
		window.setMainMenu(menu);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

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

	public void testMDirectMenuItem_Check_Bug316752() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menuItem.setType(ItemType.CHECK);
		menuItem.setSelected(true);

		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		((MenuManager) ((Widget) menu.getWidget()).getData()).updateAll(true);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget1;
		assertTrue(menuItemWidget.getSelection());
	}

	public void testMHandledMenuItem_Check_Bug316752() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MHandledMenuItem menuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();

		command.setElementId("commandId");

		menuItem.setCommand(command);
		menuItem.setType(ItemType.CHECK);
		menuItem.setSelected(true);

		menu.getChildren().add(menuItem);
		window.setMainMenu(menu);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManager barManager = (MenuManager) ((Menu) menu.getWidget())
				.getData();
		barManager.updateAll(true);

		Object widget1 = menuItem.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget1;
		assertTrue(menuItemWidget.getSelection());
	}

	public void testSubMenuCreation() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);
		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(3, fileManager.getSize());
	}

	public void testTbrItem() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);
		item2.setToBeRendered(false);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);
		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(2, fileManager.getSize());
	}

	public void testInvisibleItem() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);
		item2.setVisible(false);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);
		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull("failed to create menu bar manager", manager);

		assertEquals(1, manager.getSize());

		MenuManager fileManager = (MenuManager) manager.getItems()[0];
		MenuManager fileR = renderer.getManager(fileMenu);
		assertEquals(fileManager, fileR);

		assertEquals(3, fileManager.getSize());

		assertEquals(false, fileManager.getItems()[2].isVisible());
	}

	public void testMenuContribution() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);
		application.getMenuContributions().add(createContribution(false));

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);

		MenuManager fileManager = renderer.getManager(fileMenu);
		assertNotNull("No file menu?", fileManager);

		assertEquals(4, fileManager.getSize());

		assertEquals("mmc.item1", fileManager.getItems()[3].getId());
	}

	public void testWithVisible() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);
		application.getMenuContributions().add(createContribution(true));

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);

		MenuManager fileManager = renderer.getManager(fileMenu);
		assertNotNull("No file menu?", fileManager);

		assertEquals(4, fileManager.getSize());

		IContributionItem mmcItem = fileManager.getItems()[3];
		assertEquals("mmc.item1", mmcItem.getId());
		assertEquals("before the first show, we have no context to evaluate",
				true, mmcItem.isVisible());

		MenuManager manager = renderer.getManager(menuBar);
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

		assertEquals("after the first show, it should not be visible", false,
				mmcItem.isVisible());

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

	public void testMenuBarVisibility() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MMenu fileMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		fileMenu.setElementId("file");
		fileMenu.setLabel("File");
		menuBar.getChildren().add(fileMenu);

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("group1");
		fileMenu.getChildren().add(sep);

		MMenuItem item2 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);
		createMenuContribution(application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		MenuManagerRenderer renderer = getRenderer(appContext, menuBar);
		MenuManager manager = renderer.getManager(menuBar);
		manager.updateAll(true);

		assertEquals(2, manager.getSize());

		MenuManager vanishManager = (MenuManager) manager.getItems()[1];
		assertEquals("vanish", vanishManager.getId());

		assertFalse(vanishManager.isVisible());
		assertNull(vanishManager.getMenu());

		appContext.set("mmc1", Boolean.TRUE);

		assertTrue(vanishManager.isVisible());
		assertNotNull(vanishManager.getMenu());

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

	public void testElementHierarchyInContext_DirectItem() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MPartStack stack = MBasicFactory.INSTANCE.createPartStack();
		final MPart activePart = MBasicFactory.INSTANCE.createPart();
		final MPart inactivePart = MBasicFactory.INSTANCE.createPart();
		stack.getChildren().add(activePart);
		stack.getChildren().add(inactivePart);
		stack.setSelectedElement(activePart);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MDirectMenuItem menuItem = MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem();
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

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

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

	public void testElementHierarchyInContext_HandledItem() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MPartStack stack = MBasicFactory.INSTANCE.createPartStack();
		final MPart activePart = MBasicFactory.INSTANCE.createPart();
		final MPart inactivePart = MBasicFactory.INSTANCE.createPart();
		stack.getChildren().add(activePart);
		stack.getChildren().add(inactivePart);
		stack.setSelectedElement(activePart);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		command.setElementId("testElementHierarchyInContext_HandledItem");

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MHandledMenuItem menuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		menuItem.setCommand(command);

		MHandler handler = CommandsFactoryImpl.eINSTANCE.createHandler();
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

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getCommands().add(command);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

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
		MMenuContribution mmc = MenuFactoryImpl.eINSTANCE
				.createMenuContribution();
		mmc.setElementId("test.contrib1");
		mmc.setParentId("file");
		mmc.setPositionInParent("after=additions");

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("mmc.item1");
		item1.setLabel("mmc.item1");
		mmc.getChildren().add(item1);

		if (withVisibleWhen) {
			MCoreExpression exp = UiFactoryImpl.eINSTANCE
					.createCoreExpression();
			exp.setCoreExpressionId("org.eclipse.e4.ui.tests.withMmc1");
			mmc.setVisibleWhen(exp);
		}

		return mmc;
	}

	private void createMenuContribution(MApplication application) {
		MMenuContribution mmc = MenuFactoryImpl.eINSTANCE
				.createMenuContribution();
		mmc.setElementId("test.contrib2");
		mmc.setParentId("org.eclipse.ui.main.menu");
		mmc.setPositionInParent("after=additions");

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		menu.setElementId("vanish");
		menu.setLabel("Vanish");
		mmc.getChildren().add(menu);

		MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		exp.setCoreExpressionId("org.eclipse.e4.ui.tests.withMmc1");
		mmc.setVisibleWhen(exp);

		application.getMenuContributions().add(mmc);

		mmc = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		mmc.setElementId("test.contrib3");
		mmc.setParentId("vanish");
		mmc.setPositionInParent("after=additions");

		MMenuItem item1 = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item1.setElementId("mmc.item2");
		item1.setLabel("mmc.item2");
		mmc.getChildren().add(item1);

		// exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		// exp.setCoreExpressionId("org.eclipse.e4.ui.tests.withMmc1");
		// mmc.setVisibleWhen(exp);

		application.getMenuContributions().add(mmc);
	}

	private MenuManagerRenderer getRenderer(IEclipseContext context,
			MUIElement element) {
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		AbstractPartRenderer renderer = rendererFactory.getRenderer(element,
				null);
		assertEquals(MenuManagerRenderer.class, renderer.getClass());
		return (MenuManagerRenderer) renderer;
	}
}
