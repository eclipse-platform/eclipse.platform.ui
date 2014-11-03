/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IWorkbenchPart;

/**
 * This class contains tests that apply to both views and editors. Subclasses
 * will overload the abstract methods to determine whether editors or views
 * are being tested, and can define additional tests that only apply to editors
 * or views (or that have different results).
 */
public abstract class CloseTest extends ZoomTestCase {
    public CloseTest(String name) {
        super(name);
    }

    public abstract IWorkbenchPart getStackedPart1();
    public abstract IWorkbenchPart getStackedPart2();
    public abstract IWorkbenchPart getUnstackedPart();

    /**
     * <p>Test: Zoom a part and hide an inactive fast view</p>
     * <p>Expected result: Part remains zoomed</p>
     */
    public void testCloseInactiveFastView() {
        IWorkbenchPart zoomPart = getStackedPart1();

        zoom(zoomPart);
        close(fastView);

        assertZoomed(zoomPart);
        assertActive(zoomPart);
    }

    /**
     * <p>Test: Zoom a part, activate a fast view, then hide the fast view</p>
     * <p>Expected result: Part remains zoomed</p>
     */
    public void testCloseActiveFastView() {
        IWorkbenchPart zoomPart = getStackedPart1();

        zoom(zoomPart);
        page.activate(fastView);
        close(fastView);

        assertZoomed(zoomPart);
        assertActive(zoomPart);
    }

    /**
     * <p>Test: Activate an unstacked view, zoom and activate a stacked part, then close the active part.</p>
     * <p>Expected result: Stack remains zoomed, another part in the zoomed stack is active</p>
     * <p>Note: This ensures that when the active part is closed, it will try to activate a part that
     *    doesn't affect the zoom even if something else was activated more recently.</p>
     */
    public void testCloseZoomedStackedPartAfterActivatingView() {
        IWorkbenchPart zoomPart = getStackedPart1();
        IWorkbenchPart otherStackedPart = getStackedPart2();
        IWorkbenchPart unstackedPart = unstackedView;

        page.activate(unstackedPart);
        zoom(zoomPart);
        close(zoomPart);

        assertZoomed(otherStackedPart);
        assertActive(otherStackedPart);
    }

    /**
     * <p>Test: Activate an unstacked editor, zoom and activate a stacked part, then close the active part.</p>
     * <p>Expected result: Stack remains zoomed, another part in the zoomed stack is active</p>
     * <p>Note: This ensures that when the active part is closed, it will try to activate a part that
     *    doesn't affect the zoom even if something else was activated more recently.</p>
     */
    public void testCloseZoomedStackedPartAfterActivatingEditor() {
        IWorkbenchPart zoomPart = getStackedPart1();
        IWorkbenchPart otherStackedPart = getStackedPart2();
        IWorkbenchPart unstackedPart = editor3;

        page.activate(unstackedPart);
        zoom(zoomPart);
        close(zoomPart);

        assertZoomed(otherStackedPart);
        assertActive(otherStackedPart);
    }

    /**
     * <p>Test: Activate an unstacked editor, activate a stacked part, then close the active part.</p>
     * <p>Expected result: The unstacked part becomes active</p>
     * <p>Note: This isn't really a zoom test, but it ensures that the behavior tested by
     *    testHideZoomedStackedPartAfterActivatingEditor does not affect activation when there is no zoom.</p>
     */
    public void testCloseUnzoomedStackedPartAfterActivatingEditor() {
        IWorkbenchPart activePart = getStackedPart1();
        IWorkbenchPart unstackedPart = editor3;

        page.activate(unstackedPart);
        page.activate(activePart);
        close(activePart);

        assertZoomed(null);
        assertActive(unstackedPart);
    }

    /**
     * <p>Test: Zoom an unstacked part and close it.</p>
     * <p>Expected result: The page is unzoomed and the previously active part becomes active</p>
     * <p>Note: This ensures that the activation list is used if there is nothing available
     *    in the currently zoomed stack.</p>
     */
    public void testCloseZoomedUnstackedPartAfterActivatingEditor() {
        IWorkbenchPart previousActive = editor1;
        IWorkbenchPart zoomedPart = getUnstackedPart();

        page.activate(previousActive);
        zoom(zoomedPart);
        close(zoomedPart);

        assertZoomed(null);
        assertActive(previousActive);
    }

    /**
     * <p>Test: Zoom a stacked part and close an inactive, unstacked editor.</p>
     * <p>Expected result: No change in activation or zoom</p>
     */
    public void testCloseHiddenUnstackedEditor() {
        IWorkbenchPart zoomedPart = getStackedPart1();

        // Activate another editor to ensure that we aren't closing the active editor
        page.activate(editor1);
        zoom(zoomedPart);
        close(editor3);

        assertZoomed(zoomedPart);
        assertActive(zoomedPart);
    }

    /**
     * <p>Test: Zoom a stacked part and close an inactive, unstacked view.</p>
     * <p>Expected result: No change in activation or zoom</p>
     */
    public void testCloseHiddenUnstackedView() {
        IWorkbenchPart zoomedPart = getStackedPart1();

        zoom(zoomedPart);
        close(unstackedView);

        assertZoomed(zoomedPart);
        assertActive(zoomedPart);
    }

}
