/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI Implementation.
 */
public class InternalTestSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() {
        return new InternalTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public InternalTestSuite() {
        addTest(new TestSuite(EditorActionBarsTest.class));
        addTest(new TestSuite(ActionSetExpressionTest.class));
        addTest(new TestSuite(PopupMenuExpressionTest.class));
        addTest(new TestSuite(Bug41931Test.class));
        addTest(Bug75909Test.suite());
    }
}
