/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.PreferenceMementoRule;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.0
 */
@RunWith(JUnit4.class)
public class StickyViewTest extends UITestCase {

	@Rule
	public final PreferenceMementoRule preferenceMemento = new PreferenceMementoRule();

	private IWorkbenchWindow window;

	private IWorkbenchPage page;

	public StickyViewTest() {
		super(StickyViewTest.class.getSimpleName());
	}

	@Test
	public void testStackPlacementRight() throws PartInitException {
		testStackPlacement("Right");
	}

	@Test
	public void testStackPlacementLeft() throws PartInitException {
		testStackPlacement("Left");
	}

	@Test
	public void testStackPlacementTop() throws PartInitException {
		testStackPlacement("Top");
	}

	@Test
	public void testStackPlacementBottom() throws PartInitException {
		testStackPlacement("Bottom");
	}

	/**
	 * Tests to ensure that sticky views are opened in the same stack.
	 *
	 * @throws PartInitException
	 */
	private void testStackPlacement(String location) throws PartInitException {
		IViewPart part1 = page.showView("org.eclipse.ui.tests.api.StickyView" + location + "1");
		assertNotNull(part1);
		IViewPart part2 = page.showView("org.eclipse.ui.tests.api.StickyView" + location + "2");
		assertNotNull(part2);
		IViewPart[] stack = page.getViewStack(part1);

		assertTrue(ViewUtils.findInStack(stack, part1));
		assertTrue(ViewUtils.findInStack(stack, part2));
	}

	/**
	 * Tests to ensure that all views in a stack with a known sticky view are also
	 * sticky.
	 *
	 * @throws PartInitException
	 */
	@Test
	public void testStackContents() throws PartInitException {
		IViewPart part1 = page.showView("org.eclipse.ui.tests.api.StickyViewRight1");
		assertNotNull(part1);

		IViewPart[] stack = page.getViewStack(part1);

		for (IViewPart element : stack) {
			assertTrue(element.getTitle(), ViewUtils.isSticky(element));
		}
	}

	/**
	 * Tests whether the moveable flag is being picked up and honoured from the XML.
	 *
	 * @throws PartInitException
	 */
	@Test
	@Ignore
	public void XXXtestClosableFlag() throws PartInitException {
		//explicit closeable = true
		testCloseable("org.eclipse.ui.tests.api.StickyViewRight1", true);
		//explicit closeable = false
		testCloseable("org.eclipse.ui.tests.api.StickyViewRight2", false);
		//implicit closeable = true
		testCloseable("org.eclipse.ui.tests.api.StickyViewLeft1", true);
	}

	@Test
	@Ignore
	public void XXXtestMoveableFlag() throws PartInitException {
		//explicit closeable = true
		testMoveable("org.eclipse.ui.tests.api.StickyViewRight1", true);
		//explicit closeable = false
		testMoveable("org.eclipse.ui.tests.api.StickyViewRight2", false);
		//implicit closeable = true
		testMoveable("org.eclipse.ui.tests.api.StickyViewLeft1", true);
	}

	/**
	 * Tests whether a sticky view with the given id is moveable or not.
	 *
	 * @param id          the id
	 * @param expectation the expected moveable state
	 * @throws PartInitException
	 */
	private void testMoveable(String id, boolean expectation) throws PartInitException {
		IViewPart part = page.showView(id);
		assertNotNull(part);
		assertTrue(ViewUtils.isSticky(part));

		// tests to ensure that the XML was read correctly
		IStickyViewDescriptor[] descs = PlatformUI.getWorkbench().getViewRegistry().getStickyViews();
		for (IStickyViewDescriptor desc : descs) {
			if (desc.getId().equals(id)) {
				assertEquals(expectation, desc.isMoveable());
			}
		}

		// tests to ensure that the property is being honoured by the perspective
		assertEquals(expectation, ViewUtils.isMoveable(part));
	}

	/**
	 * Tests whether a sticky view with the given id is closeable or not.
	 *
	 * @param id          the id
	 * @param expectation the expected closeable state
	 * @throws PartInitException
	 */
	private void testCloseable(String id, boolean expectation) throws PartInitException {
		IViewPart part = page.showView(id);
		assertNotNull(part);
		assertTrue(ViewUtils.isSticky(part));

		// tests to ensure that the XML was read correctly
		IStickyViewDescriptor[] descs = PlatformUI.getWorkbench().getViewRegistry().getStickyViews();
		for (IStickyViewDescriptor desc : descs) {
			if (desc.getId().equals(id)) {
				assertEquals(expectation, desc.isCloseable());
			}
		}

		// tests to ensure that the property is being honoured by the perspective
		assertEquals(expectation, ViewUtils.isCloseable(part));
	}

	/**
	 * Sticky views should remain after perspective reset.
	 *
	 * @throws PartInitException
	 */
	@Test
	public void testPerspectiveReset() throws PartInitException {
		page.showView("org.eclipse.ui.tests.api.StickyViewRight1");
		page.resetPerspective();
		assertNotNull(page.findView("org.eclipse.ui.tests.api.StickyViewRight1"));
	}

