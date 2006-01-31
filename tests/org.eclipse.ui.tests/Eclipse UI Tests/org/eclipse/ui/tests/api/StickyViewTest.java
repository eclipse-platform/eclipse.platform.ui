/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.FastViewBar;
import org.eclipse.ui.internal.FastViewBarContextMenuContribution;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.IStickyViewDescriptor;

/**
 * @since 3.0
 */
public class StickyViewTest extends UITestCase {
	
	/**
	 * Allow tests to run just in this class.
	 * @return the TestSuite to run.
	 */
	public static TestSuite suite() {
		return new TestSuite(StickyViewTest.class);
	}
	
    private IWorkbenchWindow window;

    private IWorkbenchPage page;

    /**
     * @param testName
     */
    public StickyViewTest(String testName) {
        super(testName);
    }

    public void testStackPlacementRight() {
        testStackPlacement("Right");
    }

    public void testStackPlacementLeft() {
        testStackPlacement("Left");
    }

    public void testStackPlacementTop() {
        testStackPlacement("Top");
    }

    public void testStackPlacementBottom() {
        testStackPlacement("Bottom");
    }

    /**
     * Tests to ensure that sticky views are opened in the same stack.
     */
    private void testStackPlacement(String location) {
        try {
            IViewPart part1 = page
                    .showView("org.eclipse.ui.tests.api.StickyView" + location
                            + "1");
            assertNotNull(part1);
            IViewPart part2 = page
                    .showView("org.eclipse.ui.tests.api.StickyView" + location
                            + "2");
            assertNotNull(part2);
            IViewPart[] stack = page.getViewStack(part1);

            assertTrue(ViewUtils.findInStack(stack, part1));
            assertTrue(ViewUtils.findInStack(stack, part2));

        } catch (PartInitException e) {
            fail(e.getMessage());
        }

    }

