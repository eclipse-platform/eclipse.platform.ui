/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
public class ZoomedEditorCloseTest extends CloseTest {

    /**
     * @param name
     */
    public ZoomedEditorCloseTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.CloseTest#getStackedPart1()
     */
    public IWorkbenchPart getStackedPart1() {
        return editor1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.CloseTest#getStackedPart2()
     */
    public IWorkbenchPart getStackedPart2() {
        return editor2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.CloseTest#getUnstackedPart()
     */
    public IWorkbenchPart getUnstackedPart() {
        return editor3;
    }
    
    /**
     * <p>Test: Activate a view, then zoom an unstacked editor and close it.</p>
     * <p>Expected result: The previously active Editor becomes active and unzoomed</p>
     * <p>Note: This ensures that the activation list is used if there is nothing available
     *    in the currently zoomed stack. It also ensures that activation never moves from
     *    an editor to a view when an editor is closed.</p>
     */
    public void testCloseZoomedUnstackedEditorAfterActivatingView() {
    	System.out.println("Bogus test: we don't unsoom in this case");
//        IWorkbenchPart previousActive = stackedView1;
//        IWorkbenchPart zoomedPart = editor3;
//        
//        page.activate(editor1);
//        page.activate(previousActive);
//        zoom(zoomedPart);
//        close(zoomedPart);
//
//        assertZoomed(null);
//        assertActive(editor1);
    }
    
    /**
     * <p>Test: Activate an unstacked editor, activate an unstacked view, activate a stacked editor, 
     *    then close the active editor.</p>
     * <p>Expected result: The previously active editor becomes active (even though a view is next
     *    in the activation list)</p>
     * <p>Note: This isn't really a zoom test, but it ensures that activation doesn't move from an editor
     *    to a view when the active editor is closed. Activating an editor in a different stack first 
     *    ensures that activation WILL move between editor stacks to follow the activation order.</p> 
     */
    public void testCloseUnzoomedStackedEditorAfterActivatingView() {
        page.activate(editor3);
        page.activate(unstackedView);
        page.activate(editor1);
        close(editor1);
        
        // Ensure that activation moved to the previously active editor, even though
        // a view was next in the activation list.
        assertZoomed(null);
        assertActive(editor3);
    }
}
