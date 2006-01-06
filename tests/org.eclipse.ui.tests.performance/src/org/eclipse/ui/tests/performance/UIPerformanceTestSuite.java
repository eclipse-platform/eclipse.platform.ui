/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.ui.tests.performance.presentations.PresentationPerformanceTestSuite;

/**
 * Test all areas of the UI API.
 */
public class UIPerformanceTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
    	return new UIPerformanceTestSetup(new UIPerformanceTestSuite());
    }

    /**
     * Construct the test suite.
     */
    public UIPerformanceTestSuite() {
        addTest(new ActivitiesPerformanceSuite());
        addTest(new PresentationPerformanceTestSuite());
        addTest(new WorkbenchPerformanceSuite());
        addTest(new ViewPerformanceSuite());
        addTest(new EditorPerformanceSuite());
        addTest(new TestSuite(CommandsPerformanceTest.class));
    }
}
