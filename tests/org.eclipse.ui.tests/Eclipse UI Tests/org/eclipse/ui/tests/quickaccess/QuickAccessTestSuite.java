/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.quickaccess;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test areas of the Property Sheet API.
 */
public class QuickAccessTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new QuickAccessTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public QuickAccessTestSuite() {
        addTest(new TestSuite(CamelUtilTest.class));
        addTest(new TestSuite(QuickAccessDialogTest.class));
		addTest(new TestSuite(ShellClosingTest.class));
    }
}
