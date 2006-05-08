/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.contexts;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The suite of tests related to the "org.eclipse.ui.contexts" extension point,
 * and the "org.eclise.ui.contexts" Java API. This includes tests dealing with
 * other extension points or elements in other extension points that have been
 * deprecated to be replaced by this one.
 * 
 * @since 3.0
 */
public final class ContextsTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static final Test suite() {
        return new ContextsTestSuite();
    }

    /**
     * Constructs a new instance of <code>ContextsTestSuite</code> with all of
     * the relevent test cases.
     */
    public ContextsTestSuite() {
        addTestSuite(Bug74990Test.class);
        addTestSuite(Bug84763Test.class);
        addTestSuite(ExtensionTestCase.class);
        addTestSuite(PartContextTest.class);
    }
}
