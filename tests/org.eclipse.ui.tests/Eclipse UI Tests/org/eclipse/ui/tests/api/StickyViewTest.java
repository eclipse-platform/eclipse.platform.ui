/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IStickyViewDescriptor;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * @since 3.0
 */
public class StickyViewTest extends UITestCase {

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

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        window = openTestWindow();
        page = window.getActivePage();
    }
}