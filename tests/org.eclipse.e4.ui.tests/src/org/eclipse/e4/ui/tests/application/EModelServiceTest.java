/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.Test;

public class EModelServiceTest extends UITest {

	@Test
	public void testGetPerspectiveFor_RegularElement() {
		var window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		var perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		var perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		var partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		getEngine().createGui(window);

		var modelService = window.getContext().get(EModelService.class);
		var foundPerspective = modelService.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testGetPerspectiveFor_SharedElement() {
		var window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		var perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		var perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		var placeholder = ems.createModelElement(MPlaceholder.class);
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		var partStack = ems.createModelElement(MPartStack.class);
		placeholder.setRef(partStack);
		partStack.setCurSharedRef(placeholder);

		getEngine().createGui(window);

		var modelService = window.getContext().get(EModelService.class);
		var foundPerspective = modelService.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testGetPerspectiveFor_SharedElement2() {
		var window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		var perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		var perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		var placeholder = ems.createModelElement(MPlaceholder.class);
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		var partSashContainer = ems.createModelElement(MPartSashContainer.class);
		placeholder.setRef(partSashContainer);
		partSashContainer.setCurSharedRef(placeholder);

		var partStack = ems.createModelElement(MPartStack.class);
		partSashContainer.getChildren().add(partStack);
		partSashContainer.setSelectedElement(partStack);

		getEngine().createGui(window);

		var modelService = window.getContext().get(EModelService.class);
		var foundPerspective = modelService.getPerspectiveFor(partStack);
		assertNotNull(foundPerspective);
		assertEquals(perspective, foundPerspective);
	}

	@Test
	public void testBringToTop01() {
		var windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		var windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		assertEquals(windowA, application.getSelectedElement());

		var modelService = applicationContext.get(EModelService.class);
		modelService.bringToTop(windowA);
		assertEquals(windowA, application.getSelectedElement());

		modelService.bringToTop(windowB);
		assertEquals(windowB, application.getSelectedElement());
	}

	@Test
	public void testBringToTop02() {
		var windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		var windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		var partB = ems.createModelElement(MPart.class);
		windowB.getChildren().add(partB);
		windowB.setSelectedElement(partB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		assertEquals(windowB, application.getSelectedElement());

		var modelService = applicationContext.get(EModelService.class);
		modelService.bringToTop(windowA);
		assertEquals(windowA, application.getSelectedElement());

		modelService.bringToTop(partB);
		assertEquals(windowA, application.getSelectedElement());
	}

	@Test
	public void testBringToTop_Bug334411() {
		var window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		var detachedWindow = ems.createModelElement(MWindow.class);
		detachedWindow.setToBeRendered(false);
		window.getWindows().add(detachedWindow);

		var part = ems.createModelElement(MPart.class);
		part.setToBeRendered(false);
		detachedWindow.getChildren().add(part);

		getEngine().createGui(window);

		assertEquals(window, application.getSelectedElement());

		var modelService = applicationContext.get(EModelService.class);
		modelService.bringToTop(part);
		assertTrue(part.isToBeRendered());
		assertTrue(detachedWindow.isToBeRendered());
	}

	@Test
	public void testGetElementLocation_Bug331062_01() {
		var perspective = ems.createModelElement(MPerspective.class);
		var part = ems.createModelElement(MPart.class);
		perspective.getChildren().add(part);

		var modelService = applicationContext.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI, modelService.getElementLocation(part));
	}

	@Test
	public void testGetElementLocation_Bug331062_02() {
		var perspective = ems.createModelElement(MPerspective.class);
		var detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		var innerWindow = ems.createModelElement(MWindow.class);
		detachedWindow.getWindows().add(innerWindow);

		var modelService = applicationContext.get(EModelService.class);
		assertEquals(EModelService.NOT_IN_UI, modelService.getElementLocation(innerWindow));
	}

	@Test
	public void testMoveWithoutIndexNoOtherElements() {
		var source = ems.createModelElement(MWindow.class);
		var window = ems.createModelElement(MWindow.class);
		var part = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		var modelService = applicationContext.get(EModelService.class);
		modelService.move(part, window);
		assertEquals(part, window.getChildren().get(0));
	}

	@Test
	public void testMoveWithoutIndexWithOneOtherElements() {
		var source = ems.createModelElement(MWindow.class);
		var window = ems.createModelElement(MWindow.class);
		var part = ems.createModelElement(MPart.class);
		var part2 = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		window.getChildren().add(part2);
		var modelService = applicationContext.get(EModelService.class);
		modelService.move(part, window);
		assertSame(part, window.getChildren().get(1));
	}

	@Test
	public void testMoveWithIndexWithTwoOtherElement() {
		var source = ems.createModelElement(MWindow.class);
		var window = ems.createModelElement(MWindow.class);
		var part = ems.createModelElement(MPart.class);
		var part2 = ems.createModelElement(MPart.class);
		var part3 = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		window.getChildren().add(part2);
		window.getChildren().add(part3);
		var modelService = applicationContext.get(EModelService.class);
		modelService.move(part, window, 1);
		assertSame(part, window.getChildren().get(1));
	}

	@Test
	public void moveWithIndexShouldNotChangeSelectedElement() {
		var source = ems.createModelElement(MWindow.class);
		var window = ems.createModelElement(MWindow.class);
		var part = ems.createModelElement(MPart.class);
		var part2 = ems.createModelElement(MPart.class);
		var part3 = ems.createModelElement(MPart.class);
		source.getChildren().add(part);
		window.getChildren().add(part2);
		window.getChildren().add(part3);
		window.setSelectedElement(part2);
		var modelService = applicationContext.get(EModelService.class);
		modelService.move(part, window, 1);
		assertSame(part, window.getChildren().get(1));
		assertSame(part2, window.getSelectedElement());
	}

	@Test
	public void testCountRenderableChildren_WithWindows() {
		var window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		var perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		var perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		var partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		var perspectiveWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(perspectiveWindow);

		getEngine().createGui(window);

		var modelService = window.getContext().get(EModelService.class);
		assertEquals(2, modelService.countRenderableChildren(perspective));
	}

	@Test
	public void testCreatePartFromDescriptorWithTrimBars() {
		var mPartDescriptor = ems.createModelElement(MPartDescriptor.class);
		var mTrimBar = ems.createModelElement(MTrimBar.class);
		mTrimBar.setElementId("test.trimbar.id");
		mPartDescriptor.getTrimBars().add(mTrimBar);

		var newPart = ems.createPart(mPartDescriptor);

		assertEquals(1, newPart.getTrimBars().size());
		assertEquals(1, mPartDescriptor.getTrimBars().size());
		assertEquals(newPart.getTrimBars().get(0).getElementId(), mPartDescriptor.getTrimBars().get(0).getElementId());

	}
}
