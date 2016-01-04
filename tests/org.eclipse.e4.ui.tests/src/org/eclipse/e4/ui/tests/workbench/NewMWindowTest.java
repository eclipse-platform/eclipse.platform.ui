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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 *
 */
public class NewMWindowTest {
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
	public void testCreateWindow() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		assertEquals("MyWindow", ((Shell) topWidget).getText());
		assertEquals(topWidget, appContext.get(IServiceConstants.ACTIVE_SHELL));
	}

	@Test
	public void testCreateView() {
		final MWindow window = createWindowWithOneView();
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		Shell shell = (Shell) topWidget;
		assertEquals("MyWindow", shell.getText());
		Control[] controls = shell.getChildren();
		assertEquals(1, controls.length);
		SashForm sash = (SashForm) controls[0];
		Control[] sashChildren = sash.getChildren();
		assertEquals(1, sashChildren.length);

		CTabFolder folder = (CTabFolder) sashChildren[0];
		assertEquals(1, folder.getItemCount());
		Control c = folder.getItem(0).getControl();
		assertTrue(c instanceof Composite);
		Control[] viewPart = ((Composite) c).getChildren();
		assertEquals(1, viewPart.length);
		assertTrue(viewPart[0] instanceof Tree);
	}

	@Test
	public void testContextChildren() {
		final MWindow window = createWindowWithOneView();
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		Shell shell = (Shell) topWidget;
		assertEquals("MyWindow", shell.getText());

		// should get the window context
		IEclipseContext child = appContext.getActiveChild();
		assertNotNull(child);
		assertEquals(window.getContext(), child);

		MPart modelPart = getContributedPart(window);
		assertNotNull(modelPart);
		assertEquals(window, modelPart.getParent().getParent().getParent());

		// "activate" the part, same as (in theory) an
		// SWT.Activate event.
		AbstractPartRenderer factory = (AbstractPartRenderer) modelPart
				.getRenderer();
		factory.activate(modelPart);

		IEclipseContext next = child.getActiveChild();
		while (next != null) {
			child = next;
			next = child.getActiveChild();
			if (next == child) {
				fail("Cycle detected in part context");
				break;
			}
		}
		assertFalse(window.getContext() == child);

		MPart contextPart = (MPart) child.get(MPart.class.getName());

		assertNotNull(contextPart);
		assertEquals(window, contextPart.getParent().getParent().getParent());
	}

	@Test
	public void testCreateMenu() {
		final MWindow window = createWindowWithOneViewAndMenu();
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		Shell shell = (Shell) topWidget;
		final Menu menuBar = shell.getMenuBar();
		assertNotNull(menuBar);
		assertEquals(1, menuBar.getItemCount());
		final MenuItem fileItem = menuBar.getItem(0);
		assertEquals("File", fileItem.getText());
		final Menu fileMenu = fileItem.getMenu();
		fileMenu.notifyListeners(SWT.Show, null);
		assertEquals(2, fileMenu.getItemCount());
		fileMenu.notifyListeners(SWT.Hide, null);

		MMenu mainMenu = window.getMainMenu();
		MMenu modelFileMenu = (MMenu) mainMenu.getChildren().get(0);
		final MMenuItem item2Model = (MMenuItem) modelFileMenu.getChildren()
				.get(0);
		item2Model.setToBeRendered(false);
		fileMenu.notifyListeners(SWT.Show, null);
		assertEquals(1, fileMenu.getItemCount());
		fileMenu.notifyListeners(SWT.Hide, null);

		item2Model.setToBeRendered(true);
		fileMenu.notifyListeners(SWT.Show, null);
		assertEquals(2, fileMenu.getItemCount());
		fileMenu.notifyListeners(SWT.Hide, null);
	}

	private MPart getContributedPart(MWindow window) {
		MPartSashContainer psc = (MPartSashContainer) window.getChildren().get(
				0);
		MPartStack stack = (MPartStack) psc.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);
		assertTrue("part is incorrect type " + part, part instanceof MPart);
		return part;
	}

	private MWindow createWindowWithOneView() {
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
		contributedPart.setLabel("Sample View");
		contributedPart
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

	private MWindow createWindowWithOneViewAndMenu() {
		final MWindow window = createWindowWithOneView();
		final MMenu menuBar = ems.createModelElement(MMenu.class);
		window.setMainMenu(menuBar);
		final MMenu fileMenu = ems.createModelElement(MMenu.class);
		fileMenu.setLabel("File");
		fileMenu.setElementId("file");
		menuBar.getChildren().add(fileMenu);

		final MMenuItem item1 = ems.createModelElement(MDirectMenuItem.class);
		item1.setElementId("item1");
		item1.setLabel("item1");
		fileMenu.getChildren().add(item1);
		final MMenuItem item2 = ems.createModelElement(MDirectMenuItem.class);
		item2.setElementId("item2");
		item2.setLabel("item2");
		fileMenu.getChildren().add(item2);

		return window;
	}
}
