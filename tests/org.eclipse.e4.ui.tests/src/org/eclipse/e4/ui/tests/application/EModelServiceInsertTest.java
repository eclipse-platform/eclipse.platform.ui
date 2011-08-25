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

import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class EModelServiceInsertTest extends TestCase {

	private IEclipseContext applicationContext;

	MApplication app = null;

	@Override
	protected void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();
	}

	private MApplication createSimpleApplication() {
		MApplication app = ApplicationFactoryImpl.eINSTANCE.createApplication();
		app.setContext(applicationContext);
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		window.setElementId("main.Window");
		app.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.setElementId("theStack");
		window.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		part1.setElementId("part1");
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		part2.setElementId("part2");
		stack.getChildren().add(part2);

		return app;
	}

	private MApplication createApplication() {
		MApplication app = ApplicationFactoryImpl.eINSTANCE.createApplication();
		app.setContext(applicationContext);
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		window.setElementId("main.Window");
		app.getChildren().add(window);

		MPartSashContainer psc = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		psc.setHorizontal(true);
		psc.setElementId("topSash");
		window.getChildren().add(psc);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		stack.setElementId("theStack");
		psc.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		part1.setElementId("part1");
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		part2.setElementId("part2");
		stack.getChildren().add(part2);

		return app;
	}

	private void testInsert(MApplication app, String relToId, int where,
			int ratio) {
		EModelService modelService = (EModelService) app.getContext().get(
				EModelService.class.getName());
		assertNotNull(modelService);

		MUIElement relTo = modelService.find(relToId, app);

		MPart newPart = BasicFactoryImpl.eINSTANCE.createPart();
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

	public void testSimpleInsertAbove() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.ABOVE, 25);
	}

	public void testSimpleInsertBelow() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.BELOW, 25);
	}

	public void testSimpleInsertLeftOf() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.LEFT_OF, 25);
	}

	public void testSimpleInsertRightOf() {
		MApplication application = createSimpleApplication();
		testInsert(application, "theStack", EModelService.RIGHT_OF, 25);
	}

	public void testInsertAbove() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.ABOVE, 35);
	}

	public void testInsertBelow() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.BELOW, 35);
	}

	public void testInsertLeftOf() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.LEFT_OF, 35);
	}

	public void testInsertRightOf() {
		MApplication application = createApplication();
		testInsert(application, "theStack", EModelService.RIGHT_OF, 35);
	}
}
