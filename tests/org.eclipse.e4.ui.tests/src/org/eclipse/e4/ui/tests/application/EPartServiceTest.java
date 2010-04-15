/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.workbench.modeling.IPartListener;
import org.eclipse.e4.workbench.modeling.ISaveHandler;
import org.eclipse.e4.workbench.modeling.ISaveHandler.Save;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.internal.UIEventPublisher;
import org.eclipse.emf.common.notify.Notifier;

public class EPartServiceTest extends TestCase {

	private IEclipseContext applicationContext;

	private IPresentationEngine engine;

	@Override
	protected void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();

		super.setUp();
	}

	protected String getEngineURI() {
		return "platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.HeadlessContextPresentationEngine"; //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (applicationContext instanceof IDisposable) {
			((IDisposable) applicationContext).dispose();
		}
	}

	private IPresentationEngine getEngine() {
		if (engine == null) {
			IContributionFactory contributionFactory = (IContributionFactory) applicationContext
					.get(IContributionFactory.class.getName());
			Object newEngine = contributionFactory.create(getEngineURI(),
					applicationContext);
			assertTrue(newEngine instanceof IPresentationEngine);
			applicationContext.set(IPresentationEngine.class.getName(),
					newEngine);

			engine = (IPresentationEngine) newEngine;
		}

		return engine;
	}

	public void testFindPart_PartInWindow() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.findPart("partId");
		assertNotNull(part);

		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);

		part = partService.findPart("invalidPartId");
		assertNull(part);
	}

	public void testFindPart_PartNotInWindow() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.findPart("invalidPartId");
		assertNull(part);
	}

	public void testFindPart_PartInAnotherWindow() {
		MApplication application = createApplication(
				new String[] { "partInWindow1" },
				new String[] { "partInWindow2" });

		MWindow window1 = application.getChildren().get(0);
		MWindow window2 = application.getChildren().get(1);

		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService partService = (EPartService) window1.getContext().get(
				EPartService.class.getName());
		MPart part = partService.findPart("partInWindow2");
		assertNull(part);
		part = partService.findPart("partInWindow1");
		assertNotNull(part);

		MPartStack partStack = (MPartStack) window1.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);

		partService = (EPartService) window2.getContext().get(
				EPartService.class.getName());
		part = partService.findPart("partInWindow1");
		assertNull(part);
		part = partService.findPart("partInWindow2");
		assertNotNull(part);

		partStack = (MPartStack) window2.getChildren().get(0);
		assertEquals(partStack.getChildren().get(0), part);
	}

	public void testBringToTop_PartOnTop() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partFront);
		assertEquals(partStack.getSelectedElement(), partFront);
	}

	public void testBringToTop_PartOnTop_myService() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) partFront.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partFront);
		assertEquals(partStack.getSelectedElement(), partFront);
	}

	public void testBringToTop_PartNotOnTop() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partBack);
		assertEquals(partStack.getSelectedElement(), partBack);
	}

	public void testBringToTop_PartNotOnTop_myService() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) partFront.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partBack);
		assertEquals(partStack.getSelectedElement(), partBack);
	}

	public void testBringToTop_PartInAnotherWindow() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	public void testBringToTop_PartInAnotherWindow_myService() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

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

	public void testBringToTop_ActivationChanges() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPart partFrontA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partBackA = BasicFactoryImpl.eINSTANCE.createPart();
		partStackA.getChildren().add(partFrontA);
		partStackA.getChildren().add(partBackA);
		window.getChildren().add(partStackA);

		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPart partFrontB = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partBackB = BasicFactoryImpl.eINSTANCE.createPart();
		partStackB.getChildren().add(partFrontB);
		partStackB.getChildren().add(partBackB);
		window.getChildren().add(partStackB);

		partStackA.setSelectedElement(partFrontA);
		partStackB.setSelectedElement(partFrontB);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testBringToTop_Unrendered() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partBack.setToBeRendered(false);

		partStack.setSelectedElement(partFront);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		assertFalse(partBack.isToBeRendered());

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.bringToTop(partBack);
		assertTrue("Bringing a part to the top should cause it to be rendered",
				partBack.isToBeRendered());
	}

	public void testGetParts_Empty() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MPart> parts = partService.getParts();
		assertNotNull(parts);
		assertEquals(0, parts.size());
	}

	public void testGetParts_OneWindow() {
		MApplication application = createApplication("partId", "partId2");
		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MPart> parts = partService.getParts();
		assertNotNull(parts);
		assertEquals(2, parts.size());
		assertTrue(parts.containsAll(partStack.getChildren()));
	}

	public void testGetParts_TwoWindows() {
		MApplication application = createApplication(new String[] { "partId",
				"partId2" }, new String[] { "partIA", "partIdB", "partIdC" });

		MWindow windowA = application.getChildren().get(0);
		MWindow windowB = application.getChildren().get(1);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	public void testGetInputParts() {
		final String uri1 = "file:///a.txt";
		final String uri2 = "file:///b.txt";

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MInputPart inputPart = BasicFactoryImpl.eINSTANCE.createInputPart();
		inputPart.setInputURI(uri1);
		window.getChildren().add(inputPart);

		part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		inputPart = BasicFactoryImpl.eINSTANCE.createInputPart();
		inputPart.setInputURI(uri2);
		window.getChildren().add(inputPart);

		inputPart = BasicFactoryImpl.eINSTANCE.createInputPart();
		inputPart.setInputURI(uri1);
		window.getChildren().add(inputPart);

		part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertEquals(6, partService.getParts().size());
		assertEquals(2, partService.getInputParts(uri1).size());
		assertEquals(1, partService.getInputParts(uri2).size());
		assertEquals(0, partService.getInputParts("totally unknown").size());
		try {
			partService.getInputParts(null);
			fail("Passing null should throw an AssertionFailedException");
		} catch (AssertionFailedException e) {
		}
	}

	public void testIsPartVisible_NotInStack(boolean selected, boolean visible) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setVisible(visible);
		window.getChildren().add(part);

		if (selected) {
			window.setSelectedElement(part);
		}

		initialize(applicationContext, application);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertEquals(visible, partService.isPartVisible(part));
		partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertEquals(visible, partService.isPartVisible(part));
	}

	public void testIsPartVisible_NotInStackTrueTrue() {
		testIsPartVisible_NotInStack(true, true);
	}

	public void testIsPartVisible_NotInStackTrueFalse() {
		testIsPartVisible_NotInStack(true, false);
	}

	public void testIsPartVisible_NotInStackFalseTrue() {
		testIsPartVisible_NotInStack(false, true);
	}

	public void testIsPartVisible_NotInStackFalseFalse() {
		testIsPartVisible_NotInStack(false, false);
	}

	public void testIsPartVisible_ViewVisible() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = partStack.getChildren().get(0);
		partStack.setSelectedElement(part);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertTrue(partService.isPartVisible(part));
	}

	public void testIsPartVisible_ViewVisible_myService() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = partStack.getChildren().get(0);
		partStack.setSelectedElement(part);

		getEngine().createGui(window);

		EPartService partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertTrue(partService.isPartVisible(part));
	}

	public void testIsPartVisible_ViewNotVisible() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part = partStack.getChildren().get(1);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertFalse(partService.isPartVisible(part));
	}

	public void testIsPartVisible_ViewNotVisible_myService() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part1 = partStack.getChildren().get(0);
		MPart part2 = partStack.getChildren().get(1);

		EPartService partService1 = (EPartService) part1.getContext().get(
				EPartService.class.getName());
		assertTrue(partService1.isPartVisible(part1));
		assertFalse(partService1.isPartVisible(part2));

		partService1.activate(part2);

		EPartService partService2 = (EPartService) part2.getContext().get(
				EPartService.class.getName());
		assertFalse(partService1.isPartVisible(part1));
		assertTrue(partService1.isPartVisible(part2));
		assertFalse(partService2.isPartVisible(part1));
		assertTrue(partService2.isPartVisible(part2));
	}

	public void testIsPartVisible_ViewInAnotherWindow() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

		assertTrue(partServiceA.isPartVisible(partFrontA));
		assertFalse(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertTrue(partServiceB.isPartVisible(partFrontB));
		assertFalse(partServiceB.isPartVisible(partBackB));
	}

	public void testIsPartVisible_ViewInAnotherWindow_myService() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

		assertTrue(partServiceA.isPartVisible(partFrontA));
		assertFalse(partServiceA.isPartVisible(partBackA));
		assertFalse(partServiceA.isPartVisible(partFrontB));
		assertFalse(partServiceA.isPartVisible(partBackB));

		assertFalse(partServiceB.isPartVisible(partFrontA));
		assertFalse(partServiceB.isPartVisible(partBackA));
		assertTrue(partServiceB.isPartVisible(partFrontB));
		assertFalse(partServiceB.isPartVisible(partBackB));
	}

	public void testActivate_partService() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setSelectedElement(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part1 = partStack.getChildren().get(0);
		MPart part2 = partStack.getChildren().get(1);

		EPartService partService1 = (EPartService) part1.getContext().get(
				EPartService.class.getName());
		assertTrue(partService1.isPartVisible(part1));
		assertFalse(partService1.isPartVisible(part2));

		partService1.activate(part2);

		EPartService partService2 = (EPartService) part2.getContext().get(
				EPartService.class.getName());
		assertFalse(partService1.isPartVisible(part1));
		assertTrue(partService1.isPartVisible(part2));
		assertFalse(partService2.isPartVisible(part1));
		assertTrue(partService2.isPartVisible(part2));
	}

	public void testActivate_partService_twoWindows() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);
		windowA.setSelectedElement(partStackA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);
		windowB.setSelectedElement(partStackB);

		application.setSelectedElement(windowA);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

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

	public void testActivate_partService_SelectedElement() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setSelectedElement(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setSelectedElement(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

		partServiceA.activate(partBackA);

		assertEquals(windowA, application.getSelectedElement());
		IEclipseContext a = application.getContext();
		IEclipseContext c = (IEclipseContext) a
				.getLocal(IContextConstants.ACTIVE_CHILD);
		while (c != null) {
			a = c;
			c = (IEclipseContext) a.getLocal(IContextConstants.ACTIVE_CHILD);
		}
		MPart aPart = (MPart) a.get(MPart.class.getName());
		assertEquals(partBackA, aPart);

		partServiceB.activate(partBackB);
		assertEquals(windowB, application.getSelectedElement());
		a = application.getContext();
		c = (IEclipseContext) a.getLocal(IContextConstants.ACTIVE_CHILD);
		while (c != null) {
			a = c;
			c = (IEclipseContext) a.getLocal(IContextConstants.ACTIVE_CHILD);
		}
		aPart = (MPart) a.get(MPart.class.getName());
		assertEquals(partBackB, aPart);
	}

	public void testActivate_partService_activePart() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

		partServiceA.activate(partBackA);

		assertEquals(windowA, application.getSelectedElement());
		MPart shouldBeCorrect = (MPart) partFrontA.getContext().get(
				IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackA, partServiceA.getActivePart());

		partServiceB.activate(partBackB);
		assertEquals(windowB, application.getSelectedElement());
		shouldBeCorrect = (MPart) partFrontB.getContext().get(
				IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackB, partServiceB.getActivePart());
	}

	public void testActivate_Unrendered() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partBack.setToBeRendered(false);

		partStack.setSelectedElement(partFront);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		assertFalse(partBack.isToBeRendered());

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partBack);
		assertTrue("Activating a part should cause it to be rendered", partBack
				.isToBeRendered());
	}

	public void testCreatePart() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertNotNull(partService.createPart("partId"));
	}

	public void testCreatePart2() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertNull(partService.createPart("partId2"));
	}

	public void testShowPart_Id_ACTIVATE() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId", PartState.ACTIVATE);
		assertNotNull(part);
		assertEquals("partId", part.getElementId());
		assertEquals(part, partService.getActivePart());
		assertTrue("Shown part should be visible", part.isVisible());
	}

	public void testShowPart_Id_ACTIVATE_DefinedCategoryStackNotExists() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId2");
		application.getDescriptors().add(partDescriptor);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testShowPart_Id_ACTIVATE_DefinedCategoryStackExists() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setElementId("partId2");
		application.getDescriptors().add(partDescriptor);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.getTags().add("categoryId");
		window.getChildren().add(stack);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testShowPart_Id_CREATE() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partA2 = BasicFactoryImpl.eINSTANCE.createPart();
		partA1.setElementId("partA1");
		partA2.setElementId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB2 = BasicFactoryImpl.eINSTANCE.createPart();
		partB1.setElementId("partB1");
		partB2.setElementId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setSelectedElement(partA1);
		partStackB.setSelectedElement(partB1);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA1);
		assertEquals(partA1, partService.getActivePart());

		assertEquals(null, partA2.getContext());
		assertEquals(null, partB2.getContext());

		MPart shownPart = partService.showPart("partA2",
				EPartService.PartState.CREATE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(shownPart, partA2);
		assertNotNull(
				"The part should have been created so it should have a context",
				partA2.getContext());
		assertEquals(
				"This part has not been instantiated yet, it should have no context",
				null, partB2.getContext());

		shownPart = partService.showPart("partB2",
				EPartService.PartState.CREATE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(shownPart, partB2);
		assertNotNull(
				"The part should have been created so it should have a context",
				partA2.getContext());
		assertNotNull(
				"The part should have been created so it should have a context",
				partB2.getContext());
	}

	public void testShowPart_Id_CREATE2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB",
				EPartService.PartState.CREATE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals(
				"Only creating the part, the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertFalse(partService.isPartVisible(partB));
	}

	public void testShowPart_Id_CREATE3() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStackB.getTags().add("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setSelectedElement(partA);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB",
				EPartService.PartState.CREATE);

		assertEquals(1, partStackA.getChildren().size());
		assertEquals(
				"Only creating the part, the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(
				"The part is the only one in the stack, it should be visible",
				partService.isPartVisible(partB));
	}

	public void testShowPart_Id_CREATE4() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.getTags().add("stackId");
		window.getChildren().add(stack);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("part");
		partDescriptor.setCategory("stackId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService
				.showPart("part", EPartService.PartState.CREATE);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, partService.getActivePart());
	}

	public void testShowPart_Id_VISIBLE() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partA2 = BasicFactoryImpl.eINSTANCE.createPart();
		partA1.setElementId("partA1");
		partA2.setElementId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB2 = BasicFactoryImpl.eINSTANCE.createPart();
		partB1.setElementId("partB1");
		partB2.setElementId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setSelectedElement(partA1);
		partStackB.setSelectedElement(partB1);
		window.setSelectedElement(partStackA);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA1);
		assertEquals(partA1, partService.getActivePart());

		MPart shownPart = partService.showPart("partB1",
				EPartService.PartState.VISIBLE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB1));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(partB1, shownPart);

		shownPart = partService.showPart("partB2",
				EPartService.PartState.VISIBLE);
		assertTrue(partService.isPartVisible(partA1));
		assertTrue(partService.isPartVisible(partB2));
		assertEquals(partA1, partService.getActivePart());
		assertEquals(partB2, shownPart);
	}

	public void testShowPart_Id_VISIBLE2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB",
				EPartService.PartState.VISIBLE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals(
				"The part is in the same stack as the active part, so the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(partService.isPartVisible(partA));
		assertFalse(partService.isPartVisible(partB));
	}

	public void testShowPart_Id_VISIBLE3() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStackB.getTags().add("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setSelectedElement(partA);
		window.setSelectedElement(partStackA);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB",
				EPartService.PartState.VISIBLE);

		assertEquals(1, partStackA.getChildren().size());
		assertEquals(
				"Only making a part visible, the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(
				"The part is the only one in the stack, it should be visible",
				partService.isPartVisible(partB));
	}

	public void testShowPart_Id_VISIBLE4() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.getTags().add("stackId");
		window.getChildren().add(stack);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("part");
		partDescriptor.setCategory("stackId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("part",
				EPartService.PartState.VISIBLE);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, partService.getActivePart());
	}

	public void testShowPart_Id_VISIBLE5() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStack.setElementId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partStack.getChildren().add(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setToBeRendered(false);
		partStack.getChildren().add(partB);

		partStack.setSelectedElement(partA);
		window.setSelectedElement(partStack);
		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart shownPart = partService.showPart("partB",
				EPartService.PartState.VISIBLE);

		assertEquals(2, partStack.getChildren().size());
		assertEquals(
				"The part is in the same stack as the active part, so the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(partService.isPartVisible(partA));
		assertFalse(partService.isPartVisible(partB));
		assertEquals(partB, shownPart);
		assertTrue(partB.isToBeRendered());
	}

	private void testShowPart_Id_Unrendered(EPartService.PartState partState) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setElementId("partId");
		part.setToBeRendered(false);
		window.getChildren().add(part);
		window.setSelectedElement(part);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart shownPart = partService.showPart("partId", partState);

		assertEquals(1, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));
		assertEquals(part, shownPart);
		assertTrue("A shown part should be rendered", part.isToBeRendered());
	}

	public void testShowPart_Id_Unrendered_CREATE() {
		testShowPart_Id_Unrendered(PartState.CREATE);
	}

	public void testShowPart_Id_Unrendered_VISIBLE() {
		testShowPart_Id_Unrendered(PartState.VISIBLE);
	}

	public void testShowPart_Id_Unrendered_ACTIVATE() {
		testShowPart_Id_Unrendered(PartState.ACTIVATE);
	}

	private void testShowPart_Id_PartAlreadyShown(PartState partState) {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId", partState);
		assertNotNull(part);
		assertEquals("partId", part.getElementId());
		assertEquals(part, partService.getActivePart());

		MPart part2 = partService.showPart("partId", partState);
		assertEquals("Should not have instantiated a new MPart", part, part2);
		assertEquals(part, partService.getActivePart());
	}

	public void testShowPart_Id_PartAlreadyShown_ACTIVATE() {
		testShowPart_Id_PartAlreadyShown(PartState.ACTIVATE);
	}

	public void testShowPart_Id_PartAlreadyShown_CREATE() {
		testShowPart_Id_PartAlreadyShown(PartState.CREATE);
	}

	public void testShowPart_Id_PartAlreadyShown_VISIBLE() {
		testShowPart_Id_PartAlreadyShown(PartState.VISIBLE);
	}

	private void testShowPart_Id_IncorrectDescriptor(PartState partState) {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertNull(partService.showPart("partId2", partState));
	}

	public void testShowPart_Id_IncorrectDescriptor_ACTIVATE() {
		testShowPart_Id_IncorrectDescriptor(PartState.ACTIVATE);
	}

	public void testShowPart_Id_IncorrectDescriptor_VISIBLE() {
		testShowPart_Id_IncorrectDescriptor(PartState.VISIBLE);
	}

	public void testShowPart_Id_IncorrectDescriptor_CREATE() {
		testShowPart_Id_IncorrectDescriptor(PartState.CREATE);
	}

	private void testShowPart_Id_MultipleExists(boolean multipleAllowed,
			PartState partState) {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);
		MPartStack stack = (MPartStack) window.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		stack.setSelectedElement(part);
		window.setSelectedElement(stack);
		application.setSelectedElement(window);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart shownPart = partService.showPart("partId", partState);
		assertNotNull(shownPart);
		assertEquals(part, shownPart);
	}

	public void testShowPart_Id_MultipleExists_TrueACTIVATE() {
		testShowPart_Id_MultipleExists(true, PartState.ACTIVATE);
	}

	public void testShowPart_Id_MultipleExists_FalseACTIVATE() {
		testShowPart_Id_MultipleExists(false, PartState.ACTIVATE);
	}

	public void testShowPart_Id_MultipleExists_TrueVISIBLE() {
		testShowPart_Id_MultipleExists(true, PartState.VISIBLE);
	}

	public void testShowPart_Id_MultipleExists_FalseVISIBLE() {
		testShowPart_Id_MultipleExists(false, PartState.VISIBLE);
	}

	public void testShowPart_Id_MultipleExists_TrueCREATE() {
		testShowPart_Id_MultipleExists(true, PartState.CREATE);
	}

	public void testShowPart_Id_MultipleExists_FalseCREATE() {
		testShowPart_Id_MultipleExists(false, PartState.CREATE);
	}

	public void testShowPart_Id_PartInInactivePerspective() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

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
		window.getContext().set(IContextConstants.ACTIVE_CHILD,
				perspectiveB.getContext());

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		application.setSelectedElement(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.createPart("partId");
		partService.showPart(part, partState);
	}

	public void testShowPart_Part_ACTIVATE() {
		testShowPart_Part(PartState.ACTIVATE);
	}

	public void testShowPart_Part_VISIBLE() {
		testShowPart_Part(PartState.VISIBLE);
	}

	public void testShowPart_Part_CREATE() {
		testShowPart_Part(PartState.CREATE);
	}

	private void testShowPart_Part_MultipleExists(boolean multipleAllowed,
			PartState partState) {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);
		MPartStack stack = (MPartStack) window.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		stack.setSelectedElement(part);
		window.setSelectedElement(stack);
		application.setSelectedElement(window);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, partState);
		assertNotNull(shownPart);

		if (multipleAllowed) {
			assertEquals(createdPart, shownPart);
		} else {
			assertEquals(part, shownPart);
		}
	}

	public void testShowPart_Part_MultipleExists_TrueACTIVATE() {
		testShowPart_Part_MultipleExists(true, PartState.ACTIVATE);
	}

	public void testShowPart_Part_MultipleExists_FalseACTIVATE() {
		testShowPart_Part_MultipleExists(false, PartState.ACTIVATE);
	}

	public void testShowPart_Part_MultipleExists_TrueVISIBLE() {
		testShowPart_Part_MultipleExists(true, PartState.VISIBLE);
	}

	public void testShowPart_Part_MultipleExists_FalseVISIBLE() {
		testShowPart_Part_MultipleExists(false, PartState.VISIBLE);
	}

	public void testShowPart_Part_MultipleExists_TrueCREATE() {
		testShowPart_Part_MultipleExists(true, PartState.CREATE);
	}

	public void testShowPart_Part_MultipleExists_FalseCREATE() {
		testShowPart_Part_MultipleExists(false, PartState.CREATE);
	}

	private void testShowPart_Part_MultipleNonexistent(boolean multipleAllowed,
			PartState partState) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setAllowMultiple(multipleAllowed);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, partState);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);
	}

	public void testShowPart_Part_MultipleNonexistent_TrueACTIVATE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.ACTIVATE);
	}

	public void testShowPart_Part_MultipleNonexistent_FalseACTIVATE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.ACTIVATE);
	}

	public void testShowPart_Part_MultipleNonexistent_TrueVISIBLE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.VISIBLE);
	}

	public void testShowPart_Part_MultipleNonexistent_FalseVISIBLE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.VISIBLE);
	}

	public void testShowPart_Part_MultipleNonexistent_TrueCREATE() {
		testShowPart_Part_MultipleNonexistent(true, PartState.CREATE);
	}

	public void testShowPart_Part_MultipleNonexistent_FalseCREATE() {
		testShowPart_Part_MultipleNonexistent(false, PartState.CREATE);
	}

	public void testShowPart_Part_MultipleWithoutCategory() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setAllowMultiple(true);
		partDescriptor.setElementId("partId");
		application.getDescriptors().add(partDescriptor);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, PartState.ACTIVATE);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);

		MPart createdPart2 = partService.createPart("partId");
		MPart shownPart2 = partService.showPart(createdPart2,
				PartState.ACTIVATE);
		assertFalse(shownPart.equals(shownPart2));
	}

	public void testShowPart_Part_MultipleWithCategory() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.getTags().add("categoryId");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPartDescriptor descriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor.setAllowMultiple(true);
		descriptor.setElementId("partId");
		descriptor.setCategory("categoryId");
		application.getDescriptors().add(descriptor);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart createdPart = partService.createPart("partId");
		MPart shownPart = partService.showPart(createdPart, PartState.ACTIVATE);
		assertNotNull(shownPart);
		assertEquals(createdPart, shownPart);

		MPart createdPart2 = partService.createPart("partId");
		MPart shownPart2 = partService.showPart(createdPart2,
				PartState.ACTIVATE);
		assertFalse(shownPart.equals(shownPart2));

		assertTrue(stack.getChildren().contains(shownPart));
		assertTrue(stack.getChildren().contains(shownPart2));
	}

	public void testShowPart_Part_ExistingInNonstandardCategory() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.setElementId("categoryId2");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setElementId("partId");
		stack.getChildren().add(part);
		stack.setSelectedElement(part);

		MPartDescriptor descriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor.setAllowMultiple(true);
		descriptor.setElementId("partId");
		descriptor.setCategory("categoryId");
		application.getDescriptors().add(descriptor);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart shownPart = partService.showPart("partId", PartState.ACTIVATE);
		assertEquals(part, shownPart);
		assertEquals(stack, part.getParent());
	}

	public void testHidePart_PartInAnotherWindow() {
		MApplication application = createApplication(
				new String[] { "partInWindow1" },
				new String[] { "partInWindow2" });

		MWindow window1 = application.getChildren().get(0);
		MWindow window2 = application.getChildren().get(1);

		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService partService1 = (EPartService) window1.getContext().get(
				EPartService.class.getName());
		EPartService partService2 = (EPartService) window2.getContext().get(
				EPartService.class.getName());
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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		if (tagged) {
			part.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
		}

		initialize(applicationContext, application);
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.hidePart(part);

		assertFalse(part.isToBeRendered());
		assertEquals(tagged ? null : window, part.getParent());
	}

	public void testHidePart_Tagged_True() {
		testHidePart_Tagged(true);
	}

	public void testHidePart_Tagged_False() {
		testHidePart_Tagged(false);
	}

	public void testGetDirtyParts() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MPart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	public void testGetDirtyParts2() {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MPart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	private void testGetDirtyParts3(boolean before, boolean after) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPart saveablePart = BasicFactoryImpl.eINSTANCE.createPart();
		saveablePart.setDirty(before);
		window.getChildren().add(saveablePart);

		// setup the context
		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testGetDirtyParts3_TrueTrue() {
		testGetDirtyParts3(true, true);
	}

	public void testGetDirtyParts3_TrueFalse() {
		testGetDirtyParts3(true, false);
	}

	public void testGetDirtyParts3_FalseTrue() {
		testGetDirtyParts3(false, true);
	}

	public void testGetDirtyParts3_FalseFalse() {
		testGetDirtyParts3(false, false);
	}

	public void testEvent_PartActivated() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getActivated());
		assertEquals(partBack, partListener.getActivatedParts().get(0));
		assertTrue(partListener.isValid());
	}

	public void testEvent_PartActivated2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	public void testEvent_PartDeactivated() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertEquals(partFront, partService.getActivePart());

		PartListener partListener = new PartListener();
		partService.addPartListener(partListener);

		partService.activate(partBack);

		assertEquals(1, partListener.getDeactivated());
		assertEquals(partFront, partListener.getDeactivatedParts().get(0));
		assertTrue(partListener.isValid());
	}

	public void testEvent_PartDeactivated2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	public void testEvent_PartHidden() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testEvent_PartHidden2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	public void testEvent_PartVisible() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setSelectedElement(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	public void testEvent_PartVisible2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.getChildren().add(windowB);
		application.setSelectedElement(windowA);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		windowB.getChildren().add(stack);
		windowB.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

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

	private void testSavePart(final Save returnValue, boolean confirm,
			boolean beforeDirty, boolean afterDirty, boolean success,
			boolean saveCalled, boolean throwException) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPart saveablePart = BasicFactoryImpl.eINSTANCE.createPart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		window.getContext().set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(Collection<MPart> saveablePart) {
						return null;
					}

					public Save promptToSave(MPart saveablePart) {
						return returnValue;
					}
				});

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		if (beforeDirty) {
			assertEquals(success, partService.savePart(saveablePart, confirm));
		} else {
			assertTrue(
					"The part is not dirty, the save operation should complete successfully",
					partService.savePart(saveablePart, confirm));
		}

		assertEquals(afterDirty, saveablePart.isDirty());
		assertEquals(saveCalled, editor.wasSaveCalled());
	}

	private void testSavePart(Save returnValue, boolean confirm,
			boolean beforeDirty, boolean throwException) {
		switch (returnValue) {
		case YES:
			if (throwException) {
				if (beforeDirty) {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							beforeDirty, false, true, throwException);
				} else {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							beforeDirty, true, false, throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							false, true, true, throwException);
				} else {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							false, true, true, throwException);
				}
			} else {
				if (confirm) {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							false, true, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.YES, confirm, beforeDirty,
							false, true, false, throwException);
				}
			}
			break;
		case NO:
			if (throwException) {
				if (beforeDirty) {
					if (confirm) {
						testSavePart(ISaveHandler.Save.NO, confirm,
								beforeDirty, beforeDirty, true, false,
								throwException);
					} else {
						testSavePart(ISaveHandler.Save.NO, confirm,
								beforeDirty, beforeDirty, false, true,
								throwException);
					}
				} else {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty,
							beforeDirty, true, false, throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty,
							true, true, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty,
							false, true, true, throwException);
				}
			} else {
				if (confirm) {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty,
							false, true, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.NO, confirm, beforeDirty,
							false, true, false, throwException);
				}
			}
			break;
		case CANCEL:
			if (throwException) {
				if (beforeDirty) {
					if (confirm) {
						testSavePart(ISaveHandler.Save.CANCEL, confirm,
								beforeDirty, beforeDirty, false, false,
								throwException);
					} else {
						testSavePart(ISaveHandler.Save.CANCEL, confirm,
								beforeDirty, beforeDirty, false, true,
								throwException);
					}
				} else {
					testSavePart(ISaveHandler.Save.CANCEL, confirm,
							beforeDirty, beforeDirty, true, false,
							throwException);
				}
			} else if (beforeDirty) {
				if (confirm) {
					testSavePart(ISaveHandler.Save.CANCEL, confirm,
							beforeDirty, true, false, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.CANCEL, confirm,
							beforeDirty, false, true, true, throwException);
				}
			} else {
				if (confirm) {
					testSavePart(ISaveHandler.Save.CANCEL, confirm,
							beforeDirty, false, true, false, throwException);
				} else {
					testSavePart(ISaveHandler.Save.CANCEL, confirm,
							beforeDirty, false, true, false, throwException);
				}
			}
			break;
		default:
			fail("Unknown expected return value set: " + returnValue);
		}
	}

	public void testSavePart_YesTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.YES, true, true, true);
	}

	public void testSavePart_YesTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.YES, true, true, false);
	}

	public void testSavePart_YesTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.YES, true, false, true);
	}

	public void testSavePart_YesTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.YES, true, false, false);
	}

	public void testSavePart_YesFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.YES, false, true, true);
	}

	public void testSavePart_YesFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.YES, false, true, false);
	}

	public void testSavePart_YesFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.YES, false, false, true);
	}

	public void testSavePart_YesFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.YES, false, false, false);
	}

	public void testSavePart_NoTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.NO, true, true, true);
	}

	public void testSavePart_NoTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.NO, true, true, false);
	}

	public void testSavePart_NoTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.NO, true, false, true);
	}

	public void testSavePart_NoTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.NO, true, false, false);
	}

	public void testSavePart_NoFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.NO, false, true, true);
	}

	public void testSavePart_NoFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.NO, false, true, false);
	}

	public void testSavePart_NoFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.NO, false, false, true);
	}

	public void testSavePart_NoFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.NO, false, false, false);
	}

	public void testSavePart_CancelTrueTrueTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, true, true, true);
	}

	public void testSavePart_CancelTrueTrueFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, true, true, false);
	}

	public void testSavePart_CancelTrueFalseTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, true, false, true);
	}

	public void testSavePart_CancelTrueFalseFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, true, false, false);
	}

	public void testSavePart_CancelFalseTrueTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, false, true, true);
	}

	public void testSavePart_CancelFalseTrueFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, false, true, false);
	}

	public void testSavePart_CancelFalseFalseTrue() {
		testSavePart(ISaveHandler.Save.CANCEL, false, false, true);
	}

	public void testSavePart_CancelFalseFalseFalse() {
		testSavePart(ISaveHandler.Save.CANCEL, false, false, false);
	}

	private void testSavePart_NoHandler(boolean beforeDirty,
			boolean throwException, boolean confirm) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPart saveablePart = BasicFactoryImpl.eINSTANCE.createPart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		// no handlers
		applicationContext.set(ISaveHandler.class.getName(), null);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		if (beforeDirty) {
			assertEquals(!throwException, partService.savePart(saveablePart,
					confirm));
		} else {
			assertTrue(
					"The part is not dirty, the save operation should have complete successfully",
					partService.savePart(saveablePart, confirm));
		}

		assertEquals(beforeDirty && throwException, saveablePart.isDirty());
		assertEquals(beforeDirty, editor.wasSaveCalled());
	}

	public void testSavePart_NoHandler_TTT() {
		testSavePart_NoHandler(true, true, true);
	}

	public void testSavePart_NoHandler_TTF() {
		testSavePart_NoHandler(true, true, false);
	}

	public void testSavePart_NoHandler_TFT() {
		testSavePart_NoHandler(true, false, true);
	}

	public void testSavePart_NoHandler_TFF() {
		testSavePart_NoHandler(true, false, false);
	}

	public void testSavePart_NoHandler_FTT() {
		testSavePart_NoHandler(false, true, true);
	}

	public void testSavePart_NoHandler_FTF() {
		testSavePart_NoHandler(false, true, false);
	}

	public void testSavePart_NoHandler_FFT() {
		testSavePart_NoHandler(false, false, true);
	}

	public void testSavePart_NoHandler_FFF() {
		testSavePart_NoHandler(false, false, false);
	}

	private MPart createSaveablePart(
			MElementContainer<MWindowElement> container, boolean beforeDirty) {
		MPart saveablePart = BasicFactoryImpl.eINSTANCE.createPart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		container.getChildren().add(saveablePart);
		return saveablePart;
	}

	private Save prompt(Save[] candidates, MPart partToTest, MPart part) {
		return partToTest == part ? candidates[0] : candidates[1];
	}

	private void testSaveAll(final Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] afterDirty, boolean success,
			boolean[] saveCalled, boolean[] throwException) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		final MPart saveablePart = createSaveablePart(window, beforeDirty[0]);
		final MPart saveablePart2 = createSaveablePart(window, beforeDirty[1]);

		// setup the context
		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException[0]);

		ClientEditor editor2 = (ClientEditor) saveablePart2.getObject();
		editor2.setThrowException(throwException[1]);

		window.getContext().set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(Collection<MPart> saveableParts) {
						int index = 0;
						Save[] prompt = new Save[saveableParts.size()];
						Iterator<MPart> it = saveableParts.iterator();
						while (it.hasNext()) {
							prompt[index] = prompt(returnValues, it.next(),
									saveablePart);
							index++;
						}
						return prompt;
					}

					public Save promptToSave(MPart saveablePart) {
						return null;
					}
				});

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
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

	private boolean isSuccessful(Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] throwException) {
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

	private boolean[] afterDirty(Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] throwException) {
		if (confirm) {
			if (returnValues[0] == Save.YES) {
				if (returnValues[1] == Save.YES) {
					if (beforeDirty[0]) {
						return new boolean[] {
								throwException[0],
								beforeDirty[1] ? throwException[0]
										|| throwException[1] : false };
					}
					return new boolean[] { beforeDirty[0],
							beforeDirty[1] ? throwException[1] : false };
				}
				return new boolean[] {
						beforeDirty[0] ? throwException[0] : false,
						beforeDirty[1] };
			} else if (returnValues[1] == Save.YES) {
				return new boolean[] { beforeDirty[0],
						beforeDirty[1] ? throwException[1] : false };
			}
			return beforeDirty;
		}
		return afterDirty(beforeDirty, throwException);
	}

	private boolean[] afterDirty(boolean[] beforeDirty, boolean[] throwException) {
		if (beforeDirty[0]) {
			if (beforeDirty[1]) {
				return new boolean[] { throwException[0],
						throwException[0] || throwException[1] };
			}
			return new boolean[] { throwException[0], false };
		} else if (beforeDirty[1]) {
			return new boolean[] { false, throwException[1] };
		}
		return new boolean[] { false, false };
	}

	private boolean[] saveCalled(Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] throwException) {
		if (confirm) {
			if (returnValues[0] == Save.YES) {
				if (returnValues[1] == Save.YES) {
					if (beforeDirty[0]) {
						return new boolean[] { true,
								!throwException[0] && beforeDirty[1] };
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
		return new boolean[] {
				beforeDirty[0],
				beforeDirty[0] ? !throwException[0] && beforeDirty[1]
						: beforeDirty[1] };
	}

	private void testSaveAll(Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] throwException) {
		if (hasCancel(returnValues, beforeDirty) && confirm) {
			testSaveAll(returnValues, confirm, beforeDirty, beforeDirty, false,
					new boolean[] { false, false }, throwException);
		} else {
			testSaveAll(returnValues, confirm, beforeDirty, afterDirty(
					returnValues, confirm, beforeDirty, throwException),
					isSuccessful(returnValues, confirm, beforeDirty,
							throwException), saveCalled(returnValues, confirm,
							beforeDirty, throwException), throwException);
		}
	}

	public void testSaveAll_YY_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YY_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YY_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YY_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YY_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YY_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YY_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YN_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YN_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YN_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YN_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YN_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YN_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YN_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YC_True_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_True_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_True_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_True_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_True_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_True_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_True_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YC_True_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_True_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_True_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_True_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_True_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_True_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_True_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YC_False_TT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_False_TT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_False_TT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_False_TF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_False_TF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_False_TF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_False_TF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_YC_False_FT_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_False_FT_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_False_FT_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_False_FF_TT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_YC_False_FF_TF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_YC_False_FF_FT() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_YC_False_FF_FF() {
		testSaveAll(new Save[] { Save.YES, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NY_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NY_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NY_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NY_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NY_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NY_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NY_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NN_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NN_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NN_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NN_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NN_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NN_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NN_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NC_True_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_True_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_True_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_True_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_True_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_True_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_True_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NC_True_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_True_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_True_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_True_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_True_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_True_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_True_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NC_False_TT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_False_TT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_False_TT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_False_TF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_False_TF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_False_TF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_False_TF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_NC_False_FT_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_False_FT_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_False_FT_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_False_FF_TT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_NC_False_FF_TF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_NC_False_FF_FT() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_NC_False_FF_FF() {
		testSaveAll(new Save[] { Save.NO, Save.CANCEL }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CY_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CY_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CY_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CY_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CY_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CY_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CY_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.YES }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CN_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CN_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, true, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CN_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CN_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CN_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CN_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CN_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.NO }, false, new boolean[] {
				false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CC_True_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_True_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_True_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_True_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_True_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_True_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_True_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CC_True_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_True_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_True_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_True_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_True_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_True_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_True_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, true,
				new boolean[] { false, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CC_False_TT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_False_TT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_False_TT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_False_TF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_False_TF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_False_TF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_False_TF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { true, false }, new boolean[] { false, false });
	}

	public void testSaveAll_CC_False_FT_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, true }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_False_FT_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, true }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_False_FT_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, true }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_False_FF_TT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, false }, new boolean[] { true, true });
	}

	public void testSaveAll_CC_False_FF_TF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, false }, new boolean[] { true, false });
	}

	public void testSaveAll_CC_False_FF_FT() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, false }, new boolean[] { false, true });
	}

	public void testSaveAll_CC_False_FF_FF() {
		testSaveAll(new Save[] { Save.CANCEL, Save.CANCEL }, false,
				new boolean[] { false, false }, new boolean[] { false, false });
	}

	private void testSaveAll_NoHandler(boolean beforeDirty,
			boolean throwException, boolean confirm) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MPart saveablePart = BasicFactoryImpl.eINSTANCE.createPart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		// no handlers
		applicationContext.set(ISaveHandler.class.getName(), null);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		if (beforeDirty) {
			assertEquals(!throwException, partService.saveAll(confirm));
		} else {
			assertTrue(
					"The part is not dirty, the save operation should have complete successfully",
					partService.saveAll(confirm));
		}

		assertEquals(beforeDirty && throwException, saveablePart.isDirty());
		assertEquals(beforeDirty, editor.wasSaveCalled());
	}

	public void testSaveAll_NoHandler_TTT() {
		testSaveAll_NoHandler(true, true, true);
	}

	public void testSaveAll_NoHandler_TTF() {
		testSaveAll_NoHandler(true, true, false);
	}

	public void testSaveAll_NoHandler_TFT() {
		testSaveAll_NoHandler(true, false, true);
	}

	public void testSaveAll_NoHandler_TFF() {
		testSaveAll_NoHandler(true, false, false);
	}

	public void testSaveAll_NoHandler_FTT() {
		testSaveAll_NoHandler(false, true, true);
	}

	public void testSaveAll_NoHandler_FTF() {
		testSaveAll_NoHandler(false, true, false);
	}

	public void testSaveAll_NoHandler_FFT() {
		testSaveAll_NoHandler(false, false, true);
	}

	public void testSaveAll_NoHandler_FFF() {
		testSaveAll_NoHandler(false, false, false);
	}

	private void testSaveAll_NoHandlers(boolean confirm, boolean[] beforeDirty,
			boolean[] afterDirty, boolean success, boolean[] saveCalled,
			boolean[] throwException) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		final MPart saveablePart = createSaveablePart(window, beforeDirty[0]);
		final MPart saveablePart2 = createSaveablePart(window, beforeDirty[1]);

		// setup the context
		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException[0]);

		ClientEditor editor2 = (ClientEditor) saveablePart2.getObject();
		editor2.setThrowException(throwException[1]);

		window.getContext().set(ISaveHandler.class.getName(), null);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertEquals(success, partService.saveAll(confirm));

		assertEquals(afterDirty[0], saveablePart.isDirty());
		assertEquals(saveCalled[0], editor.wasSaveCalled());

		assertEquals(afterDirty[1], saveablePart2.isDirty());
		assertEquals(saveCalled[1], editor2.wasSaveCalled());
	}

	private void testSaveAll_NoHandlers(boolean confirm, boolean[] beforeDirty,
			boolean[] throwException) {
		testSaveAll_NoHandlers(confirm, beforeDirty, afterDirty(beforeDirty,
				throwException), isSuccessful(beforeDirty, throwException),
				saveCalled(beforeDirty, throwException), throwException);
	}

	public void testSaveAll_NoHandlers_T_TT_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_T_TT_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_T_TT_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_T_TT_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, true },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_T_TF_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_T_TF_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_T_TF_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_T_TF_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_T_FT_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_T_FT_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_T_FT_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_T_FT_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, true },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_T_FF_TT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_T_FF_TF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_T_FF_FT() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_T_FF_FF() {
		testSaveAll_NoHandlers(true, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_F_TT_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_F_TT_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_F_TT_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_F_TT_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, true },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_F_TF_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_F_TF_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_F_TF_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_F_TF_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { true, false },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_F_FT_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_F_FT_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_F_FT_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_F_FT_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, true },
				new boolean[] { false, false });
	}

	public void testSaveAll_NoHandlers_F_FF_TT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false },
				new boolean[] { true, true });
	}

	public void testSaveAll_NoHandlers_F_FF_TF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false },
				new boolean[] { true, false });
	}

	public void testSaveAll_NoHandlers_F_FF_FT() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false },
				new boolean[] { false, true });
	}

	public void testSaveAll_NoHandlers_F_FF_FF() {
		testSaveAll_NoHandlers(false, new boolean[] { false, false },
				new boolean[] { false, false });
	}

	public void testSwitchWindows() {
		// create an application with two windows
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window1 = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window1);
		application.getChildren().add(window2);
		application.setSelectedElement(window1);

		// place a part in the first window
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window1.getChildren().add(part);
		window1.setSelectedElement(part);

		// setup the context
		initialize(applicationContext, application);

		// render the windows
		getEngine().createGui(window1);
		getEngine().createGui(window2);

		EPartService windowService1 = (EPartService) window1.getContext().get(
				EPartService.class.getName());
		EPartService windowService2 = (EPartService) window2.getContext().get(
				EPartService.class.getName());

		assertNotNull(windowService1);
		assertNotNull(windowService2);

		assertNotNull("The first part is active in the first window",
				windowService1.getActivePart());
		assertNull("There should be nothing active in the second window",
				windowService2.getActivePart());

		// activate the part
		windowService1.activate(part);

		assertEquals("The part should have been activated", part,
				windowService1.getActivePart());
		assertNull("The second window has no parts, this should be null",
				windowService2.getActivePart());

		// now move the part over from the first window to the second window
		windowService1.deactivate(part);
		window2.getChildren().add(part);
		part.getContext().set(IContextConstants.PARENT, window2.getContext());
		// activate the part
		windowService2.activate(part);

		assertEquals("No parts in this window, this should be null", null,
				windowService1.getActivePart());
		assertEquals("We activated it just now, this should be active", part,
				windowService2.getActivePart());
	}

	public void testApplicationContextHasActivePart() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		// setup the context
		initialize(applicationContext, application);

		// render the windows
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

		partService.activate(partA);

		Object o = applicationContext.get(IServiceConstants.ACTIVE_PART);
		assertEquals(partA, o);

		partService.activate(partB);

		o = applicationContext.get(IServiceConstants.ACTIVE_PART);
		assertEquals(partB, o);
	}

	private void testShowPart_Bug307747(PartState partState) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MPartDescriptor partDescriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setElementId("partId");
		partDescriptor.setCategory("category");
		application.getDescriptors().add(partDescriptor);

		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		// create a stack
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.setElementId("category");
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		// setup the context
		initialize(applicationContext, application);
		// render the window
		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		MPart partB = partService.showPart("partId", partState);

		// showPart should instantiate the part
		assertNotNull("The part should have been rendered", partB.getContext());
	}

	public void testShowPart_Bug307747_CREATE() {
		testShowPart_Bug307747(PartState.CREATE);
	}

	public void testShowPart_Bug307747_VISIBLE() {
		testShowPart_Bug307747(PartState.VISIBLE);
	}

	public void testShowPart_Bug307747_ACTIVATE() {
		testShowPart_Bug307747(PartState.ACTIVATE);
	}

	private MApplication createApplication(String partId) {
		return createApplication(new String[] { partId });
	}

	private MApplication createApplication(String... partIds) {
		return createApplication(new String[][] { partIds });
	}

	private MApplication createApplication(String[]... partIds) {
		return createApplication(partIds.length, partIds);
	}

	private MApplication createApplication(int windows, String[][] partIds) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		for (int i = 0; i < windows; i++) {
			MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
			application.getChildren().add(window);

			MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
			window.getChildren().add(partStack);

			for (int j = 0; j < partIds[i].length; j++) {
				MPart part = BasicFactoryImpl.eINSTANCE.createPart();
				part.setElementId(partIds[i][j]);
				partStack.getChildren().add(part);
			}
		}

		initialize(applicationContext, application);

		return application;
	}

	private void initialize(IEclipseContext applicationContext,
			MApplication application) {
		application.setContext(applicationContext);
		applicationContext.set(MApplication.class.getName(), application);
		E4Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		applicationContext.set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(Collection<MPart> saveablePart) {
						Save[] ret = new Save[saveablePart.size()];
						Arrays.fill(ret, ISaveHandler.Save.YES);
						return ret;
					}

					public Save promptToSave(MPart saveablePart) {
						return ISaveHandler.Save.YES;
					}
				});
	}

	class PartListener implements IPartListener {

		private List<MPart> activatedParts = new ArrayList<MPart>();
		private List<MPart> deactivatedParts = new ArrayList<MPart>();
		private List<MPart> hiddenParts = new ArrayList<MPart>();
		private List<MPart> visibleParts = new ArrayList<MPart>();

		private int activated = 0;
		private int deactivated = 0;
		private int hidden = 0;
		private int visible = 0;

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

		public void partActivated(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			activated++;
			activatedParts.add(part);
		}

		public void partBroughtToTop(MPart part) {

		}

		public void partDeactivated(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			deactivated++;
			deactivatedParts.add(part);
		}

		public void partHidden(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			hidden++;
			hiddenParts.add(part);
		}

		public void partVisible(MPart part) {
			if (valid && part == null) {
				valid = false;
			}
			visible++;
			visibleParts.add(part);
		}

	}
}
