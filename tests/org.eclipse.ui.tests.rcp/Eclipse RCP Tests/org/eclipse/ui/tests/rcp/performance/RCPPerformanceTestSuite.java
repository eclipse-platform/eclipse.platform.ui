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
package org.eclipse.ui.tests.rcp.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class RCPPerformanceTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
    	return new RCPPerformanceTestSetup(new RCPPerformanceTestSuite());
    }

    /**
     * Construct the test suite.
     */
    public RCPPerformanceTestSuite() {
        addTestSuite(PlatformUIPerfTest.class);
        addTestSuite(EmptyWorkbenchPerfTest.class);
    }
}
