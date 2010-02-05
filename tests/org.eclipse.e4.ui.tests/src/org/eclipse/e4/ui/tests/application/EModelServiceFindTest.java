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
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.workbench.modeling.EModelService;

public class EModelServiceFindTest extends TestCase {

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
		if (applicationContext instanceof IDisposable) {
			((IDisposable) applicationContext).dispose();
		}
	}

	private MApplication createApplication() {
		MApplication app = MApplicationFactory.eINSTANCE.createApplication();
		app.setContext(applicationContext);
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setId("singleValidId");
		app.getChildren().add(window);

		MPartSashContainer psc = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		psc.setId("twoValidIds");
		psc.getTags().add("oneValidTag");
		window.getChildren().add(psc);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.getTags().add("twoValidTags");
		psc.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId("twoValidIds");
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.getTags().add("twoValidTags");
		part2.getTags().add("secondTag");
		stack.getChildren().add(part2);

		MPart part3 = MApplicationFactory.eINSTANCE.createPart();
		psc.getChildren().add(part3);

		return app;
	}

	public void testFindElementsIdOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MUIElement> elements = new ArrayList<MUIElement>();
		modelService.findAllElements(application, "singleValidId", null, null,
				elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		modelService.findAllElements(application, "twoValidIds", null, null,
				elements);
		assertEquals(elements.size(), 2);

		elements.clear();
		modelService.findAllElements(application, "invalidId", null, null,
				elements);
		assertEquals(elements.size(), 0);
	}

	public void testFindElementsTypeOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MUIElement> elements = new ArrayList<MUIElement>();
		modelService
				.findAllElements(application, null, "MPart", null, elements);
		assertEquals(elements.size(), 3);

		elements.clear();
		modelService.findAllElements(application, null, "MPartStack", null,
				elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		modelService.findAllElements(application, null, "invalidType", null,
				elements);
		assertEquals(elements.size(), 0);
	}

	public void testFindElementsTagsOnly() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<MUIElement> elements = new ArrayList<MUIElement>();

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");
		modelService.findAllElements(application, null, null, tags, elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		tags.clear();
		tags.add("twoValidTags");
		modelService.findAllElements(application, null, null, tags, elements);
		assertEquals(elements.size(), 2);

		elements.clear();
		tags.clear();
		tags.add("invalidTag");
		modelService.findAllElements(application, null, null, tags, elements);
		assertEquals(elements.size(), 0);

		elements.clear();
		tags.clear();
		tags.add("twoValidTags");
		tags.add("secondTag");
		modelService.findAllElements(application, null, null, tags, elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		tags.clear();
		tags.add("oneValidTag");
		tags.add("secondTag");
		modelService.findAllElements(application, null, null, tags, elements);
		assertEquals(elements.size(), 0);
	}

	public void testFindElementsCombinations() {
		MApplication application = createApplication();

		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());
		assertNotNull(modelService);

		List<String> tags = new ArrayList<String>();
		tags.add("oneValidTag");

		List<MUIElement> elements = new ArrayList<MUIElement>();
		modelService.findAllElements(application, "twoValidIds",
				"MPartSashContainer", tags, elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		modelService.findAllElements(application, null, "MPartSashContainer",
				tags, elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		modelService.findAllElements(application, "twoValidIds", null, tags,
				elements);
		assertEquals(elements.size(), 1);

		elements.clear();
		modelService.findAllElements(application, "twoValidIds",
				"MPartSashContainer", null, elements);
		assertEquals(elements.size(), 1);
	}
}
