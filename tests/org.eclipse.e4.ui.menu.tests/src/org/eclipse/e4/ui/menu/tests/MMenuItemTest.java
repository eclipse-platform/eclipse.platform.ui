/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 452764
 *******************************************************************************/

package org.eclipse.e4.ui.menu.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributionRecord;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.menus.MenuPersistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MMenuItemTest {

	final static String PDE_SEARCH_AS_ID = "org.eclipse.pde.ui.SearchActionSet";

	final static String SEARCH_AS_ID = "org.eclipse.search.searchActionSet";

	protected IEclipseContext appContext;

	protected E4Workbench wb;

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

	@Before
	public void setUp() {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		ContextInjectionFactory.make(BindingServiceAddon.class, appContext);
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	@After
	public void tearDown() {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	@Test
	@Ignore("See bug 452765")
	public void testContributionRecordMerging() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MMenu menuBar = application.getChildren().get(0).getMainMenu();

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p1");
		mp.reRead();
		// printContributions(application);

		List<MMenuContribution> menuContributions = application
				.getMenuContributions();
		assertEquals(6, menuContributions.size());

		ContributionRecord twoMenus = new ContributionRecord(menuBar,
				menuContributions.get(0), renderer);
		assertTrue(twoMenus.mergeIntoModel());
		assertEquals(2, menuBar.getChildren().size());

		MMenu withHandlers = (MMenu) menuBar.getChildren().get(1);
		assertEquals(0, withHandlers.getChildren().size());

		ContributionRecord handlers = new ContributionRecord(withHandlers,
				menuContributions.get(2), renderer);
		assertTrue(handlers.mergeIntoModel());
		assertEquals(4, withHandlers.getChildren().size());
	}

	@Test
	public void testMenuContribution() {
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

	@Test
	@Ignore("See bug 452765")
	public void testMenuContributionGeneration() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MMenu menuBar = application.getChildren().get(0).getMainMenu();

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p1");
		mp.reRead();

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(2, manager.getSize());

		MenuManager withGroup = (MenuManager) manager.getItems()[0];
		assertEquals("WithGroup", withGroup.getId());
		assertEquals(4, withGroup.getSize());

		IContributionItem[] withGroupItems = withGroup.getItems();
		assertEquals("group1", withGroupItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandOne",
				withGroupItems[1].getId());
		assertEquals("group2", withGroupItems[2].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandTwo",
				withGroupItems[3].getId());

		MenuManager withHandlers = (MenuManager) manager.getItems()[1];
		assertEquals("WithHandlers", withHandlers.getId());
		assertEquals(5, withHandlers.getSize());

		IContributionItem[] withHandlerItems = withHandlers.getItems();
		assertEquals("org.eclipse.e4.ui.menu.tests.commandOne",
				withHandlerItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandTwo",
				withHandlerItems[1].getId());
		assertEquals("group1", withHandlerItems[2].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandFour",
				withHandlerItems[3].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandThree",
				withHandlerItems[4].getId());

	}

	@Test
	@Ignore("See bug 452765")
	public void testMenuContributionVisibility() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MMenu menuBar = application.getChildren().get(0).getMainMenu();

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p4");
		mp.reRead();

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(2, manager.getSize());

		MenuManager withGroup = (MenuManager) manager.getItems()[0];
		assertEquals("WithGroup", withGroup.getId());
		assertEquals(5, withGroup.getSize());

		IContributionItem[] withGroupItems = withGroup.getItems();
		assertEquals("group1", withGroupItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandOne",
				withGroupItems[1].getId());
		IContributionItem p4InvOne = withGroupItems[2];
		assertEquals("p4.invisible.commandOne", p4InvOne.getId());
		assertEquals("group2", withGroupItems[3].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandTwo",
				withGroupItems[4].getId());

		assertTrue(p4InvOne.isVisible());

		Menu withGroupMenu = withGroup.getMenu();
		assertNotNull(withGroupMenu);

		Event show = new Event();
		show.widget = withGroupMenu;
		show.type = SWT.Show;

		withGroupMenu.notifyListeners(SWT.Show, show);

		Event hide = new Event();
		hide.widget = withGroupMenu;
		hide.type = SWT.Hide;

		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertFalse(p4InvOne.isVisible());

		MWindow window = application.getChildren().get(0);
		IEclipseContext context = window.getContext();

		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertTrue(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				new Object[] { "show.p4.invisible.commandOne",
						"show.p4.invisible.commandOne" }));
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertFalse(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertTrue(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				"hide.p4.invisible.commandOne"));
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertFalse(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertTrue(p4InvOne.isVisible());

		context.remove("selection");
		withGroupMenu.notifyListeners(SWT.Show, show);
		withGroupMenu.notifyListeners(SWT.Hide, hide);

		assertFalse(p4InvOne.isVisible());

		MenuManager withHandlers = (MenuManager) manager.getItems()[1];
		assertEquals("WithHandlers", withHandlers.getId());
		assertEquals(5, withHandlers.getSize());

		IContributionItem[] withHandlerItems = withHandlers.getItems();
		assertEquals("org.eclipse.e4.ui.menu.tests.commandOne",
				withHandlerItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandTwo",
				withHandlerItems[1].getId());
		assertEquals("group1", withHandlerItems[2].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandFour",
				withHandlerItems[3].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.commandThree",
				withHandlerItems[4].getId());

	}

	@Test
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

	@Test
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

	@Test
	public void testMMenuItem_Text_EmptyEmpty() {
		testMMenuItem_Text("", "", "", "");
	}

	@Test
	public void testMMenuItem_Text_EmptyNull() {
		testMMenuItem_Text("", "", null, "");
	}

	@Test
	public void testMMenuItem_Text_EmptyString() {
		testMMenuItem_Text("", "", "label", "label");
	}

	@Test
	public void testMMenuItem_Text_NullEmpty() {
		testMMenuItem_Text(null, "", "", "");
	}

	@Test
	public void testMMenuItem_Text_NullNull() {
		testMMenuItem_Text(null, "", null, "");
	}

	@Test
	public void testMMenuItem_Text_NullString() {
		testMMenuItem_Text(null, "", "label", "label");
	}

	@Test
	public void testMMenuItem_Text_StringEmpty() {
		testMMenuItem_Text("label", "label", "", "");
	}

	@Test
	public void testMMenuItem_Text_StringNull() {
		testMMenuItem_Text("label", "label", null, "");
	}

	@Test
	public void testMMenuItem_Text_StringStringChanged() {
		testMMenuItem_Text("label", "label", "label2", "label2");
	}

	@Test
	public void testMMenuItem_Text_StringStringUnchanged() {
		testMMenuItem_Text("label", "label", "label", "label");
	}

	@Test
	public void testSubMenuCreation() {
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

	@Test
	public void testTbrItem() {
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

	@Test
	@Ignore("TODO")
	public void TODOtestWithVisible() {
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

		fileWidget.notifyListeners(SWT.Show, show);

		Event hide = new Event();
		hide.widget = fileWidget;
		hide.type = SWT.Hide;

		fileWidget.notifyListeners(SWT.Hide, hide);

		assertEquals("after the first show, it should not be visible", false,
				mmcItem.isVisible());

		appContext.set("mmc1", Boolean.TRUE);

		assertEquals("Change should not show up until next show", false,
				mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Show, show);

		fileWidget.notifyListeners(SWT.Hide, hide);

		assertEquals(true, mmcItem.isVisible());

		appContext.remove("mmc1");

		fileWidget.notifyListeners(SWT.Show, show);

		assertEquals(false, mmcItem.isVisible());

		fileWidget.notifyListeners(SWT.Hide, hide);
	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetGeneration() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MMenu menuBar = application.getChildren().get(0).getMainMenu();

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(menuBar);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p2");
		mp.reRead();

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(5, manager.getSize());

		MenuManager editManager = (MenuManager) manager.getItems()[1];
		assertEquals(5, editManager.getSize());
		IContributionItem[] editItems = editManager.getItems();
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.action4",
				editItems[4].getId());

		MenuManager actionManager = (MenuManager) manager.getItems()[3];
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.menu1",
				actionManager.getId());
		assertEquals(5, actionManager.getSize());

		IContributionItem[] actionItems = actionManager.getItems();
		assertEquals("group1", actionItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.action2",
				actionItems[1].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.action1",
				actionItems[2].getId());
		assertEquals("group2", actionItems[3].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.action3",
				actionItems[4].getId());

	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetSharedBothActive() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MWindow window = application.getChildren().get(0);
		MMenu menuBar = window.getMainMenu();

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(menuBar);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p3");
		mp.reRead();
		// printContributions(application);

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(6, manager.getSize());

		MenuManager editManager = (MenuManager) manager.getItems()[1];
		assertEquals(4, editManager.getSize());

		MenuManager searchManager = (MenuManager) manager.getItems()[4];
		assertEquals("org.eclipse.search.menu", searchManager.getId());
		assertFalse(searchManager.isVisible());

		IEclipseContext windowContext = window.getContext();
		EContextService ecs = windowContext.get(EContextService.class);
		ecs.activateContext(SEARCH_AS_ID);
		ecs.activateContext(PDE_SEARCH_AS_ID);
		assertTrue(searchManager.isVisible());

		assertEquals(10, searchManager.getSize());
		IContributionItem[] searchItems = searchManager.getItems();
		assertEquals("internalDialogGroup", searchItems[0].getId());
		assertEquals("org.eclipse.search.OpenSearchDialog",
				searchItems[1].getId());
		assertEquals("org.eclipse.search.OpenFileSearchPage",
				searchItems[2].getId());
		assertEquals("dialogGroup", searchItems[3].getId());
		assertEquals("org.eclipse.pde.ui.actions.OpenPluginSearchPage",
				searchItems[4].getId());
		assertEquals("fileSearchContextMenuActionsGroup",
				searchItems[5].getId());
		assertEquals("textSearchSubMenu", searchItems[6].getId());
		assertEquals("contextMenuActionsGroup", searchItems[7].getId());
		assertEquals("occurencesActionsGroup", searchItems[8].getId());
		assertEquals("extraSearchGroup", searchItems[9].getId());

	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetSharedMenuGeneration() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MWindow window = application.getChildren().get(0);
		MMenu menuBar = window.getMainMenu();

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(menuBar);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p3");
		mp.reRead();
		// printContributions(application);

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(6, manager.getSize());

		MenuManager editManager = (MenuManager) manager.getItems()[1];
		assertEquals(4, editManager.getSize());

		MenuManager searchManager = (MenuManager) manager.getItems()[4];
		assertEquals("org.eclipse.search.menu", searchManager.getId());
		assertFalse(searchManager.isVisible());

		assertEquals(10, searchManager.getSize());
		IContributionItem[] searchItems = searchManager.getItems();
		assertEquals("internalDialogGroup", searchItems[0].getId());
		assertEquals("org.eclipse.search.OpenSearchDialog",
				searchItems[1].getId());
		assertEquals("org.eclipse.search.OpenFileSearchPage",
				searchItems[2].getId());
		assertEquals("dialogGroup", searchItems[3].getId());
		assertEquals("org.eclipse.pde.ui.actions.OpenPluginSearchPage",
				searchItems[4].getId());
		assertEquals("fileSearchContextMenuActionsGroup",
				searchItems[5].getId());
		assertEquals("textSearchSubMenu", searchItems[6].getId());
		assertEquals("contextMenuActionsGroup", searchItems[7].getId());
		assertEquals("occurencesActionsGroup", searchItems[8].getId());
		assertEquals("extraSearchGroup", searchItems[9].getId());

	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetSharedPDEActive() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MWindow window = application.getChildren().get(0);
		MMenu menuBar = window.getMainMenu();

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(menuBar);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p3");
		mp.reRead();
		// printContributions(application);

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(6, manager.getSize());

		MenuManager editManager = (MenuManager) manager.getItems()[1];
		assertEquals(4, editManager.getSize());

		MenuManager searchManager = (MenuManager) manager.getItems()[4];
		assertEquals("org.eclipse.search.menu", searchManager.getId());
		assertFalse(searchManager.isVisible());

		IEclipseContext windowContext = window.getContext();
		EContextService ecs = windowContext.get(EContextService.class);
		ecs.activateContext(PDE_SEARCH_AS_ID);
		assertTrue(searchManager.isVisible());

		assertEquals(10, searchManager.getSize());
		IContributionItem[] searchItems = searchManager.getItems();
		assertEquals("internalDialogGroup", searchItems[0].getId());
		assertEquals("org.eclipse.search.OpenSearchDialog",
				searchItems[1].getId());
		assertEquals("org.eclipse.search.OpenFileSearchPage",
				searchItems[2].getId());
		assertEquals("dialogGroup", searchItems[3].getId());
		assertEquals("org.eclipse.pde.ui.actions.OpenPluginSearchPage",
				searchItems[4].getId());
		assertEquals("fileSearchContextMenuActionsGroup",
				searchItems[5].getId());
		assertEquals("textSearchSubMenu", searchItems[6].getId());
		assertEquals("contextMenuActionsGroup", searchItems[7].getId());
		assertEquals("occurencesActionsGroup", searchItems[8].getId());
		assertEquals("extraSearchGroup", searchItems[9].getId());

	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetSharedSearchActive() {
		MApplication application = TestUtil.setupRenderer(appContext);
		MenuManagerRenderer renderer = appContext
				.get(MenuManagerRenderer.class);
		MWindow window = application.getChildren().get(0);
		MMenu menuBar = window.getMainMenu();

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(menuBar);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p3");
		mp.reRead();
		// printContributions(application);

		// render the main menu bar
		Shell shell = new Shell();
		Menu menu = (Menu) renderer.createWidget(menuBar, shell);
		assertNotNull(menu);
		Object obj = menuBar;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		MenuManager manager = renderer.getManager(menuBar);
		assertNotNull(manager);
		assertEquals(6, manager.getSize());

		MenuManager editManager = (MenuManager) manager.getItems()[1];
		assertEquals(4, editManager.getSize());

		MenuManager searchManager = (MenuManager) manager.getItems()[4];
		assertEquals("org.eclipse.search.menu", searchManager.getId());
		assertFalse(searchManager.isVisible());

		IEclipseContext windowContext = window.getContext();
		EContextService ecs = windowContext.get(EContextService.class);
		ecs.activateContext(SEARCH_AS_ID);
		assertTrue(searchManager.isVisible());

		assertEquals(10, searchManager.getSize());
		IContributionItem[] searchItems = searchManager.getItems();
		assertEquals("internalDialogGroup", searchItems[0].getId());
		assertEquals("org.eclipse.search.OpenSearchDialog",
				searchItems[1].getId());
		assertEquals("org.eclipse.search.OpenFileSearchPage",
				searchItems[2].getId());
		assertEquals("dialogGroup", searchItems[3].getId());
		assertEquals("org.eclipse.pde.ui.actions.OpenPluginSearchPage",
				searchItems[4].getId());
		assertEquals("fileSearchContextMenuActionsGroup",
				searchItems[5].getId());
		assertEquals("textSearchSubMenu", searchItems[6].getId());
		assertEquals("contextMenuActionsGroup", searchItems[7].getId());
		assertEquals("occurencesActionsGroup", searchItems[8].getId());
		assertEquals("extraSearchGroup", searchItems[9].getId());

	}
}
