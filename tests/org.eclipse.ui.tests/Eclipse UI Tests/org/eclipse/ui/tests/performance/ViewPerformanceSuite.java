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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.tests.api.MockViewPart;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class ViewPerformanceSuite extends TestSuite {

	public static final String BASIC_VIEW = "org.eclipse.ui.tests.perf_basic";
    public static final String [] VIEW_IDS = {BASIC_VIEW, IPageLayout.ID_RES_NAV, MockViewPart.ID};
    
    public static final int ITERATIONS = 100;
    
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
        for (int i = 0; i < VIEW_IDS.length; i++) {
        	//tag
            addTest(new OpenCloseViewTest(VIEW_IDS[i], i == 0 ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE));            
        }         
    }
}
