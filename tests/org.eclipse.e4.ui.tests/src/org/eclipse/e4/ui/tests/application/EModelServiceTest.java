/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.Test;

public class EModelServiceTest extends UITest {

	@Test
	public void testGetPerspectiveFor_RegularElement() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(
				EModelService.class);
		MPerspective foundPerspective = modelService
				.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testGetPerspectiveFor_SharedElement() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		placeholder.setRef(partStack);
		partStack.setCurSharedRef(placeholder);

		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(
				EModelService.class);
		MPerspective foundPerspective = modelService
				.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testGetPerspectiveFor_SharedElement2() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		placeholder.setRef(partSashContainer);
		partSashContainer.setCurSharedRef(placeholder);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partSashContainer.getChildren().add(partStack);
		partSashContainer.setSelectedElement(partStack);

		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(
				EModelService.class);
		MPerspective foundPerspective = modelService
				.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testBringToTop01() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		assertEquals(windowA, application.getSelectedElement());

		EModelService modelService = applicationContext
				.get(EModelService.class);
		modelService.bringToTop(windowA);
		assertEquals(windowA, application.getSelectedElement());

		modelService.bringToTop(windowB);
		assertEquals(windowB, application.getSelectedElement());
	}

	@Test
	public void testBringToTop02() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		MPart partB = ems.createModelElement(MPart.class);
		windowB.getChildren().add(partB);
		windowB.setSelectedElement(partB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		assertEquals(windowB, application.getSelectedElement());

		EModelService modelService = applicationContext
				.get(EModelService.class);
		modelService.bringToTop(windowA);
		assertEquals(windowA, application.getSelectedElement());

		modelService.bringToTop(partB);
		assertEquals(windowA, application.getSelectedElement());
	}

	@Test
	public void testBringToTop_Bug334411() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		detachedWindow.setToBeRendered(false);
		window.getWindows().add(detachedWindow);

		MPart part = ems.createModelElement(MPart.class);
		part.setToBeRendered(false);
		detachedWindow.getChildren().add(part);

		getEngine().createGui(window);

		assertEquals(window, application.getSelectedElement());

		EModelService modelService = applicationContext
				.get(EModelService.class);
		modelService.bringToTop(part);
		assertTrue(part.isToBeRendered());
		assertTrue(detachedWindow.isToBeRendered());
	}

	@Test
	public void testGetElementLocation_Bug331062_01() {
		MPerspective perspective = ems.createModelElement(MPerspective.class);
		MPart part = ems.createModelElement(MPart.class);
		perspective.getChildren().add(part);

		EModelService modelService = applicationContext
				.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI,
				modelService.getElementLocation(part));
	}

	@Test
	public void testGetElementLocation_Bug331062_02() {
		MPerspective perspective = ems.createModelElement(MPerspective.class);
		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MWindow innerWindow = ems.createModelElement(MWindow.class);
		detachedWindow.getWindows().add(innerWindow);

		EModelService modelService = applicationContext
				.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI,
				modelService.getElementLocation(innerWindow));
	}

	@Test
	public void testMoveWithoutIndexNoOtherElements() {
		MWindow source = ems.createModelElement(MWindow.class);

		// The following casts are necessary because BR 465292 and can be
		// removed, once it is fixed
		MWindow window = ems.createModelElement(MWindow.class);
		MElementContainer<? extends MUIElement> erase1 = window;
		MElementContainer<MUIElement> target = (MElementContainer<MUIElement>) erase1;

		MPart part = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		MUIElement uiElement = part;
		EModelService modelService = applicationContext.get(EModelService.class);
		modelService.move(uiElement, target);
		assertEquals(part, target.getChildren().get(0));
	}

	@Test
	public void testMoveWithoutIndexWithOneOtherElements() {
		MWindow source = ems.createModelElement(MWindow.class);

		// The following casts are necessary because BR 465292 and can be
		// removed, once it is fixed
		MWindow window = ems.createModelElement(MWindow.class);
		MElementContainer<? extends MUIElement> erase1 = window;
		MElementContainer<MUIElement> target = (MElementContainer<MUIElement>) erase1;
		MPart part = ems.createModelElement(MPart.class);
		MPart part2 = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		target.getChildren().add(part2);
		EModelService modelService = applicationContext.get(EModelService.class);
		modelService.move(part, target);
		assertSame(part, target.getChildren().get(1));
	}

	@Test
	public void testMoveWithIndexWithTwoOtherElement() {
		MWindow source = ems.createModelElement(MWindow.class);

		// The following casts are necessary because BR 465292 and can be
		// removed, once it is fixed
		MWindow window = ems.createModelElement(MWindow.class);
		MElementContainer<? extends MUIElement> erase1 = window;
		MElementContainer<MUIElement> target = (MElementContainer<MUIElement>) erase1;
		MPart part = ems.createModelElement(MPart.class);
		MPart part2 = ems.createModelElement(MPart.class);
		MPart part3 = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		target.getChildren().add(part2);
		target.getChildren().add(part3);
		EModelService modelService = applicationContext.get(EModelService.class);
		modelService.move(part, target, 1);
		assertSame(part, target.getChildren().get(1));
	}

	@Test
	public void testCountRenderableChildren_WithWindows() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MWindow perspectiveWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(perspectiveWindow);

		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(EModelService.class);
		assertEquals(2, modelService.countRenderableChildren(perspective));
	}

}
