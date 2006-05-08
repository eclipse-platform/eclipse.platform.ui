/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * <code>CallHistory</code> is used to record the invocation
 * of methods within a target object.  This is useful during
 * lifecycle testing for an object.
 * <p>
 * To use <code>CallHistory</code> ..
 * <ol>
 * <li>Create a CallHistory in the target or pass one in.</li>
 * <li>Invoke some test scenario.  </li>
 * <li>If a method is called on the target record the invocation
 * 	in the call history</li>
 * <li>Verify the call history after the test scenario is
 * 	complete.</li>
 * </ol>
 * </p><p>
 * Each <code>CallHistory</code> has a target which is used to 
 * verify the method names passed to the history.  If an invalid 
 * name is passed an <code>IllegalArgumentException</code> will 
 * be thrown.
 * </p>
 */
public class CallHistory {
    private ArrayList methodList;

    private Class classType;

    /**
     * Creates a new call history for an object.  
     * 
     * @param target the call history target. 
     */
    public CallHistory(Object target) {
        methodList = new ArrayList();
        classType = target.getClass();
    }

    /**
     * Throws an exception if the method name is invalid
     * for the given target class.
     */
    private void testMethodName(String methodName) {
        Method[] methods = classType.getMethods();
        for (int i = 0; i < methods.length; i++)
            if (methods[i].getName().equals(methodName))
                return;
        throw new IllegalArgumentException("Target class ("
                + classType.getName() + ") does not contain method: "
                + methodName);
    }

    /**
     * Adds a method name to the call history.  
     * 
     * @param methodName the name of a method
     */
    public void add(String methodName) {
        testMethodName(methodName);
        methodList.add(methodName);
    }

    /**
     * Clears the call history.
     */
    public void clear() {
        methodList.clear();
    }

    /**
     * Returns whether a list of methods have been called in
     * order.  
     * 
     * @param testNames an array of the method names in the order they are expected
     * @return <code>true</code> if the methods were called in order
     */
    public boolean verifyOrder(String[] testNames)
            throws IllegalArgumentException {
        int testIndex = 0;
        int testLength = testNames.length;
        if (testLength == 0)
            return true;
        for (int nX = 0; nX < methodList.size(); nX++) {
            String methodName = (String) methodList.get(nX);
            String testName = testNames[testIndex];
            testMethodName(testName);
            if (testName.equals(methodName))
                ++testIndex;
            if (testIndex >= testLength)
                return true;
        }
        return false;
    }

    /**
     * Returns whether a method has been called.
     * 
     * @param methodName a method name
     * @return <code>true</code> if the method was called
     */
    public boolean contains(String methodName) {
        testMethodName(methodName);
        return methodList.contains(methodName);
    }

    /**
     * Returns whether a list of methods were called.
     * 
     * @param methodNames a list of methods
     * @return <code>true</code> if the methods were called
     */
    public boolean contains(String[] methodNames) {
        for (int i = 0; i < methodNames.length; i++) {
            testMethodName(methodNames[i]);
            if (!methodList.contains(methodNames[i]))
                return false;
        }
        return true;
    }

    /**
     * Returns whether the list of methods called is empty.
     * 
     * @return <code>true</code> iff the list of methods is empty
     */
    public boolean isEmpty() {
        return methodList.isEmpty();
    }

    /**
     * Prints the call history to the console.
     */
    public void printToConsole() {
        for (int i = 0; i < methodList.size(); i++)
            System.out.println(methodList.get(i));
    }
}
