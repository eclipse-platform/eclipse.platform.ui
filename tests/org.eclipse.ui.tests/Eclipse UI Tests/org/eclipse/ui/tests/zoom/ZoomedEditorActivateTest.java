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
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.1
 */
public class ZoomedEditorActivateTest extends ActivateTest {

    /**
     * @param name
     */
    public ZoomedEditorActivateTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getStackedPart1()
     */
    public IWorkbenchPart getStackedPart1() {
        return editor1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getStackedPart2()
     */
    public IWorkbenchPart getStackedPart2() {
        return editor2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getUnstackedPart()
     */
    public IWorkbenchPart getUnstackedPart() {
        return editor3;
    }

    /** 
     * <p>Test: Zoom an editor then activate a view</p>
     * <p>Expected result: page unzooms</p> 
     */
    public void testActivateView() {
        zoom(editor1);
        page.activate(stackedView1);
        
        assertZoomed(null);
        assertActive(stackedView1);
    }

    
}
