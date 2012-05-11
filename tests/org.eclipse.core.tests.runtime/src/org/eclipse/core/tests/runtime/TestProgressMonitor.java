/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 */
public class TestProgressMonitor implements IProgressMonitor {

	private double totalWork;

	/**
	 * Records the number of times worked or internalWorked was called with
	 * an argument of 0. 
	 */
	private int redundantWorkCalls;

	/**
	 * Records the number of times setTaskName was called without changing the
	 * existing task name.
	 */
	private int redundantSetTaskCalls;

	/**
	 * Records the number of times subTask was called without changing the
	 * existing task name
	 */
	private int redundantSubTaskCalls;

	/**
	 * Stores the number of calls to the integer worked(...) method
	 */
	private int intWorkedCalls;

	/**
	 * Stores the number of calls to the double internalWorked(...) method
	 */
	private int doubleWorkedCalls;

	/**
	 * Stores the total number of calls to worked and internalWorked
	 */
	private int workCalls;

	/**
	 * Stores the total number of calls to setTaskName
	 */
	private int taskNameCalls;

	/**
	 * Stores the total number of calls to subTask
	 */
	private int subTaskCalls;

	/**
	 * Stores the total number of calls to isCanceled
	 */
	private int isCanceledCalls;

	private int beginTaskCalls;

	private int doneCalls;

	private String taskName = null;

	private String subTaskName = null;

	private int expectedWork;

	private String beginTaskName = "";

	private boolean cancelled = false;

	public String getBeginTaskName() {
		return beginTaskName;
	}

	private static boolean equals(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o2 == null)
			return false;
		return o1.equals(o2);
	}

	/**
	 * Returns the number of times beginTask() was called. For a correctly written job, 
	 * this should equal 1 on completion.
	 * 
	 * @return the number of calls to beginTask
	 */
	public int getBeginTaskCalls() {
		return beginTaskCalls;
	}

	/**
	 * Returns the number of times done() was called. For a correctly written job, 
	 * this should equal 1 on completion. 
	 * 
	 * @return the number of calls to done
	 */
	public int getDoneCalls() {
		return doneCalls;
	}

	/**
	 * Returns the number of times worked was called as a no-op.
	 * That is, it counts the number of times worked() or internalWorked() had 
	 * ever been called with a value of 0. This should return 0 for an 
	 * optimally-written job.
	 * 
	 * @return true iff redundant calls were ever made to *worked() on this
	 * monitor.
	 */
	public int getRedundantWorkCalls() {
		return redundantWorkCalls;
	}

	/**
	 * Returns the number of calls to isCancelled(). Optimally-written
	 * jobs may call this an unbounded number of times.
	 * 
	 * @return the number of calls to isCancelled().
	 */
	public int getIsCanceledCalls() {
		return isCanceledCalls;
	}

	/**
	 * Returns the number of calls to subTask().
	 */
	public int getSubTaskCalls() {
		return subTaskCalls;
	}

	/**
	 * Returs the number of calls to setTaskName().
	 */
	public int getTaskNameCalls() {
		return taskNameCalls;
	}

	/**
	 * Returns the number of calls to work() and internalWorked(). For the top-level
	 * progress monitor in an optimally-written job, this should be at least 100 and
	 * no more than 1000. A job that reports work less often than this will seem to
	 * have jumpy progress, and a job that reports work more often than this is reporting
	 * progress that won't be visible to the user and is wasting time in progress monitoring
	 * code.
	 *  
	 * @return the number of calls to worked(int) or internalWorked(double)
	 */
	public int getWorkCalls() {
		return workCalls;
	}

	/**
	 * Returns the number of calls to internalWorked. For an optimally-written job,
	 * this should be 0, since integer work is faster and has no chance
	 * of floating-point rounding errors. 
	 *  
	 * @return the number of calls to internalWorked
	 */
	public int getDoubleWorkedCalls() {
		return doubleWorkedCalls;
	}

	/**
	 * Returns the number of calls to worked(int). For an optimally-written job,
	 * this should equal getWorkCalls, since integer work is faster and has no
	 * chance of floating-point rounding errors.
	 * 
	 * @return the number of calls to worked(int)
	 */
	public int getIntWorkedCalls() {
		return intWorkedCalls;
	}

	public int getRedundantSetTaskCalls() {
		return redundantSetTaskCalls;
	}

	public int getRedundantSubTaskCalls() {
		return redundantSubTaskCalls;
	}

	/**
	 * Returns the total work reported on this monitor. For an optimally-written job,
	 * this should be +/- a small epsilon to account for floating point error.
	 * 
	 * @return the total work reported on this job
	 */
	public double getTotalWork() {
		return totalWork;
	}

	public void beginTask(String name, int workToDo) {
		beginTaskCalls++;
		this.expectedWork = workToDo;
		this.beginTaskName = name;
	}

	public void done() {
		doneCalls++;
	}

	public void internalWorked(double work) {
		workCalls++;
		doubleWorkedCalls++;
		if (work == 0.0)
			redundantWorkCalls++;
		totalWork += work;
	}

	public boolean isCanceled() {
		isCanceledCalls++;
		return cancelled;
	}

	public void setCanceled(boolean value) {
		this.cancelled = value;
	}

	public void setTaskName(String name) {
		taskNameCalls++;
		if (equals(name, taskName))
			redundantSetTaskCalls++;
		taskName = name;
	}

	public void subTask(String name) {
		subTaskCalls++;
		if (equals(name, subTaskName))
			redundantSubTaskCalls++;
		subTaskName = name;
	}

	public void worked(int work) {
		workCalls++;
		intWorkedCalls++;
		if (work == 0)
			redundantWorkCalls++;
		totalWork += work;
	}

	public int getExpectedWork() {
		return expectedWork;
	}

	/**
	 * <p>Asserts that the progress reported on this monitor was optimal. That is,
	 * there were no redundant method calls, and progress was reported in between
	 * 100 and 1000 increments.</p>
	 */
	public void assertOptimal() {
		Assert.assertEquals("The progress monitor did not reach 100%", expectedWork, getTotalWork(), 0.01d);
		Assert.assertTrue("This monitor reported progress with less than 1% accuracy", getWorkCalls() >= 100);
		Assert.assertTrue("This monitor reported progress with more than 0.1% accuracy (the job spent too much time reporting redundant progress)", getWorkCalls() <= 1000);
		Assert.assertEquals("Null work was reported on this monitor", 0, getRedundantWorkCalls());

		if (expectedWork >= 1000) {
			// Only check for internalWorked usage if there were enough ticks allocated on this progress
			// monitor that worked(int) could have been used
			Assert.assertEquals("internalWorked was being used instead of worked()", 0, getDoubleWorkedCalls());
		}

		Assert.assertEquals("Redundant calls were made to setTaskName", 0, getRedundantSetTaskCalls());
		Assert.assertEquals("Redundant calls were made to subTask", 0, getRedundantSubTaskCalls());
		Assert.assertEquals("The number of calls to done should match the number of calls to beginTask", getBeginTaskCalls(), getDoneCalls());
		Assert.assertEquals("beginTask should be called exactly once", getBeginTaskCalls(), 1);
	}

	public String getSubTaskName() {
		return subTaskName == null ? "" : subTaskName;
	}

	public String getTaskName() {
		return taskName == null ? "" : taskName;
	}

}
