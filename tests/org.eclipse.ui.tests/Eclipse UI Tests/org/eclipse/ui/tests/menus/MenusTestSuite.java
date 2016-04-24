/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Axel Richard <axel.richard@obeo.fr> - Bug 392457
 *******************************************************************************/
package org.eclipse.ui.tests.menus;


import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests for all code related to menus. This includes the
 * <code>popupMenus</code> extension point, and others.
 */
@RunWith(AllTests.class)
public class MenusTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new MenusTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public MenusTestSuite() {
		/*
		 * TODO: MenusTestSuite was previously disabled in UiTestSuite and these
		 * commented-out tests must be validated and fixed up
		 */
		// addTest(new TestSuite(ObjectContributionTest.class));
		// addTest(new TestSuite(MenuVisibilityTest.class));
		// addTest(new TestSuite(MenuBaseTests.class));
		// addTest(new TestSuite(MenuPopulationTest.class));
		// addTest(new TestSuite(DynamicMenuTest.class));
		// addTest(new TestSuite(Bug231304Test.class));
		// addTest(new TestSuite(ShowViewMenuTest.class));
		// addTest(new TestSuite(Bug264804Test.class));
		// addTest(new TestSuite(MenuHelperTest.class));
		// addTest(new TestSuite(Bug410426Test.class));
		addTest(new TestSuite(DynamicToolbarTest.class));
    }
}
