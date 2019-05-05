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
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632, 378495
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MToolItemTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MToolBar toolBar;
	private MTrimmedWindow window;

	@Before
	public void setUp() throws Exception {
		window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);

		MTrimBar trimBar = ems.createModelElement(MTrimBar.class);
		window.getTrimBars().add(trimBar);

		toolBar = ems.createModelElement(MToolBar.class);
		trimBar.getChildren().add(toolBar);
	}

	private void testMToolItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MToolItem toolItem = ems.createModelElement(MDirectToolItem.class);

		toolItem.setLabel(before);

		toolBar.getChildren().add(toolItem);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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
		MToolItem toolItem = ems.createModelElement(MDirectToolItem.class);

		toolItem.setTooltip(before);

		toolBar.getChildren().add(toolItem);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);

		toolItem1.setType(ItemType.RADIO);
		toolItem2.setType(ItemType.RADIO);

		toolBar.getChildren().add(toolItem1);
		toolBar.getChildren().add(toolItem2);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

	@Test
	public void testDynamicItem_AddOne() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, tbm.getSize());

		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		assertEquals(1, tbm.getSize());
	}

	@Test
	public void testDynamicItem_AddOneBefore() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(tbm.getSize(), 1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setElementId("Item2");
		toolBar.getChildren().add(0, toolItem2);

		assertEquals(2, tbm.getSize());
		assertEquals("Item2", tbm.getItems()[0].getId());
	}

	@Test
	public void testDynamicItem_AddMany() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, tbm.getSize());

		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);

		List<MToolItem> itemList = Arrays.asList(toolItem1, toolItem2);
		toolBar.getChildren().addAll(itemList);

		assertEquals(2, tbm.getSize());
	}

	@Test
	public void testDynamicItem_RemoveOne() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setElementId("Item2");
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(2, tbm.getSize());
		assertNotNull(toolItem1.getWidget());

		toolBar.getChildren().remove(0);

		assertEquals(1, tbm.getSize(), 1);
		assertEquals("Item2", tbm.getItems()[0].getId());

		// Ensure that the removed item is disposed
		assertNull(toolItem1.getWidget());
	}

	@Test
	public void testDynamicItem_RemoveMany() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setElementId("Item2");
		toolBar.getChildren().add(toolItem2);

		MToolItem toolItem3 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem3);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(3, tbm.getSize());

		List<MToolItem> itemList = Arrays.asList(toolItem1, toolItem3);
		toolBar.getChildren().removeAll(itemList);

		assertEquals(1, tbm.getSize());
		assertEquals("Item2", tbm.getItems()[0].getId());
	}

	@Test
	public void testDynamicItem_RemoveAll() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(2, tbm.getSize());

		toolBar.getChildren().clear();

		assertEquals(0, tbm.getSize());
	}

	@Test
	public void testDynamicItem_Move() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolItem1.setElementId("Item1");
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setElementId("Item2");
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(2, tbm.getSize(), 2);
		assertEquals("Item1", tbm.getItems()[0].getId());
		assertEquals("Item2", tbm.getItems()[1].getId());

		ECollections.move(toolBar.getChildren(), 0, 1);

		assertEquals(2, tbm.getSize(), 2);
		assertEquals("Item2", tbm.getItems()[0].getId());
		assertEquals("Item1", tbm.getItems()[1].getId());
	}

	private ToolBarManager getToolBarManager() {
		Object renderer = toolBar.getRenderer();
		assertEquals(ToolBarManagerRenderer.class, renderer.getClass());
		return ((ToolBarManagerRenderer) renderer).getManager(toolBar);
	}

}
