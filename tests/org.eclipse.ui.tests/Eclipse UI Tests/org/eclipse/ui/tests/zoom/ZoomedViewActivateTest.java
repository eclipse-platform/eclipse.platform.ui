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
public class ZoomedViewActivateTest extends ActivateTest {

    /**
     * @param name
     */
    public ZoomedViewActivateTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getStackedPart1()
     */
    @Override
	public IWorkbenchPart getStackedPart1() {
        return stackedView1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getStackedPart2()
     */
    @Override
	public IWorkbenchPart getStackedPart2() {
        return stackedView2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.zoom.ActivateTest#getUnstackedPart()
     */
    @Override
	public IWorkbenchPart getUnstackedPart() {
        return unstackedView;
    }

    /** 
     * <p>Test: Zoom a view then activate an editor</p>
     * <p>Expected result: page unzooms</p> 
     */
    public void testActivateEditor() {
    	// We allow an editor to be activated *without* unzooming
    	System.out.println("Bogus Test: " + getName());
    	
//        zoom(stackedView1);
//        page.activate(editor1);
//        
//        assertZoomed(null);
//        assertActive(editor1);
    }
}
