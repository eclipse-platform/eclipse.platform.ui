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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.ISaveHandler;
import org.eclipse.e4.workbench.modeling.ISaveHandler.Save;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.UIEventPublisher;
import org.eclipse.e4.workbench.ui.internal.Workbench;
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
		partStack.setActiveChild(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partFront);
		assertEquals(partStack.getActiveChild(), partFront);
	}

	public void testBringToTop_PartOnTop_myService() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		partStack.setActiveChild(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) partFront.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partFront);
		assertEquals(partStack.getActiveChild(), partFront);
	}

	public void testBringToTop_PartNotOnTop() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setActiveChild(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partBack);
		assertEquals(partStack.getActiveChild(), partBack);
	}

	public void testBringToTop_PartNotOnTop_myService() {
		MApplication application = createApplication("partFront", "partBack");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart partFront = partStack.getChildren().get(0);
		MPart partBack = partStack.getChildren().get(1);
		partStack.setActiveChild(partFront);

		getEngine().createGui(window);

		EPartService partService = (EPartService) partFront.getContext().get(
				EPartService.class.getName());

		partService.bringToTop(partBack);
		assertEquals(partStack.getActiveChild(), partBack);
	}

	public void testBringToTop_PartInAnotherWindow() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setActiveChild(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) windowA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) windowB.getContext().get(
				EPartService.class.getName());

		partServiceA.bringToTop(partBackB);
		assertEquals(partStackA.getActiveChild(), partFrontA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceB.bringToTop(partBackA);
		assertEquals(partStackA.getActiveChild(), partFrontA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceA.bringToTop(partBackA);
		assertEquals(partStackA.getActiveChild(), partBackA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceB.bringToTop(partBackB);
		assertEquals(partStackA.getActiveChild(), partBackA);
		assertEquals(partStackB.getActiveChild(), partBackB);
	}

	public void testBringToTop_PartInAnotherWindow_myService() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setActiveChild(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

		partServiceA.bringToTop(partBackB);
		assertEquals(partStackA.getActiveChild(), partFrontA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceB.bringToTop(partBackA);
		assertEquals(partStackA.getActiveChild(), partFrontA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceA.bringToTop(partBackA);
		assertEquals(partStackA.getActiveChild(), partBackA);
		assertEquals(partStackB.getActiveChild(), partFrontB);

		partServiceB.bringToTop(partBackB);
		assertEquals(partStackA.getActiveChild(), partBackA);
		assertEquals(partStackB.getActiveChild(), partBackB);
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

	public void testIsVisible_ViewVisible() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = partStack.getChildren().get(0);
		partStack.setActiveChild(part);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertTrue(partService.isPartVisible(part));
	}

	public void testIsVisible_ViewVisible_myService() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		MPart part = partStack.getChildren().get(0);
		partStack.setActiveChild(part);

		getEngine().createGui(window);

		EPartService partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertTrue(partService.isPartVisible(part));
	}

	public void testIsVisible_ViewNotVisible() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setActiveChild(partStack.getChildren().get(0));

		getEngine().createGui(window);

		MPart part = partStack.getChildren().get(1);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		assertFalse(partService.isPartVisible(part));
	}

	public void testIsVisible_ViewNotVisible_myService() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setActiveChild(partStack.getChildren().get(0));

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

	public void testIsVisible_ViewInAnotherWindow() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setActiveChild(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

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

	public void testIsVisible_ViewInAnotherWindow_myService() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setActiveChild(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

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
		partStack.setActiveChild(partStack.getChildren().get(0));

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
		partStackA.setActiveChild(partFrontA);
		windowA.setActiveChild(partStackA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);
		windowB.setActiveChild(partStackB);

		application.setActiveChild(windowA);

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

	public void testActivate_partService_activeChild() {
		MApplication application = createApplication(new String[] {
				"partFrontA", "partBackA" }, new String[] { "partFrontB",
				"partBackB" });

		MWindow windowA = application.getChildren().get(0);
		MPartStack partStackA = (MPartStack) windowA.getChildren().get(0);
		MPart partFrontA = partStackA.getChildren().get(0);
		MPart partBackA = partStackA.getChildren().get(1);
		partStackA.setActiveChild(partFrontA);

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

		getEngine().createGui(windowA);
		getEngine().createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partFrontB.getContext().get(
				EPartService.class.getName());

		partServiceA.activate(partBackA);

		assertEquals(windowA, application.getActiveChild());
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
		assertEquals(windowB, application.getActiveChild());
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

		assertEquals(windowA, application.getActiveChild());
		MPart shouldBeCorrect = (MPart) partFrontA.getContext().get(
				IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackA, partServiceA.getActivePart());

		partServiceB.activate(partBackB);
		assertEquals(windowB, application.getActiveChild());
		shouldBeCorrect = (MPart) partFrontB.getContext().get(
				IServiceConstants.ACTIVE_PART);
		assertNotNull(shouldBeCorrect);
		assertEquals(partBackB, partServiceB.getActivePart());
	}

	public void testShowPart() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");
		assertNotNull(part);
		assertEquals("partId", part.getId());
		assertEquals(part, partService.getActivePart());
		assertTrue("Shown part should be visible", part.isVisible());
	}

	public void testShowPart_PartAlreadyShown() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);
		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partId");
		application.getDescriptors().add(partDescriptor);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");
		assertNotNull(part);
		assertEquals("partId", part.getId());
		assertEquals(part, partService.getActivePart());

		MPart part2 = partService.showPart("partId");
		assertEquals("Should not have instantiated a new MPart", part, part2);
		assertEquals(part, partService.getActivePart());
	}

	public void testShowPart_DefinedCategoryStackNotExists() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = MApplicationFactory.eINSTANCE.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setId("partId2");
		application.getDescriptors().add(partDescriptor);

		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPartStack);

		MPartStack stack = (MPartStack) window.getChildren().get(0);
		assertEquals("categoryId", stack.getId());

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, stack.getActiveChild());

		MPart part2 = partService.showPart("partId2");
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
		assertEquals(part2, stack.getActiveChild());
	}

	public void testShowPart_DefinedCategoryStackExists() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setId("partId");
		application.getDescriptors().add(partDescriptor);

		partDescriptor = MApplicationFactory.eINSTANCE.createPartDescriptor();
		partDescriptor.setCategory("categoryId");
		partDescriptor.setId("partId2");
		application.getDescriptors().add(partDescriptor);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId("categoryId");
		window.getChildren().add(stack);

		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");
		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part, stack.getActiveChild());

		MPart part2 = partService.showPart("partId2");
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
		assertEquals(part2, stack.getActiveChild());
	}

	public void testShowPart_CREATE() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStackA = MApplicationFactory.eINSTANCE.createPartStack();
		MPartStack partStackB = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = MApplicationFactory.eINSTANCE.createPart();
		MPart partA2 = MApplicationFactory.eINSTANCE.createPart();
		partA1.setId("partA1");
		partA2.setId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = MApplicationFactory.eINSTANCE.createPart();
		MPart partB2 = MApplicationFactory.eINSTANCE.createPart();
		partB1.setId("partB1");
		partB2.setId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setActiveChild(partA1);
		partStackB.setActiveChild(partB1);
		window.setActiveChild(partStackA);

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

	public void testShowPart_CREATE2() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = MApplicationFactory.eINSTANCE.createPartStack();
		partStack.setId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partStack.getChildren().add(partA);

		partStack.setActiveChild(partA);
		window.setActiveChild(partStack);
		application.setActiveChild(window);

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

	public void testShowPart_CREATE3() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		MPartStack partStackB = MApplicationFactory.eINSTANCE.createPartStack();
		partStackB.setId("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setActiveChild(partA);
		window.setActiveChild(partStackA);
		application.setActiveChild(window);

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

	public void testShowPart_VISIBLE() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStackA = MApplicationFactory.eINSTANCE.createPartStack();
		MPartStack partStackB = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		window.getChildren().add(partStackB);

		MPart partA1 = MApplicationFactory.eINSTANCE.createPart();
		MPart partA2 = MApplicationFactory.eINSTANCE.createPart();
		partA1.setId("partA1");
		partA2.setId("partA2");
		partStackA.getChildren().add(partA1);
		partStackA.getChildren().add(partA2);

		MPart partB1 = MApplicationFactory.eINSTANCE.createPart();
		MPart partB2 = MApplicationFactory.eINSTANCE.createPart();
		partB1.setId("partB1");
		partB2.setId("partB2");
		partStackB.getChildren().add(partB1);
		partStackB.getChildren().add(partB2);

		partStackA.setActiveChild(partA1);
		partStackB.setActiveChild(partB1);
		window.setActiveChild(partStackA);

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

	public void testShowPart_VISIBLE2() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStack = MApplicationFactory.eINSTANCE.createPartStack();
		partStack.setId("aCategory");
		window.getChildren().add(partStack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partStack.getChildren().add(partA);

		partStack.setActiveChild(partA);
		window.setActiveChild(partStack);
		application.setActiveChild(window);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		partService.activate(partA);
		assertEquals(partA, partService.getActivePart());

		MPart partB = partService.showPart("partB",
				EPartService.PartState.VISIBLE);

		assertEquals(2, partStack.getChildren().size());
		assertFalse(
				"The part is in the same stack as the active part, so the active part should not have changed",
				partB.equals(partService.getActivePart()));
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(partService.isPartVisible(partA));
		assertFalse(partService.isPartVisible(partB));
	}

	public void testShowPart_VISIBLE3() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartDescriptor partDescriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		partDescriptor.setId("partB");
		partDescriptor.setCategory("aCategory");
		application.getDescriptors().add(partDescriptor);

		MPartStack partStackA = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStackA);
		MPartStack partStackB = MApplicationFactory.eINSTANCE.createPartStack();
		partStackB.setId("aCategory");
		window.getChildren().add(partStackB);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partStackA.getChildren().add(partA);

		partStackA.setActiveChild(partA);
		window.setActiveChild(partStackA);
		application.setActiveChild(window);

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
				"Only creating the part, the active part should not have changed",
				partA, partService.getActivePart());
		assertNotNull("The shown part should have a context", partB
				.getContext());
		assertTrue(
				"The part is the only one in the stack, it should be visible",
				partService.isPartVisible(partB));
	}

	public void testGetSaveableParts() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> saveableParts = partService
				.getSaveableParts();
		assertNotNull(saveableParts);
		assertEquals(0, saveableParts.size());
	}

	public void testGetSaveableParts2() {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> saveableParts = partService
				.getSaveableParts();
		assertNotNull(saveableParts);
		assertEquals(0, saveableParts.size());
	}

	public void testGetSaveableParts3() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MSaveablePart saveablePart = MApplicationFactory.eINSTANCE
				.createSaveablePart();
		window.getChildren().add(saveablePart);

		// setup the context
		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> saveableParts = partService
				.getSaveableParts();
		assertNotNull(saveableParts);
		assertEquals(1, saveableParts.size());
	}

	public void testGetDirtyParts() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	public void testGetDirtyParts2() {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	private void testGetDirtyParts3(boolean before, boolean after) {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MSaveablePart saveablePart = MApplicationFactory.eINSTANCE
				.createSaveablePart();
		saveablePart.setDirty(before);
		window.getChildren().add(saveablePart);

		// setup the context
		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		getEngine().createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> dirtyParts = partService.getDirtyParts();
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

	private void testSavePart(final Save returnValue, boolean confirm,
			boolean beforeDirty, boolean afterDirty, boolean success,
			boolean saveCalled, boolean throwException) {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		MSaveablePart saveablePart = MApplicationFactory.eINSTANCE
				.createSaveablePart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		window.getChildren().add(saveablePart);

		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException);

		window.getContext().set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(
							Collection<MSaveablePart> saveablePart) {
						return null;
					}

					public Save promptToSave(MSaveablePart saveablePart) {
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

	private MSaveablePart createSaveablePart(
			MElementContainer<MPSCElement> container, boolean beforeDirty) {
		MSaveablePart saveablePart = MApplicationFactory.eINSTANCE
				.createSaveablePart();
		saveablePart.setDirty(beforeDirty);
		saveablePart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.ClientEditor");
		container.getChildren().add(saveablePart);
		return saveablePart;
	}

	private Save prompt(Save[] candidates, MSaveablePart partToTest,
			MSaveablePart part) {
		return partToTest == part ? candidates[0] : candidates[1];
	}

	private void testSaveAll(final Save[] returnValues, boolean confirm,
			boolean[] beforeDirty, boolean[] afterDirty, boolean success,
			boolean[] saveCalled, boolean[] throwException) {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		final MSaveablePart saveablePart = createSaveablePart(window,
				beforeDirty[0]);
		final MSaveablePart saveablePart2 = createSaveablePart(window,
				beforeDirty[1]);

		// setup the context
		initialize(applicationContext, application);

		getEngine().createGui(window);

		ClientEditor editor = (ClientEditor) saveablePart.getObject();
		editor.setThrowException(throwException[0]);

		ClientEditor editor2 = (ClientEditor) saveablePart2.getObject();
		editor2.setThrowException(throwException[1]);

		window.getContext().set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(
							Collection<MSaveablePart> saveableParts) {
						int index = 0;
						Save[] prompt = new Save[saveableParts.size()];
						Iterator<MSaveablePart> it = saveableParts.iterator();
						while (it.hasNext()) {
							prompt[index] = prompt(returnValues, it.next(),
									saveablePart);
							index++;
						}
						return prompt;
					}

					public Save promptToSave(MSaveablePart saveablePart) {
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
		} else if (beforeDirty[0]) {
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
		} else if (beforeDirty[0]) {
			if (beforeDirty[1]) {
				return new boolean[] { throwException[0], throwException[1] };
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
		return beforeDirty;
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

	public void testSwitchWindows() {
		// create an application with two windows
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window1 = MApplicationFactory.eINSTANCE.createWindow();
		MWindow window2 = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window1);
		application.getChildren().add(window2);
		application.setActiveChild(window1);

		// place a part in the first window
		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window1.getChildren().add(part);
		window1.setActiveChild(part);

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
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();

		for (int i = 0; i < windows; i++) {
			MWindow window = MApplicationFactory.eINSTANCE.createWindow();
			application.getChildren().add(window);

			MPartStack partStack = MApplicationFactory.eINSTANCE
					.createPartStack();
			window.getChildren().add(partStack);

			for (int j = 0; j < partIds[i].length; j++) {
				MPart part = MApplicationFactory.eINSTANCE.createPart();
				part.setId(partIds[i][j]);
				partStack.getChildren().add(part);
			}
		}

		initialize(applicationContext, application);

		return application;
	}

	private void initialize(IEclipseContext applicationContext,
			MApplication application) {
		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		applicationContext.set(ISaveHandler.class.getName(),
				new ISaveHandler() {
					public Save[] promptToSave(
							Collection<MSaveablePart> saveablePart) {
						Save[] ret = new Save[saveablePart.size()];
						Arrays.fill(ret, ISaveHandler.Save.YES);
						return ret;
					}

					public Save promptToSave(MSaveablePart saveablePart) {
						return ISaveHandler.Save.YES;
					}
				});
	}
}
