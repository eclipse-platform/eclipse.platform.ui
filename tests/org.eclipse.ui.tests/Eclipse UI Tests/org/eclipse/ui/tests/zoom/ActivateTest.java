/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;

public abstract class ActivateTest extends ZoomTestCase {
    public ActivateTest(String name) {
        super(name);
    }
    
    public abstract IWorkbenchPart getStackedPart1();
    public abstract IWorkbenchPart getStackedPart2();
    public abstract IWorkbenchPart getUnstackedPart();
    
    /**
     * <p>Test: Zoom a part and activate it</p>
     * <p>Expected result: Part remains zoomed</p>
     */
    public void testZoomAndActivate() {
        IWorkbenchPart stacked1 = getStackedPart1();
        
        zoom(stacked1);
        page.activate(stacked1);
        
        assertZoomed(stacked1);
        assertActive(stacked1);
    }
    
    /** 
     * <p>Test: Zoom a view then activate another view in the same stack</p>
     * <p>Expected result: Stack remains zoomed</p> 
     */
    public void testActivateSameStack() {
        IWorkbenchPart stacked1 = getStackedPart1();
        IWorkbenchPart stacked2 = getStackedPart2();
        
        // Ensure that every view in the stack is zoomed
        zoom(stacked1);
        
        // Ensure that activating another zoomed part in the same stack doesn't affect zoom
        page.activate(stacked2);
        
        assertZoomed(stacked2);
        assertActive(stacked2);        
    }
    
    /** 
     * <p>Test: Zoom a view than activate a view in a different stack</p>
     * <p>Expected result: page unzooms</p> 
     */
    public void testActivateOtherStack() {
        IWorkbenchPart stacked1 = getStackedPart1();
        IWorkbenchPart unstacked = getUnstackedPart();
        
        zoom(stacked1);
        page.activate(unstacked);
        
        assertZoomed(null);
        assertActive(unstacked);
    }
    
    /** 
     * <p>Test: Zoom a view, activate a fast view, then activate the zoomed view again</p>
     * <p>Expected result: view remains zoomed</p> 
     */
    public void testActivateFastView() {
        IWorkbenchPart stacked1 = getStackedPart1();
        zoom(stacked1);
        page.activate(fastView);
        
        assertZoomed(stacked1);
        assertActive(fastView);
        
        page.activate(stacked1);
        
        assertZoomed(stacked1);
        assertActive(stacked1);
    }
    
    /** 
     * <p>Test: Zoom a pane then create a new fast view with the VIEW_ACTIVATE mode</p>
     * <p>Expected result: the original pane remains zoomed</p> 
     */
    public void testCreateFastView() {
        IWorkbenchPart zoomedPart = getStackedPart1();
        
        close(fastView);
        zoom(zoomedPart);
        fastView = showFastView(ZoomPerspectiveFactory.FASTVIEW1);
        
        assertZoomed(zoomedPart);
        assertActive(fastView);
    }

    /** 
     * <p>Test: Zoom a pane, then turn the fast view back into a regular view</p>
     * <p>Expected result: the original pane remains zoomed</p> 
     */
    public void testRestoreFastView() {
        IWorkbenchPart zoomedPart = getStackedPart1();
        
        zoom(zoomedPart);
        
        // Restore the fast view. Shouldn't have any effect on zoom.
        page.removeFastView((IViewReference)page.getReference(fastView));

        assertZoomed(zoomedPart);
        assertActive(zoomedPart);
    }
    
    /**
     * <p>Test: Zoom a pane, then reset perspective.</p>
     * <p>Expected result: the page unzooms but the original pane remains active</p>
     * 
     * @since 3.1
     */
    public void testResetPerspective() {
        IWorkbenchPart zoomedPart = getStackedPart1();
        
        zoom(zoomedPart);
        
        page.resetPerspective();
        
        assertZoomed(null);
        assertActive(zoomedPart);
    }

}