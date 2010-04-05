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
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class MSaveablePartTest extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
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

	public void testCreateView() {
		final MWindow window = createWindowWithOneView("Part Name");

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);
		Shell shell = (Shell) topWidget;
		assertEquals("MyWindow", shell.getText());
		Control[] controls = getPresentationControls(shell);
		assertEquals(1, controls.length);
		SashForm sash = (SashForm) controls[0];
		Control[] sashChildren = sash.getChildren();
		assertEquals(1, sashChildren.length);

		CTabFolder folder = (CTabFolder) sashChildren[0];
		CTabItem item = folder.getItem(0);
		assertEquals("Part Name", item.getText());

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);
		assertFalse(part.isDirty());

		part.setDirty(true);
		assertEquals("*Part Name", item.getText());

		part.setDirty(false);
		assertEquals("Part Name", item.getText());
	}

	private MWindow createWindowWithOneView(String partName) {
		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);
		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		sash.getChildren().add(stack);
		MPart contributedPart = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
