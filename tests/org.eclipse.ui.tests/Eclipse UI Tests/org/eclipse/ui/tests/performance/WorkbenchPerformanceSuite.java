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

package org.eclipse.ui.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
class WorkbenchPerformanceSuite extends TestSuite {

	private static String RESOURCE_PERSPID = "org.eclipse.ui.resourcePerspective";
    // Note: to test perspective switching properly, we need perspectives with lots of
    // associated actions. 
	// NOTE - do not change the order of the IDs below.  the PerspectiveSwitchTest has a 
	// fingerprint test for performance that releys on this not changing.
    public static final String [] PERSPECTIVE_IDS = {
        UIPerformanceTestSetup.PERSPECTIVE1, 
        RESOURCE_PERSPID,
        "org.eclipse.jdt.ui.JavaPerspective", 
        "org.eclipse.debug.ui.DebugPerspective"};
    
    public static final String [][] PERSPECTIVE_SWITCH_PAIRS = {
    	{UIPerformanceTestSetup.PERSPECTIVE1, UIPerformanceTestSetup.PERSPECTIVE2, "1.perf_basic"},
        
        // Test switching between a perspective with lots of actions and a perspective with none
        {"org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.ui.tests.util.EmptyPerspective", "1.perf_basic"},
        
        // Test switching between two perspectives with lots of actions but some commonality
        {"org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.debug.ui.DebugPerspective", "1.java"},
        {RESOURCE_PERSPID, "org.eclipse.jdt.ui.JavaPerspective", "1.java"} 
    };
    
    public static final String[] VIEW_IDS = {
        "org.eclipse.ui.views.ProblemView",
        "org.eclipse.ui.views.ResourceNavigator"
    };
    public static final int ITERATIONS = 25;
    
    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new WorkbenchPerformanceSuite();
    }
    
    /**
     * 
     */
    public WorkbenchPerformanceSuite() {
        addPerspectiveSwitchScenarios();
        addPerspectiveOpenCloseScenarios();
        addWindowOpenCloseScenarios();
        addWindowResizeScenarios();
    }

    /**
     * 
     */
    private void addWindowOpenCloseScenarios() {
        for (int i = 0; i < PERSPECTIVE_IDS.length; i++) {
            addTest(new OpenCloseWindowTest(PERSPECTIVE_IDS[i], i == 0 ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE));
        }        
    }

    /**
     * 
     *
     */
    private void addPerspectiveOpenCloseScenarios() {
        for (int i = 0; i < PERSPECTIVE_IDS.length; i++) {
            addTest(new OpenClosePerspectiveTest(PERSPECTIVE_IDS[i], i == 0 ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE));
        }
    }
    
    /**
     * 
     */
    private void addPerspectiveSwitchScenarios() {
        for (int i = 0; i < PERSPECTIVE_SWITCH_PAIRS.length; i++) {
            addTest(new PerspectiveSwitchTest(PERSPECTIVE_SWITCH_PAIRS[i], i == 0 ? BasicPerformanceTest.GLOBAL : BasicPerformanceTest.NONE));            
        }   
    }
    
    /**
     * add a single fingerprint test for window resize
     */
    private void addWindowResizeScenarios() {
    	if (PERSPECTIVE_IDS.length == 0)
    		return;
    	
        addTest(new WindowResizeTest(new String[] {RESOURCE_PERSPID}, BasicPerformanceTest.GLOBAL));
    }
    
}