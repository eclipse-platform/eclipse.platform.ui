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
package org.eclipse.ui.tests.dynamicplugins;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for dynamic plug-in support.
 */
public class DynamicPluginsTestSuite extends TestSuite {
    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new DynamicPluginsTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public DynamicPluginsTestSuite() {
    	addTest(new TestSuite(EditorTests.class));
    	addTest(new TestSuite(IntroTests.class));
        addTest(new TestSuite(PerspectiveTests.class));
        addTest(new TestSuite(ViewTests.class));
        addTest(new TestSuite(ActionSetTests.class));
        addTest(new TestSuite(NewWizardTests.class));
        addTest(new TestSuite(ObjectContributionTests.class));
    }
}