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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MSaveablePartTest {
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
	public void testCreateView() {
		final MWindow window = createWindowWithOneView("Part Name");

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);
		assertEquals("Part Name", item.getText());

		assertFalse(part.isDirty());

		part.setDirty(true);
		assertEquals("*Part Name", item.getText());

		part.setDirty(false);
		assertEquals("Part Name", item.getText());
	}

	private MWindow createWindowWithOneView(String partName) {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(sash);
		MPartStack stack = ems.createModelElement(MPartStack.class);
		sash.getChildren().add(stack);
		MPart contributedPart = ems.createModelElement(MPart.class);
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
