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

    public static final String [] PERSPECTIVE_IDS = {UIPerformanceTestSetup.PERSPECTIVE, "org.eclipse.ui.resourcePerspective"};
    public static final String [][] PERSPECTIVE_SWITCH_PAIRS = {{"org.eclipse.ui.tests.dnd.dragdrop", "org.eclipse.ui.tests.fastview_perspective"}};
    public static final int ITERATIONS = 100;
    
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
        addPerspectiveOpenCloseScenarios();
        addPerspectiveSwitchScenarios();
        addWindowOpeningScenarios();
    }

    /**
     * 
     */
    private void addWindowOpeningScenarios() {
        
    }

    /**
     * 
     *
     */
    private void addPerspectiveOpenCloseScenarios() {
        for (int i = 0; i < PERSPECTIVE_IDS.length; i++) {
            addTest(new OpenClosePerspectiveTest(PERSPECTIVE_IDS[i]));
        }
    }
    
    /**
     * 
     */
    private void addPerspectiveSwitchScenarios() {
        for (int i = 0; i < PERSPECTIVE_SWITCH_PAIRS.length; i++) {
            addTest(new PerspectiveSwitchTest(PERSPECTIVE_SWITCH_PAIRS[i]));            
        }   
    }
}
