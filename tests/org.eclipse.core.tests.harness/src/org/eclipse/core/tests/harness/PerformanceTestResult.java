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

import java.io.PrintStream;
import java.util.*;

import junit.framework.*;

/**
 * Test result for a performance test.  Keeps track of all timers that
 * have been created within the test. 
 */

public class PerformanceTestResult extends TestResult {
	Hashtable fTimers = new Hashtable();
	Vector fTimerList = new Vector();
/**
 * PerformanceTestResult constructor comment.
 */
public PerformanceTestResult() {
	super();
}
/**
 * Informs the result that a test was completed.
 */
public synchronized void endTest(Test test) {
	print();
}
/**
 * Prints the test result
 */


public synchronized void print() {
	stopTimers();
	printHeader(System.out);
	printErrors(System.out);
	printTimings(System.out);
}
/**
 * Prints the errors to the standard output
 */
protected void printErrors(PrintStream out) {
	int count = errorCount();
	if (count != 0) {
		if (count == 1)
			out.println("There was " + count + " error:");
		else
			out.println("There were " + count + " errors:");
		int i = 1;
		for (Enumeration e = errors(); e.hasMoreElements(); i++) {
			TestFailure failure = (TestFailure) e.nextElement();
			out.println(i + ") " + failure.failedTest());
			failure.thrownException().printStackTrace();
		}
	}
}
/**
 * Prints the header of the report
 */
protected void printHeader(PrintStream out) {
	if (wasSuccessful()) {
		out.println();
		out.print("OK");
		out.println(" (" + runCount() + " tests)");
	} else {
		out.println();
		out.println("!!!FAILURES!!!");
		out.println("Test Results:");
		out.println("Run: " + runCount() + " Failures: " + failureCount() + " Errors: " + errorCount());
	}
}
	/**
	 * Prints the timings of the result.
	 */

	protected void printTimings(PrintStream out) {
		
		// print out all timing results to the console
		Enumeration enum = fTimerList.elements();
		while (enum.hasMoreElements()) {
			PerformanceTimer timer = (PerformanceTimer)enum.nextElement();
			out.println("Timing " + timer.getName() + " : " + timer.getElapsedTime() + " ms ");
		}
	}
/**
 * Start the test
 */

public synchronized void startTest(Test test) {
	super.startTest(test);
	System.out.print(".");
}
/**
 * Start the timer with the given name.  If the timer has already
 * been created, send it a startTiming message.  If not, create it
 * and send the new timer the startTiming message.
 */

 public synchronized void startTimer(String timerName) {
	PerformanceTimer timer = (PerformanceTimer) fTimers.get(timerName);
	if( timer == null) {
		timer = new PerformanceTimer(timerName);
		fTimers.put(timerName, timer);
		fTimerList.addElement(timer);
	}
	timer.startTiming();
 } 
/**
 * Look up the timer with the given name and send it a stopTiming
 * message.  If the timer does not exist, report an error.
 */

 public synchronized void stopTimer(String timerName) {
	PerformanceTimer timer = (PerformanceTimer) fTimers.get(timerName);
	if (timer == null) {
		throw new Error(timerName + " is not a valid timer name ");
	}
	timer.stopTiming();
 } 
	/**
	 * Stops all timers
	 */

	protected void stopTimers() {

		Enumeration enum = fTimerList.elements();
		PerformanceTimer timer = null;

		while (enum.hasMoreElements()) {
			((PerformanceTimer) enum.nextElement()).stopTiming();
		}	
	}
}
