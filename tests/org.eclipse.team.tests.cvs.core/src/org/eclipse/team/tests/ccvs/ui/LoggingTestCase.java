/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;


import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;

public class LoggingTestCase extends CVSTestSetup {
	private LoggingTestResult logResult;
	private int disableLogStack;
	
	/**
	 * Creates a new logging test case.
	 */
	public LoggingTestCase(Test test) {
		super(test);
	}
	
	/**
	 * Runs a test.
	 * @param result the result object
	 */
	public void run(TestResult result) {
		// run the garbage collector now to improve benchmark precision
		for (int i = 0; i < 4; ++i) {
			System.runFinalization();
			System.gc();
		}
		if (result instanceof LoggingTestResult) {
			logResult = (LoggingTestResult) result;
			disableLogStack = 0;
		} else {
			logResult = null;
			disableLogStack = 1;
		}
		super.run(result);
	}

	/**
	 * Marks the beginning of a new task group.
	 * @param groupName the name for the group
	 */
	protected void startGroup(String groupName) {
		if (disableLogStack == 0) logResult.startGroup(groupName);		
	}
	
	/**
	 * Marks the ends of the active task group.
	 */
	protected void endGroup() {
		if (disableLogStack == 0) logResult.endGroup();		
	}

	/**
	 * Marks the beginning of a new task.
	 * @param taskName the name for the task
	 */
	protected void startTask(String taskName) {
		if (disableLogStack == 0) logResult.startTask(taskName);		
	}
	
	/**
	 * Marks the ends of the active task.
	 */
	protected void endTask() {
		if (disableLogStack == 0) logResult.endTask();		
	}

	/**
	 * Disables logging until re-enabled.  (this call nests)
	 */
	protected void disableLog() {
		disableLogStack += 1;
	}

	/**
	 * Enables logging when all previous calls to disableLog are matched.
	 */
	protected void enableLog() {
		Assert.assertTrue(disableLogStack > 0);
		disableLogStack -= 1;
		Assert.assertTrue(disableLogStack != 0 || logResult != null);
	}
	
	/**
	 * Prints a warning message to the log.
	 * @param message the message, or null
	 * @param error an exception with a stack trace, or null
	 * @param status a status code, or null
	 */
	protected void printWarning(String message, Throwable error, IStatus status) {
		if (disableLogStack == 0) logResult.printWarning(message, error, status);
	}
}
