/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.autotests;

import java.net.URL;

import org.eclipse.ui.WorkbenchException;

/**
 * @since 3.1
 */
public class AutoTestLogger extends AbstractTestLogger {

    private TestResults errors = new TestResults();
    private TestResults expectedResults;
    private TestResults unknownTests = new TestResults();
    
    public AutoTestLogger(URL expectedResultsFile) throws WorkbenchException {
        this(new TestResults(XmlUtil.read(expectedResultsFile)));
    }
    
    public AutoTestLogger(TestResults expectedResults) {
        this.expectedResults = expectedResults;
    }
    
    public AutoTestLogger() {
        this(new TestResults());
    }
    
    public void setExpectedResults(TestResults results) {
        this.expectedResults = results;
    }
    
    public TestResults getErrors() {
        return errors;
    }
    
    public TestResults getUnknownTests() {
        return unknownTests;
    }
    
    @Override
	public void reportResult(String testName, TestResult result) throws Throwable {
        TestResultFilter expectedResult = expectedResults.get(testName);
        
        if (expectedResult == null) {
            // If unknown test name
            unknownTests.put(testName, new TestResultFilter(result));
        } else {
            try {
                // Check if this is the expected result
                expectedResult.assertResult(result);
            } catch (Throwable t) {
                // If not, record the invalid result
                errors.put(testName, new TestResultFilter(result));
                throw t;
            }
        }
        
        // Test passed. Nothing to do.
    }
}
