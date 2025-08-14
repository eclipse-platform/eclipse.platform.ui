/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for bug 540297. Call {@link IWorkbenchPage#findView(String)} while the
 * view is open in some perspective and window, and see if the find view method
 * behaves properly.
 */
public class Bug540297WorkbenchPageFindViewTest {

	public static class MyPerspective implements IPerspectiveFactory {
		public static String ID1 = "org.eclipse.ui.tests.internal.Bug540297WorkbenchPageFindViewTest.MyPerspective1";
		public static String ID2 = "org.eclipse.ui.tests.internal.Bug540297WorkbenchPageFindViewTest.MyPerspective2";

		@Override
		public void createInitialLayout(IPageLayout layout) {
			// we want an empty perspective in this test
		}
	}

	public static class MyViewPart extends ViewPart {
		public static String ID = "org.eclipse.ui.tests.internal.Bug540297WorkbenchPageFindViewTest.MyViewPart";

		@Override
		public void createPartControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(getSite().getId());
		}

		@Override
		public void setFocus() {
			// we don't care about view functionality, so do nothing here
		}
	}

	private IWorkbenchWindow firstWindow;
	private IWorkbenchPage firstWindowActivePage;
	private IWorkbenchWindow secondWindow;
	private IWorkbenchPage secondWindowActivePage;
	private IPerspectiveDescriptor originalPerspective;
	private IPerspectiveDescriptor activePerspective;
	private IPerspectiveDescriptor inactivePerspective;

	@Before
	public void doSetUp() throws Exception {
		firstWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		secondWindow = openTestWindow();

		firstWindowActivePage = firstWindow.getActivePage();
		secondWindowActivePage = secondWindow.getActivePage();

		originalPerspective = firstWindowActivePage.getPerspective();

		activePerspective = getPerspetiveDescriptor(MyPerspective.ID1);
		inactivePerspective = getPerspetiveDescriptor(MyPerspective.ID2);

		firstWindowActivePage.setPerspective(activePerspective);
		prepareWorkbenchPageForTest(firstWindowActivePage);
		prepareWorkbenchPageForTest(secondWindowActivePage);

		processEvents();
	}

	private void prepareWorkbenchPageForTest(IWorkbenchPage page) {
		page.setPerspective(inactivePerspective);
		page.resetPerspective();
		page.closeAllEditors(false);
		page.setPerspective(activePerspective);
		page.resetPerspective();
		page.closeAllEditors(false);
		IViewReference[] views = page.getViewReferences();
		for (IViewReference view : views) {
			page.hideView(view);
		}
	}

	@After
	public void doTearDown() throws Exception {
		secondWindow.close();
		firstWindowActivePage.setPerspective(originalPerspective);
		firstWindowActivePage.resetPerspective();
		firstWindowActivePage.closePerspective(inactivePerspective, false, false);
		firstWindowActivePage.closePerspective(activePerspective, false, false);
		processEvents();
	}

	/**
	 * Tests that if the active perspective of the first window has a view open, the
	 * view can be found while in the active perspective of the first window.
	 *
	 * Also checks that the second window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInFirstWindowAndActivePerspective() throws Exception {
		showView(firstWindowActivePage);
		assertCanFindView(firstWindowActivePage);
		assertCannotFindView(secondWindowActivePage);
	}

	/**
	 * Tests that if the inactive perspective of the first window has a view open,
	 * the view cannot be found while in the active perspective of the first window.
	 *
	 * Also checks that the second window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInFirstWindowAndInactivePerspective() throws Exception {
		showViewInInactivePerspective(firstWindowActivePage);
		assertCannotFindView(secondWindowActivePage);
		assertCannotFindView(firstWindowActivePage);
	}

	/**
	 * Tests that if the inactive perspective of the first window has a view open,
	 * the view cannot be found while in the active perspective of the first window.
	 * Shows and hides the view in the active perspective before checking.
	 *
	 * Also checks that the second window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInFirstWindowAndInactivePerspectiveWithOpenAndClose() throws Exception {
		showViewInInactivePerspective(firstWindowActivePage);
		showAndHideView(firstWindowActivePage);
		assertCannotFindView(secondWindowActivePage);
		assertCannotFindView(firstWindowActivePage);
	}

	/**
	 * Tests that if the active perspective of the second window has a view open,
	 * the view can be found while in the active perspective of the second window.
	 *
	 * Also checks that the first window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInSecondWindowAndActivePerspective() throws Exception {
		showView(secondWindowActivePage);
		assertCanFindView(secondWindowActivePage);
		assertCannotFindView(firstWindowActivePage);
	}

	/**
	 * Tests that if the inactive perspective of the second window has a view open,
	 * the view cannot be found while in the active perspective of the second
	 * window.
	 *
	 * Also checks that the first window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInSecondWindowAndInactivePerspective() throws Exception {
		showViewInInactivePerspective(secondWindowActivePage);
		assertCannotFindView(firstWindowActivePage);
		assertCannotFindView(secondWindowActivePage);
	}

	/**
	 * Tests that if the inactive perspective of the second window has a view open,
	 * the view cannot be found while in the active perspective of the second
	 * window. Shows and hides the view in the active perspective before checking.
	 *
	 * Also checks that the first window cannot find the view, since the view was
	 * never open in that window.
	 */
	@Test
	public void testFindViewInSecondWindowAndInactivePerspectiveWithOpenAndClose() throws Exception {
		showViewInInactivePerspective(secondWindowActivePage);
		showAndHideView(secondWindowActivePage);
		assertCannotFindView(firstWindowActivePage);
		assertCannotFindView(secondWindowActivePage);
	}

	private void showViewInInactivePerspective(IWorkbenchPage pageForTest) throws Exception {
		setPerspective(pageForTest, inactivePerspective);
		showView(pageForTest);
		setPerspective(pageForTest, activePerspective);
	}

	private static void setPerspective(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		page.setPerspective(perspective);
		page.resetPerspective();
		processEvents();
	}

	private static void showAndHideView(IWorkbenchPage page) throws Exception {
		showView(page);
		hideView(page);
	}

	private static void showView(IWorkbenchPage page) throws Exception {
		page.showView(MyViewPart.ID);
		processEvents();
	}

	private static void hideView(IWorkbenchPage page) throws Exception {
		IViewPart view = page.findView(MyViewPart.ID);
		page.hideView(view);
		processEvents();
	}

	private IPerspectiveDescriptor getPerspetiveDescriptor(String perspectiveId) {
		return PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
	}

	private static void assertCanFindView(IWorkbenchPage page) throws Exception {
		assertFindViewResult(page, true);
	}

	private static void assertCannotFindView(IWorkbenchPage page) throws Exception {
		assertFindViewResult(page, false);
	}

	private static void assertFindViewResult(IWorkbenchPage page, boolean expectedFound) throws Exception {
		IViewPart viewPart = page.findView(MyViewPart.ID);
		boolean actualFound = viewPart != null;
		assertEquals("unexpected result from IWorkbenchPage.findView(String): " + viewPart, expectedFound, actualFound);
	}

}
