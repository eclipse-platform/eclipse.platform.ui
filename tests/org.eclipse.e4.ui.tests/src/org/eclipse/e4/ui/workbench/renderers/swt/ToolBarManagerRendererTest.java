/*******************************************************************************
 * Copyright (c) 2019, 2025 Rolf Theunissen and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWTException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

public class ToolBarManagerRendererTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker eventBroker;

	private String toolBarId;
	private MToolBar toolBar;
	private MTrimmedWindow window;

	@Before
	public void setUp() throws Exception {
		window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);

		MTrimBar trimBar = ems.createModelElement(MTrimBar.class);
		window.getTrimBars().add(trimBar);

		toolBarId = "ToolBarManagerRendererTest.toolBar";
		toolBar = ems.createModelElement(MToolBar.class);
		toolBar.setElementId(toolBarId);
		trimBar.getChildren().add(toolBar);
	}

	@Test
	public void testMToolItem_isVisible() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setVisible(false);
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(2, tbm.getSize());
		assertTrue(tbm.getItems()[0].isVisible());
		assertFalse(tbm.getItems()[1].isVisible());

		toolItem1.setVisible(false);

		assertEquals(2, tbm.getSize());
		assertFalse(tbm.getItems()[0].isVisible());
		assertFalse(tbm.getItems()[1].isVisible());

		toolItem1.setVisible(true);

		assertEquals(2, tbm.getSize());
		assertTrue(tbm.getItems()[0].isVisible());
		assertFalse(tbm.getItems()[1].isVisible());
	}

	@Test
	public void testMToolItem_toBeRendered() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setToBeRendered(false);
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManager tbm = getToolBarManager();

		assertEquals(1, tbm.getSize());
		assertTrue(tbm.getItems()[0].isVisible());

		toolItem1.setToBeRendered(false);

		assertEquals(0, tbm.getSize());

		toolItem1.setToBeRendered(true);

		assertEquals(1, tbm.getSize());
		assertTrue(tbm.getItems()[0].isVisible());
	}

	@Test
	public void testMToolBarContribution_toBeRendered() {
		List<String> errors = new ArrayList<>();

		EventHandler eventHandler = event -> {
			if (UIEvents.isADD(event)) {
				MToolBar toolbar = (MToolBar) event.getProperty(UIEvents.EventTags.ELEMENT);
				toolbar.setToBeRendered(false);
			}
		};

		ILogListener logListener = (status, plugin) -> {
			if (status.getException() instanceof SWTException) {
				errors.add(plugin + ":" + status);
			}
		};

		try {
			Platform.addLogListener(logListener);
			eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, eventHandler);

			MToolBarContribution toolContribution = ems.createModelElement(MToolBarContribution.class);
			toolContribution.setParentId(toolBarId);
			toolContribution.getChildren().add(ems.createModelElement(MDirectToolItem.class));
			application.getToolBarContributions().add(toolContribution);

			contextRule.createAndRunWorkbench(window);

			assertNull(toolBar.getRenderer());
			assertTrue("Error(s) occurred while rendering toolbar: " + errors, errors.isEmpty());
		} finally {
			eventBroker.unsubscribe(eventHandler);
			Platform.removeLogListener(logListener);
		}
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

		assertEquals(1, tbm.getSize());

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

		assertEquals(1, tbm.getSize());
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

		assertEquals(2, tbm.getSize());
		assertEquals("Item1", tbm.getItems()[0].getId());
		assertEquals("Item2", tbm.getItems()[1].getId());

		ECollections.move(toolBar.getChildren(), 0, 1);

		assertEquals(2, tbm.getSize());
		assertEquals("Item2", tbm.getItems()[0].getId());
		assertEquals("Item1", tbm.getItems()[1].getId());
	}

	@Test
	public void testDynamicItem_Reconcile_AddOne() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, toolBar.getChildren().size());
		assertEquals(0, tbm.getSize());

		TestActionContributionItem item1 = new TestActionContributionItem();
		item1.setId("Item1");
		tbm.add(item1);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals("Item1", toolBar.getChildren().get(0).getElementId());
		assertEquals(1, tbm.getSize());
		assertEquals(item1, tbm.getItems()[0]);
	}

	@Test
	public void testDynamicItem_Reconcile_RemoveOne() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		TestActionContributionItem item1 = new TestActionContributionItem();
		tbm.add(item1);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(1, tbm.getSize());
		assertFalse(item1.disposed);

		tbm.remove(item1);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(0, toolBar.getChildren().size());
		assertEquals(0, tbm.getSize());

		assertFalse(item1.disposed);
		item1.dispose();
		assertTrue(item1.disposed);
	}

	@Test
	public void testDynamicItem_Reconcile_RemoveOneByID() {
		MToolItem toolItem1 = ems.createModelElement(MDirectToolItem.class);
		toolItem1.setElementId("Item1");
		toolBar.getChildren().add(toolItem1);

		MToolItem toolItem2 = ems.createModelElement(MDirectToolItem.class);
		toolItem2.setElementId("Item2");
		toolBar.getChildren().add(toolItem2);

		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		assertEquals(2, toolBar.getChildren().size());
		assertEquals(2, tbm.getSize());

		tbm.remove("Item1");
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(1, tbm.getSize());
		assertEquals("Item2", tbm.getItems()[0].getId());
	}

	@Test
	public void testDynamicItem_Reconcile_Move() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, toolBar.getChildren().size());
		assertEquals(0, tbm.getSize());

		TestActionContributionItem item1 = new TestActionContributionItem();
		item1.setId("Item1");
		tbm.add(item1);
		TestActionContributionItem item2 = new TestActionContributionItem();
		item2.setId("Item2");
		tbm.add(item2);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
		assertEquals("Item1", toolBar.getChildren().get(0).getElementId());
		assertEquals("Item2", toolBar.getChildren().get(1).getElementId());

		assertEquals(2, tbm.getSize());
		assertEquals(item1, tbm.getItems()[0]);
		assertEquals(item2, tbm.getItems()[1]);
		assertFalse(item1.disposed);
		assertFalse(item2.disposed);

		tbm.remove(item1);
		tbm.remove(item2);
		tbm.add(item2);
		tbm.add(item1);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
		assertEquals("Item2", toolBar.getChildren().get(0).getElementId());
		assertEquals("Item1", toolBar.getChildren().get(1).getElementId());

		assertEquals(2, tbm.getSize());
		assertEquals(item2, tbm.getItems()[0]);
		assertEquals(item1, tbm.getItems()[1]);
		assertFalse(item1.disposed);
		assertFalse(item2.disposed);
	}

	@Test
	public void testDynamicItem_Reconcile_Visibility() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, toolBar.getChildren().size());
		assertEquals(0, tbm.getSize());

		TestActionContributionItem item1 = new TestActionContributionItem();
		tbm.add(item1);
		TestActionContributionItem item2 = new TestActionContributionItem();
		tbm.add(item2);
		item2.setVisible(false);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
		assertTrue(toolBar.getChildren().get(0).isVisible());
		assertFalse(toolBar.getChildren().get(1).isVisible());
		assertTrue(item1.isVisible());
		assertFalse(item2.isVisible());

		item1.setVisible(false);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
		assertFalse(toolBar.getChildren().get(0).isVisible());
		assertFalse(toolBar.getChildren().get(1).isVisible());
		assertFalse(item1.isVisible());
		assertFalse(item2.isVisible());

		item1.setVisible(true);
		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
		assertTrue(toolBar.getChildren().get(0).isVisible());
		assertFalse(toolBar.getChildren().get(1).isVisible());
		assertTrue(item1.isVisible());
		assertFalse(item2.isVisible());
	}

	/*
	 * Bug 562645 - Ensure that Object Identity is used instead of equals
	 */
	@Test
	public void testDynamicItem_Reconcile_Action_Multiple() {
		contextRule.createAndRunWorkbench(window);
		ToolBarManagerRenderer renderer = getToolBarManagerRenderer();
		ToolBarManager tbm = getToolBarManager();

		assertEquals(0, toolBar.getChildren().size());
		assertEquals(0, tbm.getSize());

		Action action = new Action("Dummy") {
		};

		tbm.add(action);
		tbm.add(action);

		assertEquals(2, tbm.getSize());

		renderer.reconcileManagerToModel(tbm, toolBar);

		assertEquals(2, toolBar.getChildren().size());
	}

	private ToolBarManagerRenderer getToolBarManagerRenderer() {
		Object renderer = toolBar.getRenderer();
		assertEquals(ToolBarManagerRenderer.class, renderer.getClass());
		return (ToolBarManagerRenderer) renderer;
	}

	private ToolBarManager getToolBarManager() {
		return (getToolBarManagerRenderer()).getManager(toolBar);
	}


	static private class TestActionContributionItem extends ActionContributionItem {
		private boolean disposed = false;

		public TestActionContributionItem() {
			super(new Action("Dummy") {
			});
		}

		@Override
		public void dispose() {
			disposed = true;
			super.dispose();
		}

	}

}
