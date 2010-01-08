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

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class MPartTest extends TestCase {
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

	public void testSetName() {
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

		// HACK: see bug #280632 - always a composite around
		// CTabFolder so can implement margins
		Composite marginHolder = (Composite) sashChildren[0];
		assertEquals(1, marginHolder.getChildren().length);
		CTabFolder folder = (CTabFolder) marginHolder.getChildren()[0];
		CTabItem item = folder.getItem(0);
		assertEquals("Part Name", item.getText());

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		part.setLabel("Another Name");
		assertEquals("Another Name", item.getText());
	}

	public void testCTabItem_GetImage() {
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
		Control[] controls = getPresentationControls(shell);
		assertEquals(1, controls.length);
		SashForm sash = (SashForm) controls[0];
		Control[] sashChildren = sash.getChildren();
		assertEquals(1, sashChildren.length);

		// HACK: see bug #280632 - always a composite around
		// CTabFolder so can implement margins
		Composite marginHolder = (Composite) sashChildren[0];
		assertEquals(1, marginHolder.getChildren().length);
		CTabFolder folder = (CTabFolder) marginHolder.getChildren()[0];
		CTabItem item = folder.getItem(0);
		assertNotNull(item.getImage());
	}

	private void testDeclaredTooltip(String partToolTip, String expectedToolTip) {
		final MWindow window = createWindowWithOneView("Part Name", partToolTip);

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
		Control[] controls = getPresentationControls(shell);
		assertEquals(1, controls.length);
		SashForm sash = (SashForm) controls[0];
		Control[] sashChildren = sash.getChildren();
		assertEquals(1, sashChildren.length);

		// HACK: see bug #280632 - always a composite around
		// CTabFolder so can implement margins
		Composite marginHolder = (Composite) sashChildren[0];
		assertEquals(1, marginHolder.getChildren().length);
		CTabFolder folder = (CTabFolder) marginHolder.getChildren()[0];
		CTabItem item = folder.getItem(0);
		assertEquals(expectedToolTip, item.getToolTipText());
	}

	public void testDeclaredTooltipNull() {
		testDeclaredTooltip(null, null);
	}

	public void testDeclaredTooltipEmptyString() {
		testDeclaredTooltip("", "");
	}

	public void testDeclaredTooltipDefined() {
		testDeclaredTooltip("partToolTip", "partToolTip");
	}

	private void testMPart_setTooltip(String partToolTip, String expectedToolTip) {
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
		Control[] controls = getPresentationControls(shell);
		assertEquals(1, controls.length);
		SashForm sash = (SashForm) controls[0];
		Control[] sashChildren = sash.getChildren();
		assertEquals(1, sashChildren.length);

		// HACK: see bug #280632 - always a composite around
		// CTabFolder so can implement margins
		Composite marginHolder = (Composite) sashChildren[0];
		assertEquals(1, marginHolder.getChildren().length);
		CTabFolder folder = (CTabFolder) marginHolder.getChildren()[0];
		CTabItem item = folder.getItem(0);
		assertEquals(null, item.getToolTipText());

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		part.setTooltip(partToolTip);
		assertEquals(expectedToolTip, item.getToolTipText());
	}

	public void testMPart_setTooltipNull() {
		testMPart_setTooltip(null, null);
	}

	public void testMPart_setTooltipEmptyString() {
		testMPart_setTooltip("", "");
	}

	public void testMPart_setTooltipDefined() {
		testMPart_setTooltip("partToolTip", "partToolTip");
	}

	public void testMPart_getContext() {
		final MWindow window = createWindowWithOneView("Part Name");

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.removeGui(part);
		assertNull(part.getContext());
	}

	private MWindow createWindowWithOneView(String partName) {
		return createWindowWithOneView(partName, null);
	}

	private MWindow createWindowWithOneView(String partName, String toolTip) {
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
		contributedPart.setTooltip(toolTip);
		contributedPart
				.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.gif");
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
