/*******************************************************************************
 * Copyright (c) 2015 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Manumitting Technologies Inc - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ElementMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test EModelService with its various IN_*_PERSPECTIVE and OUTSIDE_PERSPECTIVE
 * flags. See bug 450130.
 */
@RunWith(Parameterized.class)
public class EModelServicePerspectiveFindTest {
	@Parameters
	public static Object[] data() {
		return new Object[] { true, false };
	}

	/** If true, create simple app setup, otherwise a traditional setup */
	@Parameter
	public boolean simple;

	private IEclipseContext applicationContext;
	private EModelService modelService;
	private MApplication app = null;
	private MWindow window;
	private MPerspectiveStack perspectiveStack;
	private MPart partA;
	private MPart partB;
	private MPart outerPart;

	private Selector selectAll;

	@Before
	public void setUp() {
		applicationContext = E4Application.createDefaultContext();
		modelService = applicationContext.get(EModelService.class);
		selectAll = new ElementMatcher(null, null, (String) null);
		if (simple) {
			setupSimpleApplication();
		} else {
			setupWorkbenchApplication();
		}
	}

	@After
	public void tearDown() {
		applicationContext.dispose();
	}

	/**
	 * A simpler form of application setup as might be found in a new E4 app
	 */
	private void setupSimpleApplication() {
		app = modelService.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		window = modelService.createModelElement(MWindow.class);
		window.setElementId("singleValidId");
		app.getChildren().add(window);

		setupPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		outerPart = modelService.createModelElement(MPart.class);
		MPartStack outerPartStack = modelService.createModelElement(MPartStack.class);
		outerPartStack.getChildren().add(outerPart);
		window.getChildren().add(outerPartStack);

		window.setSelectedElement(perspectiveStack);
	}

	/**
	 * The form of application as might be found with an E3.x-based compat layer
	 */
	private void setupWorkbenchApplication() {
		app = modelService.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		window = modelService.createModelElement(MWindow.class);
		window.setElementId("singleValidId");
		app.getChildren().add(window);

		MPartSashContainer topPSC = modelService.createModelElement(MPartSashContainer.class);
		window.getChildren().add(topPSC);

		setupPerspectiveStack();
		topPSC.getChildren().add(perspectiveStack);

		MPartStack outerPartStack = modelService.createModelElement(MPartStack.class);
		outerPart = modelService.createModelElement(MPart.class);
		outerPartStack.getChildren().add(outerPart);
		topPSC.getChildren().add(outerPartStack);

		window.setSelectedElement(topPSC);
		topPSC.setSelectedElement(perspectiveStack);
	}

	/**
	 * Creates a perspective stack with two perspectives A and B, each with a
	 * part stack with a single part.
	 */
	private void setupPerspectiveStack() {
		perspectiveStack = modelService.createModelElement(MPerspectiveStack.class);

		MPerspective perspectiveA = modelService.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveA);
		MPartStack partStackA = modelService.createModelElement(MPartStack.class);
		perspectiveA.getChildren().add(partStackA);
		partA = modelService.createModelElement(MPart.class);
		partStackA.getChildren().add(partA);

		MPerspective perspectiveB = modelService.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspectiveB);
		MPartStack partStackB = modelService.createModelElement(MPartStack.class);
		perspectiveB.getChildren().add(partStackB);
		partB = modelService.createModelElement(MPart.class);
		partStackB.getChildren().add(partB);

		perspectiveStack.setSelectedElement(perspectiveA);
	}

	@Test
	public void testInActivePerspective() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.IN_ACTIVE_PERSPECTIVE,
				selectAll);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertEquals(partA, elements.get(0));
	}

	@Test
	public void testInAnyPerspective() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.IN_ANY_PERSPECTIVE,
				selectAll);
		assertNotNull(elements);
		assertEquals(2, elements.size());
		assertTrue(elements.contains(partA));
		assertTrue(elements.contains(partB));
	}

	@Test
	public void testOuterPerspective() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.OUTSIDE_PERSPECTIVE,
				selectAll);
		assertNotNull(elements);
		assertEquals(1, elements.size());
		assertTrue(elements.contains(outerPart));
	}

	@Test
	public void testInTrim() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.IN_TRIM, selectAll);
		assertNotNull(elements);
		assertEquals(0, elements.size());
	}

	@Test
	public void testPresentation() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.PRESENTATION, selectAll);
		assertNotNull(elements);
		assertEquals(2, elements.size());
		assertTrue(elements.contains(partA));
		assertTrue(elements.contains(outerPart));
	}

	@Test
	public void testAnywhere() {
		List<MPart> elements = modelService.findElements(window, MPart.class, EModelService.ANYWHERE, selectAll);
		assertNotNull(elements);
		assertEquals(3, elements.size());
		assertTrue(elements.contains(partA));
		assertTrue(elements.contains(partB));
		assertTrue(elements.contains(outerPart));
	}
}
