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
package org.eclipse.ui.tests.performance;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Test Window resize performance on the resource perspective
 */

public class WindowResizeTest extends BasicPerformanceTest {
    private String id1;

	/**
     * Constructor.
     * 
     * @param id  the perspective ID to use
     */
    public WindowResizeTest(String [] ids) {
        super("testWindowResize:" + ids[0]);
        
    	if (ids == null || ids.length == 0) {
    		ids[0] = "org.eclipse.ui.resourcePerspective";
    	}
    	
        this.id1 = ids[0];
    }
	
    /**
     * Test window resize performance. 
     */
    protected void runTest() throws CoreException, WorkbenchException {
        // Get the two perspectives to switch between.
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry
                .findPerspectiveWithId(id1);

        // Don't fail if we reference an unknown perspective ID. This can be
        // a normal occurrance since the test suites reference JDT perspectives, which
        // might not exist. Just skip the test.
        if (perspective1 == null) {
            System.out.println("Unknown perspective ID: " + id1);
            return;
        }
            
        // Open a file.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
  
        // Open both perspective outside the loop so as not to include
        // the initial time to open, just switching.        
        activePage.setPerspective(perspective1);
        activePage.resetPerspective();
  
        tagAsGlobalSummary("Window Resize", new Dimension[] {Dimension.ELAPSED_PROCESS , Dimension.CPU_TIME});

        activePage.setPerspective(perspective1);
        Shell shell = activePage.getWorkbenchWindow().getShell();
        int w1 = 117;
        int h1 = 117;
        int w2 = 1000;
        int h2 = 700;
        int currentW = w1;
        int currentH = h1;
        boolean setSize1 = true;
        for (int i = 0; i < 20; i++) {
            processEvents();
            if (setSize1) {
            	currentW = w1;
            	currentH = h1;
            } else {
            	currentW = w2;
            	currentH = h2;
            }	
            setSize1 = !setSize1;
            
            startMeasuring();
            processEvents();
            shell.setSize(currentW, currentH);
            processEvents();
            stopMeasuring();
        	
        }
        commitMeasurements();
        assertPerformance();
    }
}