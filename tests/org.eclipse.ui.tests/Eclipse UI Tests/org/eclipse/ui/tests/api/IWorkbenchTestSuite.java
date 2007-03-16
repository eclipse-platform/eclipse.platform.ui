/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the workbench. This suite was created as a 
 * workaround for problems running the suites from the
 * command line.
 */
public class IWorkbenchTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new IWorkbenchTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public IWorkbenchTestSuite() {
        addTest(new TestSuite(IWorkbenchTest.class));
        addTest(new TestSuite(IWorkbenchWindowTest.class));
    }
}
