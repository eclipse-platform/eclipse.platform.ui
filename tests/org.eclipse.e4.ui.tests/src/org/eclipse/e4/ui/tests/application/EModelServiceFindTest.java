/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class EModelServiceFindTest extends TestCase {

	private IEclipseContext applicationContext;

	MApplication app = null;

	@Override
	protected void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();
	}

	private MApplication createApplication() {
		MApplication app = ApplicationFactoryImpl.eINSTANCE.createApplication();
		app.setContext(applicationContext);
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		window.setElementId("singleValidId");
		app.getChildren().add(window);

		MMenu mainMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(mainMenu);

		MMenu mainMenuItem = MenuFactoryImpl.eINSTANCE.createMenu();
		mainMenu.getChildren().add(mainMenuItem);

		MPartSashContainer psc = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		psc.setElementId("twoValidIds");
		psc.getTags().add("oneValidTag");
		window.getChildren().add(psc);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.getTags().add("twoValidTags");
		psc.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		part1.setElementId("twoValidIds");
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		part2.getTags().add("twoValidTags");
		part2.getTags().add("secondTag");
		stack.getChildren().add(part2);

		MPart part3 = BasicFactoryImpl.eINSTANCE.createPart();
		psc.getChildren().add(part3);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		menu.setElementId("menuId");
		part1.getMenus().add(menu);

		MMenu menuItem1 = MenuFactoryImpl.eINSTANCE.createMenu();
		menuItem1.setElementId("menuItem1Id");
		menu.getChildren().add(menuItem1);

		MMenu menuItem2 = MenuFactoryImpl.eINSTANCE.createMenu();
		menuItem2.setElementId("menuItem2Id");
		menu.getChildren().add(menuItem2);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		toolBar.setElementId("toolBarId");
		part2.setToolbar(toolBar);

		MToolControl toolControl1 = MenuFactoryImpl.eINSTANCE
				.createToolControl();
		toolControl1.setElementId("toolControl1Id");
		toolBar.getChildren().add(toolControl1);

		MToolControl toolControl2 = MenuFactoryImpl.eINSTANCE
				.createToolControl();
		toolControl2.setElementId("toolControl2Id");
		toolBar.getChildren().add(toolControl2);

		return app;
	}

	public void testFindElementsIdOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MUIElement> elements1 = modelService.findElements(application,
				"singleValidId", null, null);
		assertEquals(elements1.size(), 1);

		List<MUIElement> elements2 = modelService.findElements(application,
				"twoValidIds", null, null);
		assertEquals(elements2.size(), 2);

		List<MUIElement> elements3 = modelService.findElements(application,
				"invalidId", null, null);
		assertEquals(elements3.size(), 0);

		List<MUIElement> elements4 = modelService.findElements(application,
				"menuItem1Id", null, null, EModelService.ANYWHERE
						| EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements4.size());

		List<MUIElement> elements5 = modelService.findElements(application,
				"toolControl1Id", null, null, EModelService.ANYWHERE
						| EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements5.size());
	}

	public void testFindElementsTypeOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MPart> parts = modelService.findElements(application, null,
				MPart.class, null);
		assertEquals(parts.size(), 3);

		List<MPartStack> stacks = modelService.findElements(application, null,
				MPartStack.class, null);
		assertEquals(stacks.size(), 1);

		List<MDirtyable> dirtyableElements = modelService.findElements(
				application, null, MDirtyable.class, null);
		assertEquals(dirtyableElements.size(), 3);

		List<MMenuElement> menuElements = modelService.findElements(
				application, null, MMenuElement.class, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART);
		assertEquals(5, menuElements.size());

		List<MToolBarElement> toolBarElements = modelService.findElements(
				application, null, MToolBarElement.class, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU
						| EModelService.IN_PART);
		assertEquals(2, toolBarElements.size());

		// Should find all the elements
		List<MUIElement> uiElements = modelService.findElements(application,
				null, null, null, EModelService.ANYWHERE
						| EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(15, uiElements.size());

		// Should match 0 since String is not an MUIElement
		List<String> strings = modelService.findElements(application, null,
				String.class, null);
		assertEquals(strings.size(), 0);
	}

	public void testFindElementsTagsOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MUIElement> oneTags = modelService.findElements(application, null,
				null, tags);
		assertEquals(oneTags.size(), 1);

		tags.clear();
		tags.add("twoValidTags");
		List<MUIElement> twoTags = modelService.findElements(application, null,
				null, tags);
		assertEquals(twoTags.size(), 2);

		tags.clear();
		tags.add("invalidTag");
		List<MUIElement> invalidTags = modelService.findElements(application,
				null, null, tags);
		assertEquals(invalidTags.size(), 0);

		tags.clear();
		tags.add("twoValidTags");
		tags.add("secondTag");
		List<MUIElement> combinedTags = modelService.findElements(application,
				null, null, tags);
		assertEquals(combinedTags.size(), 1);

		tags.clear();
		tags.add("oneValidTag");
		tags.add("secondTag");
		List<MUIElement> unmatchedTags = modelService.findElements(application,
				null, null, tags);
		assertEquals(unmatchedTags.size(), 0);
	}

	public void testFindElementsCombinations() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MPartSashContainer> idAndType = modelService.findElements(
				application, "twoValidIds", MPartSashContainer.class, tags);
		assertEquals(idAndType.size(), 1);

		List<MPartSashContainer> typeAndTag = modelService.findElements(
				application, null, MPartSashContainer.class, tags);
		assertEquals(typeAndTag.size(), 1);

		List<MUIElement> idAndTag = modelService.findElements(application,
				"twoValidIds", null, tags);
		assertEquals(idAndTag.size(), 1);

		List<MPartSashContainer> idAndTypeAndTags = modelService.findElements(
				application, "twoValidIds", MPartSashContainer.class, null);
		assertEquals(idAndTypeAndTags.size(), 1);

		List<MPartSashContainer> badIdAndTypeAndTags = modelService
				.findElements(application, "invalidId",
						MPartSashContainer.class, null);
		assertEquals(badIdAndTypeAndTags.size(), 0);
	}

	public void testFindElements_NullCheck() {
		MApplication application = createApplication();
		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		try {
			modelService.find("a", null);
			fail("An exception should have prevented a null parameter to find(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			modelService.findElements(null, null, null, null);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			modelService.findElements(null, null, null, null,
					EModelService.ANYWHERE);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testFlags() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MToolBarElement> toolBarElements = modelService.findElements(
				application, null, MToolBarElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, toolBarElements.size());

		toolBarElements = modelService.findElements(application, null,
				MToolBarElement.class, null, EModelService.IN_ANY_PERSPECTIVE
						| EModelService.IN_PART);
		assertEquals(2, toolBarElements.size());

		List<MMenuElement> menuElements = modelService.findElements(
				application, null, MMenuElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, menuElements.size());

		menuElements = modelService.findElements(application, null,
				MMenuElement.class, null, EModelService.IN_ANY_PERSPECTIVE
						| EModelService.IN_PART);
		assertEquals(3, menuElements.size());

		menuElements = modelService.findElements(application, null,
				MMenuElement.class, null, EModelService.IN_ANY_PERSPECTIVE
						| EModelService.IN_MAIN_MENU);
		assertEquals(2, menuElements.size());
	}

	private MHandler findHandler(EModelService ms,
			MApplicationElement searchRoot, final String id) {
		if (searchRoot == null || id == null)
			return null;

		List<MHandler> handlers = ms.findElements(searchRoot, MHandler.class,
				EModelService.ANYWHERE, new Selector() {
					@Override
					public boolean select(MApplicationElement element) {
						return element instanceof MHandler
								&& id.equals(element.getElementId());
					}
				});
		if (handlers.size() > 0) {
			return handlers.get(0);
		}
		return null;
	}

	public void testFindHandler() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		MHandler handler1 = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler1.setElementId("handler1");
		application.getHandlers().add(handler1);

		MHandler handler2 = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler2.setElementId("handler2");
		application.getHandlers().add(handler2);

		MHandler foundHandler = null;

		foundHandler = findHandler(modelService, application, "handler1");
		assertNotNull(foundHandler);
		assertSame(handler1, foundHandler);

		foundHandler = findHandler(modelService, application, "invalidId");
		assertNull(foundHandler);

		foundHandler = findHandler(modelService, null, "handler1");
		assertNull(foundHandler);

		foundHandler = findHandler(modelService, application, "");
		assertNull(foundHandler);

		foundHandler = findHandler(modelService, application, null);
		assertNull(foundHandler);
	}

	public void testBug314685() {
		MApplication application = createApplication();
		application.setContext(applicationContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(partStack);
		perspectiveA.getChildren().add(placeholderA);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MPart> elements = modelService.findElements(window, null,
				MPart.class, null);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertEquals(part, elements.get(0));
	}
}
