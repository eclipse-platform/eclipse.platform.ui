/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.harness;

import java.io.File;

import junit.framework.TestResult;

/**
 * This class is the common superclass for all performance tests for
 * the Core project.  It provides extensions to the JUnit infrastructure
 * to incorporate timers into test cases.
 *
 * There are two distinct sets of timing mechanisms available, depending
 * on the type of operation being testing.  For long running operations
 * with noticeable delays, use startTimer() and stopTimer().  This is
 * a more heavy-weight mechanism that allows logging, multiple
 * timers, and overlapping timers.
 *
 * For benchmarking operations that should be instantaneous, use the 
 * startBench(), stopBench() methods.  Here is an example:
 *
 *	<code>
 * 	String benchName = "myOperation";
 * 	int repeat = 500;
 *	startBench();
 *	for (int i = 0; i < repeat; i++) {
 *		//run the operation to be tested here
 *	}
 *	stopBench(benchName, repeat);
 *	</code>
 * Note that only one operation can be tested at a time with this mechanism.
 *
 * If an instance of this class is run using a LoggingPerformanceTestResult,
 * an HTML log file will be maintained of all timing and garbage collecting,
 * in addition to any messages added using the log() method.  In the absence
 * of a logging test result, all log events are written to the standard output.
 */
public abstract class CorePerformanceTest extends EclipseWorkspaceTest {
	public static String fLogFile;

	protected LoggingPerformanceTestResult fLogger = null;
	protected PerformanceTestResult fResult = null;
	protected File fTempDir;
	protected File fDir;

	protected long benchStart;
public CorePerformanceTest() {
}
public CorePerformanceTest(String name) {
	super(name);
}
	protected PerformanceTestResult defaultTest() {
		return new PerformanceTestResult();
	}
	/**
	 * Logs or writes string to console.
	 */
	public void perfLog(String s) {
		if (fLogger != null) {
			fLogger.log(s);
		} else {
			System.out.println(s);
		}
	}
	/**
	 * A convenience method to run this test, collecting the results with a
	 * default PerformanceTestResult object.
	 *
	 * @see PerformanceTestResult
	 */
	public TestResult run() {
		PerformanceTestResult test = defaultTest();
		run(test);
		return test;
	}
	/**
	 * Runs the test case and collects the results in a PerformanceTestResult.
	 * This is the template method that defines the control flow
	 * for running a test case.
	 */
	public void run(PerformanceTestResult test) {
		fResult = test;

		if (test instanceof LoggingPerformanceTestResult) {
			fLogger = (LoggingPerformanceTestResult)test;
		}

		super.run(test);
	}
protected void startBench() {
	for (int i = 0; i < 20; ++i) {
		System.gc();
	}
	benchStart = System.currentTimeMillis();
}
/**
 * Tell the result to start a timer with the given name.
 * If no timer exists with that name, result creates a new timer
 * and starts it running.
 */ 
protected void startTimer(String timerName) {
	fResult.startTimer(timerName);
}
protected void stopBench(String benchName, int numOperations) {
	long duration = System.currentTimeMillis() - benchStart;
	double perOp = (double) duration / (double) numOperations;

	String opString;
	if (perOp > 100.0) {
		opString = "(" + perOp + "ms per operation)";
	} else {
		//Note us == microseconds
		opString = "(" + (perOp * 1000.0) + "us per operation)";
	}
		
	System.out.println(benchName + " took " + duration + "ms " + opString);
}
/**
 * Tell the result to stop the timer with the given name.
 */ 
protected void stopTimer(String timerName) {
	fResult.stopTimer(timerName);
}
}
