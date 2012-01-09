/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class EModelServiceTest extends UITest {

	public void testGetPerspectiveFor_RegularElement() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
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

	public void testGetPerspectiveFor_SharedElement() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
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

	public void testGetPerspectiveFor_SharedElement2() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		MPartSashContainer partSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		placeholder.setRef(partSashContainer);
		partSashContainer.setCurSharedRef(placeholder);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
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

	public void testBringToTop01() {
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
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

	public void testBringToTop02() {
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowB);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		windowB.getChildren().add(partB);
		windowB.setSelectedElement(partB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		assertEquals(windowA, application.getSelectedElement());

		EModelService modelService = applicationContext
				.get(EModelService.class);
		modelService.bringToTop(windowA);
		assertEquals(windowA, application.getSelectedElement());

		modelService.bringToTop(partB);
		assertEquals(windowA, application.getSelectedElement());
	}

	public void testBringToTop_Bug334411() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		detachedWindow.setToBeRendered(false);
		window.getWindows().add(detachedWindow);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

	public void testGetElementLocation_Bug331062_01() {
		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		perspective.getChildren().add(part);

		EModelService modelService = applicationContext
				.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI,
				modelService.getElementLocation(part));
	}

	public void testGetElementLocation_Bug331062_02() {
		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		perspective.getWindows().add(detachedWindow);

		MWindow innerWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		detachedWindow.getWindows().add(innerWindow);

		EModelService modelService = applicationContext
				.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI,
				modelService.getElementLocation(innerWindow));
	}
}