    /**
     * Tests to ensure that all views in a stack with a known sticky view are also sticky.
     */
    public void testStackContents() {
        try {
            IViewPart part1 = page
                    .showView("org.eclipse.ui.tests.api.StickyViewRight1");
            assertNotNull(part1);

            IViewPart[] stack = page.getViewStack(part1);

            for (int i = 0; i < stack.length; i++) {
                assertTrue(stack[i].getTitle(), ViewUtils.isSticky(stack[i]));
            }
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests whether the moveable flag is being picked up and honoured
     * from the XML.
     */
    public void testClosableFlag() {
        //explicit closeable = true
        testCloseable("org.eclipse.ui.tests.api.StickyViewRight1", true);
        //explicit closeable = false
        testCloseable("org.eclipse.ui.tests.api.StickyViewRight2", false);
        //implicit closeable = true
        testCloseable("org.eclipse.ui.tests.api.StickyViewLeft1", true);
    }

    public void testMoveableFlag() {
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
     * @param id the id
     * @param expectation the expected moveable state
     */
    private void testMoveable(String id, boolean expectation) {
        try {
            IViewPart part = page.showView(id);
            assertNotNull(part);
            assertTrue(ViewUtils.isSticky(part));

            //tests to ensure that the XML was read correctly            
            IStickyViewDescriptor[] descs = WorkbenchPlugin.getDefault()
                    .getViewRegistry().getStickyViews();
            for (int i = 0; i < descs.length; i++) {
                if (descs[i].getId().equals(id)) {
                    assertEquals(expectation, descs[i].isMoveable());
                }
            }

            // tests to ensure that the property is being honoured by the perspective
            assertEquals(expectation, ViewUtils.isMoveable(part));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /** 
     * Tests whether a sticky view with the given id is closeable or not.
     * 
     * @param id the id
     * @param expectation the expected closeable state
     */
    private void testCloseable(String id, boolean expectation) {
        try {
            IViewPart part = page.showView(id);
            assertNotNull(part);
            assertTrue(ViewUtils.isSticky(part));

            //tests to ensure that the XML was read correctly            
            IStickyViewDescriptor[] descs = WorkbenchPlugin.getDefault()
                    .getViewRegistry().getStickyViews();
            for (int i = 0; i < descs.length; i++) {
                if (descs[i].getId().equals(id)) {
                    assertEquals(expectation, descs[i].isCloseable());
                }
            }

            // tests to ensure that the property is being honoured by the perspective
            assertEquals(expectation, ViewUtils.isCloseable(part));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Sticky views should remain after perspective reset.
     */
    public void testPerspectiveReset() {
        try {
            page.showView("org.eclipse.ui.tests.api.StickyViewRight1");
            page.resetPerspective();
            assertNotNull(page
                    .findView("org.eclipse.ui.tests.api.StickyViewRight1"));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that a sticky view is opened in successive perspectives.
     */
    public void testPerspectiveOpen() {
        try {
            page.showView("org.eclipse.ui.tests.api.StickyViewRight1");
            page.setPerspective(WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry().findPerspectiveWithId(
                            "org.eclipse.ui.tests.api.SessionPerspective"));
            assertNotNull(page
                    .findView("org.eclipse.ui.tests.api.StickyViewRight1"));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Test that closing a stand-alone view remove the editor stack and
     * doesn't throw an NPE.
     * 
     * @throws Throwable on error
     * @since 3.2
     */
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
	 * Test that a view marked as non-closable cannot be closed as a fast view.
	 * 
	 * @throws Throwable
	 * @since 3.1.1
	 */
	public void testPerspectiveCloseFastView() throws Throwable {
		page.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						PerspectiveViewsBug88345.PERSP_ID));

		try {
			// the non-closeable view
			IViewReference stickyRef = page
					.findViewReference(MockViewPart.IDMULT);
			IViewPart stickyView = (IViewPart) stickyRef.getPart(true);
			page.activate(stickyView);

			IViewReference viewRef = page
					.findViewReference(PerspectiveViewsBug88345.NORMAL_VIEW_ID);

			WorkbenchPage wpage = (WorkbenchPage) page;
			assertFalse(wpage.isFastView(stickyRef));

			wpage.addFastView(stickyRef);
			assertTrue(wpage.isFastView(stickyRef));

			wpage.addFastView(viewRef);
			assertTrue(wpage.isFastView(viewRef));

			FastViewBar fastViewBar = ((WorkbenchWindow) page
					.getWorkbenchWindow()).getFastViewBar();
			FastViewBarContextMenuContribution menuContribution = fastViewBar
					.testContextMenu();

			// set the target of a normal view that is now a fast view
			// close should be enabled
			menuContribution.setTarget(viewRef);
			checkEnabledMenuItem(wpage, menuContribution, "Close", true);

			// set the target of our non-closeable fast view
			// close should not be enabled
			menuContribution.setTarget(stickyRef);
			checkEnabledMenuItem(wpage, menuContribution, "Close", false);
		} finally {
			page.closePerspective(page.getPerspective(), false, false);
		}
	}
	
	/**
	 * Test that a fast view marked as non-moveable cannot be docked.
	 * 
	 * @throws Throwable
	 * @since 3.1.1
	 */
	public void testPerspectiveMoveFastView() throws Throwable {
		page.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						PerspectiveViewsBug88345.PERSP_ID));

		try {
			// the non-moveable view
			IViewReference stickyRef = page
					.findViewReference(MockViewPart.IDMULT, "1");

			IViewReference viewRef = page
					.findViewReference(PerspectiveViewsBug88345.NORMAL_VIEW_ID);

			WorkbenchPage wpage = (WorkbenchPage) page;
			assertFalse(wpage.isFastView(viewRef));
			assertTrue(wpage.isFastView(stickyRef));

			wpage.addFastView(viewRef);
			assertTrue(wpage.isFastView(viewRef));

			FastViewBar fastViewBar = ((WorkbenchWindow) page
					.getWorkbenchWindow()).getFastViewBar();
			FastViewBarContextMenuContribution menuContribution = fastViewBar
					.testContextMenu();

			// set the target of a normal view that is now a fast view
			// Fast View should be enabled
			menuContribution.setTarget(viewRef);
			checkEnabledMenuItem(wpage, menuContribution, "Fast View", true);

			// set the target of our non-closeable fast view
			// Fast View should not be enabled
			menuContribution.setTarget(stickyRef);
			checkEnabledMenuItem(wpage, menuContribution, "Fast View", false);
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
	private void checkEnabledMenuItem(WorkbenchPage wpage,
			FastViewBarContextMenuContribution menuContribution,
			String itemName,
			boolean isEnabled) {
		Menu m = new Menu(wpage.getWorkbenchWindow().getShell());
		try {
			menuContribution.fill(m, 0);
			MenuItem[] items = m.getItems();
			MenuItem checkItem = null;
			for (int i = 0; i < items.length; i++) {
				MenuItem item = items[i];
				if (item.getText().indexOf(itemName) >= 0) {
					checkItem = item;
				}
			}
			assertNotNull(checkItem);
			assertEquals(isEnabled, checkItem.isEnabled());
		} finally {
			menuContribution.dispose();
			m.dispose();
		}
	}

	/**
	 * Test that the view toolbar visibility matches the presentation
	 * visibility for a view.
	 * 
	 * @throws Throwable on an error
	 * @since 3.2
	 */
	public void testPerspectiveViewToolBarVisible() throws Throwable {
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
			PartSite site = (PartSite) viewRef.getPart(true).getSite();
			ViewPane pane = (ViewPane) site.getPane();

			assertTrue(pane.isVisible());
			assertNotNull("This view must have a toolbar", pane.getToolBar());
			assertTrue(pane.getToolBar().isVisible());

			// open the editor and zoom it.
			editor = page.openEditor(new FileEditorInput(test01), registry
					.getDefaultEditor(test01.getName()).getId());
			assertNotNull("must have my editor", editor);

			IWorkbenchPartReference ref = page.getReference(editor);
			page.toggleZoom(ref);
			assertFalse(pane.isVisible());
			assertFalse(pane.getToolBar().isVisible());

			// switch to another perspective, and then switch back.
			page.setPerspective(secondPerspective);

			assertFalse(pane.isVisible());
			assertFalse(pane.getToolBar().isVisible());

			page.setPerspective(perspective);
			processEvents();

			// both the view and the toolbar must be not visible
			assertFalse(pane.isVisible());
			assertFalse(pane.getToolBar().isVisible());

		} finally {
			if (editor != null) {
				page.closeEditor(editor, false);
			}
			page.closePerspective(perspective, false, false);
			page.closePerspective(secondPerspective, false, false);
		}
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
    protected void doSetUp() throws Exception {
        window = openTestWindow();
        page = window.getActivePage();
    }
}
