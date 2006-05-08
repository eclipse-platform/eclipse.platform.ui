/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
            Class clazz = Class.forName(className);
            if (TestSuite.class.isAssignableFrom(clazz))
                addTest((Test) clazz.newInstance());
            else
                addTestSuite(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
