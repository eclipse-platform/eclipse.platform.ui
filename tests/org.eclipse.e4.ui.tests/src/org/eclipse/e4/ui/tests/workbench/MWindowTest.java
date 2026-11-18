/*******************************************************************************
 * Copyright (c) 2009, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import jakarta.inject.Inject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;
import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer;
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
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MWindowTest {

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private IEclipseContext appContext;

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	@Test
	public void testCreateWindow() {
		assumeFalse(Platform.OS_MACOSX.equals(Platform.getOS()), "Test fails on Mac: Bug 537639");

		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);

		Shell shell = (Shell) topWidget;
		assertTrue(shell.getVisible());

		window.setVisible(false);
		assertFalse(shell.getVisible());

		window.setVisible(true);
		assertTrue(shell.getVisible());
	}

	@Test
	public void testWindowInvisibleCreate() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		window.setVisible(false);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		Widget topWidget = (Widget) window.getWidget();
		assertNotNull(topWidget);
		assertTrue(topWidget instanceof Shell);

		Shell shell = (Shell) topWidget;
		assertFalse(shell.getVisible());
	}

	@Test
	public void testCreateView() {
		final MWindow window = createWindowWithOneView();

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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
		assumeFalse(Platform.OS_MACOSX.equals(Platform.getOS()), "Test fails on Mac: Bug 537639");

		final MWindow window = createWindowWithOneView();

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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
		assertNotEquals(window.getContext(), child);

		MPart contextPart = child.get(MPart.class);

		assertNotNull(contextPart);
		assertEquals(window, contextPart.getParent().getParent().getParent());
	}

	@Test
	public void testCreateMenu() {
		final MWindow window = createWindowWithOneViewAndMenu();

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

	@Disabled
	@Test
	public void TODOtestWindow_X() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

	@Disabled
	@Test
	public void TODOtestWindow_Y() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setX(200);
		window.setY(200);
		window.setWidth(200);
		window.setHeight(200);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

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

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		Object widget = window.getWidget();
		assertTrue(widget instanceof Shell);

		Shell shell = (Shell) widget;
		assertEquals(shell.getBounds().height, window.getHeight());
		assertEquals(200, shell.getBounds().height);

		// the shell's width should have been updated
		window.setHeight(300);

		// Give time for change to propagate
		DisplayHelper.waitForCondition(shell.getDisplay(), 10000, () -> (300 == shell.getBounds().height));
		assertEquals(shell.getBounds().height, window.getHeight());
	}

	@Test
	public void testDetachedWindow() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		final MWindow detachedWindow = ems.createModelElement(MWindow.class);
		detachedWindow.setLabel("DetachedWindow");
		window.getWindows().add(detachedWindow);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		assertTrue(window.getWidget() instanceof Shell);
		assertTrue(detachedWindow.getWidget() instanceof Shell);
		Shell topShell = (Shell) window.getWidget();
		Shell detachedShell = (Shell) detachedWindow.getWidget();
		assertEquals(window, ems.getContainer(detachedWindow));
		assertNull(topShell.getImage(), "Should have no shell image");
		assertEquals(topShell.getImage(), detachedShell.getImage(), "Detached should have same image");

		// now set icon on top-level window; detached window should inherit it
		window.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.svg");
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertNotNull(topShell.getImage(), "Should have shell image");
		assertEquals(topShell.getImage(), detachedShell.getImage(), "Detached should have same image");

		// change top-level icon; detached window should inherit it
		window.setIconURI(null);
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertNull(topShell.getImage(), "Should have no shell image");
		assertEquals(topShell.getImage(), detachedShell.getImage(), "Detached should have same image");

		// turn detached into top-level window; inherited icon should be removed
		window.setIconURI("platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.svg");
		application.getChildren().add(detachedWindow);
		while (topShell.getDisplay().readAndDispatch()) {
		}
		assertTrue(window.getWindows().isEmpty());
		assertNotEquals(window, ems.getContainer(detachedWindow));
		assertNotNull(topShell.getImage());
		assertNull(detachedShell.getImage());
	}

	@Test
	public void testDetachedWindowIsChildShellByDefault() {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setLabel("MyWindow");
		final MWindow detachedWindow = ems.createModelElement(MWindow.class);
		detachedWindow.setLabel("DetachedWindow");
		window.getWindows().add(detachedWindow);

		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		Shell topShell = (Shell) window.getWidget();
		Shell detachedShell = (Shell) detachedWindow.getWidget();
		assertEquals(topShell, detachedShell.getParent(), "Detached shell should be a child of the window shell");
	}

	@Test
	public void testDetachedWindowTopLevelPreference() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		prefs.putBoolean(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL, true);
		try {
			final MWindow window = ems.createModelElement(MWindow.class);
			window.setLabel("MyWindow");
			final MWindow detachedWindow = ems.createModelElement(MWindow.class);
			detachedWindow.setLabel("DetachedWindow");
			window.getWindows().add(detachedWindow);

			application.getChildren().add(window);
			contextRule.createAndRunWorkbench(window);

			Shell detachedShell = (Shell) detachedWindow.getWidget();
			assertNotNull(detachedShell);
			assertNull(detachedShell.getParent(), "Detached shell should be a top level shell");
			assertEquals(window, ems.getContainer(detachedWindow));
		} finally {
			prefs.remove(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL);
		}
	}

	@Test
	public void testDetachedPartTopLevelPreference() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		prefs.putBoolean(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL, true);
		try {
			final MWindow window = createWindowWithOneView();
			application.getChildren().add(window);
			contextRule.createAndRunWorkbench(window);

			// Detach the part of a rendered window, as the Detach menu does
			MPart part = getContributedPart(window);
			ems.detach(part, 100, 100, 300, 200);

			assertEquals(1, window.getWindows().size());
			MWindow detachedWindow = window.getWindows().get(0);
			Shell detachedShell = (Shell) detachedWindow.getWidget();
			assertNotNull(detachedShell);
			assertNull(detachedShell.getParent(), "Detached shell should be a top level shell");
			assertTrue((detachedShell.getStyle() & SWT.MIN) != 0, "Top level detached shell should be minimizable");
			assertEquals("Sample View", detachedShell.getText(), "Title should be the label of the detached part");

			part.setLabel("Renamed View");
			while (detachedShell.getDisplay().readAndDispatch()) {
			}
			assertEquals("Renamed View", detachedShell.getText(), "Title should follow the label of the part");
		} finally {
			prefs.remove(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL);
		}
	}

	@Test
	public void testDetachedWindowTitleFollowsSelectedPart() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		prefs.putBoolean(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL, true);
		try {
			final MWindow window = createWindowWithOneView();
			final MWindow detachedWindow = ems.createModelElement(MWindow.class);
			MPartStack stack = ems.createModelElement(MPartStack.class);
			detachedWindow.getChildren().add(stack);
			MPart partA = createSampleView("Part A");
			MPart partB = createSampleView("Part B");
			stack.getChildren().add(partA);
			stack.getChildren().add(partB);
			stack.setSelectedElement(partA);
			window.getWindows().add(detachedWindow);

			application.getChildren().add(window);
			contextRule.createAndRunWorkbench(window);

			Shell detachedShell = (Shell) detachedWindow.getWidget();
			assertNull(detachedShell.getParent(), "Detached shell should be a top level shell");
			assertEquals("Part A", detachedShell.getText());

			stack.setSelectedElement(partB);
			while (detachedShell.getDisplay().readAndDispatch()) {
			}
			assertEquals("Part B", detachedShell.getText());

			// A label of the window itself wins over the label of its parts
			detachedWindow.setLabel("Window Label");
			while (detachedShell.getDisplay().readAndDispatch()) {
			}
			partB.setLabel("Renamed Part B");
			while (detachedShell.getDisplay().readAndDispatch()) {
			}
			assertEquals("Window Label", detachedShell.getText());

			// The main window keeps its own title
			assertEquals("MyWindow", ((Shell) window.getWidget()).getText());
		} finally {
			prefs.remove(WBWRenderer.DETACHED_WINDOWS_TOP_LEVEL);
		}
	}

	@Test
	public void testDetachedWindowTitleUnchangedByDefault() {
		final MWindow window = createWindowWithOneView();
		application.getChildren().add(window);
		contextRule.createAndRunWorkbench(window);

		MPart part = getContributedPart(window);
		ems.detach(part, 100, 100, 300, 200);

		Shell detachedShell = (Shell) window.getWindows().get(0).getWidget();
		assertEquals(window.getWidget(), detachedShell.getParent());
		assertEquals("", detachedShell.getText(), "A child detached shell keeps its empty title");
	}

	private MPart createSampleView(String label) {
		MPart part = ems.createModelElement(MPart.class);
		part.setLabel(label);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		return part;
	}

	private MPart getContributedPart(MWindow window) {
		MPartSashContainer psc = (MPartSashContainer) window.getChildren().get(0);
		MPartStack stack = (MPartStack) psc.getChildren().get(0);
		return (MPart) stack.getChildren().get(0);
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
