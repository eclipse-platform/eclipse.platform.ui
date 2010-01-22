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

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.workbench.modeling.EPartService;
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

		IContributionFactory contributionFactory = (IContributionFactory) applicationContext
				.get(IContributionFactory.class.getName());
		Object newEngine = contributionFactory.create(getEngineURI(),
				applicationContext);
		assertTrue(newEngine instanceof IPresentationEngine);
		applicationContext.set(IPresentationEngine.class.getName(), newEngine);

		engine = (IPresentationEngine) newEngine;

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

	public void testFindPart_PartInWindow() {
		MApplication application = createApplication("partId");

		MWindow window = application.getChildren().get(0);
		engine.createGui(window);

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
		engine.createGui(window);

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

		engine.createGui(window1);
		engine.createGui(window2);

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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

		EPartService partServiceA = (EPartService) partFrontA.getContext().get(
				EPartService.class.getName());
		EPartService partServiceB = (EPartService) partBackB.getContext().get(
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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(window);

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

		engine.createGui(window);

		EPartService partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertTrue(partService.isPartVisible(part));
	}

	public void testIsVisible_ViewNotVisible() {
		MApplication application = createApplication("partId", "partId2");

		MWindow window = application.getChildren().get(0);
		MPartStack partStack = (MPartStack) window.getChildren().get(0);
		partStack.setActiveChild(partStack.getChildren().get(0));

		engine.createGui(window);

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

		engine.createGui(window);

		MPart part = partStack.getChildren().get(1);

		EPartService partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertFalse(partService.isPartVisible(part));
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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(window);

		MPart part = partStack.getChildren().get(1);

		EPartService partService = (EPartService) part.getContext().get(
				EPartService.class.getName());
		assertFalse(partService.isPartVisible(part));

		partService.activate(part);
		assertTrue(partService.isPartVisible(part));
		assertEquals(part, partService.getActivePart());
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

		MWindow windowB = application.getChildren().get(1);
		MPartStack partStackB = (MPartStack) windowB.getChildren().get(0);
		MPart partFrontB = partStackB.getChildren().get(0);
		MPart partBackB = partStackB.getChildren().get(1);
		partStackB.setActiveChild(partFrontB);

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(windowA);
		engine.createGui(windowB);

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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPartStack);

		MPartStack stack = (MPartStack) window.getChildren().get(0);
		assertEquals("categoryId", stack.getId());

		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));

		MPart part2 = partService.showPart("partId2");
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
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

		engine.createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart part = partService.showPart("partId");
		assertEquals(1, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));

		MPart part2 = partService.showPart("partId2");
		assertEquals(2, stack.getChildren().size());
		assertEquals(part, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));
	}

	public void testGetSaveableParts() {
		MApplication application = createApplication(1, new String[1][0]);
		MWindow window = application.getChildren().get(0);

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(window);

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

		engine.createGui(window);

		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		Collection<MSaveablePart> dirtyParts = partService.getDirtyParts();
		assertNotNull(dirtyParts);
		assertEquals(0, dirtyParts.size());
	}

	public void testGetDirtyParts2() {
		MApplication application = createApplication("partId");
		MWindow window = application.getChildren().get(0);

		engine.createGui(window);

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

		engine.createGui(window);

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

	public void testSwitchWindows() {
		// create an application with two windows
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window1 = MApplicationFactory.eINSTANCE.createWindow();
		MWindow window2 = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window1);
		application.getChildren().add(window2);

		// place a part in the first window
		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window1.getChildren().add(part);

		// setup the context
		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		// render the windows
		engine.createGui(window1);
		engine.createGui(window2);

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

		assertNull("No parts in this window, this should be null",
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

		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));

		return application;
	}
}
