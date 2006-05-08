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
package org.eclipse.ui.tests.autotests;

import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public abstract class UITestCaseWithResult extends UITestCase implements AutoTest {
    private AbstractTestLogger resultLog;
    
    public UITestCaseWithResult(String testName, AbstractTestLogger log) {
        super(testName);
        this.resultLog = log;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected final void runTest() throws Throwable {
        String testName = getName();
        
        TestResult result;
        try {
            result = new TestResult(performTest());
        } catch (Throwable t) {
            result = new TestResult(t);
        }
        
        resultLog.reportResult(testName, result);
    }
    
}
