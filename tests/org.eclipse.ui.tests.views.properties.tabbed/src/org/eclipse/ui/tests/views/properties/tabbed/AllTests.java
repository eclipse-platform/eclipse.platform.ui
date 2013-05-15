/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests
    extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TabbedPropertySheetPageTest.class);
        suite.addTestSuite(TabbedPropertySheetPageDynamicTest.class);
        suite.addTestSuite(TabbedPropertySheetPageTextTest.class);
        suite.addTestSuite(TabbedPropertySheetPageOverrideTest.class);
        suite.addTestSuite(TabbedPropertySheetPageDecorationsTest.class);
        return suite;
    }

}
