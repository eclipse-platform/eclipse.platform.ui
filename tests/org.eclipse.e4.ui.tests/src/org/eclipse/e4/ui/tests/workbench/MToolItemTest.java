/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MToolItemTest {
	protected IEclipseContext appContext;
	protected E4Workbench wb;
	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	private void testMToolItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MTrimmedWindow window = ems.createModelElement(MTrimmedWindow.class);
		MTrimBar trimBar = ems.createModelElement(MTrimBar.class);
		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		MToolItem toolItem = ems.createModelElement(MDirectToolItem.class);

		toolItem.setLabel(before);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

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
	public void testMToolItem_Text_NullNull() {
		testMToolItem_Text(null, "", null, "");
	}

	@Test
	public void testMToolItem_Text_NullEmpty() {
		testMToolItem_Text(null, "", "", "");
	}

	@Test
	public void testMToolItem_Text_NullString() {
		testMToolItem_Text(null, "", "label", "label");
	}

	@Test
	public void testMToolItem_Text_EmptyNull() {
		testMToolItem_Text("", "", null, "");
	}

	@Test
	public void testMToolItem_Text_EmptyEmpty() {
		testMToolItem_Text("", "", "", "");
	}

	@Test
	public void testMToolItem_Text_EmptyString() {
		testMToolItem_Text("", "", "label", "label");
	}

	@Test
	public void testMToolItem_Text_StringNull() {
		testMToolItem_Text("label", "label", null, "");
	}

	@Test
	public void testMToolItem_Text_StringEmpty() {
		testMToolItem_Text("label", "label", "", "");
	}

	@Test
	public void testMToolItem_Text_StringStringUnchanged() {
		testMToolItem_Text("label", "label", "label", "label");
	}

	@Test
	public void testMToolItem_Text_StringStringChanged() {
		testMToolItem_Text("label", "label", "label2", "label2");
	}

	private void testMToolItem_Tooltip(String before, String beforeExpected,
			String after, String afterExpected) {
		MTrimmedWindow window = ems.createModelElement(MTrimmedWindow.class);
		MTrimBar trimBar = ems.createModelElement(MTrimBar.class);
		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		MToolItem toolItem = ems.createModelElement(MDirectToolItem.class);

		toolItem.setTooltip(before);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

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
	public void testMToolItem_Tooltip_NullNull() {
		testMToolItem_Tooltip(null, null, null, null);
	}

	@Test
	public void testMToolItem_Tooltip_NullEmpty() {
		testMToolItem_Tooltip(null, null, "", "");
	}

	@Test
	public void testMToolItem_Tooltip_NullString() {
		testMToolItem_Tooltip(null, null, "toolTip", "toolTip");
	}

	@Test
	public void testMToolItem_Tooltip_EmptyNull() {
		testMToolItem_Tooltip("", "", null, null);
	}

	@Test
	public void testMToolItem_Tooltip_EmptyEmpty() {
		testMToolItem_Tooltip("", "", "", "");
	}

	@Test
	public void testMToolItem_Tooltip_EmptyString() {
		testMToolItem_Tooltip("", "", "toolTip", "toolTip");
	}

	@Test
	public void testMToolItem_Tooltip_StringNull() {
		testMToolItem_Tooltip("toolTip", "toolTip", null, null);
	}

	@Test
	public void testMToolItem_Tooltip_StringEmpty() {
		testMToolItem_Tooltip("toolTip", "toolTip", "", "");
	}

	@Test
	public void testMToolItem_Tooltip_StringStringUnchanged() {
		testMToolItem_Tooltip("toolTip", "toolTip", "toolTip", "toolTip");
	}

	@Test
	public void testMToolItem_Tooltip_StringStringChanged() {
		testMToolItem_Tooltip("toolTip", "toolTip", "toolTip2", "toolTip2");
	}

	@Test
	public void testMToolItem_RadioItems() {
		MTrimmedWindow window = ems.createModelElement(MTrimmedWindow.class);
		MTrimBar trimBar = ems.createModelElement(MTrimBar.class);
		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);

		toolItem1.setType(ItemType.RADIO);
		toolItem2.setType(ItemType.RADIO);

		window.getTrimBars().add(trimBar);
		trimBar.getChildren().add(toolBar);
		toolBar.getChildren().add(toolItem1);
		toolBar.getChildren().add(toolItem2);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

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
}
