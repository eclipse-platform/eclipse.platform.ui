/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import junit.framework.Test;
import junit.framework.TestSuite;

public class NavigatorTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new NavigatorTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public NavigatorTestSuite() {
        addTest(new TestSuite(ExceptionDecoratorTestCase.class));
        addTest(new TestSuite(ResourceNavigatorTest.class));
        addTest(new TestSuite(NavigatorTest.class));
        addTest(new TestSuite(DecoratorTestCase.class));
        addTest(new TestSuite(LightweightDecoratorTestCase.class));
        addTest(new TestSuite(DuplicateMenuItemTest.class));
    }

}

