package org.eclipse.core.tests.harness;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.AssertionFailedError;

/**
 * This class can be used for testing progress monitoring.
 * If you want to reuse one instance of this class for several
 * invocations, make sure that you call prepare() before every
 * invocation.
 * Call sanityCheck() after the operation whose progress monitoring
 * you are testing.
 */

public class FussyProgressMonitor extends TestProgressMonitor {
	private static final double EPS_FACTOR = 0.01;
	private String taskName;
	private int totalWork;
	private double workedSoFar = 0;
	private boolean beginTaskCalled = false;
	private long beginTime;
	private static final long NOTICEABLE_DELAY = 1000; // milliseconds
	private int doneCalls = 0;
	private boolean sanityCheckCalled = true;
	private boolean hasFailed = false;
	public class FussyProgressAssertionFailed extends AssertionFailedError {
		FussyProgressAssertionFailed(String name) {
			super(name);
		}
	}
public FussyProgressMonitor() {
	prepare();
}
/**
 * 
 * @param reason java.lang.String
 * @param condition boolean
 */
private void assert(String reason, boolean condition) {
	// silently ignore follow-up failures
	if (hasFailed)
		return;
	if (!condition) {
		hasFailed = true;
		throw new FussyProgressAssertionFailed(reason);
	}
	//Assert.assert(reason, condition);
}
/**
 * @see IProgressMonitor#beginTask
 */
public void beginTask(String name, int totalWork) {
	//if (beginTaskCalled && doneCalls > 0) {
		// this is a second call to beginTask which is allowed because
		// the previous task is done.
		//prepare();
	//}
	assert("beginTask may only be called once (old name=" + taskName + ")", beginTaskCalled == false);
	beginTaskCalled = true;
	taskName = name;
	assert("total work must be positive or UNKNOWN", totalWork > 0 || totalWork == UNKNOWN);
	this.totalWork = totalWork;
}
/**
 * @see IProgressMonitor#done
 */
public void done() {
	assert("done must be called after beginTask", beginTaskCalled);
	assert("done can only be called once", doneCalls==0);
	//assert("done is called before all work is done", totalWork==UNKNOWN || totalWork==workedSoFar);
	workedSoFar = totalWork;
	doneCalls++;
}
public void internalWorked(double work) {
	assert("can accept calls to worked/internalWorked only after beginTask", beginTaskCalled);
	assert("can accept calls to worked/internalWorked only before done is called", doneCalls == 0);
	assert("amount worked should be positive, not " + work, work >= 0);
	if(work==0) EclipseWorkspaceTest.log("INFO: amount worked should be positive, not " + work);
	workedSoFar += work;
	assert("worked " + (workedSoFar - totalWork) + " more than totalWork", totalWork == UNKNOWN || workedSoFar <= totalWork + (totalWork * EPS_FACTOR));
}
/**
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return false;
}
/**
 * should be called before every use of a FussyProgressMonitor
 */
public void prepare() {
	//if (!sanityCheckCalled)
	//EclipseWorkspaceTest.log("sanityCheck has not been called for previous use");
	sanityCheckCalled = false;
	taskName = null;
	totalWork = 0;
	workedSoFar = 0;
	beginTaskCalled = false;
	doneCalls = 0;
	hasFailed = false;
	beginTime = System.currentTimeMillis();
}
/**
 *  should be called after every use of a FussyProgressMonitor
 */
public void sanityCheck() {
	if (sanityCheckCalled)
		EclipseWorkspaceTest.log("sanityCheck has already been called");
	sanityCheckCalled = true;
//	EclipseWorkspaceTest.log("sanity checking: " + taskName + " : " + (System.currentTimeMillis() - beginTime) + " ms, " + workedSoFar);
	if (System.currentTimeMillis() - beginTime > NOTICEABLE_DELAY) {
		assert("this operation took a long time, it should report progress", workedSoFar > 0);
	}
	assert("done has not been called on ProgressMonitor", hasFailed || !beginTaskCalled || doneCalls > 0);
}
/**
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean b) {
	assert("1FV0BJL: Who might be calling this method?", false);
}
/**
 * @see IProgressMonitor#setTaskName
 */
public void setTaskName(String name) {
	taskName = name;
}
/**
 * @see IProgressMonitor#subTask
 */
public void subTask(String name) {
	// do nothing
}
/**
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
	internalWorked(work);
}
}
