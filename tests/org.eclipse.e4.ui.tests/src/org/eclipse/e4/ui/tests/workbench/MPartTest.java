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

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

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
		appContext.dispose();
	}

	public void testSetName() {
		final MWindow window = createWindowWithOneView("Part Name");

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);
		assertEquals("Part Name", item.getText());

		part.setLabel("Another Name");
		assertEquals("Another Name", item.getText());
	}

	public void testCTabItem_GetImage() {
		final MWindow window = createWindowWithOneView("Part Name");

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);
		assertNotNull(item.getImage());
	}

	private void testDeclaredName(String declared, String expected) {
		final MWindow window = createWindowWithOneView(declared);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);
		assertEquals(expected, item.getText());
	}

	public void testDeclaredNameNull() {
		testDeclaredName(null, "");
	}

	public void testDeclaredNameEmpty() {
		testDeclaredName("", "");
	}

	public void testDeclaredNameDefined() {
		testDeclaredName("partName", "partName");
	}

	private void testDeclaredTooltip(String partToolTip, String expectedToolTip) {
		final MWindow window = createWindowWithOneView("Part Name", partToolTip);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
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

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);
		assertEquals(null, item.getToolTipText());

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

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.removeGui(part);
		assertNull(part.getContext());
	}

	public void testMPartBug369866() {
		final MWindow window = createWindowWithOneView("Part");

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem item = folder.getItem(0);

		// bug 369866 has a StringIOOBE from toggling the dirty flag with an
		// empty part name
		assertFalse(part.isDirty());
		assertEquals("Part", item.getText());

		part.setDirty(true);
		assertEquals("*Part", item.getText());

		part.setLabel("");
		assertEquals("*", item.getText());

		part.setDirty(false);
		assertEquals("", item.getText());

		part.setDirty(true);
		assertEquals("*", item.getText());
	}

	private MWindow createWindowWithOneView(String partName) {
		return createWindowWithOneView(partName, null);
	}

	private MWindow createWindowWithOneView(String partName, String toolTip) {
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		sash.getChildren().add(stack);
		MPart contributedPart = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart.setTooltip(toolTip);
		contributedPart
				.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.gif");
		contributedPart
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
