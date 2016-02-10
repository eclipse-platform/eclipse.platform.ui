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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class MWindowTest {
	protected IEclipseContext appContext;
	protected E4Workbench wb;
	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		appContext.set(E4Workbench.PRESENTATION_URI_ARG, PartRenderingEngine.engineURI);
		appContext.set(UISynchronize.class, new UISynchronize() {
			@Override
			public void syncExec(Runnable runnable) {
				runnable.run();
			}

			@Override
			public void asyncExec(final Runnable runnable) {
				runnable.run();
			}
		});
		ContextInjectionFactory.setDefault(appContext);
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
		ContextInjectionFactory.setDefault(null);
	}

	@Test
	public void testCreateWindow() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);
		assertEquals("MyWindow", ((Shell) topWidget).getText());
		// XXX Use of ACTIVE_SHELL fails when running standalone
		assertEquals(topWidget, appContext.get(IServiceConstants.ACTIVE_SHELL));
	}

	@Test
	public void testWindowVisibility() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);

		Shell shell = (Shell) topWidget;
		assertTrue(shell.getVisible() == true);

		window.setVisible(false);
		assertTrue(shell.getVisible() == false);

		window.setVisible(true);
		assertTrue(shell.getVisible() == true);
	}

	@Test
	public void testWindowInvisibleCreate() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		window.setVisible(false);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);

		Shell shell = (Shell) topWidget;
		assertTrue(shell.getVisible() == false);
	}

	@Test
	public void testCreateView() {
		final MWindow window = createWindowWithOneView();

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);

		CTabFolder folder = (CTabFolder) stack.getWidget();
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

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
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
		AbstractPartRenderer factory = (AbstractPartRenderer) modelPart.getRenderer();
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

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
		((MenuManager) ((Widget) window.getMainMenu().getWidget()).getData()).updateAll(true);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
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
		final MMenuItem item2Model = (MMenuItem) modelFileMenu.getChildren().get(0);
		item2Model.setToBeRendered(false);
		fileMenu.notifyListeners(SWT.Show, null);
		assertEquals(1, fileMenu.getItemCount());
		fileMenu.notifyListeners(SWT.Hide, null);

		item2Model.setToBeRendered(true);
		fileMenu.notifyListeners(SWT.Show, null);
		assertEquals(2, fileMenu.getItemCount());
		fileMenu.notifyListeners(SWT.Hide, null);
	}

	@Test
	public void testWindow_Name() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("windowName");

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = window.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		assertEquals(shell.getText(), window.getLabel());
		assertEquals("windowName", shell.getText());

		// the shell's name should have been updated
		window.setLabel("windowName2");
		assertEquals(shell.getText(), window.getLabel());
		assertEquals("windowName2", shell.getText());
	}

	@Ignore
	@Test
	public void TODOtestWindow_X() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = window.getWidget();
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		Rectangle bounds = shell.getBounds();
		assertEquals(window.getX(), bounds.x);
		assertEquals(200, bounds.x);

		// the shell's X coordinate should have been updated
		window.setX(300);

		while (shell.getDisplay().readAndDispatch()) {
			// spin the event loop
		}

		bounds = shell.getBounds();
		assertEquals(300, window.getX());
		assertEquals(window.getX(), bounds.x);
		assertEquals(300, bounds.x);
	}

	@Ignore
	@Test
	public void TODOtestWindow_Y() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = window.getWidget();
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		Rectangle bounds = shell.getBounds();
		assertEquals(window.getY(), bounds.y);
		assertEquals(200, bounds.y);

		// the shell's Y coordinate should have been updated
		window.setY(300);

		while (shell.getDisplay().readAndDispatch()) {
			// spin the event loop
		}

		bounds = shell.getBounds();
		assertEquals(300, window.getY());
		assertEquals(window.getY(), bounds.y);
		assertEquals(300, bounds.y);
	}

	@Test
	public void testWindow_Width() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = window.getWidget();
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		assertEquals(shell.getBounds().width, window.getWidth());
		assertEquals(200, shell.getBounds().width);

		// the shell's width should have been updated
		window.setWidth(300);

		while (shell.getDisplay().readAndDispatch()) {
			// spin the event loop
		}

		assertEquals(shell.getBounds().width, window.getWidth());
		assertEquals(300, shell.getBounds().width);
	}

	@Test
	public void testWindow_Height() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Object widget = window.getWidget();
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		assertEquals(shell.getBounds().height, window.getHeight());
		assertEquals(200, shell.getBounds().height);

		// the shell's width should have been updated
		window.setHeight(300);

		while (shell.getDisplay().readAndDispatch()) {
			// spin the event loop
		}

		assertEquals(shell.getBounds().height, window.getHeight());
		assertEquals(300, shell.getBounds().height);
	}

	@Test
	public void testDetachedWindow() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		final MWindow detachedWindow = ems.createModelElement(MWindow.class);
		detachedWindow.setLabel("DetachedWindow");
		window.getWindows().add(detachedWindow);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(window.getWidget() instanceof Shell);
		assertTrue(detachedWindow.getWidget() instanceof Shell);
		Shell topShell = (Shell) window.getWidget();
		Shell detachedShell = (Shell) detachedWindow.getWidget();
		assertEquals(window, ems.getContainer(detachedWindow));
		assertNull("Should have no shell image", topShell.getImage());
		assertEquals("Detached should have same image", topShell.getImage(), detachedShell.getImage());

		// now set icon on top-level window; detached window should inherit it
		window.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.png");
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertNotNull("Should have shell image", topShell.getImage());
		assertEquals("Detached should have same image", topShell.getImage(), detachedShell.getImage());

		// change top-level icon; detached window should inherit it
		window.setIconURI(null);
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertNull("Should have no shell image", topShell.getImage());
		assertEquals("Detached should have same image", topShell.getImage(), detachedShell.getImage());

		// turn detached into top-level window; inherited icon should be removed
		window.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.png");
		application.getChildren().add(detachedWindow);
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertTrue(window.getWindows().isEmpty());
		assertNotEquals(window, ems.getContainer(detachedWindow));
		assertNotNull(topShell.getImage());
		assertNull(detachedShell.getImage());
	}

	private MPart getContributedPart(MWindow window) {
		MPartSashContainer psc = (MPartSashContainer) window.getChildren().get(0);
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
		contributedPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
