/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ClosePerspectiveAction;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.1
 */
public class OpenClosePerspectiveTest extends BasicPerformanceTest {

    private String id;

    /**
     * @param tagging
     * @param testName
     */
    public OpenClosePerspectiveTest(String id, int tagging) {
        super("testOpenClosePerspectives:" + id, tagging);
        this.id = id;
    }
    
    protected void runTest() throws Throwable {
        // Get the two perspectives to switch between.
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry
                .findPerspectiveWithId(id);

        // Don't fail if we reference an unknown perspective ID. This can be
        // a normal occurrance since the test suites reference JDT perspectives, which
        // might not exist.
        if (perspective1 == null) {
            System.out.println("Unknown perspective id: " + id);
            return;
        }
        
        // create a nice clean window.
        IWorkbenchWindow window = openTestWindow();          
        final IWorkbenchPage activePage = window.getActivePage();
        
        //causes creation of all views 
        activePage.setPerspective(perspective1);
        IViewReference [] refs = activePage.getViewReferences();
        //get the IDs now - after we close hte perspective the view refs will be partiall disposed and their IDs will be null
        String [] ids = new String[refs.length];
        for (int i = 0; i < refs.length; i++) {
            ids[i] = refs[i].getId();
        }
        closePerspective(activePage);
        //populate the empty perspective with all view that will be shown in the test view
        for (int i = 0; i < ids.length; i++) {
            activePage.showView(ids[i]);
        }      

        tagIfNecessary("UI - Open/Close Perspective", Dimension.ELAPSED_PROCESS);
        
        exercise(new TestRunnable() {
            public void run() throws Exception {
                processEvents();
                EditorTestHelper.calmDown(500, 30000, 500);
                
                startMeasuring();
                activePage.setPerspective(perspective1);
                processEvents();      
                closePerspective(activePage);
                processEvents(); 
                stopMeasuring();
            } 
        });
        
        commitMeasurements();
        assertPerformance();
    }

    /**
     * @param activePage
     */
    private void closePerspective(IWorkbenchPage activePage) {
        // we dont have API to close a perspective so use the close perspective action instead.
        ClosePerspectiveAction action = new ClosePerspectiveAction(activePage.getWorkbenchWindow());            
        action.run();
        action.dispose();
    }
}