	/**
	 * Tests that a sticky view is opened in successive perspectives.
	 *
	 * @throws PartInitException
	 */
	@Test
	public void testPerspectiveOpen() throws PartInitException {
		page.showView("org.eclipse.ui.tests.api.StickyViewRight1");
		page.setPerspective(WorkbenchPlugin.getDefault().getPerspectiveRegistry()
				.findPerspectiveWithId("org.eclipse.ui.tests.api.SessionPerspective"));
		assertNotNull(page.findView("org.eclipse.ui.tests.api.StickyViewRight1"));
	}

	/**
	 * Test that closing a stand-alone view remove the editor stack and
	 * doesn't throw an NPE.
	 *
	 * @throws Throwable on error
	 * @since 3.2
	 */
	@Test
	public void testPerspectiveCloseStandaloneView() throws Throwable {
		page.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						PerspectiveViewsBug120934.PERSP_ID));

		try {
			// find the stand-alone view
			IViewReference standAloneRef = page
					.findViewReference(IPageLayout.ID_OUTLINE);

			page.hideView(standAloneRef);
		} finally {
			page.closePerspective(page.getPerspective(), false, false);
		}
	}

	/**
	 * Find the supplied menu item and make sure it's enabled/disabled.
	 *
	 * @param wpage the workbench page
	 * @param menuContribution the fast bar menu contribution item
	 * @param isEnabled should the item be enabled
	 * @since 3.1.1
	 */
//	private void checkEnabledMenuItem(IWorkbenchPage wpage,
//			IContributionItem menuContribution,
//			String itemName,
//			boolean isEnabled) {
//		Menu m = new Menu(wpage.getWorkbenchWindow().getShell());
//		try {
//			menuContribution.fill(m, 0);
//			MenuItem[] items = m.getItems();
//			MenuItem checkItem = null;
//			for (int i = 0; i < items.length; i++) {
//				MenuItem item = items[i];
//				if (item.getText().indexOf(itemName) >= 0) {
//					checkItem = item;
//				}
//			}
//			assertNotNull(checkItem);
//			assertEquals(isEnabled, checkItem.isEnabled());
//		} finally {
//			menuContribution.dispose();
//			m.dispose();
//		}
//	}

	/**
	 * Test that the view toolbar visibility matches the presentation
	 * visibility for a view.
	 *
	 * @throws Throwable on an error
	 * @since 3.2
	 */
	@Test
	@Ignore
	public void XXXtestPerspectiveViewToolBarVisible() throws Throwable {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
		preferenceMemento.setPreference(PrefUtil.getAPIPreferenceStore(),
				IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);

		IPerspectiveDescriptor perspective = WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						PerspectiveViewsBug88345.PERSP_ID);
		page.setPerspective(perspective);

		IEditorPart editor = null;
		IEditorRegistry registry = window.getWorkbench().getEditorRegistry();
		IPerspectiveDescriptor secondPerspective = WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						SessionPerspective.ID);
		try {
			// a view with it's toolbar on the line below the tab
			page.showView(PerspectiveViewsBug88345.PROP_SHEET_ID);
			IViewReference viewRef = page
					.findViewReference(PerspectiveViewsBug88345.PROP_SHEET_ID);

			IProject proj = FileUtil.createProject("TBTest");
			IFile test01 = FileUtil.createFile("test01.txt", proj);

			// make sure the view is active
			assertNotNull("The view must exist", viewRef.getPart(true));
			page.activate(viewRef.getPart(true));


//			assertTrue(facade.isViewPaneVisible(viewRef));
			// FIXME: No implementation for facade.isViewPaneVisible()

//			assertTrue(facade.isViewToolbarVisible(viewRef));
			// FIXME: No implementation for facade.isViewToolbarVisible()


			// open the editor and zoom it.
			editor = page.openEditor(new FileEditorInput(test01), registry
					.getDefaultEditor(test01.getName()).getId());
			assertNotNull("must have my editor", editor);

			IWorkbenchPartReference ref = page.getReference(editor);
			page.toggleZoom(ref);
//			assertFalse(facade.isViewPaneVisible(viewRef));
			// FIXME: No implementation for facade.isViewPaneVisible()

//			assertFalse(facade.isViewToolbarVisible(viewRef));
			// FIXME: No implementation facade.isViewToolbarVisible()


			// switch to another perspective, and then switch back.
			page.setPerspective(secondPerspective);

//			assertFalse(facade.isViewPaneVisible(viewRef));
			// FIXME: No implementation facade.isViewPaneVisible()

//			assertFalse(facade.isViewToolbarVisible(viewRef));
			// FIXME: No implementation facade.isViewToolbarVisible()


			page.setPerspective(perspective);
			processEvents();

			// both the view and the toolbar must be not visible
//			assertFalse(facade.isViewPaneVisible(viewRef));
			// FIXME: No implementation facade.isViewPaneVisible()

//			assertFalse(facade.isViewToolbarVisible(viewRef));
			// FIXME: No implementation facade.isViewToolbarVisible()

		} finally {
			if (editor != null) {
				page.closeEditor(editor, false);
			}
			page.closePerspective(perspective, false, false);
			page.closePerspective(secondPerspective, false, false);
		}
	}

	@Override
	protected void doSetUp() throws Exception {
		window = openTestWindow();
		page = window.getActivePage();
	}
}
