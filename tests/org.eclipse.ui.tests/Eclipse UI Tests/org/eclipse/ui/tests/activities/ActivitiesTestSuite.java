/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The ActivitiesTestSuite class runs the activities' test suites.
 */
public class ActivitiesTestSuite extends TestSuite {
    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new ActivitiesTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public ActivitiesTestSuite() {
        addTest(new TestSuite(StaticTest.class));
        addTest(new TestSuite(DynamicTest.class));
    }
}