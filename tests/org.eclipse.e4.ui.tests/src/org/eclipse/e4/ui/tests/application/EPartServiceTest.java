/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.tests.workbench.TargetedView;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler.Save;
import org.eclipse.emf.common.notify.Notifier;
import org.junit.Test;

public class EPartServiceTest extends UITest {

	@Test
	public void testFindPart_PartInWindow() {
		createApplication("partId");

		MWindow window = application.getChildren().get(0);
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.findPart("partId");
		assertNotNull(part);

		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);

		part = partService.findPart("invalidPartId");
		assertNull(part);
	}

	@Test
	public void testFindPart_PartNotInWindow() {
		createApplication("partId");

		MWindow window = application.getChildren().get(0);
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.findPart("invalidPartId");
		assertNull(part);
	}

	@Test
	public void testFindPart_PartInAnotherWindow() {
		createApplication(new String[] { "partInWindow1" }, new String[] { "partInWindow2" });

		MWindow window1 = application.getChildren().get(0);
		MWindow window2 = application.getChildren().get(1);

		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService partService = window1.getContext().get(EPartService.class);
		MPart part = partService.findPart("partInWindow2");
		assertNull(part);
		part = partService.findPart("partInWindow1");
		assertNotNull(part);

		MPartStack partStack = (MPartStack) window1.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);

		partService = window2.getContext().get(EPartService.class);
		part = partService.findPart("partInWindow1");
		assertNull(part);
		part = partService.findPart("partInWindow2");
		assertNotNull(part);

		partStack = (MPartStack) window2.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);
	}

	@Test
	public void testBringToTop_PartOnTop() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.bringToTop(partFront);
		assertEquals(partStack.getSelectedElement(), partFront);
	}

	@Test
	public void testBringToTop_PartOnTop_myService() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = partFront.getContext().get(EPartService.class);

		partService.bringToTop(partFront);
		assertEquals(partStack.getSelectedElement(), partFront);
	}

	@Test
	public void testBringToTop_PartNotOnTop() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.bringToTop(partBack);
		assertEquals(partStack.getSelectedElement(), partBack);
	}

	@Test
	public void testBringToTop_PartNotOnTop_myService() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = partFront.getContext().get(EPartService.class);

		partService.bringToTop(partBack);
		assertEquals(partStack.getSelectedElement(), partBack);
	}

	@Test
	public void testBringToTop_PartInAnotherWindow() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		partServiceA.bringToTop(partBackB);
		assertEquals(partStackA.getSelectedElement(), partFrontA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceB.bringToTop(partBackA);
		assertEquals(partStackA.getSelectedElement(), partFrontA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceA.bringToTop(partBackA);
		assertEquals(partStackA.getSelectedElement(), partBackA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceB.bringToTop(partBackB);
		assertEquals(partStackA.getSelectedElement(), partBackA);
		assertEquals(partStackB.getSelectedElement(), partBackB);
	}

	@Test
	public void testBringToTop_PartInAnotherWindow_myService() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = partFrontA.getContext().get(EPartService.class);
		EPartService partServiceB = partFrontB.getContext().get(EPartService.class);

		partServiceA.bringToTop(partBackB);
		assertEquals(partStackA.getSelectedElement(), partFrontA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceB.bringToTop(partBackA);
		assertEquals(partStackA.getSelectedElement(), partFrontA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceA.bringToTop(partBackA);
		assertEquals(partStackA.getSelectedElement(), partBackA);
		assertEquals(partStackB.getSelectedElement(), partFrontB);

		partServiceB.bringToTop(partBackB);
		assertEquals(partStackA.getSelectedElement(), partBackA);
		assertEquals(partStackB.getSelectedElement(), partBackB);
	}

	/**
	 * Test to ensure that calling bringToTop(MPart) will change the active part
	 * if the active part is obscured by the part that's being brought to top.
	 */
	@Test
	public void testBringToTop_ActivationChanges01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		MPart partFrontA = ems.createModelElement(MPart.class);
		MPart partBackA = ems.createModelElement(MPart.class);
		partStackA.getChildren().add(partFrontA);
		partStackA.getChildren().add(partBackA);
		window.getChildren().add(partStackA);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		MPart partFrontB = ems.createModelElement(MPart.class);
		MPart partBackB = ems.createModelElement(MPart.class);
		partStackB.getChildren().add(partFrontB);
		partStackB.getChildren().add(partBackB);
		window.getChildren().add(partStackB);

		partStackA.setSelectedElement(partFrontA);
		partStackB.setSelectedElement(partFrontB);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partFrontA);
		assertEquals(partFrontA, partService.getActivePart());

		partService.bringToTop(partBackB);

		assertEquals(partFrontA, partService.getActivePart());
		assertTrue(partService.isPartVisible(partFrontA));
		assertFalse(partService.isPartVisible(partBackA));
		assertFalse(partService.isPartVisible(partFrontB));
		assertTrue(partService.isPartVisible(partBackB));

		partService.bringToTop(partBackA);

		assertEquals(partBackA, partService.getActivePart());
		assertFalse(partService.isPartVisible(partFrontA));
		assertTrue(partService.isPartVisible(partBackA));
		assertFalse(partService.isPartVisible(partFrontB));
		assertTrue(partService.isPartVisible(partBackB));
	}

	/**
	 * Test to ensure that calling bringToTop(MPart) will change the active part
	 * if the active part is obscured by the part that's being brought to top.
	 * The part that is being passed to bringToTop(MPart) is a part that's being
	 * represented by a placeholder in this case.
	 */
	@Test
	public void testBringToTop_ActivationChanges02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		partB.setCurSharedRef(placeholderB);
		placeholderB.setRef(partB);
		partStack.getChildren().add(placeholderB);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.bringToTop(partB);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testBringToTop_Unrendered() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partBack.setToBeRendered(false);

		partStack.setSelectedElement(partFront);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		assertFalse(partBack.isToBeRendered());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.bringToTop(partBack);
		assertTrue("Bringing a part to the top should cause it to be rendered", partBack.isToBeRendered());
	}

	@Test
	public void testBringToTop_Bug330508_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.bringToTop(partB);
		assertEquals(
				"Bringing a part to top that's not in the same container as the active part shouldn't change the active part",
				partA, partService.getActivePart());
	}

	@Test
	public void testBringToTop_Bug330508_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		partB.setCurSharedRef(placeholderB);
		placeholderB.setRef(partB);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.bringToTop(partB);
		assertEquals(
				"Bringing a part to top that's not in the same container as the active part shouldn't change the active part",
				partA, partService.getActivePart());
	}

	@Test
	public void testBringToTop_Bug330508_03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.activate(partC);
		assertEquals(partC, partService.getActivePart());

		partService.bringToTop(partB);
		assertEquals(
				"Bringing a part to top that's not in the same container as the active part shouldn't change the active part",
				partC, partService.getActivePart());
	}

	@Test
	public void testBringToTop_Bug330508_04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		partStack.getChildren().add(placeholderA);
		partStack.setSelectedElement(placeholderA);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.activate(partC);
		assertEquals(partC, partService.getActivePart());

		partService.bringToTop(partB);
		assertEquals(
				"Bringing a part to top that's not in the same container as the active part shouldn't change the active part",
				partC, partService.getActivePart());
	}

	@Test
	public void testGetParts_Empty() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> parts = partService.getParts();
		assertNotNull(parts);
		assertEquals(0, parts.size());
	}

	@Test
	public void testGetParts_OneWindow() {
		createApplication("partId", "partId2");
		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> parts = partService.getParts();
		assertNotNull(parts);
		assertEquals(2, parts.size());
		assertTrue(parts.containsAll(partStack.getChildren()));
	}

	@Test
	public void testGetParts_TwoWindows() {
		createApplication(new String[] { "partId", "partId2" }, new String[] { "partIA", "partIdB", "partIdC" });

		MWindow windowA = application.getChildren().get(0);
		MWindow windowB = application.getChildren().get(1);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);

		Collection<MPart> partsA = partServiceA.getParts();
		Collection<MPart> partsB = partServiceB.getParts();

		assertNotNull(partsA);
		assertEquals(2, partsA.size());
		assertTrue(partsA.containsAll(partStackA.getChildren()));

		assertNotNull(partsB);
		assertEquals(3, partsB.size());
		assertTrue(partsB.containsAll(partStackB.getChildren()));

		for (MPart partA : partsA) {
			assertFalse(partsB.contains(partA));
		}
	}

	@Test
	public void testGetParts_Bug334559_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> parts = partService.getParts();
		assertEquals(3, parts.size());
		assertTrue(parts.contains(partA));
		assertTrue(parts.contains(partB));
		assertTrue(parts.contains(partC));
	}

	@Test
	public void testGetParts_Bug334559_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> parts = partService.getParts();
		assertEquals(3, parts.size());
		assertTrue(parts.contains(partA));
		assertTrue(parts.contains(partB));
		assertTrue(parts.contains(partC));
	}


	@Test
	public void testGetActivePart() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MPart partA = ems.createModelElement(MPart.class);
		windowA.getChildren().add(partA);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		MPart partB = ems.createModelElement(MPart.class);
		windowB.getChildren().add(partB);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService applicationPartService = application.getContext().get(EPartService.class);
		EPartService windowPartServiceA = windowA.getContext().get(EPartService.class);
		EPartService windowPartServiceB = windowB.getContext().get(EPartService.class);

		windowPartServiceA.activate(partA);
		assertEquals(partA, applicationPartService.getActivePart());
		assertEquals(partA, windowPartServiceA.getActivePart());
		assertEquals(partB, windowPartServiceB.getActivePart());

		windowPartServiceB.activate(partB);
		assertEquals(partB, applicationPartService.getActivePart());
		assertEquals(partA, windowPartServiceA.getActivePart());
		assertEquals(partB, windowPartServiceB.getActivePart());
	}

	public void testIsPartVisible_NotInStack(boolean selected, boolean visible) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setVisible(visible);
		window.getChildren().add(part);

		if (selected) {
			window.setSelectedElement(part);
		}

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(visible, partService.isPartVisible(part));
		partService = part.getContext().get(EPartService.class);
		assertEquals(visible, partService.isPartVisible(part));
	}

	@Test
	public void testIsPartVisible_NotInStackTrueTrue() {
		testIsPartVisible_NotInStack(true, true);
	}

	@Test
	public void testIsPartVisible_NotInStackFalseTrue() {
		testIsPartVisible_NotInStack(false, true);
	}

	@Test
	public void testIsPartVisible_NotInStackFalseFalse() {
		testIsPartVisible_NotInStack(false, false);
	}

	@Test
	public void testIsPartVisible_ViewVisible() {
		createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = (MPart) partStack.getChildren().get(0);
		partStack.setSelectedElement(part);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertTrue(partService.isPartVisible(part));
	}

	@Test
	public void testIsPartVisible_ViewVisible_myService() {
		createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = (MPart) partStack.getChildren().get(0);
		partStack.setSelectedElement(part);

		getEngine().createGui(window);

		EPartService partService = part.getContext().get(EPartService.class);
		assertTrue(partService.isPartVisible(part));
	}

	@Test
	public void testIsPartVisible_ViewNotVisible() {
		createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part = (MPart) partStack.getChildren().get(1);

		EPartService partService = window.getContext().get(EPartService.class);
		assertFalse(partService.isPartVisible(part));
	}

	@Test
	public void testIsPartVisible_ViewNotVisible_myService() {
		createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part1 = (MPart) partStack.getChildren().get(0);
		MPart part2 = (MPart) partStack.getChildren().get(1);

		EPartService partService1 = part1.getContext().get(EPartService.class);
		assertTrue(partService1.isPartVisible(part1));
		assertFalse(partService1.isPartVisible(part2));

		partService1.activate(part2);

		EPartService partService2 = part2.getContext().get(EPartService.class);
		assertFalse(partService1.isPartVisible(part1));
		assertTrue(partService1.isPartVisible(part2));
		assertFalse(partService2.isPartVisible(part1));
		assertTrue(partService2.isPartVisible(part2));
	}

	@Test
	public void testIsPartVisible_ViewInAnotherWindow() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		assertTrue(partServiceA.isPartVisible(partFrontA));
		assertFalse(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertTrue(partServiceB.isPartVisible(partFrontB));
		assertFalse(partServiceB.isPartVisible(partBackB));
	}

	@Test
	public void testIsPartVisible_ViewInAnotherWindow_myService() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = partFrontA.getContext().get(EPartService.class);
		EPartService partServiceB = partFrontB.getContext().get(EPartService.class);

		assertTrue(partServiceA.isPartVisible(partFrontA));
		assertFalse(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertTrue(partServiceB.isPartVisible(partFrontB));
		assertFalse(partServiceB.isPartVisible(partBackB));
	}

	@Test
	public void testIsPartVisible_Placeholder() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("containerTag");
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

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
		partStack.getTags().add("containerTag");
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPlaceholder placeholder = partService.createSharedPart("partId", true);
		MPart sharedPart = (MPart) placeholder.getRef();
		sharedPart.setCurSharedRef(placeholder);
		partService.showPart(sharedPart, PartState.ACTIVATE);

		assertTrue(partService.isPartVisible(sharedPart));
	}

	@Test
	public void testActivate_partService() {
		createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part1 = (MPart) partStack.getChildren().get(0);
		MPart part2 = (MPart) partStack.getChildren().get(1);

		EPartService partService1 = part1.getContext().get(EPartService.class);
		assertTrue(partService1.isPartVisible(part1));
		assertFalse(partService1.isPartVisible(part2));

		partService1.activate(part2);

		EPartService partService2 = part2.getContext().get(EPartService.class);
		assertFalse(partService1.isPartVisible(part1));
		assertTrue(partService1.isPartVisible(part2));
		assertFalse(partService2.isPartVisible(part1));
		assertTrue(partService2.isPartVisible(part2));
	}

	@Test
	public void testActivate_partService_twoWindows() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);
		windowA.setSelectedElement(partStackA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);
		windowB.setSelectedElement(partStackB);

		application.setSelectedElement(windowA);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = partFrontA.getContext().get(EPartService.class);
		EPartService partServiceB = partFrontB.getContext().get(EPartService.class);

		partServiceA.activate(partBackA);
		assertEquals(partBackA, partServiceA.getActivePart());

		assertFalse(partServiceA.isPartVisible(partFrontA));
		assertTrue(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		partServiceA.activate(partBackB);
		assertEquals(partBackA, partServiceA.getActivePart());

		assertFalse(partServiceA.isPartVisible(partFrontA));
		assertTrue(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertTrue(partServiceB.isPartVisible(partFrontB));
		assertFalse(partServiceB.isPartVisible(partBackB));

		partServiceB.activate(partBackB);
		assertEquals(partBackB, partServiceB.getActivePart());
		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertFalse(partServiceB.isPartVisible(partFrontB));
		assertTrue(partServiceB.isPartVisible(partBackB));
	}

	@Test
	public void testActivate_partService_SelectedElement() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = partFrontA.getContext().get(EPartService.class);
		EPartService partServiceB = partFrontB.getContext().get(EPartService.class);

		partServiceA.activate(partBackA);

		assertEquals(windowA, application.getSelectedElement());
		IEclipseContext a = application.getContext().getActiveLeaf();
		MPart aPart = a.get(MPart.class);
		assertEquals(partBackA, aPart);

		partServiceB.activate(partBackB);
		assertEquals(windowB, application.getSelectedElement());
		a = application.getContext().getActiveLeaf();
		aPart = a.get(MPart.class);
		assertEquals(partBackB, aPart);
	}

	@Test
	public void testActivate_partService_activePart() {
		createApplication(new String[] { "partFrontA", "partBackA" }, new String[] { "partFrontB", "partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = (MPart) partStackA.getChildren().get(0);
		MPart partBackA = (MPart) partStackA.getChildren().get(1);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = (MPart) partStackB.getChildren().get(0);
		MPart partBackB = (MPart) partStackB.getChildren().get(1);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = partFrontA.getContext().get(EPartService.class);
		EPartService partServiceB = partFrontB.getContext().get(EPartService.class);

		partServiceA.activate(partBackA);

		assertEquals(windowA, application.getSelectedElement());
		MPart shouldBeCorrect = (MPart) partFrontA.getContext().get(IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackA, partServiceA.getActivePart());

		partServiceB.activate(partBackB);
		assertEquals(windowB, application.getSelectedElement());
		shouldBeCorrect = (MPart) partFrontB.getContext().get(IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackB, partServiceB.getActivePart());
	}

	@Test
	public void testActivate_Unrendered() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partBack.setToBeRendered(false);

		partStack.setSelectedElement(partFront);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		assertFalse(partBack.isToBeRendered());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partBack);
		assertTrue("Activating a part should cause it to be rendered", partBack.isToBeRendered());
	}

	@Test
	public void testActivate_Focus() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(partB);

		initialize();

		getEngine().createGui(window);

		ClientEditor editorB = (ClientEditor) partB.getObject();

		assertFalse(editorB.wasFocusCalled());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		assertTrue(editorB.wasFocusCalled());
	}

	@Test
	public void testActivate_ChildWindow() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		window.getWindows().add(detachedWindow);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testActivate_DetachedWindow() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testActivate_Bug326300() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MPart partA = ems.createModelElement(MPart.class);
		windowA.getChildren().add(partA);
		windowA.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		windowA.getChildren().add(partB);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		MPart partC = ems.createModelElement(MPart.class);
		windowB.getChildren().add(partC);
		windowB.setSelectedElement(partC);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		windowA.getContext().get(EPartService.class).activate(partB);
		assertEquals(windowA, application.getSelectedElement());
		assertEquals(partB, windowA.getContext().get(EPartService.class).getActivePart());

		windowB.getContext().get(EPartService.class).activate(partC);
		assertEquals(windowB, application.getSelectedElement());
		assertEquals(partC, windowB.getContext().get(EPartService.class).getActivePart());

		windowA.getContext().get(EPartService.class).activate(partB);
		assertEquals(windowA, application.getSelectedElement());
		assertEquals(partB, windowA.getContext().get(EPartService.class).getActivePart());
	}

	@Test
	public void testActivate_Bug371894() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getChildren().add(partB);
		window.setSelectedElement(partB);

		initialize();
		getEngine().createGui(window);

		IEclipseContext context = window.getContext();
		context.get(EModelService.class).hostElement(partA, window, window.getWidget(), context);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
	}

	@Test
	public void testCreatePart() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertNotNull(partService.createPart("partId"));
	}

	@Test
	public void testCreatePart2() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertNull(partService.createPart("partId2"));
	}

	@Test
	public void testCreatePart_WithVariables() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.getVariables().add("testVariable");
		partDescriptor.getProperties().put("testVariable", "testValue");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPart part = partService.createPart("partId");
		assertNotNull(part);
		assertEquals(1, part.getVariables().size());
		assertEquals("testVariable", part.getVariables().get(0));
		assertEquals(1, part.getProperties().size());
		assertTrue(part.getProperties().containsKey("testVariable"));
		assertEquals("testValue", part.getProperties().get("testVariable"));
	}

	@Test
	public void testCreateSharedPart_NoDescriptor() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertNull(partService.createSharedPart("partId"));
	}

	@Test
	public void testCreateSharedPart_ForceFalse() {
		createApplication(1, new String[1][0]);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholderA = partService.createSharedPart("partId", false);
		MPlaceholder placeholderB = partService.createSharedPart("partId", false);

		assertEquals(1, window.getSharedElements().size());

		MPart part = (MPart) window.getSharedElements().get(0);
		assertEquals(part, placeholderA.getRef());
		assertEquals(part, placeholderB.getRef());
	}

	@Test
	public void testCreateSharedPart_ForceTrue() {
		createApplication(1, new String[1][0]);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholderA = partService.createSharedPart("partId", true);
		MPlaceholder placeholderB = partService.createSharedPart("partId", true);

		assertEquals(2, window.getSharedElements().size());

		MPart part1 = (MPart) window.getSharedElements().get(0);
		MPart part2 = (MPart) window.getSharedElements().get(1);
		assertTrue(part1 == placeholderA.getRef() || part1 == placeholderB.getRef());
		assertTrue(part2 == placeholderA.getRef() || part2 == placeholderB.getRef());
	}

	@Test
	public void testShowPart_Id_ACTIVATE() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("partId", PartState.ACTIVATE);
		assertNotNull(part);
		assertEquals("partId", part.getElementId());
		assertEquals(part, partService.getActivePart());
		assertTrue("Shown part should be visible", part.isVisible());
	}

	@Test
	public void testShowPart_Id_ACTIVATE_DefinedCategoryStackNotExists() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId2");
		application.getDescriptors().add(partDescriptor);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("partId", PartState.ACTIVATE);

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPartStack);

		MPartStack stack = (MPartStack) window.getChildren().get(0);
		assertTrue(stack.getTags().contains("categoryId"));

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, stack.getSelectedElement());

		MPart part2 = partService.showPart("partId2", PartState.ACTIVATE);
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
		assertEquals(part2, stack.getSelectedElement());
	}

	@Test
	public void testShowPart_Id_ACTIVATE_DefinedCategoryStackExists() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId2");
		application.getDescriptors().add(partDescriptor);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.getTags().add("categoryId");
		window.getChildren().add(stack);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("partId", PartState.ACTIVATE);
		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, stack.getSelectedElement());

		MPart part2 = partService.showPart("partId2", PartState.ACTIVATE);
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
		assertEquals(part2, stack.getSelectedElement());
	}

	@Test
	public void testShowPart_Id_CREATE() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = ems.createModelElement(MPart.class);
		MPart partA2 = ems.createModelElement(MPart.class);
		partA1.setElementId("partA1");
		partA2.setElementId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = ems.createModelElement(MPart.class);
		MPart partB2 = ems.createModelElement(MPart.class);
		partB1.setElementId("partB1");
		partB2.setElementId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setSelectedElement(partA1);
		partStackB.setSelectedElement(partB1);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA1);
		assertEquals(partA1, partService.getActivePart());

		assertEquals(null, partA2.getContext());
		assertEquals(null, partB2.getContext());

		MPart shownPart = partService.showPart("partA2", EPartService.PartState.CREATE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(shownPart, partA2);
		assertNotNull("The part should have been created so it should have a context", partA2.getContext());
		assertEquals("This part has not been instantiated yet, it should have no context", null, partB2.getContext());

		shownPart = partService.showPart("partB2", EPartService.PartState.CREATE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(shownPart, partB2);
		assertNotNull("The part should have been created so it should have a context", partA2.getContext());
		assertNotNull("The part should have been created so it should have a context", partB2.getContext());
	}

	@Test
	public void testShowPart_Id_CREATE2() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB", EPartService.PartState.CREATE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals("Only creating the part, the active part should not have changed", partA,
				partService.getActivePart());
		assertNotNull("The shown part should have a context", partB.getContext());
		assertFalse(partService.isPartVisible(partB));
	}

	@Test
	public void testShowPart_Id_CREATE3() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStackA);
		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		partStackB.getTags().add("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setSelectedElement(partA);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB", EPartService.PartState.CREATE);

		assertEquals(1, partStackA.getChildren().size());
		assertEquals("Only creating the part, the active part should not have changed", partA,
				partService.getActivePart());
		assertNotNull("The shown part should have a context", partB.getContext());
		assertTrue("The part is the only one in the stack, it should be visible", partService.isPartVisible(partB));
	}

	@Test
	public void testShowPart_Id_CREATE4() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.getTags().add("stackId");
		window.getChildren().add(stack);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("part");
		partDescriptor.setCategory("stackId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("part", EPartService.PartState.CREATE);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, partService.getActivePart());
	}

	@Test
	public void testShowPart_Id_VISIBLE() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = ems.createModelElement(MPart.class);
		MPart partA2 = ems.createModelElement(MPart.class);
		partA1.setElementId("partA1");
		partA2.setElementId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = ems.createModelElement(MPart.class);
		MPart partB2 = ems.createModelElement(MPart.class);
		partB1.setElementId("partB1");
		partB2.setElementId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setSelectedElement(partA1);
		partStackB.setSelectedElement(partB1);
		window.setSelectedElement(partStackA);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA1);
		assertEquals(partA1, partService.getActivePart());

		MPart shownPart = partService.showPart("partB1", EPartService.PartState.VISIBLE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(partB1, shownPart);

		shownPart = partService.showPart("partB2", EPartService.PartState.VISIBLE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB2));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(partB2, shownPart);
	}

	@Test
	public void testShowPart_Id_VISIBLE2() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB", EPartService.PartState.VISIBLE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals("The part is in the same stack as the active part, so the active part should have changed", partB,
				partService.getActivePart());
		assertNotNull("The shown part should have a context", partB.getContext());
		assertFalse(partService.isPartVisible(partA));
		assertTrue(partService.isPartVisible(partB));
	}

	@Test
	public void testShowPart_Id_VISIBLE3() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStackA);
		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		partStackB.getTags().add("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setSelectedElement(partA);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB", EPartService.PartState.VISIBLE);

		assertEquals(1, partStackA.getChildren().size());
		assertEquals("Only making a part visible, the active part should not have changed", partA,
				partService.getActivePart());
		assertNotNull("The shown part should have a context", partB.getContext());
		assertTrue("The part is the only one in the stack, it should be visible", partService.isPartVisible(partB));
	}

	@Test
	public void testShowPart_Id_VISIBLE4() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.getTags().add("stackId");
		window.getChildren().add(stack);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("part");
		partDescriptor.setCategory("stackId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("part", EPartService.PartState.VISIBLE);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, partService.getActivePart());
	}

	@Test
	public void testShowPart_Id_VISIBLE5() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setElementId("partB");
		partB.setToBeRendered(false);
		partStack.getChildren().add(partB);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart shownPart = partService.showPart("partB", EPartService.PartState.VISIBLE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals("The part is in the same stack as the active part, so the active part should have changed", partB,
				partService.getActivePart());
		assertNotNull("The shown part should have a context", partB.getContext());
		assertFalse(partService.isPartVisible(partA));
		assertTrue(partService.isPartVisible(partB));
		assertEquals(partB, shownPart);
		assertTrue(partB.isToBeRendered());
	}

	private void testShowPart_Id_Unrendered(EPartService.PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId");
		part.setToBeRendered(false);
		window.getChildren().add(part);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart shownPart = partService.showPart("partId", partState);

		assertEquals(1, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));
		assertEquals(part, shownPart);
		assertTrue("A shown part should be rendered", part.isToBeRendered());
		assertNotNull("A shown part should have a widget", part.getWidget());
	}

	@Test
	public void testShowPart_Id_Unrendered_CREATE() {
		testShowPart_Id_Unrendered(PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_Unrendered_VISIBLE() {
		testShowPart_Id_Unrendered(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_Unrendered_ACTIVATE() {
		testShowPart_Id_Unrendered(PartState.ACTIVATE);
	}

	private void testShowPart_Id_Unrendered2(EPartService.PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		window.setToBeRendered(true);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setToBeRendered(false);
		window.getChildren().add(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId");
		part.setToBeRendered(false);
		partStack.getChildren().add(part);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart shownPart = partService.showPart("partId", partState);

		assertEquals(1, partStack.getChildren().size());
		assertEquals(part, partStack.getChildren().get(0));
		assertEquals(part, shownPart);
		assertTrue("A shown part should be rendered", part.isToBeRendered());
		assertNotNull("A shown part should have a widget", part.getWidget());
	}

	@Test
	public void testShowPart_Id_Unrendered_CREATE2() {
		testShowPart_Id_Unrendered2(PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_Unrendered_VISIBLE2() {
		testShowPart_Id_Unrendered2(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_Unrendered_ACTIVATE2() {
		testShowPart_Id_Unrendered2(PartState.ACTIVATE);
	}

	private void testShowPart_Id_Unrendered3(EPartService.PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		window.setToBeRendered(true);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		partSashContainer.setToBeRendered(false);
		window.getChildren().add(partSashContainer);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setToBeRendered(false);
		partSashContainer.getChildren().add(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId");
		part.setToBeRendered(false);
		partStack.getChildren().add(part);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart shownPart = partService.showPart("partId", partState);

		assertEquals(1, partStack.getChildren().size());
		assertEquals(part, partStack.getChildren().get(0));
		assertEquals(part, shownPart);
		assertTrue("A shown part should be rendered", part.isToBeRendered());
		assertNotNull("A shown part should have a widget", part.getWidget());
	}

	@Test
	public void testShowPart_Id_Unrendered_CREATE3() {
		testShowPart_Id_Unrendered3(PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_Unrendered_VISIBLE3() {
		testShowPart_Id_Unrendered3(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_Unrendered_ACTIVATE3() {
		testShowPart_Id_Unrendered3(PartState.ACTIVATE);
	}

	private void testShowPart_Id_PartAlreadyShown(PartState partState) {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.showPart("partId", partState);
		assertNotNull(part);
		assertEquals("partId", part.getElementId());
		assertEquals(part, partService.getActivePart());

		MPart part2 = partService.showPart("partId", partState);
		assertEquals("Should not have instantiated a new MPart", part, part2);
		assertEquals(part, partService.getActivePart());
	}

	@Test
	public void testShowPart_Id_PartAlreadyShown_ACTIVATE() {
		testShowPart_Id_PartAlreadyShown(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Id_PartAlreadyShown_CREATE() {
		testShowPart_Id_PartAlreadyShown(PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_PartAlreadyShown_VISIBLE() {
		testShowPart_Id_PartAlreadyShown(PartState.VISIBLE);
	}

	private void testShowPart_Id_IncorrectDescriptor(PartState partState) {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertNull(partService.showPart("partId2", partState));
	}

	@Test
	public void testShowPart_Id_IncorrectDescriptor_ACTIVATE() {
		testShowPart_Id_IncorrectDescriptor(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Id_IncorrectDescriptor_VISIBLE() {
		testShowPart_Id_IncorrectDescriptor(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_IncorrectDescriptor_CREATE() {
		testShowPart_Id_IncorrectDescriptor(PartState.CREATE);
	}

	private void testShowPart_Id_MultipleExists(boolean multipleAllowed, PartState partState) {
		createApplication("partId");
		MWindow window = application.getChildren().get(0);
		MPartStack stack = (MPartStack) window.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		stack.setSelectedElement(part);
		window.setSelectedElement(stack);
		application.setSelectedElement(window);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart shownPart = partService.showPart("partId", partState);
		assertNotNull(shownPart);
		assertEquals(part, shownPart);
	}

	@Test
	public void testShowPart_Id_MultipleExists_TrueACTIVATE() {
		testShowPart_Id_MultipleExists(true, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Id_MultipleExists_FalseACTIVATE() {
		testShowPart_Id_MultipleExists(false, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Id_MultipleExists_TrueVISIBLE() {
		testShowPart_Id_MultipleExists(true, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_MultipleExists_FalseVISIBLE() {
		testShowPart_Id_MultipleExists(false, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Id_MultipleExists_TrueCREATE() {
		testShowPart_Id_MultipleExists(true, PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_MultipleExists_FalseCREATE() {
		testShowPart_Id_MultipleExists(false, PartState.CREATE);
	}

	@Test
	public void testShowPart_Id_PartInInactivePerspective() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPart part = partService.showPart("partId", PartState.ACTIVATE);
		MElementContainer<?> parent = part.getParent();
		while (parent != null) {
			if (parent == perspectiveA) {
				break;
			} else if (parent == perspectiveB) {
				fail("Parent should not have been perspectiveB");
			}
			parent = parent.getParent();
		}
		assertNotNull(parent);

		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();

		MPart part2 = partService.showPart("partId", PartState.ACTIVATE);
		parent = part2.getParent();
		while (parent != null) {
			if (parent == perspectiveB) {
				break;
			} else if (parent == perspectiveA) {
				fail("Parent should not have been perspectiveA");
			}
			parent = parent.getParent();
		}
		assertNotNull(parent);
		assertFalse(part == part2);
	}

	private void testShowPart_Part(PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart part = partService.createPart("partId");
		partService.showPart(part, partState);
	}

	@Test
	public void testShowPart_Part_ACTIVATE() {
		testShowPart_Part(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Part_VISIBLE() {
		testShowPart_Part(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Part_CREATE() {
		testShowPart_Part(PartState.CREATE);
	}

	private void testShowPart_Part_MultipleExists(boolean multipleAllowed, PartState partState) {
		createApplication("partId");
		MWindow window = application.getChildren().get(0);
		MPartStack stack = (MPartStack) window.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		stack.setSelectedElement(part);
		window.setSelectedElement(stack);
		application.setSelectedElement(window);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, partState);
		assertNotNull(shownPart);

		if (multipleAllowed) {
			assertEquals(createdPart, shownPart);
		} else {
			assertEquals(part, shownPart);
		}
	}

	@Test
	public void testShowPart_Part_MultipleExists_TrueACTIVATE() {
		testShowPart_Part_MultipleExists(true, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Part_MultipleExists_FalseACTIVATE() {
		testShowPart_Part_MultipleExists(false, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Part_MultipleExists_TrueVISIBLE() {
		testShowPart_Part_MultipleExists(true, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Part_MultipleExists_FalseVISIBLE() {
		testShowPart_Part_MultipleExists(false, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Part_MultipleExists_TrueCREATE() {
		testShowPart_Part_MultipleExists(true, PartState.CREATE);
	}

	@Test
	public void testShowPart_Part_MultipleExists_FalseCREATE() {
		testShowPart_Part_MultipleExists(false, PartState.CREATE);
	}

	private void testShowPart_Part_MultipleNonexistent(boolean multipleAllowed, PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, partState);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_TrueACTIVATE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_FalseACTIVATE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_TrueVISIBLE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_FalseVISIBLE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_TrueCREATE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.CREATE);
	}

	@Test
	public void testShowPart_Part_MultipleNonexistent_FalseCREATE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.CREATE);
	}

	@Test
	public void testShowPart_Part_MultipleWithoutCategory() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setAllowMultiple(true);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, PartState.ACTIVATE);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);

		MPart createdPart2 = partService.createPart("partId");
		MPart shownPart2 = partService.showPart(createdPart2, PartState.ACTIVATE);
		assertFalse(shownPart.equals(shownPart2));
	}

	@Test
	public void testShowPart_Part_MultipleWithCategory() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.getTags().add("categoryId");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPartDescriptor descriptor = ems.createModelElement(MPartDescriptor.class);
		descriptor.setAllowMultiple(true);
		descriptor.setElementId("partId");
		descriptor.setCategory("categoryId");
		application.getDescriptors().add(descriptor);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, PartState.ACTIVATE);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);

		MPart createdPart2 = partService.createPart("partId");
		MPart shownPart2 = partService.showPart(createdPart2, PartState.ACTIVATE);
		assertFalse(shownPart.equals(shownPart2));

		assertTrue(stack.getChildren().contains(shownPart));
		assertTrue(stack.getChildren().contains(shownPart2));
	}

	@Test
	public void testShowPart_Part_ExistingInNonstandardCategory() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.setElementId("categoryId2");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId");
		stack.getChildren().add(part);
		stack.setSelectedElement(part);

		MPartDescriptor descriptor = ems.createModelElement(MPartDescriptor.class);
		descriptor.setAllowMultiple(true);
		descriptor.setElementId("partId");
		descriptor.setCategory("categoryId");
		application.getDescriptors().add(descriptor);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart shownPart = partService.showPart("partId", PartState.ACTIVATE);
		assertEquals(part, shownPart);
		assertEquals(stack, part.getParent());
	}

	@Test
	public void testShowPart_Bug318931() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPlaceholder placeholderA = partService.createSharedPart("partId", true);
		MPart partA = (MPart) placeholderA.getRef();
		partA.setCurSharedRef(placeholderA);
		perspective.getChildren().add(placeholderA);

		MPlaceholder placeholderB = partService.createSharedPart("partId", true);
		MPart partB = (MPart) placeholderB.getRef();
		partB.setCurSharedRef(placeholderB);
		perspective.getChildren().add(placeholderB);

		partService.hidePart(partB);
		partService.showPart(partB, PartState.ACTIVATE);

		assertEquals(2, perspective.getChildren().size());
		assertEquals(placeholderA, perspective.getChildren().get(0));
		assertEquals(placeholderB, perspective.getChildren().get(1));
	}

	@Test
	public void testShowPart_Bug321755() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart part = ems.createModelElement(MPart.class);
		perspective.getChildren().add(part);

		initialize();

		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(EModelService.class);
		EPartService partService = window.getContext().get(EPartService.class);

		MPlaceholder placeholder = partService.createSharedPart("partId", true);
		MPart sharedPart = (MPart) placeholder.getRef();
		sharedPart.setCurSharedRef(placeholder);
		partService.showPart(sharedPart, PartState.ACTIVATE);

		List<MPlaceholder> placeholders = modelService.findElements(perspective, null, MPlaceholder.class);
		assertEquals(1, placeholders.size());
		assertEquals(placeholder, placeholders.get(0));
	}

	@Test
	public void testShowPart_Bug321757() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setCategory("containerTag");
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspective.getTags().add("containerTag");
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholder = partService.createSharedPart("partId", true);
		MPart sharedPart = (MPart) placeholder.getRef();
		sharedPart.setCurSharedRef(placeholder);
		partService.showPart(sharedPart, PartState.ACTIVATE);

		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
	}

	private void testShowPart_Bug322368_Part(PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setToBeRendered(false);
		window.getChildren().add(part);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(part, partState);

		assertNotNull(window.getSelectedElement());
		assertEquals(part, window.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug322368_Part_ACTIVATE() {
		testShowPart_Bug322368_Part(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Bug322368_Part_VISIBLE() {
		testShowPart_Bug322368_Part(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug322368_Part_CREATE() {
		testShowPart_Bug322368_Part(PartState.CREATE);
	}

	private void testShowPart_Bug322368_Placeholder(PartState partState) {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholder = partService.createSharedPart("partId", true);
		MPart sharedPart = (MPart) placeholder.getRef();
		sharedPart.setCurSharedRef(placeholder);

		placeholder.setToBeRendered(false);
		sharedPart.setToBeRendered(false);
		perspective.getChildren().add(placeholder);

		partService.showPart(sharedPart, partState);

		assertNotNull(perspective.getSelectedElement());
		assertEquals(placeholder, perspective.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug322368_Placeholder_ACTIVATE() {
		testShowPart_Bug322368_Placeholder(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Bug322368_Placeholder_VISIBLE() {
		testShowPart_Bug322368_Placeholder(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug322368_Placeholder_CREATE() {
		testShowPart_Bug322368_Placeholder(PartState.CREATE);
	}

	@Test
	public void testShowPart_Bug322403_A() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackA);
		perspective.setSelectedElement(partStackA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		partStackA.getChildren().add(placeholderA);
		partStackA.setSelectedElement(placeholderA);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);
		partA.setCurSharedRef(placeholderA);
		placeholderA.setRef(partA);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackB);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholderB = partService.createSharedPart("partId", true);
		MPart partB = (MPart) placeholderB.getRef();
		partB.setCurSharedRef(placeholderB);

		placeholderB.setToBeRendered(false);
		partB.setToBeRendered(false);
		partStackB.getChildren().add(placeholderB);

		partStackB.setSelectedElement(null);

		partService.showPart(partB, PartState.VISIBLE);

		assertNotNull(partStackB.getSelectedElement());
		assertEquals(placeholderB, partStackB.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug322403_B() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setAllowMultiple(true);
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackA);
		perspective.setSelectedElement(partStackA);

		MPart partA = ems.createModelElement(MPart.class);
		partStackA.getChildren().add(partA);
		partStackA.setSelectedElement(partA);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackB);

		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholderB = partService.createSharedPart("partId", true);
		MPart partB = (MPart) placeholderB.getRef();
		partB.setCurSharedRef(placeholderB);

		placeholderB.setToBeRendered(false);
		partB.setToBeRendered(false);
		partStackB.getChildren().add(placeholderB);

		partStackB.setSelectedElement(null);

		partService.showPart(partB, PartState.VISIBLE);

		assertNotNull(partStackB.getSelectedElement());
		assertEquals(placeholderB, partStackB.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug322403_C() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackA);
		perspective.setSelectedElement(partStackA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		partStackA.getChildren().add(placeholderA);
		partStackA.setSelectedElement(placeholderA);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);
		partA.setCurSharedRef(placeholderA);
		placeholderA.setRef(partA);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStackB);

		initialize();

		getEngine().createGui(window);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setToBeRendered(false);
		partStackB.getChildren().add(partB);
		partStackB.setSelectedElement(null);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(partB, PartState.VISIBLE);

		assertNotNull(partStackB.getSelectedElement());
		assertEquals(partB, partStackB.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug320578_A() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId"); //$NON-NLS-1$
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setElementId("partId"); //$NON-NLS-1$
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		initialize();
		getEngine().createGui(window);

		EPartService partService = perspectiveA.getContext().get(EPartService.class);
		partService.showPart(partA, PartState.ACTIVATE);

		assertNotNull(partA.getCurSharedRef());
	}

	@Test
	public void testShowPart_Bug320578_B() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		initialize();
		getEngine().createGui(window);

		EPartService partService = perspectiveA.getContext().get(EPartService.class);
		partService.showPart(partA, PartState.ACTIVATE);

		assertNotNull(partA.getCurSharedRef());
	}

	@Test
	public void testShowPart_Bug320578_C() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId"); //$NON-NLS-1$
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		initialize();
		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(EModelService.class);
		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(part, PartState.ACTIVATE);

		List<MPart> partsA = modelService.findElements(perspectiveA, part.getElementId(), MPart.class);
		assertEquals(1, partsA.size());
		assertEquals(part, partsA.get(0));

		List<MPlaceholder> placeholdersA = modelService.findElements(perspectiveA, part.getElementId(),
				MPlaceholder.class);
		assertEquals(1, placeholdersA.size());
		assertEquals(part.getCurSharedRef(), placeholdersA.get(0));
		assertEquals(part, placeholdersA.get(0).getRef());

		List<MPart> partsB = modelService.findElements(perspectiveB, part.getElementId(), MPart.class);
		assertEquals(0, partsB.size());

		List<MPlaceholder> placeholdersB = modelService.findElements(perspectiveB, part.getElementId(),
				MPlaceholder.class);
		assertEquals(0, placeholdersB.size());

		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();
		partService.showPart(part, PartState.ACTIVATE);

		partsA = modelService.findElements(perspectiveA, part.getElementId(), MPart.class);
		assertEquals(1, partsA.size());
		assertEquals(part, partsA.get(0));

		placeholdersA = modelService.findElements(perspectiveA, part.getElementId(), MPlaceholder.class);
		assertEquals(1, placeholdersA.size());
		assertEquals(part, placeholdersA.get(0).getRef());

		partsB = modelService.findElements(perspectiveB, part.getElementId(), MPart.class);
		assertEquals(1, partsB.size());
		assertEquals(part, partsB.get(0));

		placeholdersB = modelService.findElements(perspectiveB, part.getElementId(), MPlaceholder.class);
		assertEquals(1, placeholdersB.size());
		assertEquals(part.getCurSharedRef(), placeholdersB.get(0));
	}

	@Test
	public void testShowPart_Bug320578_D() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId"); //$NON-NLS-1$
		application.getDescriptors().add(partDescriptor);

		MPart part = ems.createModelElement(MPart.class);
		part.setElementId("partId"); //$NON-NLS-1$
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		initialize();
		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(EModelService.class);
		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(part, PartState.ACTIVATE);

		List<MPart> partsA = modelService.findElements(perspectiveA, part.getElementId(), MPart.class);
		assertEquals(1, partsA.size());
		assertEquals(part, partsA.get(0));

		List<MPlaceholder> placeholdersA = modelService.findElements(perspectiveA, part.getElementId(),MPlaceholder.class);
		assertEquals(1, placeholdersA.size());
		assertEquals(part.getCurSharedRef(), placeholdersA.get(0));
		assertEquals(part, placeholdersA.get(0).getRef());

		List<MPart> partsB = modelService.findElements(perspectiveB, part.getElementId(), MPart.class);
		assertEquals(0, partsB.size());

		List<MPlaceholder> placeholdersB = modelService.findElements(perspectiveB, part.getElementId(),
				MPlaceholder.class);
		assertEquals(0, placeholdersB.size());

		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();
		partService.showPart(part, PartState.ACTIVATE);

		partsA = modelService.findElements(perspectiveA, part.getElementId(), MPart.class);
		assertEquals(1, partsA.size());
		assertEquals(part, partsA.get(0));

		placeholdersA = modelService.findElements(perspectiveA, part.getElementId(), MPlaceholder.class);
		assertEquals(1, placeholdersA.size());
		assertEquals(part, placeholdersA.get(0).getRef());

		partsB = modelService.findElements(perspectiveB, part.getElementId(), MPart.class);
		assertEquals(1, partsB.size());
		assertEquals(part, partsB.get(0));

		placeholdersB = modelService.findElements(perspectiveB, part.getElementId(), MPlaceholder.class);
		assertEquals(1, placeholdersB.size());
		assertEquals(part.getCurSharedRef(), placeholdersB.get(0));
	}

	@Test
	public void testShowPart_Bug320578_E() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		initialize();
		getEngine().createGui(window);

		EModelService modelService = window.getContext().get(EModelService.class);
		EPartService partService = window.getContext().get(EPartService.class);
		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);
		part.setCurSharedRef(placeholder);
		partService.showPart(part, PartState.ACTIVATE);

		List<MPart> parts = modelService.findElements(perspective, part.getElementId(), MPart.class);
		assertEquals(1, parts.size());
		assertEquals(part, parts.get(0));

		List<MPlaceholder> placeholders = modelService.findElements(perspective, part.getElementId(),
				MPlaceholder.class);
		assertEquals(1, placeholders.size());
		assertEquals(placeholder, placeholders.get(0));
		assertEquals(part.getCurSharedRef(), placeholders.get(0));
		assertEquals(part, placeholders.get(0).getRef());
	}

	@Test
	public void testShowPart_Bug329310_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		perspective.getChildren().add(partSashContainer);
		perspective.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		MPart partB = ems.createModelElement(MPart.class);
		partService.showPart(partB, PartState.ACTIVATE);

		assertEquals(2, partSashContainer.getChildren().size());
		assertEquals(partA, partSashContainer.getChildren().get(0));
		assertTrue(partSashContainer.getChildren().get(1) instanceof MPartStack);
	}

	@Test
	public void testShowPart_Bug329310_02() {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		perspective.getChildren().add(partSashContainer);
		perspective.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart("partId", PartState.ACTIVATE);

		assertEquals(2, partSashContainer.getChildren().size());
		assertEquals(partA, partSashContainer.getChildren().get(0));
		assertTrue(partSashContainer.getChildren().get(1) instanceof MPartStack);
	}

	private void testShowPart_Bug331047(PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partStack.getChildren().add(placeholderB);

		initialize();
		getEngine().createGui(window);

		assertNull("The part shouldn't have been rendered", partB.getContext());
		assertEquals(partB, placeholderB.getRef());
		assertNull(partB.getCurSharedRef());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(partB, partState);
		assertNotNull("The part should have been rendered", partB.getContext());
		assertEquals(partB, placeholderB.getRef());
		assertEquals(placeholderB, partB.getCurSharedRef());
	}

	@Test
	public void testShowPart_Bug331047_CREATE() {
		testShowPart_Bug331047(PartState.CREATE);
	}

	@Test
	public void testShowPart_Bug331047_VISIBLE() {
		testShowPart_Bug331047(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug331047_ACTIVATE() {
		testShowPart_Bug331047(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Bug347837() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		partSashContainer.setToBeRendered(false);
		window.getChildren().add(partSashContainer);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setToBeRendered(false);
		placeholder.setRef(part);
		part.setCurSharedRef(placeholder);
		partSashContainer.getChildren().add(placeholder);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(part, PartState.CREATE);
		assertNotNull(part.getContext());
	}

	@Test
	public void testHidePart_PartInAnotherWindow() {
		createApplication(new String[] { "partInWindow1" }, new String[] { "partInWindow2" });

		MWindow window1 = application.getChildren().get(0);
		MWindow window2 = application.getChildren().get(1);

		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService partService1 = window1.getContext().get(EPartService.class);
		EPartService partService2 = window2.getContext().get(EPartService.class);
		MPart part1 = partService1.findPart("partInWindow1");
		MPart part2 = partService2.findPart("partInWindow2");

		assertTrue(part1.isToBeRendered());
		assertTrue(part2.isToBeRendered());

		partService1.hidePart(part2);
		assertTrue(part1.isToBeRendered());
		assertTrue(part2.isToBeRendered());

		partService2.hidePart(part1);
		assertTrue(part1.isToBeRendered());
		assertTrue(part2.isToBeRendered());

		partService1.hidePart(part1);
		assertFalse(part1.isToBeRendered());
		assertTrue(part2.isToBeRendered());

		partService2.hidePart(part2);
		assertFalse(part1.isToBeRendered());
		assertFalse(part2.isToBeRendered());
	}

	private void testHidePart_Tagged(boolean tagged) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);
		window.setSelectedElement(part);

		if (tagged) {
			part.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
		}

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(part);

		assertFalse(part.isToBeRendered());
		assertEquals(tagged ? null : window, part.getParent());
	}

	@Test
	public void testHidePart_Tagged_True() {
		testHidePart_Tagged(true);
	}

	@Test
	public void testHidePart_Tagged_False() {
		testHidePart_Tagged(false);
	}

	@Test
	public void testGetDirtyParts() {
		createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	@Test
	public void testGetDirtyParts2() {
		createApplication("partId");
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	private void testGetDirtyParts3(boolean before, boolean after) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPart saveablePart = ems.createModelElement(MPart.class);
		saveablePart.setDirty(before);
		window.getChildren().add(saveablePart);

		// setup the context
		initialize();

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		Collection<MPart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);

		if (before) {
			assertEquals(1, dirtyParts.size());
			assertEquals(saveablePart, dirtyParts.iterator().next());
		} else {
			assertEquals(0, dirtyParts.size());
		}

		saveablePart.setDirty(after);
		dirtyParts = partService.getDirtyParts();

		if (after) {
			assertEquals(1, dirtyParts.size());
			assertEquals(saveablePart, dirtyParts.iterator().next());
		} else {
			assertEquals(0, dirtyParts.size());
		}
	}

	@Test
	public void testGetDirtyParts3_TrueTrue() {
		testGetDirtyParts3(true, true);
	}

	@Test
	public void testGetDirtyParts3_TrueFalse() {
		testGetDirtyParts3(true, false);
	}

	@Test
	public void testGetDirtyParts3_FalseTrue() {
		testGetDirtyParts3(false, true);
	}

	@Test
	public void testGetDirtyParts3_FalseFalse() {
		testGetDirtyParts3(false, false);
	}

	@Test
	public void testEvent_PartActivated() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getActivated());
		assertEquals(partBack, partListener.getActivatedParts().get(0));
		assertTrue(partListener.isValid());
	}

	@Test
	public void testPartActivationTimeData_Bug461063() {
		createApplication("activePart", "nonActivePart");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart activePart = (MPart) partStack.getChildren().get(0);
		MPart nonActivePart = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(activePart);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertTrue(activePart.getTransientData().containsKey(PartServiceImpl.PART_ACTIVATION_TIME));
		assertFalse(nonActivePart.getTransientData().containsKey(PartServiceImpl.PART_ACTIVATION_TIME));

		assertTrue(Long.class.isInstance(activePart.getTransientData().get(PartServiceImpl.PART_ACTIVATION_TIME)));

		partService.activate(nonActivePart);

		assertTrue(activePart.getTransientData().containsKey(PartServiceImpl.PART_ACTIVATION_TIME));
		assertTrue(nonActivePart.getTransientData().containsKey(PartServiceImpl.PART_ACTIVATION_TIME));
	}

	@Test
	public void testEvent_PartActivated2() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		PartListener partListener = new PartListener();
		partServiceA.addPartListener(partListener);

		partServiceB.activate(partB);

		assertEquals(0, partListener.getActivated());
		assertEquals(0, partListener.getActivatedParts().size());
		assertTrue(partListener.isValid());

		partListener.clear();
		partServiceB.activate(partA);

		assertEquals(0, partListener.getActivated());
		assertEquals(0, partListener.getActivatedParts().size());
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartDeactivated() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getDeactivated());
		assertEquals(partFront, partListener.getDeactivatedParts().get(0));
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartDeactivated2() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		PartListener partListener = new PartListener();
		partServiceA.addPartListener(partListener);

		partServiceB.activate(partB);

		assertEquals(0, partListener.getDeactivated());
		assertEquals(0, partListener.getDeactivatedParts().size());
		assertTrue(partListener.isValid());

		partListener.clear();
		partServiceB.activate(partA);

		assertEquals(0, partListener.getDeactivated());
		assertEquals(0, partListener.getDeactivatedParts().size());
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartHidden() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getHidden());
		assertEquals(partFront, partListener.getHiddenParts().get(0));
		assertTrue(partListener.isValid());

		partListener.clear();
		partService.activate(partFront);

		assertEquals(1, partListener.getHidden());
		assertEquals(partBack, partListener.getHiddenParts().get(0));
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartHidden2() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		PartListener partListener = new PartListener();
		partServiceA.addPartListener(partListener);

		partServiceB.activate(partB);

		assertEquals(0, partListener.getHidden());
		assertEquals(0, partListener.getHiddenParts().size());
		assertTrue(partListener.isValid());

		partListener.clear();
		partServiceB.activate(partA);

		assertEquals(0, partListener.getHidden());
		assertEquals(0, partListener.getHiddenParts().size());
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartVisible() {
		createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = (MPart) partStack.getChildren().get(0);
		MPart partBack = (MPart) partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getVisible());
		assertEquals(partBack, partListener.getVisibleParts().get(0));
		assertTrue(partListener.isValid());

		partListener.clear();
		partService.activate(partFront);

		assertEquals(1, partListener.getVisible());
		assertEquals(partFront, partListener.getVisibleParts().get(0));
		assertTrue(partListener.isValid());
	}

	@Test
	public void testEvent_PartVisible2() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = windowA.getContext().get(EPartService.class);
		EPartService partServiceB = windowB.getContext().get(EPartService.class);

		PartListener partListener = new PartListener();
		partServiceA.addPartListener(partListener);

		partServiceB.activate(partB);

		assertEquals(0, partListener.getVisible());
		assertEquals(0, partListener.getVisibleParts().size());
		assertTrue(partListener.isValid());

		partListener.clear();
		partServiceB.activate(partA);

		assertEquals(0, partListener.getVisible());
		assertEquals(0, partListener.getVisibleParts().size());
		assertTrue(partListener.isValid());
	}

	private void testSavePart(final Save returnValue, boolean confirm, boolean beforeDirty, boolean afterDirty,
			boolean success, boolean saveCalled, boolean throwException) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPart saveablePart = ems.createModelElement(MPart.class);
		saveablePart.setDirty(beforeDirty);
		saveablePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize();

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		window.getContext().set(ISaveHandler.class.getName(), new PartServiceSaveHandler() {
			@Override
			public Save[] promptToSave(Collection<MPart> saveablePart) {
				return null;
			}

			@Override
			public Save promptToSave(MPart saveablePart) {
				return returnValue;
			}
		});

		EPartService partService = window.getContext().get(EPartService.class);
		if (beforeDirty) {
			assertEquals(success, partService.savePart(saveablePart, confirm));
		} else {
			assertTrue("The part is not dirty, the save operation should complete successfully",
					partService.savePart(saveablePart, confirm));
		}

		assertEquals(afterDirty, saveablePart.isDirty());
		assertEquals(saveCalled, editor.wasSaveCalled());
	}

	private void testSavePart(Save returnValue, boolean confirm, boolean beforeDirty, boolean throwException) {
		switch (returnValue) {
		case YES:
			if (throwException) {
				if (beforeDirty) {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, beforeDirty, false, true, throwException);
				} else {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, beforeDirty, true, false, throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, false, true, true, throwException);
				} else {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, false, true, true, throwException);
				}
			} else if (confirm) {
				testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, false, true, false, throwException);
			} else {
				testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty, false, true, false, throwException);
			}
			break;
		case NO:
			if (throwException) {
				if (beforeDirty) {
					if (confirm) {
						testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, beforeDirty, true, false,
								throwException);
					} else {
						testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, beforeDirty, false, true,
								throwException);
					}
				} else {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, beforeDirty, true, false, throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, true, true, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, false, true, true, throwException);
				}
			} else if (confirm) {
				testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, false, true, false, throwException);
			} else {
				testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty, false, true, false, throwException);
			}
			break;
		case CANCEL:
			if (throwException) {
				if (beforeDirty) {
					if (confirm) {
						testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, beforeDirty, false, false,
								throwException);
					} else {
						testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, beforeDirty, false, true,
								throwException);
					}
				} else {
					testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, beforeDirty, true, false,
							throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, true, false, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, false, true, true, throwException);
				}
			} else if (confirm) {
				testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, false, true, false, throwException);
			} else {
				testSavePart(ISaveHandler.Save.CANCEL, confirm, beforeDirty, false, true, false, throwException);
			}
			break;
		default:
			fail("Unknown expected return value set: " + returnValue);
		}
	}

	@Test
	public void testSavePart_YesTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.YES, true, true, true);
	}

	@Test
	public void testSavePart_YesTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.YES, true, true, false);
	}

	@Test
	public void testSavePart_YesTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.YES, true, false, true);
	}

	@Test
	public void testSavePart_YesTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.YES, true, false, false);
	}

	@Test
	public void testSavePart_YesFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.YES, false, true, true);
	}

	@Test
	public void testSavePart_YesFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.YES, false, true, false);
	}

	@Test
	public void testSavePart_YesFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.YES, false, false, true);
	}

	@Test
	public void testSavePart_YesFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.YES, false, false, false);
	}

	@Test
	public void testSavePart_NoTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.NO, true, true, true);
	}

	@Test
	public void testSavePart_NoTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.NO, true, true, false);
	}

	@Test
	public void testSavePart_NoTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.NO, true, false, true);
	}

	@Test
	public void testSavePart_NoTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.NO, true, false, false);
	}

	@Test
	public void testSavePart_NoFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.NO, false, true, true);
	}

	@Test
	public void testSavePart_NoFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.NO, false, true, false);
	}

	@Test
	public void testSavePart_NoFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.NO, false, false, true);
	}

	@Test
	public void testSavePart_NoFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.NO, false, false, false);
	}

	@Test
	public void testSavePart_CancelTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, true, true, true);
	}

	@Test
	public void testSavePart_CancelTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, true, true, false);
	}

	@Test
	public void testSavePart_CancelTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, true, false, true);
	}

	@Test
	public void testSavePart_CancelTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, true, false, false);
	}

	@Test
	public void testSavePart_CancelFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, false, true, true);
	}

	@Test
	public void testSavePart_CancelFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, false, true, false);
	}

	@Test
	public void testSavePart_CancelFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, false, false, true);
	}

	@Test
	public void testSavePart_CancelFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, false, false, false);
	}

	private void testSavePart_NoHandler(boolean beforeDirty, boolean throwException, boolean confirm) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPart saveablePart = ems.createModelElement(MPart.class);
		saveablePart.setDirty(beforeDirty);
		saveablePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize();

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		// no handlers
		applicationContext.set(ISaveHandler.class.getName(), null);

		EPartService partService = window.getContext().get(EPartService.class);
		if (beforeDirty) {
			assertEquals(!throwException, partService.savePart(saveablePart, confirm));
		} else {
			assertTrue("The part is not dirty, the save operation should have complete successfully",
					partService.savePart(saveablePart, confirm));
		}

		assertEquals(beforeDirty && throwException, saveablePart.isDirty());
		assertEquals(beforeDirty, editor.wasSaveCalled());
	}

	@Test
	public void testSavePart_NoHandler_TTT() {
		testSavePart_NoHandler(true, true, true);
	}

	@Test
	public void testSavePart_NoHandler_TTF() {
		testSavePart_NoHandler(true, true, false);
	}

	@Test
	public void testSavePart_NoHandler_TFT() {
		testSavePart_NoHandler(true, false, true);
	}

	@Test
	public void testSavePart_NoHandler_TFF() {
		testSavePart_NoHandler(true, false, false);
	}

	@Test
	public void testSavePart_NoHandler_FTT() {
		testSavePart_NoHandler(false, true, true);
	}

	@Test
	public void testSavePart_NoHandler_FTF() {
		testSavePart_NoHandler(false, true, false);
	}

	@Test
	public void testSavePart_NoHandler_FFT() {
		testSavePart_NoHandler(false, false, true);
	}

	@Test
	public void testSavePart_NoHandler_FFF() {
		testSavePart_NoHandler(false, false, false);
	}

	private MPart createSaveablePart(MElementContainer<MWindowElement> container, boolean beforeDirty) {
		MPart saveablePart = ems.createModelElement(MPart.class);
		saveablePart.setDirty(beforeDirty);
		saveablePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		container.getChildren().add(saveablePart);
		return saveablePart;
	}

	private Save prompt(Save[] candidates, MPart partToTest, MPart part) {
		return partToTest == part ? candidates[0] : candidates[1];
	}

	private void testSaveAll(final Save[] returnValues, boolean confirm, boolean[] beforeDirty, boolean[] afterDirty,
			boolean success, boolean[] saveCalled, boolean[] throwException) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		final MPart saveablePart = createSaveablePart(window, beforeDirty[0]);
		final MPart saveablePart2 = createSaveablePart(window, beforeDirty[1]);

		// setup the context
		initialize();

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException[0]);

		ClientEditor editor2 = (ClientEditor) saveablePart2.getObject();
		editor2.setThrowException(throwException[1]);

		window.getContext().set(ISaveHandler.class.getName(), new PartServiceSaveHandler() {
			@Override
			public Save[] promptToSave(Collection<MPart> saveableParts) {
				int index = 0;
				Save[] prompt = new Save[saveableParts.size()];
				Iterator<MPart> it = saveableParts.iterator();
				while (it.hasNext()) {
					prompt[index] = prompt(returnValues, it.next(), saveablePart);
					index++;
				}
				return prompt;
			}

			@Override
			public Save promptToSave(MPart saveablePart) {
				return null;
			}
		});

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(success, partService.saveAll(confirm));

		assertEquals(afterDirty[0], saveablePart.isDirty());
		assertEquals(saveCalled[0], editor.wasSaveCalled());

		assertEquals(afterDirty[1], saveablePart2.isDirty());
		assertEquals(saveCalled[1], editor2.wasSaveCalled());
	}

	private boolean hasCancel(Save[] returnValues, boolean[] beforeDirty) {
		for (int i = 0; i < returnValues.length; i++) {
			if (returnValues[i] == Save.CANCEL && beforeDirty[i]) {
				return true;
			}
		}
		return false;
	}

	private boolean isSuccessful(Save[] returnValues, boolean confirm, boolean[] beforeDirty,
			boolean[] throwException) {
		if (confirm) {
			if (returnValues[0] == Save.YES) {
				if (returnValues[1] == Save.YES) {
					if (beforeDirty[0]) {
						if (beforeDirty[1]) {
							return !throwException[0] && !throwException[1];
						}
						return !throwException[0];
					} else if (beforeDirty[1]) {
						return !throwException[1];
					}
					return true;
				} else if (beforeDirty[0]) {
					return !throwException[0];
				}
				return true;
			} else if (returnValues[1] == Save.YES) {
				if (beforeDirty[1]) {
					return !throwException[1];
				}
			}
			return true;
		}
		return isSuccessful(beforeDirty, throwException);
	}

	private boolean isSuccessful(boolean[] beforeDirty, boolean[] throwException) {
		if (beforeDirty[0]) {
			if (beforeDirty[1]) {
				return !throwException[0] && !throwException[1];
			}
			return !throwException[0];
		} else if (beforeDirty[1]) {
			return !throwException[1];
		}
		return true;
	}

	private boolean[] afterDirty(Save[] returnValues, boolean confirm, boolean[] beforeDirty,
			boolean[] throwException) {
		if (confirm) {
			if (returnValues[0] == Save.YES) {
				if (returnValues[1] == Save.YES) {
					if (beforeDirty[0]) {
						return new boolean[] { throwException[0],
								beforeDirty[1] ? throwException[0] || throwException[1] : false };
					}
					return new boolean[] { beforeDirty[0], beforeDirty[1] ? throwException[1] : false };
				}
				return new boolean[] { beforeDirty[0] ? throwException[0] : false, beforeDirty[1] };
			} else if (returnValues[1] == Save.YES) {
				return new boolean[] { beforeDirty[0], beforeDirty[1] ? throwException[1] : false };
			}
			return beforeDirty;
		}
		return afterDirty(beforeDirty, throwException);
	}

	private boolean[] afterDirty(boolean[] beforeDirty, boolean[] throwException) {
		if (beforeDirty[0]) {
			if (beforeDirty[1]) {
				return new boolean[] { throwException[0], throwException[0] || throwException[1] };
			}
			return new boolean[] { throwException[0], false };
		} else if (beforeDirty[1]) {
			return new boolean[] { false, throwException[1] };
		}
		return new boolean[] { false, false };
	}

	private boolean[] saveCalled(Save[] returnValues, boolean confirm, boolean[] beforeDirty,
			boolean[] throwException) {
		if (confirm) {
			if (returnValues[0] == Save.YES) {
				if (returnValues[1] == Save.YES) {
					if (beforeDirty[0]) {
						return new boolean[] { true, !throwException[0] && beforeDirty[1] };
					}
					return beforeDirty;
				}
				return new boolean[] { beforeDirty[0], false };
			} else if (returnValues[1] == Save.YES) {
				return new boolean[] { false, beforeDirty[1] };
			}
			return new boolean[] { false, false };
		}
		return saveCalled(beforeDirty, throwException);
	}

	private boolean[] saveCalled(boolean[] beforeDirty, boolean[] throwException) {
		return new boolean[] { beforeDirty[0], beforeDirty[0] ? !throwException[0] && beforeDirty[1] : beforeDirty[1] };
	}

	private void testSaveAll(Save[] returnValues, boolean confirm, boolean[] beforeDirty, boolean[] throwException) {
		if (hasCancel(returnValues, beforeDirty) && confirm) {
			testSaveAll(returnValues, confirm, beforeDirty, beforeDirty, false, new boolean[] { false, false },
					throwException);
		} else {
			testSaveAll(returnValues, confirm, beforeDirty,
					afterDirty(returnValues, confirm, beforeDirty, throwException),
					isSuccessful(returnValues, confirm, beforeDirty, throwException),
					saveCalled(returnValues, confirm, beforeDirty, throwException), throwException);
		}
	}

	@Test
	public void testSaveAll_YY_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YY_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YY_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YY_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YY_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YY_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YY_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YN_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YN_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YN_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YN_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YN_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YN_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YN_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YC_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YC_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YC_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_YC_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_YC_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_YC_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_YC_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NY_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test

	public void testSaveAll_NY_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NY_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NY_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NY_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NY_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NY_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NY_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NN_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, true }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, true }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, false }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NN_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NN_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NN_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NN_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NN_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NN_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NC_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NC_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NC_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NC_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NC_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NC_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NC_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CY_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CY_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CY_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CY_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CY_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CY_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CY_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CN_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CN_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CN_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CN_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CN_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CN_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CN_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CC_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CC_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CC_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_CC_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_CC_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_CC_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_CC_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	private void testSaveAll_NoHandler(boolean beforeDirty, boolean throwException, boolean confirm) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		MPart saveablePart = ems.createModelElement(MPart.class);
		saveablePart.setDirty(beforeDirty);
		saveablePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize();

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		// no handlers
		applicationContext.set(ISaveHandler.class.getName(), null);

		EPartService partService = window.getContext().get(EPartService.class);
		if (beforeDirty) {
			assertEquals(!throwException, partService.saveAll(confirm));
		} else {
			assertTrue("The part is not dirty, the save operation should have complete successfully",
					partService.saveAll(confirm));
		}

		assertEquals(beforeDirty && throwException, saveablePart.isDirty());
		assertEquals(beforeDirty, editor.wasSaveCalled());
	}

	@Test
	public void testSaveAll_NoHandler_TTT() {
		testSaveAll_NoHandler(true, true, true);
	}

	@Test
	public void testSaveAll_NoHandler_TTF() {
		testSaveAll_NoHandler(true, true, false);
	}

	@Test
	public void testSaveAll_NoHandler_TFT() {
		testSaveAll_NoHandler(true, false, true);
	}

	@Test
	public void testSaveAll_NoHandler_TFF() {
		testSaveAll_NoHandler(true, false, false);
	}

	@Test
	public void testSaveAll_NoHandler_FTT() {
		testSaveAll_NoHandler(false, true, true);
	}

	@Test
	public void testSaveAll_NoHandler_FTF() {
		testSaveAll_NoHandler(false, true, false);
	}

	@Test
	public void testSaveAll_NoHandler_FFT() {
		testSaveAll_NoHandler(false, false, true);
	}

	@Test
	public void testSaveAll_NoHandler_FFF() {
		testSaveAll_NoHandler(false, false, false);
	}

	private void testSaveAll_NoHandlers(boolean confirm, boolean[] beforeDirty, boolean[] afterDirty, boolean success,
			boolean[] saveCalled, boolean[] throwException) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		final MPart saveablePart = createSaveablePart(window, beforeDirty[0]);
		final MPart saveablePart2 = createSaveablePart(window, beforeDirty[1]);

		// setup the context
		initialize();

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException[0]);

		ClientEditor editor2 = (ClientEditor) saveablePart2.getObject();
		editor2.setThrowException(throwException[1]);

		window.getContext().set(ISaveHandler.class.getName(), null);

		EPartService partService = window.getContext().get(EPartService.class);
		assertEquals(success, partService.saveAll(confirm));

		assertEquals(afterDirty[0], saveablePart.isDirty());
		assertEquals(saveCalled[0], editor.wasSaveCalled());

		assertEquals(afterDirty[1], saveablePart2.isDirty());
		assertEquals(saveCalled[1], editor2.wasSaveCalled());
	}

	private void testSaveAll_NoHandlers(boolean confirm, boolean[] beforeDirty, boolean[] throwException) {
		testSaveAll_NoHandlers(confirm, beforeDirty, afterDirty(beforeDirty, throwException),
				isSuccessful(beforeDirty, throwException), saveCalled(beforeDirty, throwException), throwException);
	}

	@Test
	public void testSaveAll_NoHandlers_T_TT_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TT_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TT_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TT_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TF_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TF_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TF_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_TF_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FT_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FT_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FT_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FT_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FF_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FF_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FF_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_T_FF_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TT_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TT_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TT_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TT_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TF_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TF_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TF_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_TF_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FT_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FT_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FT_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FT_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true }, new boolean[] { false, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FF_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false }, new boolean[] { true, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FF_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false }, new boolean[] { true, false });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FF_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false }, new boolean[] { false, true });
	}

	@Test
	public void testSaveAll_NoHandlers_F_FF_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false }, new boolean[] { false, false });
	}

	@Test
	public void testSwitchWindows() {
		// create an application with two windows
		MWindow window1 = ems.createModelElement(MWindow.class);
		MWindow window2 = ems.createModelElement(MWindow.class);
		application.getChildren().add(window1);
		application.getChildren().add(window2);
		application.setSelectedElement(window1);

		// place a part in the first window
		MPart part = ems.createModelElement(MPart.class);
		window1.getChildren().add(part);
		window1.setSelectedElement(part);

		// setup the context
		initialize();

		// render the windows
		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService windowService1 = window1.getContext().get(EPartService.class);
		EPartService windowService2 = window2.getContext().get(EPartService.class);

		assertNotNull(windowService1);
		assertNotNull(windowService2);

		assertNotNull("The first part is active in the first window", windowService1.getActivePart());
		assertNull("There should be nothing active in the second window", windowService2.getActivePart());

		// activate the part
		windowService1.activate(part);

		assertEquals("The part should have been activated", part, windowService1.getActivePart());
		assertNull("The second window has no parts, this should be null", windowService2.getActivePart());

		// now move the part over from the first window to the second window
		window2.getChildren().add(part);
		// activate the part
		windowService2.activate(part);

		assertEquals("No parts in this window, this should be null", null, windowService1.getActivePart());
		assertEquals("We activated it just now, this should be active", part, windowService2.getActivePart());
	}

	@Test
	public void testApplicationContextHasActivePart() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		// setup the context
		initialize();

		// render the windows
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.activate(partA);

		Object o = applicationContext.get(IServiceConstants.ACTIVE_PART);
		assertEquals(partA, o);

		partService.activate(partB);

		o = applicationContext.get(IServiceConstants.ACTIVE_PART);
		assertEquals(partB, o);
	}

	private void testShowPart_Bug307747(PartState partState) {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setCategory("category");
		application.getDescriptors().add(partDescriptor);

		final MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		// create a stack
		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.setElementId("category");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		// setup the context
		initialize();
		// render the window
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		MPart partB = partService.showPart("partId", partState);

		// showPart should instantiate the part
		assertNotNull("The part should have been rendered", partB.getContext());
	}

	@Test
	public void testShowPart_Bug307747_CREATE() {
		testShowPart_Bug307747(PartState.CREATE);
	}

	@Test
	public void testShowPart_Bug307747_VISIBLE() {
		testShowPart_Bug307747(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug307747_ACTIVATE() {
		testShowPart_Bug307747(PartState.ACTIVATE);
	}

	/**
	 * Test to ensure that we can handle the showing of a part that's under a
	 * container with a selected element that's invalid.
	 */
	private void testShowPart_Bug328078(PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		// remove the part to replicate the problem in bug 328078
		partStack.getChildren().remove(partA);
		// try to show another part in the stack
		partService.showPart(partB, partState);
		assertEquals(partB, partStack.getSelectedElement());
	}

	@Test
	public void testShowPart_Bug328078_CREATE() {
		testShowPart_Bug328078(PartState.CREATE);
	}

	@Test
	public void testShowPart_Bug328078_VISIBLE() {
		testShowPart_Bug328078(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug328078_ACTIVATE() {
		testShowPart_Bug328078(PartState.ACTIVATE);
	}

	@Test
	public void testShowPart_Bug370026_CREATE() {
		testShowPart_Bug370026(PartState.CREATE);
	}

	@Test
	public void testShowPart_Bug370026_VISIBLE() {
		testShowPart_Bug370026(PartState.VISIBLE);
	}

	@Test
	public void testShowPart_Bug370026_ACTIVATE() {
		testShowPart_Bug370026(PartState.ACTIVATE);
	}

	private void testShowPart_Bug370026(PartState partState) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SimpleView");
		window.getChildren().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.TargetedView");
		window.getChildren().add(partB);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPart partC = ems.createModelElement(MPart.class);
		partC.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SimpleView");
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		// setup the context
		initialize();

		applicationContext.set(PartState.class, partState);
		applicationContext.set(TargetedView.TARGET_MARKER, partC);

		// render the window
		getEngine().createGui(window);

		TargetedView view = (TargetedView) partB.getObject();
		assertTrue(view.passed);
	}

	private void testHidePart_Bug325148(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setToBeRendered(false);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);
		placeholder.setToBeRendered(false);
		perspective.getChildren().add(placeholder);

		// setup the context
		initialize();
		// render the window
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(part, force);
		assertEquals(force, !perspective.getChildren().contains(placeholder));
	}

	@Test
	public void testHidePart_Bug325148_True() {
		testHidePart_Bug325148(true);
	}

	@Test
	public void testHidePart_Bug325148_False() {
		testHidePart_Bug325148(false);
	}

	private void testHidePart_Bug325148_Unrendered(boolean force) {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("partId");
		partDescriptor.setCategory("category");
		application.getDescriptors().add(partDescriptor);

		final MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		// create a stack
		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.setElementId("category");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setToBeRendered(false);
		stack.getChildren().add(partB);

		// setup the context
		initialize();
		// render the window
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(partB, force);
	}

	@Test
	public void testHidePart_Bug325148_Unrendered_True() {
		testHidePart_Bug325148_Unrendered(true);
	}

	@Test
	public void testHidePart_Bug325148_Unrendered_False() {
		testHidePart_Bug325148_Unrendered(false);
	}

	@Test
	public void testHidePart_Bug327026() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setToBeRendered(false);
		stack.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		window.getContext().get(EPartService.class).hidePart(partA, true);
		// this is perhaps questionable, as to whether it should be null or
		// partB, but it should certainly not be partA anyway
		assertNull(stack.getSelectedElement());
	}

	private void testHidePart_Bug327044(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart part = ems.createModelElement(MPart.class);
		stack.getChildren().add(part);
		stack.setSelectedElement(part);

		initialize();
		getEngine().createGui(window);

		window.getContext().get(EPartService.class).hidePart(part, force);
		assertNull(stack.getSelectedElement());
	}

	@Test
	public void testHidePart_Bug327044_True() {
		testHidePart_Bug327044(true);
	}

	@Test
	public void testHidePart_Bug327044_False() {
		testHidePart_Bug327044(false);
	}

	private void testHidePart_Bug327765(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setToBeRendered(false);
		placeholderB.setRef(part);
		perspectiveB.getChildren().add(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(part);
		assertNotNull(part.getContext());

		partService.switchPerspective(perspectiveB);
		assertNotNull(part.getContext());

		partService.hidePart(part, force);
		assertNotNull(part.getContext());
	}

	@Test
	public void testHidePart_Bug327765_True() {
		testHidePart_Bug327765(true);
	}

	@Test
	public void testHidePart_Bug327765_False() {
		testHidePart_Bug327765(false);
	}

	private void testHidePart_Bug327917(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(partStackA);
		perspectiveA.setSelectedElement(partStackA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		partStackA.getChildren().add(placeholderA);
		partStackA.setSelectedElement(placeholderA);

		MPlaceholder placeholderB1 = ems.createModelElement(MPlaceholder.class);
		placeholderB1.setRef(partB);
		partB.setCurSharedRef(placeholderB1);
		partStackA.getChildren().add(placeholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStackB);
		perspectiveB.setSelectedElement(partStackB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		partC.setCurSharedRef(placeholderC);
		placeholderC.setRef(partC);
		partStackB.getChildren().add(placeholderC);
		partStackB.setSelectedElement(placeholderC);

		MPlaceholder placeholderB2 = ems.createModelElement(MPlaceholder.class);
		placeholderB2.setRef(partB);
		partStackB.getChildren().add(placeholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.switchPerspective(perspectiveB);
		partService.activate(partB);
		partService.switchPerspective(perspectiveA);
		assertEquals(partA, partService.getActivePart());

		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partC);
		assertEquals(partC, partService.getActivePart());

		partService.switchPerspective(perspectiveA);
		assertEquals(partA, partService.getActivePart());

		partService.hidePart(partB, force);
		assertFalse(placeholderB1.isToBeRendered());
	}

	@Test
	public void testHidePart_Bug327917_True() {
		testHidePart_Bug327917(true);
	}

	@Test
	public void testHidePart_Bug327917_False() {
		testHidePart_Bug327917(false);
	}

	private void testHidePart_Bug327964(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder areaPlaceholder = ems.createModelElement(MPlaceholder.class);
		areaPlaceholder.setRef(area);
		part.setCurSharedRef(areaPlaceholder);
		perspective.getChildren().add(areaPlaceholder);
		perspective.setSelectedElement(areaPlaceholder);

		MPlaceholder partPlaceholder = ems.createModelElement(MPlaceholder.class);
		partPlaceholder.setRef(part);
		part.setCurSharedRef(partPlaceholder);
		area.getChildren().add(partPlaceholder);
		area.setSelectedElement(partPlaceholder);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(part, force);
		assertFalse(partPlaceholder.isToBeRendered());
	}

	@Test
	public void testHidePart_Bug327964_True() {
		testHidePart_Bug327964(true);
	}

	@Test
	public void testHidePart_Bug327964_False() {
		testHidePart_Bug327964(false);
	}

	private void testHidePart_Bug332163(boolean force) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective1 = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective1);
		perspectiveStack.setSelectedElement(perspective1);

		MPlaceholder partPlaceholderA1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA1.setRef(part);
		part.setCurSharedRef(partPlaceholderA1);
		perspective1.getChildren().add(partPlaceholderA1);
		perspective1.setSelectedElement(partPlaceholderA1);

		MPerspective perspective2 = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective2);

		MPlaceholder partPlaceholder2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholder2.setRef(part);
		perspective2.getChildren().add(partPlaceholder2);
		perspective2.setSelectedElement(partPlaceholder2);

		initialize();
		getEngine().createGui(window);

		IEclipseContext perspectiveContext1 = perspective1.getContext();
		IEclipseContext partContext = part.getContext();

		assertEquals(perspectiveContext1, partContext.getParent());
		assertEquals(partContext, perspectiveContext1.getActiveChild());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(perspective2);

		IEclipseContext perspectiveContext2 = perspective2.getContext();

		assertEquals(partContext, perspectiveContext1.getActiveChild());
		assertEquals(perspectiveContext2, partContext.getParent());
		assertEquals(partContext, perspectiveContext2.getActiveChild());

		partService.hidePart(part, force);

		assertEquals(perspectiveContext1, partContext.getParent());
		assertEquals(partContext, perspectiveContext1.getActiveChild());
		assertNull("perspective2 doesn't have any parts, it should not have an active child context",
				perspectiveContext2.getActiveChild());
	}

	@Test
	public void testHidePart_Bug332163_True() {
		testHidePart_Bug332163(true);
	}

	@Test
	public void testHidePart_Bug332163_False() {
		testHidePart_Bug332163(false);
	}

	@Test
	public void testHidePart_ActivationHistory01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackB);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB1 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB1);
		stackB.setSelectedElement(partB1);

		MPart partB2 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB2);

		MPart partB3 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB3);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB2);
		partService.activate(partB3);
		partService.activate(partA);

		partService.hidePart(partB3);
		assertEquals(partB2, stackB.getSelectedElement());
	}

	@Test
	public void testHidePart_ActivationHistory02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackB);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);
		partService.activate(partA);

		partService.hidePart(partA);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackB);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(partA);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(partA);
		assertEquals(partB, stack.getSelectedElement());
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertNull(stack.getSelectedElement());
		assertNull(partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory05() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = ems.createModelElement(MPart.class);
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stack.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);

		partService.hidePart(partA);
		assertEquals(partB, stack.getSelectedElement());
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertNull(stack.getSelectedElement());
		assertNull(partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory06() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stack);
		perspective.setSelectedElement(stack);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		partA.setCurSharedRef(placeholderA);
		placeholderA.setRef(partA);
		stack.getChildren().add(placeholderA);
		stack.setSelectedElement(placeholderA);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		partB.setCurSharedRef(placeholderB);
		placeholderB.setRef(partB);
		stack.getChildren().add(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);

		partService.hidePart(partA);
		assertEquals(placeholderB, stack.getSelectedElement());
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertNull(stack.getSelectedElement());
		assertNull(partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory07() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stack);
		perspective.setSelectedElement(stack);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		partA.setCurSharedRef(placeholderA);
		placeholderA.setRef(partA);
		stack.getChildren().add(placeholderA);
		stack.setSelectedElement(placeholderA);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		partB.setCurSharedRef(placeholderB);
		placeholderB.setRef(partB);
		stack.getChildren().add(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);

		partService.hidePart(partA);
		assertEquals(placeholderB, stack.getSelectedElement());
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertNull(stack.getSelectedElement());
		assertNull(partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory08() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackA);
		perspective.setSelectedElement(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPlaceholder areaPlaceholder = ems.createModelElement(MPlaceholder.class);
		areaPlaceholder.setRef(area);
		area.setCurSharedRef(areaPlaceholder);
		perspective.getChildren().add(areaPlaceholder);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		area.getChildren().add(stackB);
		area.setSelectedElement(stackB);

		MPart partB1 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB1);
		stackB.setSelectedElement(partB1);

		MPart partB2 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB2);

		MPart partB3 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB3);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB1);
		partService.activate(partB2);
		partService.activate(partA);
		partService.activate(partB3);

		partService.hidePart(partB3);
		assertEquals(partB2, stackB.getSelectedElement());
		assertEquals(partB2, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory09() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackA);
		perspective.setSelectedElement(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPlaceholder areaPlaceholder = ems.createModelElement(MPlaceholder.class);
		areaPlaceholder.setRef(area);
		area.setCurSharedRef(areaPlaceholder);
		perspective.getChildren().add(areaPlaceholder);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		area.getChildren().add(stackB);
		area.setSelectedElement(stackB);

		MPartStack stackC = ems.createModelElement(MPartStack.class);
		area.getChildren().add(stackC);

		MPart partB = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		stackC.getChildren().add(partC);
		stackC.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);
		partService.activate(partB);

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory10() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackB);

		MPartStack stackC = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackC);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		stackC.getChildren().add(partC);
		stackC.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);

		partService.hidePart(partA);
		MPart activePart = partService.getActivePart();
		if (activePart == partB) {
			partService.hidePart(partB);
			assertEquals(partC, partService.getActivePart());
		} else if (activePart == partC) {
			partService.hidePart(partC);
			assertEquals(partB, partService.getActivePart());
		} else if (activePart == partA) {
			fail("Part A should have been deactivated");
		} else {
			fail("Another part should have been activated");
		}
	}

	@Test
	public void testHidePart_ActivationHistory11() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackA);
		perspective.setSelectedElement(stackA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		stackA.getChildren().add(placeholderA);
		stackA.setSelectedElement(placeholderA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		stackB.getChildren().add(placeholderB);
		stackB.setSelectedElement(placeholderB);

		MPartStack stackC = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackC);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		stackC.getChildren().add(placeholderC);
		stackC.setSelectedElement(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);

		partService.hidePart(partA);
		MPart activePart = partService.getActivePart();
		if (activePart == partB) {
			partService.hidePart(partB);
			assertEquals(partC, partService.getActivePart());
		} else if (activePart == partC) {
			partService.hidePart(partC);
			assertEquals(partB, partService.getActivePart());
		} else if (activePart == partA) {
			fail("Part A should have been deactivated");
		} else {
			fail("Another part should have been activated");
		}
	}

	@Test
	public void testHidePart_ActivationHistory12() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackA);
		perspective.setSelectedElement(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPlaceholder areaPlaceholder = ems.createModelElement(MPlaceholder.class);
		areaPlaceholder.setRef(area);
		area.setCurSharedRef(areaPlaceholder);
		perspective.getChildren().add(areaPlaceholder);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		area.getChildren().add(stackB);
		area.setSelectedElement(stackB);

		MPart partB1 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB1);
		stackB.setSelectedElement(partB1);

		MPart partB2 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB1);
		partService.activate(partB2);

		partService.hidePart(partB2);
		assertEquals(partB1, stackB.getSelectedElement());
		assertEquals(partB1, partService.getActivePart());

		partService.hidePart(partB1);
		assertNull(stackB.getSelectedElement());
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory13() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPart partB2 = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB2);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(stackA);
		perspective.setSelectedElement(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPlaceholder areaPlaceholder = ems.createModelElement(MPlaceholder.class);
		areaPlaceholder.setRef(area);
		area.setCurSharedRef(areaPlaceholder);
		perspective.getChildren().add(areaPlaceholder);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		area.getChildren().add(stackB);
		area.setSelectedElement(stackB);

		MPart partB1 = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB1);
		stackB.setSelectedElement(partB1);

		MPlaceholder placeholderPartB2 = ems.createModelElement(MPlaceholder.class);
		placeholderPartB2.setRef(partB2);
		partB2.setCurSharedRef(placeholderPartB2);
		stackB.getChildren().add(placeholderPartB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB1);
		partService.activate(partB2);

		partService.hidePart(partB2);
		assertEquals(partB1, stackB.getSelectedElement());
		assertEquals(partB1, partService.getActivePart());

		partService.hidePart(partB1);
		assertNull(stackB.getSelectedElement());
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory14() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		// one perspective with a shared part C
		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderC1 = ems.createModelElement(MPlaceholder.class);
		placeholderC1.setRef(partC);
		partC.setCurSharedRef(placeholderC1);
		perspectiveA.getChildren().add(placeholderC1);
		perspectiveA.setSelectedElement(placeholderC1);

		// second perspective with three shared parts
		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		partA.setCurSharedRef(placeholderA2);
		perspectiveB.getChildren().add(placeholderA2);
		perspectiveB.setSelectedElement(placeholderA2);

		MPlaceholder placeholderB2 = ems.createModelElement(MPlaceholder.class);
		placeholderB2.setRef(partB);
		partB.setCurSharedRef(placeholderB2);
		perspectiveB.getChildren().add(placeholderB2);

		MPlaceholder placeholderC2 = ems.createModelElement(MPlaceholder.class);
		placeholderC2.setRef(partC);
		perspectiveB.getChildren().add(placeholderC2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partC);

		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();
		partService.activate(partA);
		partService.activate(partB);

		perspectiveStack.setSelectedElement(perspectiveA);
		perspectiveA.getContext().activate();
		partService.activate(partC);
		partService.hidePart(partC);

		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();
		partService.hidePart(partB);

		assertEquals(partC, partService.getActivePart());
	}

	/**
	 * Tests that a perspective will automatically select another part as the
	 * active part after the originally active part that's being shown across
	 * multiple perspectives has been removed.
	 */
	@Test
	public void testHidePart_ActivationHistory15() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPart sharedPart = ems.createModelElement(MPart.class);
		window.getSharedElements().add(sharedPart);
		area.getChildren().add(sharedPart);
		area.setSelectedElement(sharedPart);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder areaPlaceholderA = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderA.setRef(area);
		area.setCurSharedRef(areaPlaceholderA);
		perspectiveA.getChildren().add(areaPlaceholderA);
		perspectiveA.setSelectedElement(areaPlaceholderA);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder areaPlaceholderB = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderB.setRef(area);
		perspectiveB.getChildren().add(areaPlaceholderB);
		perspectiveB.setSelectedElement(areaPlaceholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(sharedPart);

		perspectiveStack.setSelectedElement(perspectiveB);
		assertEquals(sharedPart, partService.getActivePart());

		partService.hidePart(sharedPart);
		assertNull(partService.getActivePart());

		perspectiveStack.setSelectedElement(perspectiveA);
		assertEquals(partA, partService.getActivePart());
	}

	/**
	 * Test to ensure that switching perspectives doesn't cause a hidden shared
	 * part to be displayed again.
	 */
	@Test
	public void testHidePart_ActivationHistory16A() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder partPlaceholderA1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA1.setRef(partA);
		partA.setCurSharedRef(partPlaceholderA1);
		perspectiveA.getChildren().add(partPlaceholderA1);
		perspectiveA.setSelectedElement(partPlaceholderA1);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		perspectiveA.getChildren().add(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		perspectiveB.getChildren().add(partPlaceholderA2);
		perspectiveB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		perspectiveB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		partService.switchPerspective(perspectiveB);
		assertEquals("partB is in both perspectives, active part should have been preserved", partB,
				partService.getActivePart());

		partService.hidePart(partB);
		assertEquals("Hiding partB should have caused partA to be activated", partA, partService.getActivePart());

		partService.switchPerspective(perspectiveA);
		assertEquals("partA is in both perspectives, active part should have been preserved", partA,
				partService.getActivePart());

		partService.activate(partB);
		assertEquals("partB should have been activated by activate(MPart)", partB, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(
				"partA should be the only part that's being shown in perspectiveB, thus, it should be the active part",
				partA, partService.getActivePart());
	}

	/**
	 * Test to ensure that switching perspectives doesn't cause a hidden shared
	 * part to be displayed again.
	 */
	@Test
	public void testHidePart_ActivationHistory16B() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		perspectiveA.getChildren().add(partPlaceholderB1);
		perspectiveA.setSelectedElement(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		perspectiveB.getChildren().add(partPlaceholderA2);
		perspectiveB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		perspectiveB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		partService.switchPerspective(perspectiveB);
		assertEquals("partB is in both perspectives, active part should have been preserved", partB,
				partService.getActivePart());

		partService.hidePart(partB);
		assertEquals("Hiding partB should have caused partA to be activated", partA, partService.getActivePart());

		partService.switchPerspective(perspectiveA);
		assertEquals("partB is the only part in perspectiveA, thus, it should be the active part", partB,
				partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(
				"partA should be the only part that's being shown in perspectiveB, thus, it should be the active part",
				partA, partService.getActivePart());
	}

	/**
	 * Test to ensure that switching perspectives doesn't cause a hidden shared
	 * part to be displayed again.
	 */
	@Test
	public void testHidePart_ActivationHistory16C() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(partStackA);
		perspectiveA.setSelectedElement(partStackA);

		MPlaceholder partPlaceholderA1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA1.setRef(partA);
		partA.setCurSharedRef(partPlaceholderA1);
		partStackA.getChildren().add(partPlaceholderA1);
		partStackA.setSelectedElement(partPlaceholderA1);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		partStackA.getChildren().add(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStackB);
		perspectiveB.setSelectedElement(partStackB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		partStackB.getChildren().add(partPlaceholderA2);
		partStackB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		partStackB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);
		assertEquals("partB should be the active part", partB, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		// assertEquals(
		// "partB is in both perspectives, but since partB is obscured by partA,
		// partA should be the active part",
		// partA, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals("partA should still be the active part", partA, partService.getActivePart());

		partService.switchPerspective(perspectiveA);
		assertEquals(
				"partA is in both perspectives, but since partA is obscured by partB, partB should be the active part",
				partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals("partB should have been activated by activate(MPart)", partB, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(
				"partA should be the only part that's being shown in perspectiveB, thus, it should be the active part",
				partA, partService.getActivePart());
	}

	/**
	 * Test to ensure that switching perspectives doesn't cause a hidden shared
	 * part to be displayed again.
	 */
	@Test
	public void testHidePart_ActivationHistory16D() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPartStack partStackA = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(partStackA);
		perspectiveA.setSelectedElement(partStackA);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		partStackA.getChildren().add(partPlaceholderB1);
		partStackA.setSelectedElement(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStackB = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStackB);
		perspectiveB.setSelectedElement(partStackB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		partStackB.getChildren().add(partPlaceholderA2);
		partStackB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		partStackB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		partService.switchPerspective(perspectiveB);
		assertEquals(
				"partB is in both perspectives, but since partB is obscured by partA, partA should be the active part",
				partA, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals("partA should still be the active part", partA, partService.getActivePart());

		partService.switchPerspective(perspectiveA);
		assertEquals("partB is the only part in perspectiveA, thus, it should be the active part", partB,
				partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(
				"partA should be the only part that's being shown in perspectiveB, thus, it should be the active part",
				partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory17() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		assertNull(partB.getObject());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.hidePart(partA);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory18() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		partStack.getChildren().add(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);
		partStack.setSelectedElement(placeholderC);

		initialize();
		getEngine().createGui(window);

		assertNull(partB.getObject());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.hidePart(partA);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory19() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partD = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partD);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderD1 = ems.createModelElement(MPlaceholder.class);
		placeholderD1.setRef(partD);
		partD.setCurSharedRef(placeholderD1);
		perspectiveA.getChildren().add(placeholderD1);
		perspectiveA.setSelectedElement(placeholderD1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partA);
		perspectiveB.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		MPlaceholder placeholderD2 = ems.createModelElement(MPlaceholder.class);
		placeholderD2.setRef(partD);
		partStack.getChildren().add(placeholderD2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partD);

		partService.switchPerspective(perspectiveB);
		partService.activate(partB);

		partService.switchPerspective(perspectiveA);
		partService.hidePart(partD);

		partService.switchPerspective(perspectiveB);
		partService.hidePart(partB);
		assertEquals(placeholderD2, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory20() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.hidePart(partA);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory21() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getChildren().add(area);
		window.setSelectedElement(area);

		MPart partA = ems.createModelElement(MPart.class);
		area.getChildren().add(partA);
		area.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		area.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.hidePart(partA);
		assertEquals(partC, partService.getActivePart());
	}

	/**
	 * Create two separate perspectives. The first one has a shared part partA
	 * in it. The second one has an unrendered part stack with the shared part
	 * partA in it, and then two other shared parts partB and partC are outside
	 * the part stack and are contained directly under the second perspective.
	 * <p>
	 * partA is initially active and we then switch perspectives to the second
	 * perspective. partB becomes active now. Then partB is hidden and then we
	 * want to assert that partC is the active part and that partA is not
	 * despite the fact that partA is contained in the activation history.
	 * </p>
	 */
	@Test
	public void testHidePart_ActivationHistory22() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partStack.setToBeRendered(false);
		perspectiveB.getChildren().add(partStack);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		partStack.getChildren().add(placeholderA2);
		partStack.setSelectedElement(placeholderA2);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	/**
	 * Create two separate perspectives. The first one has three shared parts in
	 * it, partA, partB, and partC. The second perspective has partA by itself,
	 * partB obscured in a part stack by another part, partD, and then partC
	 * comes after the part stack.
	 * <p>
	 * partA is initially active, and we proceed to activate partC, partB, and
	 * then partA again. Now we switch to the second perspective where partA
	 * remains the active part. Hiding partA should cause partC to become the
	 * active part as partB is obscured by partD despite the fact that partB is
	 * the next available candidate in the activation history.
	 * </p>
	 */
	@Test
	public void testHidePart_ActivationHistory23() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPlaceholder placeholderB1 = ems.createModelElement(MPlaceholder.class);
		placeholderB1.setRef(partB);
		partB.setCurSharedRef(placeholderB1);
		perspectiveA.getChildren().add(placeholderB1);

		MPlaceholder placeholderC1 = ems.createModelElement(MPlaceholder.class);
		placeholderC1.setRef(partC);
		partC.setCurSharedRef(placeholderC1);
		perspectiveA.getChildren().add(placeholderC1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		perspectiveB.getChildren().add(placeholderA2);
		perspectiveB.setSelectedElement(placeholderA2);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStack);

		MPlaceholder placeholderB2 = ems.createModelElement(MPlaceholder.class);
		placeholderB2.setRef(partB);
		partStack.getChildren().add(placeholderB2);

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partStack.setSelectedElement(partD);

		MPlaceholder placeholderC2 = ems.createModelElement(MPlaceholder.class);
		placeholderC2.setRef(partC);
		perspectiveB.getChildren().add(placeholderC2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partC);
		partService.activate(partB);
		partService.activate(partA);

		partService.switchPerspective(perspectiveB);
		assertEquals(partA, partService.getActivePart());

		partService.hidePart(partA);
		assertEquals(partC, partService.getActivePart());
	}

	/**
	 * Create a window with three parts, partA, partB contained within a part
	 * stack, and partC.
	 * <p>
	 * partA is initially active and then we proceed to activate partB and
	 * partC. Now we turn off the 'toBeRendered' flag of the part stack
	 * containing partB and then hide partC. partA should now become the active
	 * part as partB's parent container has been unrendered despite the fact
	 * that partB is the next available candidate in the activation history.
	 * </p>
	 */
	@Test
	public void testHidePart_ActivationHistory24() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partStack.setToBeRendered(false);
		partService.hidePart(partC);
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug327952_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.activate(partA);
		partService.activate(partB);
		partService.hidePart(partB);
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug327952_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		window.getWindows().add(detachedWindow);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		detachedWindow.getChildren().add(partB);
		detachedWindow.setSelectedElement(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.hidePart(partB);
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug327952_03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MArea area = ems.createModelElement(MArea.class);
		detachedWindow.getChildren().add(area);
		detachedWindow.setSelectedElement(area);

		MPart partB = ems.createModelElement(MPart.class);
		area.getChildren().add(partB);
		area.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		area.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.activate(partA);
		partService.activate(partB);
		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug327952_04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		window.getWindows().add(detachedWindow);

		MArea area = ems.createModelElement(MArea.class);
		detachedWindow.getChildren().add(area);
		detachedWindow.setSelectedElement(area);

		MPart partB = ems.createModelElement(MPart.class);
		area.getChildren().add(partB);
		area.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		area.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.activate(partA);
		partService.activate(partB);
		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MWindow detachedWindow = ems.createModelElement(MWindow.class);
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_05() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_06() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_07() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328339_08() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = ems.createModelElement(MPart.class);
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partC);
		partService.hidePart(partC);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveA.getChildren().add(partA);
		perspectiveA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partB = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partB);
		perspectiveB.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partB = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partB);
		perspectiveB.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveA.getChildren().add(partA);
		perspectiveA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPart partC = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveA.getChildren().add(partA);
		perspectiveA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partB = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partB);
		perspectiveB.setSelectedElement(partB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_05() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPart partC = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_06() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partB = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partB);
		perspectiveB.setSelectedElement(partB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_07() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveA.getChildren().add(partA);
		perspectiveA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_08() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_09() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setToBeRendered(false);
		placeholderA2.setRef(partA);
		perspectiveB.getChildren().add(placeholderA2);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		perspectiveB.getChildren().add(placeholderC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug328946_10() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partC);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		partB.setCurSharedRef(placeholderB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStack);

		MPlaceholder placeholderC = ems.createModelElement(MPlaceholder.class);
		placeholderC.setRef(partC);
		partC.setCurSharedRef(placeholderC);
		partStack.getChildren().add(placeholderC);
		partStack.setSelectedElement(placeholderC);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		partStack.getChildren().add(placeholderA2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());

		partService.activate(partB);
		assertEquals(partB, partService.getActivePart());

		partService.hidePart(partB);
		assertEquals(partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(partSashContainer);
		window.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		MArea area = ems.createModelElement(MArea.class);
		partSashContainer.getChildren().add(area);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		area.getChildren().add(partStack);
		area.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals("The active part should have remained in the area", partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_02() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(partSashContainer);
		window.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		MArea area = ems.createModelElement(MArea.class);
		partSashContainer.getChildren().add(area);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		area.getChildren().add(partStack);
		area.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals("The active part should have remained in the area", partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_03() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		perspectiveB.getChildren().add(partSashContainer);
		perspectiveB.setSelectedElement(partSashContainer);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		perspectiveB.getChildren().add(placeholderA2);
		perspectiveB.setSelectedElement(placeholderA2);

		MArea area = ems.createModelElement(MArea.class);
		partSashContainer.getChildren().add(area);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		area.getChildren().add(partStack);
		area.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals("The active part should have remained in the area", partC, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_04() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(partSashContainer);
		window.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partSashContainer.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_05() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(partSashContainer);
		window.setSelectedElement(partSashContainer);

		MPart partA = ems.createModelElement(MPart.class);
		partSashContainer.getChildren().add(partA);
		partSashContainer.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partSashContainer.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals(partA, partService.getActivePart());
	}

	@Test
	public void testHidePart_ActivationHistory_Bug329482_06() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA1 = ems.createModelElement(MPlaceholder.class);
		placeholderA1.setRef(partA);
		partA.setCurSharedRef(placeholderA1);
		perspectiveA.getChildren().add(placeholderA1);
		perspectiveA.setSelectedElement(placeholderA1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		perspectiveB.getChildren().add(partSashContainer);
		perspectiveB.setSelectedElement(partSashContainer);

		MPlaceholder placeholderA2 = ems.createModelElement(MPlaceholder.class);
		placeholderA2.setRef(partA);
		perspectiveB.getChildren().add(placeholderA2);
		perspectiveB.setSelectedElement(placeholderA2);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		partSashContainer.getChildren().add(partStack);
		partSashContainer.setSelectedElement(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		partService.switchPerspective(perspectiveB);
		assertEquals(partA, partService.getActivePart());

		MPart partD = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partD);
		partService.showPart(partD, PartState.ACTIVATE);
		assertEquals(partD, partStack.getSelectedElement());
		assertEquals(partD, partService.getActivePart());

		partService.hidePart(partD);
		assertEquals(partC, partStack.getSelectedElement());
		assertEquals(partA, partService.getActivePart());
	}

	/**
	 * Test to ensure that the active part remains constant between perspective
	 * switches.
	 */
	@Test
	public void testActivationHistory01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder partPlaceholderA1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA1.setRef(partA);
		partA.setCurSharedRef(partPlaceholderA1);
		perspectiveA.getChildren().add(partPlaceholderA1);
		perspectiveA.setSelectedElement(partPlaceholderA1);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		perspectiveA.getChildren().add(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		perspectiveB.getChildren().add(partPlaceholderA2);
		perspectiveB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		perspectiveB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		partService.switchPerspective(perspectiveB);
		assertEquals(partB, partService.getActivePart());
	}

	@Test
	public void testSwitchPerspective01() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		initialize();
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();
		IEclipseContext perspectiveContextA = perspectiveA.getContext();
		assertEquals(perspectiveContextA, windowContext.getActiveChild());

		window.getContext().get(EPartService.class).switchPerspective(perspectiveB);
		IEclipseContext perspectiveContextB = perspectiveB.getContext();
		assertEquals(perspectiveContextB, windowContext.getActiveChild());
	}

	@Test
	public void testSwitchPerspective02() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MPerspectiveStack perspectiveStackA = ems.createModelElement(MPerspectiveStack.class);
		windowA.getChildren().add(perspectiveStackA);
		windowA.setSelectedElement(perspectiveStackA);

		MPerspective perspectiveA1 = ems.createModelElement(MPerspective.class);
		perspectiveStackA.getChildren().add(perspectiveA1);
		perspectiveStackA.setSelectedElement(perspectiveA1);

		MPerspective perspectiveA2 = ems.createModelElement(MPerspective.class);
		perspectiveStackA.getChildren().add(perspectiveA2);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		MPerspectiveStack perspectiveStackB = ems.createModelElement(MPerspectiveStack.class);
		windowB.getChildren().add(perspectiveStackB);
		windowB.setSelectedElement(perspectiveStackB);

		MPerspective perspectiveB1 = ems.createModelElement(MPerspective.class);
		perspectiveStackB.getChildren().add(perspectiveB1);
		perspectiveStackB.setSelectedElement(perspectiveB1);

		MPerspective perspectiveB2 = ems.createModelElement(MPerspective.class);
		perspectiveStackB.getChildren().add(perspectiveB2);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService windowPartServiceA = windowA.getContext().get(EPartService.class);
		EPartService windowPartServiceB = windowB.getContext().get(EPartService.class);

		assertEquals(windowA.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveA1.getContext(), windowA.getContext().getActiveChild());
		assertEquals(perspectiveB1.getContext(), windowB.getContext().getActiveChild());

		windowPartServiceB.switchPerspective(perspectiveA2);
		assertEquals(windowA.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveA1.getContext(), windowA.getContext().getActiveChild());
		assertEquals(perspectiveB1.getContext(), windowB.getContext().getActiveChild());

		windowPartServiceA.switchPerspective(perspectiveA2);
		assertEquals(windowA.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveA2.getContext(), windowA.getContext().getActiveChild());
		assertEquals(perspectiveB1.getContext(), windowB.getContext().getActiveChild());

		windowPartServiceB.switchPerspective(perspectiveB2);
		assertEquals(windowA.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveA2.getContext(), windowA.getContext().getActiveChild());
		assertEquals(perspectiveB2.getContext(), windowB.getContext().getActiveChild());
	}

	@Test
	public void testSwitchPerspective03() {
		MWindow windowA = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MWindow windowB = ems.createModelElement(MWindow.class);
		application.getChildren().add(windowB);

		MPerspectiveStack perspectiveStackB = ems.createModelElement(MPerspectiveStack.class);
		windowB.getChildren().add(perspectiveStackB);
		windowB.setSelectedElement(perspectiveStackB);

		MPerspective perspectiveB1 = ems.createModelElement(MPerspective.class);
		perspectiveStackB.getChildren().add(perspectiveB1);
		perspectiveStackB.setSelectedElement(perspectiveB1);

		MPart partB1 = ems.createModelElement(MPart.class);
		perspectiveB1.getChildren().add(partB1);
		perspectiveB1.setSelectedElement(partB1);

		MPerspective perspectiveB2 = ems.createModelElement(MPerspective.class);
		perspectiveStackB.getChildren().add(perspectiveB2);

		MPart partB2 = ems.createModelElement(MPart.class);
		perspectiveB2.getChildren().add(partB2);
		perspectiveB2.setSelectedElement(partB2);

		initialize();
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService windowPartServiceB = windowB.getContext().get(EPartService.class);

		assertEquals(windowB.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveB1.getContext(), windowB.getContext().getActiveChild());

		windowPartServiceB.switchPerspective(perspectiveB2);
		assertEquals(windowB.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveB2.getContext(), windowB.getContext().getActiveChild());
	}

	@Test
	public void testSwitchPerspective04() {
		MWindow window1 = ems.createModelElement(MWindow.class);
		application.getChildren().add(window1);
		application.setSelectedElement(window1);

		MWindow window2 = ems.createModelElement(MWindow.class);
		application.getChildren().add(window2);

		MPart partA = ems.createModelElement(MPart.class);
		window2.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window2.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window2.getChildren().add(perspectiveStack);
		window2.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder partPlaceholderA1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA1.setRef(partA);
		partA.setCurSharedRef(partPlaceholderA1);
		perspectiveA.getChildren().add(partPlaceholderA1);
		perspectiveA.setSelectedElement(partPlaceholderA1);

		MPlaceholder partPlaceholderB1 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB1.setRef(partB);
		partB.setCurSharedRef(partPlaceholderB1);
		perspectiveA.getChildren().add(partPlaceholderB1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder partPlaceholderA2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderA2.setRef(partA);
		perspectiveB.getChildren().add(partPlaceholderA2);
		perspectiveB.setSelectedElement(partPlaceholderA2);

		MPlaceholder partPlaceholderB2 = ems.createModelElement(MPlaceholder.class);
		partPlaceholderB2.setRef(partB);
		perspectiveB.getChildren().add(partPlaceholderB2);

		initialize();
		getEngine().createGui(window1);
		getEngine().createGui(window2);

		assertEquals(window2.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveA.getContext(), window2.getContext().getActiveChild());

		EPartService partService = window2.getContext().get(EPartService.class);
		partService.switchPerspective(perspectiveB);

		assertEquals(window2.getContext(), application.getContext().getActiveChild());
		assertEquals(perspectiveB.getContext(), window2.getContext().getActiveChild());
	}

	/**
	 * Tests that a perspective will automatically select another part as the
	 * active part after the originally active part that's being shown across
	 * multiple perspectives has been removed.
	 */
	@Test
	public void testSwitchPerspective05() {
		MWindow window1 = ems.createModelElement(MWindow.class);
		application.getChildren().add(window1);
		application.setSelectedElement(window1);

		MWindow window2 = ems.createModelElement(MWindow.class);
		application.getChildren().add(window2);

		MArea area = ems.createModelElement(MArea.class);
		window2.getSharedElements().add(area);

		MPart sharedPart = ems.createModelElement(MPart.class);
		window2.getSharedElements().add(sharedPart);
		area.getChildren().add(sharedPart);
		area.setSelectedElement(sharedPart);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window2.getChildren().add(perspectiveStack);
		window2.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder areaPlaceholderA = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderA.setRef(area);
		area.setCurSharedRef(areaPlaceholderA);
		perspectiveA.getChildren().add(areaPlaceholderA);
		perspectiveA.setSelectedElement(areaPlaceholderA);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(stackA);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder areaPlaceholderB = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderB.setRef(area);
		perspectiveB.getChildren().add(areaPlaceholderB);
		perspectiveB.setSelectedElement(areaPlaceholderB);

		initialize();
		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService partService = window2.getContext().get(EPartService.class);
		partService.activate(sharedPart);
		partService.switchPerspective(perspectiveB);
		assertEquals(sharedPart, partService.getActivePart());

		partService.hidePart(sharedPart);
		assertNull(partService.getActivePart());

		application.setSelectedElement(window1);
		window1.getContext().activate();

		partService.switchPerspective(perspectiveA);

		assertEquals(perspectiveA.getContext(), window2.getContext().getActiveChild());
	}

	/**
	 * Test to ensure that the method annotated with the {@link Focus}
	 * annotation is invoked when switching between perspectives.
	 */
	@Test
	public void testSwitchPerspective06() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		partA.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getSharedElements().add(partA);

		MPart partB = ems.createModelElement(MPart.class);
		partB.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getSharedElements().add(partB);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(partA);
		partA.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(partB);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(perspectiveB);
		partService.switchPerspective(perspectiveA);

		ClientEditor editorA = (ClientEditor) partA.getObject();
		ClientEditor editorB = (ClientEditor) partB.getObject();
		editorA.focusCalled = false;
		editorB.focusCalled = false;

		partService.switchPerspective(perspectiveB);
		assertFalse(editorA.focusCalled);
		assertTrue(editorB.focusCalled);

		editorB.focusCalled = false;
		partService.switchPerspective(perspectiveA);
		assertTrue(editorA.focusCalled);
		assertFalse(editorB.focusCalled);
	}

	@Test
	public void testSwitchPerspective07() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partD = ems.createModelElement(MPart.class);
		window.getSharedElements().add(partD);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderD1 = ems.createModelElement(MPlaceholder.class);
		placeholderD1.setRef(partD);
		partD.setCurSharedRef(placeholderD1);
		perspectiveA.getChildren().add(placeholderD1);
		perspectiveA.setSelectedElement(placeholderD1);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPart partA = ems.createModelElement(MPart.class);
		perspectiveB.getChildren().add(partA);
		perspectiveB.setSelectedElement(partA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);

		MPlaceholder placeholderD2 = ems.createModelElement(MPlaceholder.class);
		placeholderD2.setRef(partD);
		partStack.getChildren().add(placeholderD2);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);

		partService.switchPerspective(perspectiveB);
		partService.activate(partB);
		assertEquals(perspectiveB, perspectiveStack.getSelectedElement());

		partService.switchPerspective(perspectiveA);
		assertEquals(perspectiveA, perspectiveStack.getSelectedElement());
	}

	@Test
	public void testSwitchPerspective08() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = ems.createModelElement(MArea.class);
		window.getSharedElements().add(area);

		MPart partA = ems.createModelElement(MPart.class);
		area.getChildren().add(partA);
		area.setSelectedElement(partA);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder areaPlaceholderA = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderA.setRef(area);
		area.setCurSharedRef(areaPlaceholderA);
		perspectiveA.getChildren().add(areaPlaceholderA);
		perspectiveA.setSelectedElement(areaPlaceholderA);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(partStack);

		MPart partB = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partB);

		MPart partC = ems.createModelElement(MPart.class);
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partC);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder areaPlaceholderB = ems.createModelElement(MPlaceholder.class);
		areaPlaceholderB.setRef(area);
		perspectiveB.getChildren().add(areaPlaceholderB);
		perspectiveB.setSelectedElement(areaPlaceholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.switchPerspective(perspectiveB);
		partService.hidePart(partA);

		partService.switchPerspective(perspectiveA);
		assertEquals(partC, partService.getActivePart());
	}

	/**
	 * Setup a shared part in two perspectives, perspectiveA and perspectiveB
	 * with the part being in a detached window in perspectiveB's case.
	 * <p>
	 * First activate the part in perspectiveA. Switching to perspectiveB should
	 * not cause any problems.
	 * </p>
	 */
	@Test
	public void testSwitchPerspective_Bug329184() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = ems.createModelElement(MPlaceholder.class);
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);

		MWindow detachedWindowB = ems.createModelElement(MWindow.class);
		perspectiveB.getWindows().add(detachedWindowB);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		detachedWindowB.getChildren().add(partStack);
		detachedWindowB.setSelectedElement(partStack);

		MPlaceholder placeholderB = ems.createModelElement(MPlaceholder.class);
		placeholderB.setRef(part);
		partStack.getChildren().add(placeholderB);
		partStack.setSelectedElement(placeholderB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(part);
		partService.switchPerspective(perspectiveB);
		assertEquals(part, partService.getActivePart());
	}

	/**
	 * Test to ensure that a part that has been hidden by the part service and
	 * presumably removed and is indeed longer reachable and can be garbage
	 * collected.
	 */
	@Test
	public void testLeak() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);

		WeakReference<MPart> ref = new WeakReference<>(partA);
		assertEquals(partA, ref.get());

		partService.hidePart(partA, true);
		assertEquals(partB, window.getSelectedElement());
		assertFalse(window.getChildren().contains(partA));
		partA = null;

		System.runFinalization();
		System.gc();

		assertNull("The part should no longer be reachable", ref.get());
	}

	@Test
	public void testsEventWithExceptions() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = ems.createModelElement(MPart.class);
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		window.getChildren().add(partB);

		initialize();
		getEngine().createGui(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partB);
		partService.activate(partA);

		partService.addPartListener(new ExceptionListener());
		PartListener listener = new PartListener();
		partService.addPartListener(listener);

		partService.activate(partB);
		assertEquals(1, listener.getActivated());
		assertEquals(1, listener.getDeactivated());
		assertEquals(1, listener.getVisible());
		assertEquals(1, listener.getHidden());
		assertEquals(1, listener.getBroughtToTop());
	}

	private void createApplication(String partId) {
		createApplication(new String[] { partId });
	}

	private void createApplication(String... partIds) {
		createApplication(new String[][] { partIds });
	}

	private void createApplication(String[]... partIds) {
		createApplication(partIds.length, partIds);
	}

	private void createApplication(int windows, String[][] partIds) {
		for (int i = 0; i < windows; i++) {
			MWindow window = ems.createModelElement(MWindow.class);
			application.getChildren().add(window);

			MPartStack partStack = ems.createModelElement(MPartStack.class);
			window.getChildren().add(partStack);

			for (String partId : partIds[i]) {
				MPart part = ems.createModelElement(MPart.class);
				part.setElementId(partId);
				partStack.getChildren().add(part);
			}
		}

		initialize();
	}

	private void initialize() {
		final UIEventPublisher ep = new UIEventPublisher(applicationContext);
		((Notifier) application).eAdapters().add(ep);
		applicationContext.set(UIEventPublisher.class, ep);

		applicationContext.set(ISaveHandler.class.getName(), new PartServiceSaveHandler() {
			@Override
			public Save[] promptToSave(Collection<MPart> saveablePart) {
				Save[] ret = new Save[saveablePart.size()];
				Arrays.fill(ret, ISaveHandler.Save.YES);
				return ret;
			}

			@Override
			public Save promptToSave(MPart saveablePart) {
				return ISaveHandler.Save.YES;
			}

		});
	}

	static class PartListener implements IPartListener {

		private final List<MPart> activatedParts = new ArrayList<>();
		private final List<MPart> deactivatedParts = new ArrayList<>();
		private final List<MPart> hiddenParts = new ArrayList<>();
		private final List<MPart> visibleParts = new ArrayList<>();
		private final List<MPart> broughtToTopParts = new ArrayList<>();

		private int activated = 0;
		private int deactivated = 0;
		private int hidden = 0;
		private int visible = 0;
		private int broughtToTop = 0;

		private boolean valid = true;

		public void clear() {
			activated = 0;
			deactivated = 0;
			hidden = 0;
			visible = 0;

			activatedParts.clear();
			deactivatedParts.clear();
			hiddenParts.clear();
			visibleParts.clear();

			valid = true;
		}

		public int getActivated() {
			return activated;
		}

		public int getDeactivated() {
			return deactivated;
		}

		public int getHidden() {
			return hidden;
		}

		public int getVisible() {
			return visible;
		}

		public int getBroughtToTop() {
			return broughtToTop;
		}

		public boolean isValid() {
			return valid;
		}

		public List<MPart> getActivatedParts() {
			return activatedParts;
		}

		public List<MPart> getDeactivatedParts() {
			return deactivatedParts;
		}

		public List<MPart> getHiddenParts() {
			return hiddenParts;
		}

		public List<MPart> getVisibleParts() {
			return visibleParts;
		}

		@Override
		public void partActivated(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			activated++;
			activatedParts.add(part);
		}

		@Override
		public void partBroughtToTop(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			broughtToTop++;
			broughtToTopParts.add(part);
		}

		@Override
		public void partDeactivated(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			deactivated++;
			deactivatedParts.add(part);
		}

		@Override
		public void partHidden(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			hidden++;
			hiddenParts.add(part);
		}

		@Override
		public void partVisible(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			visible++;
			visibleParts.add(part);
		}

	}

	static class ExceptionListener implements IPartListener {

		@Override
		public void partActivated(MPart part) {
			throw new RuntimeException();
		}

		@Override
		public void partBroughtToTop(MPart part) {
			throw new RuntimeException();
		}

		@Override
		public void partDeactivated(MPart part) {
			throw new RuntimeException();
		}

		@Override
		public void partHidden(MPart part) {
			throw new RuntimeException();
		}

		@Override
		public void partVisible(MPart part) {
			throw new RuntimeException();
		}

	}
}
