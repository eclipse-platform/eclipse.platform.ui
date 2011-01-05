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
package org.eclipse.ui.tests.rcp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The test suite for the RCP APIs in the generic workbench.
 * To run, use a headless JUnit Plug-in Test launcher, configured
 * to have [No Application] as its application. 
 */
public class RcpTestSuite extends TestSuite {

    /** Returns the suite. This is required to use the JUnit Launcher. */
    public static Test suite() {
        return new RcpTestSuite();
    }

    public RcpTestSuite() {
        addTest(PlatformUITest.suite());
        addTest(new TestSuite(WorkbenchAdvisorTest.class));
        addTest(new TestSuite(WorkbenchConfigurerTest.class));
        addTest(new TestSuite(WorkbenchWindowConfigurerTest.class));
        addTest(new TestSuite(ActionBarConfigurerTest.class));
        addTest(new TestSuite(IWorkbenchPageTest.class));
        addTest(new TestSuite(WorkbenchSaveRestoreStateTest.class));
        addTest(new TestSuite(WorkbenchListenerTest.class));
    }
}
