/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Pins down how menu contributions are placed and how their visibility is tracked.
 */
public class MenuContributionTest {

	private static final String SHOW_VARIABLE = "MenuContributionTest.show";
	private static final String SHOW_EXPRESSION = "bundleclass://org.eclipse.e4.ui.tests/"
			+ "org.eclipse.e4.ui.workbench.renderers.swt.MenuContributionTest$ShowExpression";
	private static final String SAMPLE_VIEW = "bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView";

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MTrimmedWindow window;
	private MMenu mainMenu;

	@BeforeEach
	public void setUp() {
		window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);
		mainMenu = ems.createModelElement(MMenu.class);
		mainMenu.setElementId("test.main");
		window.setMainMenu(mainMenu);
	}

	@Test
	public void testMenuBarContributionFollowsActivePart() {
		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		MPart partA = createPart(stack);
		MPart partB = createPart(stack);

		MMenu contributed = ems.createModelElement(MMenu.class);
		contributed.setElementId("test.contributed");
		contributed.setLabel("Contributed");
		contributed.getChildren().add(createItem("test.contributed.item"));
		MMenuContribution contribution = createContribution("test.main", null, contributed);
		contribution.setVisibleWhen(createShowExpression());

		contextRule.createAndRunWorkbench(window);
		MMenuElement merged = find(mainMenu, "test.contributed");
		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partA.getContext().set(SHOW_VARIABLE, Boolean.TRUE);
		contextRule.spinEventLoop();
		assertTrue(merged.isVisible());

		partService.activate(partB);
		contextRule.spinEventLoop();
		assertFalse(merged.isVisible());

		partService.activate(partA);
		contextRule.spinEventLoop();
		assertTrue(merged.isVisible());
	}

	@Test
	public void testTopLevelMenuWithoutVisibleChildrenIsHidden() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("test.file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);
		MMenuItem item = createItem("test.file.item");
		item.setVisible(false);
		fileMenu.getChildren().add(item);

		contextRule.createAndRunWorkbench(window);
		MenuManager fileManager = getRenderer().getManager(fileMenu);
		assertFalse(fileManager.isVisible());

		item.setVisible(true);
		assertTrue(fileManager.isVisible());
	}

	@Test
	public void testContributionsArePlacedByPositionInParent() {
		MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setElementId("test.file");
		fileMenu.setLabel("File");
		mainMenu.getChildren().add(fileMenu);
		fileMenu.getChildren().add(createItem("test.first"));
		MMenuSeparator additions = ems.createModelElement(MMenuSeparator.class);
		additions.setElementId("additions");
		fileMenu.getChildren().add(additions);

		// listed before the contribution it is placed after, so it needs a retry pass
		createContribution("test.file", "after=item.a", createItem("item.b"));
		createContribution("test.file", "after=additions", createItem("item.a"));
		createContribution("test.file", "before=test.first", createItem("item.c"));

		contextRule.createAndRunWorkbench(window);

		assertEquals(List.of("item.c", "test.first", "additions", "item.a", "item.b"), ids(fileMenu.getChildren()));
		assertEquals(List.of("item.c", "test.first", "additions", "item.a", "item.b"),
				ids(getRenderer().getManager(fileMenu).getItems()));
	}

	@Test
	public void testPopupReceivesTargetedAliasedAndAnyPopupContributions() {
		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		MPart part = createPart(stack);
		MPopupMenu popup = ems.createModelElement(MPopupMenu.class);
		popup.setElementId("test.popup");
		popup.getTags().add("popup:test.alias");
		part.getMenus().add(popup);

		createContribution("test.popup", null, createItem("popup.targeted"));
		createContribution("test.alias", null, createItem("popup.aliased"));
		createContribution("popup", null, createItem("popup.any"));
		createContribution("test.unrelated", null, createItem("popup.unrelated"));

		contextRule.createAndRunWorkbench(window);
		Control partControl = (Control) part.getWidget();
		application.getContext().get(IPresentationEngine.class).createGui(popup, partControl, part.getContext());

		assertEquals(List.of("popup.any", "popup.targeted", "popup.aliased"), ids(popup.getChildren()));
	}

	@Test
	public void testViewMenuButtonFollowsContributionVisibilityOnPartSwitch() {
		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		MPart partA = createPart(stack);
		MPart partB = createPart(stack);
		MMenu viewMenu = ems.createModelElement(MMenu.class);
		viewMenu.setElementId("test.viewmenu");
		viewMenu.getTags().add(StackRenderer.TAG_VIEW_MENU);
		partA.getMenus().add(viewMenu);
		MMenuContribution contribution = createContribution("test.viewmenu", null, createItem("test.viewmenu.item"));
		contribution.setVisibleWhen(createShowExpression());

		contextRule.createAndRunWorkbench(window);
		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		contextRule.spinEventLoop();
		ToolBar viewMenuButton = getViewMenuButton(stack);
		assertFalse(viewMenuButton.getVisible());

		partA.getContext().set(SHOW_VARIABLE, Boolean.TRUE);
		assertTrue(find(viewMenu, "test.viewmenu.item").isVisible());
		partService.activate(partB);
		partService.activate(partA);
		contextRule.spinEventLoop();
		assertTrue(viewMenuButton.getVisible());
	}

	public static class ShowExpression {
		@Evaluate
		public boolean evaluate(@Optional @Named(SHOW_VARIABLE) Boolean show) {
			return Boolean.TRUE.equals(show);
		}
	}

	private MPart createPart(MPartStack stack) {
		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI(SAMPLE_VIEW);
		stack.getChildren().add(part);
		return part;
	}

	private MMenuItem createItem(String id) {
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);
		item.setElementId(id);
		item.setLabel(id);
		return item;
	}

	private MMenuContribution createContribution(String parentId, String position, MMenuElement child) {
		MMenuContribution contribution = ems.createModelElement(MMenuContribution.class);
		contribution.setParentId(parentId);
		contribution.setPositionInParent(position);
		contribution.getChildren().add(child);
		application.getMenuContributions().add(contribution);
		return contribution;
	}

	private MImperativeExpression createShowExpression() {
		MImperativeExpression expression = ems.createModelElement(MImperativeExpression.class);
		expression.setTracking(true);
		expression.setContributionURI(SHOW_EXPRESSION);
		return expression;
	}

	private MenuManagerRenderer getRenderer() {
		return (MenuManagerRenderer) mainMenu.getRenderer();
	}

	private ToolBar getViewMenuButton(MPartStack stack) {
		Composite topRight = (Composite) ((CTabFolder) stack.getWidget()).getTopRight();
		for (Control child : topRight.getChildren()) {
			if (StackRenderer.TAG_VIEW_MENU.equals(child.getData())) {
				return (ToolBar) child;
			}
		}
		return fail("view menu button not found");
	}

	private static MMenuElement find(MMenu menu, String id) {
		for (MMenuElement element : menu.getChildren()) {
			if (id.equals(element.getElementId())) {
				return element;
			}
		}
		return fail(id + " not merged into " + menu.getElementId());
	}

	private static List<String> ids(List<? extends MMenuElement> elements) {
		List<String> ids = new ArrayList<>();
		for (MMenuElement element : elements) {
			ids.add(element.getElementId());
		}
		return ids;
	}

	private static List<String> ids(IContributionItem[] items) {
		List<String> ids = new ArrayList<>();
		for (IContributionItem item : items) {
			ids.add(item.getId());
		}
		return ids;
	}
}
