/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The suite of tests related to concurrency and deadlock.
 * 
 * @since 3.1
 */
public final class ConcurrencyTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     */
    public static final Test suite() {
        return new ConcurrencyTestSuite();
    }

    /**
     * Constructs a new instance of <code>ConcurrencyTestSuite</code> with all of
     * the relevant test cases.
     */
    public ConcurrencyTestSuite() {
    	addTestSuite(ModalContextCrashTest.class);
        addTestSuite(NestedSyncExecDeadlockTest.class);
        addTestSuite(SyncExecWhileUIThreadWaitsForRuleTest.class);
        addTestSuite(SyncExecWhileUIThreadWaitsForLock.class);
        addTestSuite(TestBug105491.class);
        addTestSuite(TestBug108162.class);
        addTestSuite(TestBug138695.class);
        addTestSuite(TestBug98621.class);
        addTestSuite(TransferRuleTest.class);
        addTestSuite(Bug_262032.class);
    }
}
