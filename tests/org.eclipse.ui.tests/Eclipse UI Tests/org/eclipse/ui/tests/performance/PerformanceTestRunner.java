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
 * Runs a test specified by the org.eclipse.ui.performance.test property.  
 * This test is prepped via the UIPerformanceTestSetup test setup.
 * 
 * @since 3.1
 */
public class PerformanceTestRunner extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new UIPerformanceTestSetup(new PerformanceTestRunner());
    }

    public PerformanceTestRunner() {
        String className = System.getProperty("org.eclipse.ui.performance.test");
        try {
            addTestSuite(Class.forName(className));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
