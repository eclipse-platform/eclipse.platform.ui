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

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MPartSashContainerTest {
	protected IEclipseContext appContext;
	protected E4Workbench wb;
	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	@Test
	public void testPartSashContainer_Horizontal() {
		MWindow window = ems.createModelElement(MWindow.class);
		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		MPart partA = ems.createModelElement(MPart.class);
		MPart partB = ems.createModelElement(MPart.class);

		partSashContainer.setHorizontal(true);
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		window.getChildren().add(partSashContainer);
		partSashContainer.getChildren().add(partA);
		partSashContainer.getChildren().add(partB);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = partSashContainer.getWidget();
		assertNotNull(widget);
	}
}
