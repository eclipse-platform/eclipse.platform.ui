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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
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
	public void testSubmenusAreEvaluatedWithoutPluginActivation() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		menu.getChildren().add(fileMenu);
		RecordingExpression fileItemExpression = new RecordingExpression();
		fileMenu.getChildren().add(createItem("item", fileItemExpression));
		MMenu subMenu = ems.createModelElement(MMenu.class);
		subMenu.setLabel("Sub");
		fileMenu.getChildren().add(subMenu);
		RecordingExpression subItemExpression = new RecordingExpression();
		subMenu.getChildren().add(createItem("subItem", subItemExpression));

		contextRule.createAndRunWorkbench(window);
		fileItemExpression.activations.clear();
		subItemExpression.activations.clear();
		MenuManagerRendererFilter.updateElementVisibility(fileMenu, getMenuManagerRenderer(),
				getMenuManagerRenderer().getManager(fileMenu), window.getContext(), 1, false);

		assertEquals(List.of(Boolean.TRUE), fileItemExpression.activations);
		assertEquals(List.of(Boolean.FALSE), subItemExpression.activations);
	}

	@Test
	public void testViewMenuIsEvaluatedWithPluginActivationOnlyWhenShown() {
		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(part);
		MMenu viewMenu = ems.createModelElement(MMenu.class);
		viewMenu.setElementId("test.viewmenu");
		viewMenu.getTags().add(StackRenderer.TAG_VIEW_MENU);
		part.getMenus().add(viewMenu);
		RecordingExpression expression = new RecordingExpression();
		MMenuContribution contribution = ems.createModelElement(MMenuContribution.class);
		contribution.setParentId("test.viewmenu");
		contribution.getChildren().add(createItem("item", null));
		contribution.setVisibleWhen(createCoreExpression(expression));
		application.getMenuContributions().add(contribution);

		contextRule.createAndRunWorkbench(window);
		window.getContext().get(EPartService.class).activate(part);
		contextRule.spinEventLoop();
		assertFalse(expression.activations.isEmpty());
		assertFalse(expression.activations.contains(Boolean.TRUE));

		expression.activations.clear();
		Menu swtMenu = getMenuManagerRenderer().getManager(viewMenu).getMenu();
		swtMenu.notifyListeners(SWT.Show, new Event());
		assertTrue(expression.activations.contains(Boolean.TRUE));
	}

	private static class RecordingExpression extends Expression {
		final List<Boolean> activations = new ArrayList<>();

		@Override
		public EvaluationResult evaluate(IEvaluationContext context) {
			activations.add(Boolean.valueOf(context.getAllowPluginActivation()));
			return EvaluationResult.TRUE;
		}
	}

	private MMenuItem createItem(String label, Expression visibleWhen) {
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);
		item.setLabel(label);
		if (visibleWhen != null) {
			item.setVisibleWhen(createCoreExpression(visibleWhen));
		}
		return item;
	}

	private MCoreExpression createCoreExpression(Expression expression) {
		MCoreExpression coreExpression = ems.createModelElement(MCoreExpression.class);
		coreExpression.setCoreExpression(expression);
		return coreExpression;
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
