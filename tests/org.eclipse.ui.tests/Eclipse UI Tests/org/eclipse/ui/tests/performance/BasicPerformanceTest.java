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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Baseclass for simple performance tests.
 * 
 * @since 3.1
 */
public abstract class BasicPerformanceTest extends UITestCase {

    protected PerformanceMeter performanceMeter;
    
    private IProject testProject;

    /**
     * @param testName
     */
    public BasicPerformanceTest(String testName) {
        super(testName);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
	    super.doSetUp();
		Performance performance = Performance.getDefault();
		performanceMeter = performance.createPerformanceMeter(performance.getDefaultScenarioId(this));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
	    super.doTearDown();
		performanceMeter.dispose();
	}
	
	protected IProject getProject() {
	    if (testProject == null) {
	        IWorkspace workspace = ResourcesPlugin.getWorkspace();
	        testProject = workspace.getRoot().getProject(UIPerformanceTestSetup.PROJECT_NAME);
	    }
	    return testProject;
	}
}
