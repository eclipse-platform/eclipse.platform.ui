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
 * @since 3.1
 */
public class ZoomedViewCloseTest extends CloseTest {

    /**
     * @param name
     */
    public ZoomedViewCloseTest(String name) {
        super(name);
    }

    @Override
	public IWorkbenchPart getStackedPart1() {
        return stackedView1;
    }

    @Override
	public IWorkbenchPart getStackedPart2() {
        return stackedView2;
    }

    @Override
	public IWorkbenchPart getUnstackedPart() {
        return unstackedView;
    }

    /**
     * <p>Test: Zoom a view, then close the active editor.</p>
     * <p>Expected result: The view remains zoomed and active. A new editor is selected as the
     *    active editor.</p>
     * <p>Note: The behavior of this test changed intentionally on 050416. Closing the active editor
     *    no longer unzooms if a view is zoomed.</p> 
     */
    public void testCloseActiveEditorWhileViewZoomed() {
        page.activate(editor1);
        zoom(stackedView1);
        close(editor1);
        
        assertZoomed(stackedView1);
        assertActive(stackedView1);
    }
    
    /**
     * <p>Test: Zoom an unstacked view and close it.</p>
     * <p>Expected result: The previously active part becomes active and unzoomed</p>
     * <p>Note: This ensures that the activation list is used if there is nothing available
     *    in the currently zoomed stack.</p>
     */
    public void testCloseZoomedUnstackedViewAfterActivatingView() {
        IWorkbenchPart previousActive = stackedView1;
        IWorkbenchPart zoomedPart = getUnstackedPart();
        
        page.activate(previousActive);
        zoom(zoomedPart);
        close(zoomedPart);

        assertZoomed(null);
        assertActive(previousActive);
    }

    /**
     * <p>Test: Activate an unstacked view, activate a stacked part, then close the active part.</p>
     * <p>Expected result: The unstacked part becomes active</p>
     * <p>Note: This isn't really a zoom test, but it ensures that activation 
     *    will move between stacks when there is no zoom.</p>
     */
    public void testCloseUnzoomedStackedViewAfterActivatingView() {
        IWorkbenchPart activePart = getStackedPart1();
        IWorkbenchPart unstackedPart = unstackedView;
        
        page.activate(unstackedPart);
        page.activate(activePart);
        close(activePart);
        
        // Ensure that the other part in the zoomed stack is now zoomed and active
        assertZoomed(null);
        assertActive(unstackedPart);
    }
}
