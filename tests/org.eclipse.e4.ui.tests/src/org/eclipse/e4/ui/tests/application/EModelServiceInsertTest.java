/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EModelServiceInsertTest {

	private IEclipseContext applicationContext;

	MApplication app = null;

	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();
		ems = applicationContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		applicationContext.dispose();
	}

	private MApplication createSimpleApplication() {
		MApplication app = ems.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		MWindow window = ems.createModelElement(MWindow.class);
		window.setElementId("main.Window");
		app.getChildren().add(window);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.setElementId("theStack");
		window.getChildren().add(stack);

		MPart part1 = ems.createModelElement(MPart.class);
		part1.setElementId("part1");
		stack.getChildren().add(part1);

		MPart part2 = ems.createModelElement(MPart.class);
		part2.setElementId("part2");
		stack.getChildren().add(part2);

		return app;
	}

	private MApplication createApplication() {
		MApplication app = ems.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		MWindow window = ems.createModelElement(MWindow.class);
		window.setElementId("main.Window");
		app.getChildren().add(window);

		MPartSashContainer psc = ems.createModelElement(MPartSashContainer.class);
		psc.setHorizontal(true);
		psc.setElementId("topSash");
		window.getChildren().add(psc);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		stack.setElementId("theStack");
		psc.getChildren().add(stack);

		MPart part1 = ems.createModelElement(MPart.class);
		part1.setElementId("part1");
		stack.getChildren().add(part1);

		MPart part2 = ems.createModelElement(MPart.class);
		part2.setElementId("part2");
		stack.getChildren().add(part2);

		return app;
	}

	private void testInsert(MApplication app, String relToId, int where, float ratio) {
		EModelService modelService = app.getContext().get(EModelService.class);
		assertNotNull(modelService);

		MUIElement relTo = modelService.find(relToId, app);

		MPart newPart = ems.createModelElement(MPart.class);
		newPart.setElementId("newPart");

		modelService.insert(newPart, (MPartSashContainerElement) relTo, where,
				ratio);

		MUIElement newPartParent = newPart.getParent();
		assertTrue("parent must be a sash",
				(newPartParent instanceof MPartSashContainer));
		MPartSashContainer psc = (MPartSashContainer) newPartParent;

		boolean horizontal = where == EModelService.LEFT_OF
				|| where == EModelService.RIGHT_OF;
		assertTrue("invalid sash orientation", psc.isHorizontal() == horizontal);

		if (where == EModelService.LEFT_OF || where == EModelService.ABOVE) {
			assertTrue("new part should be first",
					psc.getChildren().indexOf(newPart) == 0);
			assertTrue("old part should be second",
					psc.getChildren().indexOf(relTo) == 1);
		} else {
			assertTrue("old part should be first",
					psc.getChildren().indexOf(relTo) == 0);
			assertTrue("new part should be second",
					psc.getChildren().indexOf(newPart) == 1);
		}
	}

	@Test
	public void testSimpleInsertAbove() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.ABOVE, .25f);
	}

	@Test
	public void testSimpleInsertBelow() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.BELOW, .25f);
	}

	@Test
	public void testSimpleInsertLeftOf() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.LEFT_OF, .25f);
	}

	@Test
	public void testSimpleInsertRightOf() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.RIGHT_OF, .25f);
	}

	@Test
	public void testInsertAbove() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.ABOVE, .35f);
	}

	@Test
	public void testInsertBelow() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.BELOW, .35f);
	}

	@Test
	public void testInsertLeftOf() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.LEFT_OF, .35f);
	}

	@Test
	public void testInsertRightOf() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.RIGHT_OF, .35f);
	}

	@Test
	public void testInsertRightOfSharedStack() {
		EModelService modelService = (EModelService) applicationContext.get(EModelService.class.getName());
		assertNotNull(modelService);
		app = modelService.createModelElement(MApplication.class);
		app.setContext(applicationContext);
		MWindow window = modelService.createModelElement(MWindow.class);
		window.setElementId("main.Window");
		app.getChildren().add(window);

		MPartSashContainer psc = modelService.createModelElement(MPartSashContainer.class);
		psc.setHorizontal(true);
		psc.setElementId("topSash");
		window.getChildren().add(psc);

		MPartStack sharedStack = modelService.createModelElement(MPartStack.class);
		sharedStack.setElementId("sharedStack");
		window.getSharedElements().add(sharedStack);

		MPart part1 = ems.createModelElement(MPart.class);
		part1.setElementId("part1");
		sharedStack.getChildren().add(part1);

		MPlaceholder sharedStackRef = modelService.createModelElement(MPlaceholder.class);
		sharedStackRef.setElementId(sharedStack.getElementId());
		sharedStackRef.setRef(sharedStack);

		psc.getChildren().add(sharedStackRef);
		// setup complete

		MPart newPart = modelService.createModelElement(MPart.class);
		newPart.setElementId("part2");

		modelService.insert(newPart, sharedStack, EModelService.BELOW, 0.5f);
	}
}
