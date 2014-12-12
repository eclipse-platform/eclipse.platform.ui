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

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.BindingServiceAddon;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.renderers.swt.DirectContributionItem;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.menus.MenuPersistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MToolItemTest {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	private ToolBar getToolBar(Composite intermediate) {
		for (Control child : intermediate.getChildren()) {
			if (child.getData() instanceof ToolBarManager) {
				return (ToolBar) child;
			}
		}
		return null;
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
	public void test01Children() {
		MToolBar toolbarModel = MenuFactoryImpl.eINSTANCE.createToolBar();
		toolbarModel.setElementId("p2.tb1");

		MToolBar toolbarModel2 = MenuFactoryImpl.eINSTANCE.createToolBar();
		toolbarModel2.setElementId("p2.tb2");

		MToolBarSeparator sep = MenuFactoryImpl.eINSTANCE
				.createToolBarSeparator();
		sep.setElementId("additions");
		toolbarModel.getChildren().add(sep);
		assertEquals(1, toolbarModel.getChildren().size());
		assertEquals(0, toolbarModel2.getChildren().size());

		toolbarModel2.getChildren().addAll(toolbarModel.getChildren());
		assertEquals(0, toolbarModel.getChildren().size());
		assertEquals(1, toolbarModel2.getChildren().size());
	}

	@Test
	@Ignore("See bug 452765")
	public void testFileItemContributionVisibility() {
		MApplication application = TestUtil.setupRenderer(appContext);
		// MenuManagerRenderer renderer = appContext
		// .get(MenuManagerRenderer.class);
		MTrimmedWindow window = (MTrimmedWindow) application.getChildren().get(
				0);
		MTrimBar coolbar = window.getTrimBars().get(0);
		assertNotNull(coolbar);

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(coolbar);

		MToolBar file = (MToolBar) coolbar.getChildren().get(1);
		assertEquals(5, coolbar.getChildren().size());

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p4");
		mp.reRead();

		ToolBarManagerRenderer renderer = appContext
				.get(ToolBarManagerRenderer.class);
		Shell shell = new Shell();
		Composite parent = new Composite(shell, SWT.NONE);
		Composite intermediate = (Composite) renderer
				.createWidget(file, parent);
		assertNotNull(intermediate);
		Control[] childTB = intermediate.getChildren();
		assertTrue(childTB.length > 0);
		ToolBar toolbar = (ToolBar) childTB[0];

		assertNotNull(toolbar);
		Object obj = file;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		ToolBarManager tbm = renderer.getManager(file);
		assertNotNull(tbm);

		IContributionItem[] tbItems = tbm.getItems();
		assertEquals(12, tbItems.length);
		IContributionItem p4InvOne = tbItems[8];
		assertEquals("p4.invisible.commandOne", p4InvOne.getId());
		assertFalse(p4InvOne.isVisible());

		IEclipseContext context = window.getContext();

		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));

		assertTrue(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				new Object[] { "show.p4.invisible.commandOne",
						"show.p4.invisible.commandOne" }));
		assertFalse(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));
		assertTrue(p4InvOne.isVisible());

		context.set("selection", new StructuredSelection(
				"hide.p4.invisible.commandOne"));
		assertFalse(p4InvOne.isVisible());
		context.set("selection", new StructuredSelection(
				"show.p4.invisible.commandOne"));
		assertTrue(p4InvOne.isVisible());

		context.remove("selection");
		assertFalse(p4InvOne.isVisible());
	}

	@Ignore("See bug 452765")
	@Test
	public void testFileItemGeneration() {
		MApplication application = TestUtil.setupRenderer(appContext);
		// MenuManagerRenderer renderer = appContext
		// .get(MenuManagerRenderer.class);
		MTrimmedWindow window = (MTrimmedWindow) application.getChildren().get(
				0);
		MTrimBar coolbar = window.getTrimBars().get(0);
		assertNotNull(coolbar);

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(coolbar);

		MToolBar file = (MToolBar) coolbar.getChildren().get(1);
		assertEquals(5, coolbar.getChildren().size());

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p1");
		mp.reRead();

		ToolBarManagerRenderer renderer = appContext
				.get(ToolBarManagerRenderer.class);
		Shell shell = new Shell();
		Composite parent = new Composite(shell, SWT.NONE);
		Composite intermediate = (Composite) renderer
				.createWidget(file, parent);
		assertNotNull(intermediate);
		Control[] childTB = intermediate.getChildren();
		assertTrue(childTB.length > 0);
		ToolBar toolbar = (ToolBar) childTB[0];

		assertNotNull(toolbar);
		Object obj = file;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		ToolBarManager tbm = renderer.getManager(file);
		assertNotNull(tbm);

		assertEquals(13, tbm.getItems().length);
		IContributionItem saveAll = tbm.find("saveAll");
		assertNotNull(saveAll);
		assertTrue(saveAll instanceof DirectContributionItem);

		IContributionItem cmdTwo = tbm.getItems()[8];
		assertEquals("org.eclipse.e4.ui.menu.tests.commandTwo", cmdTwo.getId());

		IContributionItem cmdOne = tbm.getItems()[12];
		assertEquals("org.eclipse.e4.ui.menu.tests.commandOne", cmdOne.getId());
	}

	@Test
	@Ignore("See bug 452765")
	public void testFileToolbarRendered() {
		MApplication application = TestUtil.setupRenderer(appContext);
		// MenuManagerRenderer renderer = appContext
		// .get(MenuManagerRenderer.class);
		MTrimmedWindow window = (MTrimmedWindow) application.getChildren().get(
				0);
		MTrimBar coolbar = window.getTrimBars().get(0);
		assertNotNull(coolbar);

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(coolbar);

		MToolBar file = (MToolBar) coolbar.getChildren().get(1);
		assertEquals(5, coolbar.getChildren().size());

		// read in the relevant extensions.

		ToolBarManagerRenderer renderer = appContext
				.get(ToolBarManagerRenderer.class);
		Shell shell = new Shell();
		Composite parent = new Composite(shell, SWT.NONE);
		Composite intermediate = (Composite) renderer
				.createWidget(file, parent);
		assertNotNull(intermediate);
		Control[] childTB = intermediate.getChildren();
		assertTrue(childTB.length > 0);
		ToolBar toolbar = (ToolBar) childTB[0];

		assertNotNull(toolbar);
		Object obj = file;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		ToolBarManager tbm = renderer.getManager(file);
		assertNotNull(tbm);

		assertEquals(11, tbm.getItems().length);
		IContributionItem saveAll = tbm.find("saveAll");
		assertNotNull(saveAll);
		assertTrue(saveAll instanceof DirectContributionItem);
	}

	@Test
	public void testMToolItem_RadioItems() {
		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		MToolItem toolItem1 = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		MToolItem toolItem2 = MenuFactoryImpl.eINSTANCE.createDirectToolItem();

		toolItem1.setType(ItemType.RADIO);
		toolItem2.setType(ItemType.RADIO);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem1);
		toolBar.getChildren().add(toolItem2);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget1 = toolItem1.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof ToolItem);

		Object widget2 = toolItem2.getWidget();
		assertNotNull(widget2);
		assertTrue(widget2 instanceof ToolItem);

		ToolItem toolItemWidget1 = (ToolItem) widget1;
		ToolItem toolItemWidget2 = (ToolItem) widget2;

		// test that 'clicking' on the item updates the model
		toolItemWidget1.setSelection(false);
		toolItemWidget2.setSelection(true);
		toolItemWidget1.notifyListeners(SWT.Selection, new Event());
		toolItemWidget2.notifyListeners(SWT.Selection, new Event());

		assertFalse(toolItem1.isSelected());
		assertTrue(toolItem2.isSelected());

		toolItemWidget2.setSelection(false);
		toolItemWidget1.setSelection(true);
		toolItemWidget2.notifyListeners(SWT.Selection, new Event());
		toolItemWidget1.notifyListeners(SWT.Selection, new Event());

		assertTrue(toolItem1.isSelected());
		assertFalse(toolItem2.isSelected());

		// Check that model changes are reflected in the items
		toolItem1.setSelected(false);
		assertFalse(toolItemWidget1.getSelection());
		toolItem2.setSelected(true);
		assertTrue(toolItemWidget2.getSelection());
	}

	private void testMToolItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		MToolItem toolItem = MenuFactoryImpl.eINSTANCE.createDirectToolItem();

		toolItem.setLabel(before);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget = toolItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof ToolItem);

		ToolItem toolItemWidget = (ToolItem) widget;

		assertEquals(beforeExpected, toolItemWidget.getText());

		toolItem.setLabel(after);

		assertEquals(afterExpected, toolItemWidget.getText());
	}

	@Test
	public void testMToolItem_Text_EmptyEmpty() {
		testMToolItem_Text("", "", "", "");
	}

	@Test
	public void testMToolItem_Text_EmptyNull() {
		testMToolItem_Text("", "", null, "");
	}

	@Test
	public void testMToolItem_Text_EmptyString() {
		testMToolItem_Text("", "", "label", "label");
	}

	@Test
	public void testMToolItem_Text_NullEmpty() {
		testMToolItem_Text(null, "", "", "");
	}

	@Test
	public void testMToolItem_Text_NullNull() {
		testMToolItem_Text(null, "", null, "");
	}

	@Test
	public void testMToolItem_Text_NullString() {
		testMToolItem_Text(null, "", "label", "label");
	}

	@Test
	public void testMToolItem_Text_StringEmpty() {
		testMToolItem_Text("label", "label", "", "");
	}

	@Test
	public void testMToolItem_Text_StringNull() {
		testMToolItem_Text("label", "label", null, "");
	}

	@Test
	public void testMToolItem_Text_StringStringChanged() {
		testMToolItem_Text("label", "label", "label2", "label2");
	}

	@Test
	public void testMToolItem_Text_StringStringUnchanged() {
		testMToolItem_Text("label", "label", "label", "label");
	}

	private void testMToolItem_Tooltip(String before, String beforeExpected,
			String after, String afterExpected) {
		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		MToolItem toolItem = MenuFactoryImpl.eINSTANCE.createDirectToolItem();

		toolItem.setTooltip(before);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget = toolItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof ToolItem);

		ToolItem toolItemWidget = (ToolItem) widget;

		assertEquals(beforeExpected, toolItemWidget.getToolTipText());

		toolItem.setTooltip(after);

		assertEquals(afterExpected, toolItemWidget.getToolTipText());
	}

	@Test
	public void testMToolItem_Tooltip_EmptyEmpty() {
		testMToolItem_Tooltip("", "", "", "");
	}

	@Test
	public void testMToolItem_Tooltip_EmptyNull() {
		testMToolItem_Tooltip("", "", null, null);
	}

	@Test
	public void testMToolItem_Tooltip_EmptyString() {
		testMToolItem_Tooltip("", "", "toolTip", "toolTip");
	}

	@Test
	public void testMToolItem_Tooltip_NullEmpty() {
		testMToolItem_Tooltip(null, null, "", "");
	}

	@Test
	public void testMToolItem_Tooltip_NullNull() {
		testMToolItem_Tooltip(null, null, null, null);
	}

	@Test
	public void testMToolItem_Tooltip_NullString() {
		testMToolItem_Tooltip(null, null, "toolTip", "toolTip");
	}

	@Test
	public void testMToolItem_Tooltip_StringEmpty() {
		testMToolItem_Tooltip("toolTip", "toolTip", "", "");
	}

	@Test
	public void testMToolItem_Tooltip_StringNull() {
		testMToolItem_Tooltip("toolTip", "toolTip", null, null);
	}

	@Test
	public void testMToolItem_Tooltip_StringStringChanged() {
		testMToolItem_Tooltip("toolTip", "toolTip", "toolTip2", "toolTip2");
	}

	@Test
	public void testMToolItem_Tooltip_StringStringUnchanged() {
		testMToolItem_Tooltip("toolTip", "toolTip", "toolTip", "toolTip");
	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetAddedToFile() {
		MApplication application = TestUtil.setupRenderer(appContext);
		// MenuManagerRenderer renderer = appContext
		// .get(MenuManagerRenderer.class);
		MTrimmedWindow window = (MTrimmedWindow) application.getChildren().get(
				0);
		MTrimBar coolbar = window.getTrimBars().get(0);
		assertNotNull(coolbar);

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(coolbar);

		MToolBar file = (MToolBar) coolbar.getChildren().get(1);
		assertEquals(5, coolbar.getChildren().size());

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p3");
		mp.reRead();

		ToolBarManagerRenderer renderer = appContext
				.get(ToolBarManagerRenderer.class);
		Shell shell = new Shell();
		Composite parent = new Composite(shell, SWT.NONE);
		Composite intermediate = (Composite) renderer
				.createWidget(file, parent);
		assertNotNull(intermediate);
		Control[] childTB = intermediate.getChildren();
		assertTrue(childTB.length > 0);
		ToolBar toolbar = (ToolBar) childTB[0];

		assertNotNull(toolbar);
		Object obj = file;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		ToolBarManager tbm = renderer.getManager(file);
		assertNotNull(tbm);

		IContributionItem[] tbItems = tbm.getItems();
		assertEquals(12, tbItems.length);

		IContributionItem actionSetAction = tbItems[2];
		assertEquals("org.eclipse.e4.ui.menu.tests.p3.toolAction1",
				actionSetAction.getId());
		assertFalse(actionSetAction.isVisible());

		IEclipseContext windowContext = window.getContext();
		EContextService ecs = windowContext.get(EContextService.class);
		ecs.activateContext("org.eclipse.e4.ui.menu.tests.p3.toolSet");
		assertTrue(actionSetAction.isVisible());

		ecs.deactivateContext("org.eclipse.e4.ui.menu.tests.p3.toolSet");
		assertFalse(actionSetAction.isVisible());
	}

	@Test
	@Ignore("MenuPersistence no longer processes actionSets")
	public void XXXXtestActionSetAddedToMyToolbar() {
		MApplication application = TestUtil.setupRenderer(appContext);
		// MenuManagerRenderer renderer = appContext
		// .get(MenuManagerRenderer.class);
		MTrimmedWindow window = (MTrimmedWindow) application.getChildren().get(
				0);
		MTrimBar coolbar = window.getTrimBars().get(0);
		assertNotNull(coolbar);

		// setup structure for actionSet test
		TestUtil.setupActionBuilderStructure(coolbar);

		int idx = 0;
		for (MTrimElement child : coolbar.getChildren()) {
			if (child.getElementId().equals("additions")) {
				break;
			}
			idx++;
		}

		MToolBar toolbarModel = MenuFactoryImpl.eINSTANCE.createToolBar();
		toolbarModel.setElementId("p2.tb1");
		coolbar.getChildren().add(idx, toolbarModel);

		// read in the relevant extensions.
		MenuPersistence mp = new MenuPersistence(application, appContext,
				"org.eclipse.e4.ui.menu.tests.p2");
		mp.reRead();

		TestUtil.printContributions(application);

		ToolBarManagerRenderer renderer = appContext
				.get(ToolBarManagerRenderer.class);
		Shell shell = new Shell();
		Composite parent = new Composite(shell, SWT.NONE);
		Composite intermediate = (Composite) renderer.createWidget(
				toolbarModel, parent);
		assertNotNull(intermediate);
		Control[] childTB = intermediate.getChildren();
		assertTrue(childTB.length > 0);
		ToolBar toolbar = (ToolBar) childTB[0];

		assertNotNull(toolbar);
		Object obj = toolbarModel;
		renderer.processContents((MElementContainer<MUIElement>) obj);

		ToolBarManager tbm = renderer.getManager(toolbarModel);
		assertNotNull(tbm);

		IContributionItem[] tbItems = tbm.getItems();
		assertEquals(7, tbItems.length);

		assertEquals("group1", tbItems[0].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.tb1", tbItems[1].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.tb2", tbItems[2].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.tb3", tbItems[3].getId());
		assertEquals("group2", tbItems[4].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.tb4", tbItems[5].getId());
		assertEquals("org.eclipse.e4.ui.menu.tests.p2.tb5", tbItems[6].getId());
	}
}
