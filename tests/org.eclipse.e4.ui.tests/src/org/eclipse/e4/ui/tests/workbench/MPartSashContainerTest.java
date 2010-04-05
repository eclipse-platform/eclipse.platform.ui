/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MPartSashContainerTest extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}

		if (appContext instanceof IDisposable) {
			((IDisposable) appContext).dispose();
		}
	}

	protected Control[] getPresentationControls(Shell shell) {
		TrimmedPartLayout tpl = (TrimmedPartLayout) shell.getLayout();
		return tpl.clientArea.getChildren();
	}

	public void testPartSashContainer_Horizontal() {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();

		partSashContainer.setHorizontal(true);
		partA
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partB
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		window.getChildren().add(partSashContainer);
		partSashContainer.getChildren().add(partA);
		partSashContainer.getChildren().add(partB);

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = partSashContainer.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof SashForm);

		SashForm sashForm = (SashForm) widget;
		assertEquals(SWT.HORIZONTAL, sashForm.getOrientation());

		partSashContainer.setHorizontal(false);
		assertEquals(SWT.VERTICAL, sashForm.getOrientation());

		partSashContainer.setHorizontal(true);
		assertEquals(SWT.HORIZONTAL, sashForm.getOrientation());

		partSashContainer.setHorizontal(true);
		assertEquals(SWT.HORIZONTAL, sashForm.getOrientation());
	}
}
