/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.keys;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for all areas of the key support for the platform.
 */
public class KeysTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static Test suite() {
        return new KeysTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public KeysTestSuite() {
    	super(KeysTestSuite.class.getName());
    	addTest(new TestSuite(BindingInteractionsTest.class));
    	addTest(new TestSuite(BindingManagerTest.class));
        addTest(new TestSuite(BindingPersistenceTest.class));
        // TODO This no longer works due to focus issues related to key bindings
        //addTest(new TestSuite(Bug36420Test.class));
        addTest(new TestSuite(Bug36537Test.class));
        //		TODO Intermittent failure.  SWT Bug 44344.  XGrabPointer?
        //		addTest(new TestSuite(Bug40023Test.class));
        addTest(new TestSuite(Bug42024Test.class));
        addTest(new TestSuite(Bug42035Test.class));
        //		TODO Logging piece of fix did not go in M4.
        //		addTest(new TestSuite(Bug42627Test.class));
        addTest(new TestSuite(Bug43168Test.class));
        addTest(new TestSuite(Bug43321Test.class));
        addTest(new TestSuite(Bug43538Test.class));
        addTest(new TestSuite(Bug43597Test.class));
        addTest(new TestSuite(Bug43610Test.class));
        addTest(new TestSuite(Bug43800Test.class));
        addTest(new TestSuite(KeysCsvTest.class));
        //		TODO disabled since it refers to the Java builder and nature,
        //      which are not available in an RCP build
        //		addTest(new TestSuite(Bug44460Test.class));
        /* TODO disabled as it fails on the Mac.
         * Ctrl+S doesn't save the editor, and posting MOD1+S also doesn't seem to work.
         */
        //addTest(new TestSuite(Bug53489Test.class));
        addTest(new TestSuite(Bug189167Test.class));
        addTest(new TestSuite(KeysPreferenceModelTest.class));
    }
}
