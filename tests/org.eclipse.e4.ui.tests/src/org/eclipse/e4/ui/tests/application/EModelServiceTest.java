/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class EModelServiceTest extends TestCase {

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

	public void testGetPerspectiveFor_RegularElement() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(applicationContext);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(applicationContext);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(applicationContext);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(applicationContext);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(applicationContext);

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
}
