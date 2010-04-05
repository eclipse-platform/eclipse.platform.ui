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

import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
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

/**
 *
 */
public class NewMWindowTest extends TestCase {
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

	public void testCreateWindow() {
		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setLabel("MyWindow");
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		assertEquals("MyWindow", ((Shell) topWidget).getText());
		assertEquals(topWidget, appContext.get(IServiceConstants.ACTIVE_SHELL));
	}

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

	public void testContextChildren() {
		final MWindow window = createWindowWithOneView();
		wb = new E4Workbench(window, appContext);

		Widget topWidget = (Widget) window.getWidget();
		assertTrue(topWidget instanceof Shell);
		Shell shell = (Shell) topWidget;
		assertEquals("MyWindow", shell.getText());

		// should get the window context
		IEclipseContext child = (IEclipseContext) appContext
				.getLocal(IContextConstants.ACTIVE_CHILD);
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

		IEclipseContext next = (IEclipseContext) child
				.getLocal(IContextConstants.ACTIVE_CHILD);
		while (next != null) {
			child = next;
			next = (IEclipseContext) child
					.getLocal(IContextConstants.ACTIVE_CHILD);
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
		final MMenuItem item2Model = mainMenu.getChildren().get(0)
				.getChildren().get(0);
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
		MPart part = stack.getChildren().get(0);
		assertTrue("part is incorrect type " + part, part instanceof MPart);
		return part;
	}

	private MWindow createWindowWithOneView() {
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
		contributedPart.setLabel("Sample View");
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

	private MWindow createWindowWithOneViewAndMenu() {
		final MWindow window = createWindowWithOneView();
		final MMenu menuBar = MApplicationFactory.eINSTANCE.createMenu();
		window.setMainMenu(menuBar);
		final MMenuItem fileItem = MApplicationFactory.eINSTANCE
				.createMenuItem();
		fileItem.setLabel("File");
		fileItem.setId("file");
		menuBar.getChildren().add(fileItem);

		final MMenuItem item1 = MApplicationFactory.eINSTANCE.createMenuItem();
		item1.setId("item1");
		item1.setLabel("item1");
		fileItem.getChildren().add(item1);
		final MMenuItem item2 = MApplicationFactory.eINSTANCE.createMenuItem();
		item2.setId("item2");
		item2.setLabel("item2");
		fileItem.getChildren().add(item2);

		return window;
	}
}
