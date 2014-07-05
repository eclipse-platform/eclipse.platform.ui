/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 433228
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EModelServiceFindTest {

	private IEclipseContext applicationContext;

	MApplication app = null;


	@Before
	public void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();

	}

	@After
	public void tearDown() throws Exception {
		applicationContext.dispose();
	}

	private MApplication createApplication() {
		EModelService modelService = applicationContext.get(EModelService.class);
		MApplication app = modelService.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		MWindow window = modelService.createModelElement(MWindow.class);
		window.setElementId("singleValidId");
		app.getChildren().add(window);

		MMenu mainMenu = modelService.createModelElement(MMenu.class);
		window.setMainMenu(mainMenu);

		MMenu mainMenuItem = modelService.createModelElement(MMenu.class);
		mainMenu.getChildren().add(mainMenuItem);

		MPartSashContainer psc = modelService.createModelElement(MPartSashContainer.class);
		psc.setElementId("twoValidIds");
		psc.getTags().add("oneValidTag");
		window.getChildren().add(psc);

		MPartStack stack = modelService.createModelElement(MPartStack.class);
		stack.getTags().add("twoValidTags");
		psc.getChildren().add(stack);

		MPart part1 = modelService.createModelElement(MPart.class);
		part1.setElementId("twoValidIds");
		stack.getChildren().add(part1);

		MPart part2 = modelService.createModelElement(MPart.class);
		part2.getTags().add("twoValidTags");
		part2.getTags().add("secondTag");
		stack.getChildren().add(part2);

		MPart part3 = modelService.createModelElement(MPart.class);
		psc.getChildren().add(part3);

		MMenu menu = modelService.createModelElement(MMenu.class);
		menu.setElementId("menuId");
		part1.getMenus().add(menu);

		MMenu menuItem1 = modelService.createModelElement(MMenu.class);
		menuItem1.setElementId("menuItem1Id");
		menu.getChildren().add(menuItem1);

		MMenu menuItem2 = modelService.createModelElement(MMenu.class);
		menuItem2.setElementId("menuItem2Id");
		menu.getChildren().add(menuItem2);

		MToolBar toolBar = modelService.createModelElement(MToolBar.class);
		toolBar.setElementId("toolBarId");
		part2.setToolbar(toolBar);

		MToolControl toolControl1 = modelService.createModelElement(MToolControl.class);
		toolControl1.setElementId("toolControl1Id");
		toolBar.getChildren().add(toolControl1);

		MToolControl toolControl2 = modelService.createModelElement(MToolControl.class);
		toolControl2.setElementId("toolControl2Id");
		toolBar.getChildren().add(toolControl2);

		return app;
	}

	@Test
	public void testFindElementsIdOnly() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		List<MUIElement> elements1 = modelService.findElements(application, "singleValidId", null, null);
		assertEquals(elements1.size(), 1);

		List<MUIElement> elements2 = modelService.findElements(application, "twoValidIds", null, null);
		assertEquals(elements2.size(), 2);

		List<MUIElement> elements3 = modelService.findElements(application, "invalidId", null, null);
		assertEquals(elements3.size(), 0);

		List<MUIElement> elements4 = modelService.findElements(application, "menuItem1Id", null, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements4.size());

		List<MUIElement> elements5 = modelService.findElements(application, "toolControl1Id", null, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(1, elements5.size());
	}

	@Test
	public void testFindElementsTypeOnly() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		List<MPart> parts = modelService.findElements(application, null, MPart.class, null);
		assertEquals(parts.size(), 3);

		List<MPartStack> stacks = modelService.findElements(application, null, MPartStack.class, null);
		assertEquals(stacks.size(), 1);

		List<MDirtyable> dirtyableElements = modelService.findElements(application, null, MDirtyable.class, null);
		assertEquals(dirtyableElements.size(), 3);

		List<MMenuElement> menuElements = modelService.findElements(application, null, MMenuElement.class, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(5, menuElements.size());

		List<MToolBarElement> toolBarElements = modelService.findElements(application, null, MToolBarElement.class,
				null, EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(2, toolBarElements.size());

		// Should find all the elements
		List<MUIElement> uiElements = modelService.findElements(application, null, null, null,
				EModelService.ANYWHERE | EModelService.IN_MAIN_MENU | EModelService.IN_PART);
		assertEquals(15, uiElements.size());

		// Should match 0 since String is not an MUIElement
		List<String> strings = modelService.findElements(application, null, String.class, null);
		assertEquals(strings.size(), 0);
	}

	@Test
	public void testFindElementsTagsOnly() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MUIElement> oneTags = modelService.findElements(application, null, null, tags);
		assertEquals(oneTags.size(), 1);

		tags.clear();
		tags.add("twoValidTags");
		List<MUIElement> twoTags = modelService.findElements(application, null, null, tags);
		assertEquals(twoTags.size(), 2);

		tags.clear();
		tags.add("invalidTag");
		List<MUIElement> invalidTags = modelService.findElements(application, null, null, tags);
		assertEquals(invalidTags.size(), 0);

		tags.clear();
		tags.add("twoValidTags");
		tags.add("secondTag");
		List<MUIElement> combinedTags = modelService.findElements(application, null, null, tags);
		assertEquals(combinedTags.size(), 1);

		tags.clear();
		tags.add("oneValidTag");
		tags.add("secondTag");
		List<MUIElement> unmatchedTags = modelService.findElements(application, null, null, tags);
		assertEquals(unmatchedTags.size(), 0);
	}

	@Test
	public void testFindElementsCombinations() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MPartSashContainer> idAndType = modelService.findElements(application, "twoValidIds",
				MPartSashContainer.class, tags);
		assertEquals(idAndType.size(), 1);

		List<MPartSashContainer> typeAndTag = modelService.findElements(application, null, MPartSashContainer.class,
				tags);
		assertEquals(typeAndTag.size(), 1);

		List<MUIElement> idAndTag = modelService.findElements(application, "twoValidIds", null, tags);
		assertEquals(idAndTag.size(), 1);

		List<MPartSashContainer> idAndTypeAndTags = modelService.findElements(application, "twoValidIds",
				MPartSashContainer.class, null);
		assertEquals(idAndTypeAndTags.size(), 1);

		List<MPartSashContainer> badIdAndTypeAndTags = modelService.findElements(application, "invalidId",
				MPartSashContainer.class, null);
		assertEquals(badIdAndTypeAndTags.size(), 0);
	}

	@Test
	public void testFindElements_NullCheck() {
		MApplication application = createApplication();
		EModelService modelService = application.getContext().get(EModelService.class);
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
			modelService.findElements(null, null, null, null, EModelService.ANYWHERE);
			fail("An exception should have prevented a null parameter to findElements(*)");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testFlags() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		List<MToolBarElement> toolBarElements = modelService.findElements(application, null, MToolBarElement.class,
				null, EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, toolBarElements.size());

		toolBarElements = modelService.findElements(application, null, MToolBarElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART);
		assertEquals(2, toolBarElements.size());

		List<MMenuElement> menuElements = modelService.findElements(application, null, MMenuElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE);
		assertEquals(0, menuElements.size());

		menuElements = modelService.findElements(application, null, MMenuElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_PART);
		assertEquals(3, menuElements.size());

		menuElements = modelService.findElements(application, null, MMenuElement.class, null,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_MAIN_MENU);
		assertEquals(2, menuElements.size());
	}

	private MHandler findHandler(EModelService ms, MApplicationElement searchRoot, final String id) {
		if (searchRoot == null || id == null)
			return null;

		List<MHandler> handlers = ms.findElements(searchRoot, MHandler.class, EModelService.ANYWHERE, new Selector() {
			@Override
			public boolean select(MApplicationElement element) {
				return element instanceof MHandler && id.equals(element.getElementId());
			}
		});
		if (handlers.size() > 0) {
			return handlers.get(0);
		}
		return null;
	}

	@Test
	public void testFindHandler() {
		MApplication application = createApplication();

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		MHandler handler1 = modelService.createModelElement(MHandler.class);
		handler1.setElementId("handler1");
		application.getHandlers().add(handler1);

		MHandler handler2 = modelService.createModelElement(MHandler.class);
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

	@Test
	public void testFindMKeyBindings() {
		MApplication application = createApplication();
		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		MBindingTable bindingTable = modelService.createModelElement(MBindingTable.class);
		MKeyBinding keyBinding = modelService.createModelElement(MKeyBinding.class);
		bindingTable.getBindings().add(keyBinding);

		application.getBindingTables().add(bindingTable);

		List<MKeyBinding> elements = modelService.findElements(application, MKeyBinding.class, EModelService.ANYWHERE,
				element -> (element instanceof MKeyBinding));

		assertEquals(1, elements.size());
		assertEquals(keyBinding, elements.get(0));
	}

	@Test
	public void testFindAddons() {
		MApplication application = createApplication();
		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		MAddon addon = MApplicationFactory.INSTANCE.createAddon();

		application.getAddons().add(addon);

		List<MAddon> elements = modelService.findElements(application,
				MAddon.class, EModelService.ANYWHERE, new Selector() {
					@Override
					public boolean select(MApplicationElement element) {
						return (element instanceof MAddon);
					}
				});

		assertEquals(1, elements.size());
		assertEquals(addon, elements.get(0));
	}

	@Test
	public void testBug314685() {
		MApplication application = createApplication();
		application.setContext(applicationContext);

		EModelService modelService = application.getContext().get(EModelService.class);
		assertNotNull(modelService);

		MWindow window = modelService.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPerspectiveStack perspectiveStack = modelService.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspectiveA = modelService.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);

		MPerspective perspectiveB = modelService.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStack = modelService.createModelElement(MPartStack.class);
		window.getSharedElements().add(partStack);

		MPart part = modelService.createModelElement(MPart.class);
		partStack.getChildren().add(part);

		MPlaceholder placeholderA = modelService.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partStack);
		perspectiveA.getChildren().add(placeholderA);

		MPlaceholder placeholderB = modelService.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);



		List<MPart> elements = modelService.findElements(window, null, MPart.class, null);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertEquals(part, elements.get(0));
	}
}
