/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.popups;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for all code related to pop-up menus. This includes the
 * <code>popupMenus</code> extension point, as well as the code needed to
 * support it.
 */
public class PopupsTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new PopupsTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public PopupsTestSuite() {
        addTest(new TestSuite(ObjectContributionTest.class));
    }
}