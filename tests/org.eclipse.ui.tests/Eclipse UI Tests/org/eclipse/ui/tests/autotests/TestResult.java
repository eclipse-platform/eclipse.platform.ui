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


/**
 * @since 3.1
 */
public class TestResult {
    private String result;
    private Throwable thrownException;
    
    public TestResult(String expectedResult) {
        this.result = expectedResult;
    }
    
    public TestResult(Throwable t) {
        this.result = null;
        this.thrownException = t;
    }
    
    /**
     * Return the expected result or null if the test threw an
     * exception rather than terminate normally.
     *
     * @return the expected result or null if the test threw an
     * exception rather than terminate normally.
     */
    public String getReturnValue() {
        return result;
    }
    
    public Throwable getException() {
        return thrownException;
    }
    
}
