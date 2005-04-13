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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class ViewPerformanceSuite extends TestSuite {

	public static final String BASIC_VIEW = "org.eclipse.ui.tests.perf_basic";
    public static final String FINGERPRINT_TEST = BASIC_VIEW;
    //public static final String [] VIEW_IDS = {BASIC_VIEW, IPageLayout.ID_RES_NAV, MockViewPart.ID};
    
    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new ViewPerformanceSuite();
    }
    
    /**
     * 
     */
    public ViewPerformanceSuite() {        
        addOpenCloseScenarios();
    }

    /**
     * 
     */
    private void addOpenCloseScenarios() {
        String[] ids = WorkbenchPerformanceSuite.getAllTestableViewIds();
        
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            
            boolean fingerprint = id.equals(FINGERPRINT_TEST);
        	//tag
            addTest(new OpenCloseViewTest(id, fingerprint ? BasicPerformanceTest.LOCAL 
                    : BasicPerformanceTest.NONE));            
        }         
    }
}
