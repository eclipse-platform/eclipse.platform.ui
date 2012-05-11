/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Xenos - initial API and implementation
 *     Stefan Xenos - bug 174539 - add a 1-argument convert(...) method     
 *     Stefan Xenos - bug 174040 - SubMonitor#convert doesn't always set task name
 *     Stefan Xenos - bug 206942 - Regression test for infinite progress reporting rate
 *     IBM Corporation - bug 252446 - SubMonitor.newChild passes zero ticks to child
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.util.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * 
 */
public class SubMonitorTest extends TestCase {

	private long startTime;
	/**
	 * <p>Number of calls to worked() within each test. This was chosen to be significantly larger 
	 * than 1000 to test how well the monitor can optimize unnecessary resolution
	 * in reported progress, but small enough that the test completes in a reasonable
	 * amount of time.</p>
	 * 
	 * <p>Note: changing this constant will invalidate comparisons with old performance data.</p>
	 */
	public static final int PROGRESS_SIZE = SubProgressTest.PROGRESS_SIZE;
	/**
	 * <p>Depth of the chain chain of progress monitors. In all of the tests, we create
	 * a nested chain of progress monitors rather than a single monitor, to test its
	 * scalability under recursion. We pick a number representing a moderately deep
	 * recursion, but is still small enough that it could correspond to a real call stack
	 * without causing overflow.</p>
	 * 
	 * <p>Note: changing this constant will invalidate comparisons with old performance data.</p>
	 */
	public static final int CHAIN_DEPTH = SubProgressTest.CHAIN_DEPTH;

	public SubMonitorTest() {
		super();
	}

	public SubMonitorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		startTime = System.currentTimeMillis();
		super.setUp();
	}

	protected void tearDown() throws Exception {
		long endTime = System.currentTimeMillis();
		reportPerformance(getClass().getName(), getName(), startTime, endTime);
		super.tearDown();
	}

	/** 
	 * Reports progress by iterating over a loop of the given size, reporting 1 progress 
	 * at each iteration. Simulates the progress of worked(int) in loops.
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize size of the loop
	 */
	private static void reportWorkInLoop(IProgressMonitor monitor, int loopSize) {
		monitor.beginTask("", loopSize);
		for (int i = 0; i < loopSize; i++) {
			monitor.worked(1);
		}
	}

	/** 
	 * Reports progress by iterating over a loop of the given size, reporting 1 progress 
	 * at each iteration. Simulates the progress of internalWorked(double) in loops.
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize size of the loop
	 */
	private static void reportFloatingPointWorkInLoop(IProgressMonitor monitor, int loopSize) {
		monitor.beginTask("", loopSize);
		for (int i = 0; i < loopSize; i++) {
			monitor.internalWorked(1.0d);
		}
	}

	/**
	 * Runs an "infinite progress" loop. Each iteration will consume 1/ratio
	 * of the remaining work, and will run for the given number of iterations.
	 * Retuns the number of ticks reported (out of 1000).
	 * 
	 * @param ratio
	 * @return the number of ticks reported
	 */
	private double runInfiniteProgress(int ratio, int iterations) {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitor mon = SubMonitor.convert(monitor);

		for (int i = 0; i < iterations; i++) {
			mon.setWorkRemaining(ratio);
			mon.worked(1);
		}

		return monitor.getTotalWork();
	}

	public void testInfiniteProgress() {
		// In theory when reporting "infinite" progress, the actual progress reported after
		// n iterations should be f(n) = T(1-(1-R)^n)
		//
		// where T is the total ticks allocated on the root (T=1000) and R is the ratio
		// (R=1/the_argument_to_setWorkRemaining).

		// Reporting 1% per iteration, we should be at 993.4 ticks after 500 iterations
		double test1 = runInfiniteProgress(100, 500);
		assertEquals(993.4, test1, 1.0);

		// Reporting 0.1% per iteration, we should be at 950.2 ticks after 3000 iterations
		double test2 = runInfiniteProgress(1000, 3000);
		assertEquals(950.2, test2, 1.0);

		// Reporting 0.01% per iteration, we should be at 393.5 ticks after 5000 iterations
		double test3 = runInfiniteProgress(10000, 5000);
		assertEquals(393.5, test3, 1.0);

		// Reporting 0.01% per iteration, we should be at 864.7 ticks after 20000 iterations
		double test4 = runInfiniteProgress(10000, 20000);
		assertEquals(864.7, test4, 1.0);

		// Reporting 0.01% per iteration, we should be at 9.9 ticks after 100 iterations
		double test5 = runInfiniteProgress(10000, 100);
		assertEquals(9.9, test5, 1.0);
	}

	/**
	 * Ensures that we don't lose any progress when calling setWorkRemaining  
	 */
	public void testSetWorkRemaining() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitor mon = SubMonitor.convert(monitor, 0);

		for (int i = 1000; i >= 0; i--) {
			mon.setWorkRemaining(i);
			mon.internalWorked(0.5);

			mon.setWorkRemaining(i);
			mon.internalWorked(0.5);

			mon.internalWorked(-0.5); // should not affect progress 
		}

		monitor.done();
		monitor.assertOptimal();
	}

	/**
	 * Tests that SubMonitor.done() will clean up after an unconsumed child
	 * that was created with the explicit constructor
	 */
	public void testCleanupConstructedChildren() {
		TestProgressMonitor top = new TestProgressMonitor();

		SubMonitor monitor = SubMonitor.convert(top, 1000);
		monitor.beginTask("", 1000);

		monitor.newChild(500);
		SubMonitor child2 = monitor.newChild(100);

		child2.done();

		Assert.assertEquals("Ensure that done() reports unconsumed progress, even if beginTask wasn't called", 600.0, top.getTotalWork(), 0.01d);

		SubMonitor child3 = monitor.newChild(100);

		SubMonitor child3andAHalf = monitor.newChild(-10); // should not affect progress
		child3andAHalf.done();

		monitor.done();

		Assert.assertEquals("Ensure that done() cleans up after unconsumed children that were created by their constructor", 1000.0, top.getTotalWork(), 0.01d);

		child3.worked(100);

		Assert.assertEquals("Ensure that children can't report any progress if their parent has completed", 1000.0, top.getTotalWork(), 0.01d);
	}

	/**
	 * Tests SubMonitor under typical usage. This is the same
	 * as the performance test as the same name, but it verifies correctness
	 * rather than performance.
	 */
	public void testTypicalUsage() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitorTest.runTestTypicalUsage(monitor);
		monitor.assertOptimal();
	}

	/**
	 * Tests creating a tree of SubMonitors. This is the same
	 * as the performance test as the same name, but it verifies correctness
	 * rather than performance. 
	 */
	public void testCreateTree() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitorTest.runTestCreateTree(monitor);
		monitor.assertOptimal();
	}

	/**
	 * Tests claimed problem reported in bug 2100394.
	 */
	public void testBug210394() {
		TestProgressMonitor testMonitor = new TestProgressMonitor();
		SubMonitor monitor = SubMonitor.convert(testMonitor);
		monitor.beginTask("", 2);

		SubMonitor step1 = monitor.newChild(1);
		step1.done();

		assertEquals(500.0, testMonitor.getTotalWork(), 1.0);

		SubMonitor step2 = monitor.newChild(2);
		// Here we find out that we had really 5 additional steps to accomplish
		SubMonitor subStep2 = SubMonitor.convert(step2, 5);
		subStep2.worked(1);
		assertEquals(600.0, testMonitor.getTotalWork(), 1.0);
		subStep2.worked(1);
		assertEquals(700.0, testMonitor.getTotalWork(), 1.0);
		subStep2.worked(1);
		assertEquals(800.0, testMonitor.getTotalWork(), 1.0);
		subStep2.worked(1);
		assertEquals(900.0, testMonitor.getTotalWork(), 1.0);
		subStep2.worked(1);
		assertEquals(1000.0, testMonitor.getTotalWork(), 1.0);
	}

	/**
	 * Ensures that SubMonitor won't report more than 100% progress
	 * when a child is created with more than the amount of available progress.
	 */
	public void testChildOverflow() {
		TestProgressMonitor top = new TestProgressMonitor();

		SubMonitor mon1 = SubMonitor.convert(top, 1000);
		Assert.assertEquals(0.0, top.getTotalWork(), 0.1d);

		SubMonitor child2 = mon1.newChild(700);
		child2.done();

		Assert.assertEquals(700.0, top.getTotalWork(), 0.1d);

		SubMonitor child3 = mon1.newChild(700);
		child3.done();

		Assert.assertEquals("The reported work should not exceed 1000", 1000.0, top.getTotalWork(), 0.1d);

		mon1.done();

		top.done();
	}

	/**
	 * Tests the 1-argument convert(...) method
	 */
	public void testConvert() {
		TestProgressMonitor top = new TestProgressMonitor();
		SubMonitor mon1 = SubMonitor.convert(top);
		Assert.assertEquals(0.0, top.getTotalWork(), 0.1d);
		mon1.worked(10);
		Assert.assertEquals(0.0, top.getTotalWork(), 0.1d);
		mon1.setWorkRemaining(100);
		mon1.worked(50);
		Assert.assertEquals(500.0, top.getTotalWork(), 0.1d);
		mon1.done();
		Assert.assertEquals(1000.0, top.getTotalWork(), 0.1d);
		top.done();
	}

	/**
	 * Tests the function of the SUPPRESS_* flags
	 */
	public void testFlags() {
		TestProgressMonitor top = new TestProgressMonitor();

		SubMonitor mon1 = SubMonitor.convert(top, "initial", 100);

		// Ensure that we've called begintask on the root with the correct argument
		Assert.assertEquals(top.getBeginTaskCalls(), 1);
		Assert.assertEquals(top.getBeginTaskName(), "initial");

		mon1.beginTask("beginTask", 1000);

		// Ensure that beginTask on the child does NOT result in more than 1 call to beginTask on the root
		Assert.assertEquals(top.getBeginTaskCalls(), 1);

		// Ensure that the task name was propogated correctly
		Assert.assertEquals(top.getTaskName(), "beginTask");

		mon1.setTaskName("setTaskName");
		Assert.assertEquals(top.getTaskName(), "setTaskName");

		mon1.subTask("subTask");
		Assert.assertEquals(top.getSubTaskName(), "subTask");

		// Create a child monitor that permits calls to beginTask
		{
			SubMonitor mon2 = mon1.newChild(10, SubMonitor.SUPPRESS_NONE);

			// Ensure that everything is propogated
			mon2.beginTask("mon2.beginTask", 100);
			Assert.assertEquals(top.getTaskName(), "mon2.beginTask");

			mon2.setTaskName("mon2.setTaskName");
			Assert.assertEquals(top.getTaskName(), "mon2.setTaskName");

			mon2.subTask("mon2.subTask");
			Assert.assertEquals(top.getSubTaskName(), "mon2.subTask");
		}
	}

	private String[] runChildTest(int depth, TestProgressMonitor root, IProgressMonitor child, int ticks) {
		ArrayList results = new ArrayList();
		child.beginTask("beginTask" + depth, ticks);
		results.add(root.getTaskName());
		child.subTask("subTask" + depth);
		results.add(root.getSubTaskName());
		child.setTaskName("setTaskName" + depth);
		results.add(root.getTaskName());
		return (String[]) results.toArray(new String[results.size()]);
	}

	/**
	 * Tests the style bits in SubProgressMonitor 
	 */
	public void testStyles() {

		int[] styles = new int[] {SubMonitor.SUPPRESS_NONE, SubMonitor.SUPPRESS_BEGINTASK, SubMonitor.SUPPRESS_SETTASKNAME, SubMonitor.SUPPRESS_SUBTASK, SubMonitor.SUPPRESS_BEGINTASK | SubMonitor.SUPPRESS_SETTASKNAME, SubMonitor.SUPPRESS_BEGINTASK | SubMonitor.SUPPRESS_SUBTASK, SubMonitor.SUPPRESS_SETTASKNAME | SubMonitor.SUPPRESS_SUBTASK, SubMonitor.SUPPRESS_ALL_LABELS};

		HashMap expected = new HashMap();
		expected.put("style 5 below style 7", new String[] {"", "", ""});
		expected.put("style 7 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 7 below style 4", new String[] {"beginTask0", "subTask0", "beginTask0"});
		expected.put("style 5 below style 6", new String[] {"", "subTask0", ""});
		expected.put("style 3 below style 7", new String[] {"", "", ""});
		expected.put("style 5 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 7 below style 3", new String[] {"setTaskName0", "", "setTaskName0"});
		expected.put("style 7 below style 2", new String[] {"setTaskName0", "subTask0", "setTaskName0"});
		expected.put("style 5 below style 4", new String[] {"beginTask0", "subTask0", "beginTask0"});
		expected.put("style 3 below style 6", new String[] {"", "subTask0", ""});
		expected.put("style 1 below style 7", new String[] {"", "", ""});
		expected.put("style 3 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 5 below style 3", new String[] {"beginTask1", "", "beginTask1"});
		expected.put("style 7 below style 1", new String[] {"setTaskName0", "", "setTaskName0"});
		expected.put("style 3 below style 4", new String[] {"beginTask0", "subTask0", "beginTask0"});
		expected.put("style 5 below style 2", new String[] {"beginTask1", "subTask0", "beginTask1"});
		expected.put("style 7 below style 0", new String[] {"setTaskName0", "subTask0", "setTaskName0"});
		expected.put("style 1 below style 6", new String[] {"", "subTask0", ""});
		expected.put("style 1 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 3 below style 3", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 5 below style 1", new String[] {"beginTask1", "", "beginTask1"});
		expected.put("style 1 below style 4", new String[] {"beginTask0", "subTask0", "beginTask0"});
		expected.put("style 3 below style 2", new String[] {"setTaskName0", "subTask0", "setTaskName1"});
		expected.put("style 5 below style 0", new String[] {"beginTask1", "subTask0", "beginTask1"});
		expected.put("style 1 below style 3", new String[] {"beginTask1", "", "setTaskName1"});
		expected.put("style 3 below style 1", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 1 below style 2", new String[] {"beginTask1", "subTask0", "setTaskName1"});
		expected.put("style 3 below style 0", new String[] {"setTaskName0", "subTask0", "setTaskName1"});
		expected.put("style 1 below style 1", new String[] {"beginTask1", "", "setTaskName1"});
		expected.put("style 1 below style 0", new String[] {"beginTask1", "subTask0", "setTaskName1"});
		expected.put("style 3 as top-level monitor", new String[] {"", "", "setTaskName0"});
		expected.put("style 7 as top-level monitor", new String[] {"", "", ""});
		expected.put("style 2 as top-level monitor", new String[] {"", "subTask0", "setTaskName0"});
		expected.put("style 6 as top-level monitor", new String[] {"", "subTask0", ""});
		expected.put("style 6 below style 7", new String[] {"", "", ""});
		expected.put("style 6 below style 6", new String[] {"", "subTask1", ""});
		expected.put("style 4 below style 7", new String[] {"", "", ""});
		expected.put("style 6 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 6 below style 4", new String[] {"beginTask0", "subTask1", "beginTask0"});
		expected.put("style 4 below style 6", new String[] {"", "subTask1", ""});
		expected.put("style 2 below style 7", new String[] {"", "", ""});
		expected.put("style 4 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 6 below style 3", new String[] {"setTaskName0", "", "setTaskName0"});
		expected.put("style 4 below style 4", new String[] {"beginTask0", "subTask1", "beginTask0"});
		expected.put("style 6 below style 2", new String[] {"setTaskName0", "subTask1", "setTaskName0"});
		expected.put("style 2 below style 6", new String[] {"", "subTask1", ""});
		expected.put("style 0 below style 7", new String[] {"", "", ""});
		expected.put("style 2 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 6 below style 1", new String[] {"setTaskName0", "", "setTaskName0"});
		expected.put("style 4 below style 3", new String[] {"beginTask1", "", "beginTask1"});
		expected.put("style 2 below style 4", new String[] {"beginTask0", "subTask1", "beginTask0"});
		expected.put("style 6 below style 0", new String[] {"setTaskName0", "subTask1", "setTaskName0"});
		expected.put("style 4 below style 2", new String[] {"beginTask1", "subTask1", "beginTask1"});
		expected.put("style 0 below style 6", new String[] {"", "subTask1", ""});
		expected.put("style 0 below style 5", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 4 below style 1", new String[] {"beginTask1", "", "beginTask1"});
		expected.put("style 2 below style 3", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 0 below style 4", new String[] {"beginTask0", "subTask1", "beginTask0"});
		expected.put("style 4 below style 0", new String[] {"beginTask1", "subTask1", "beginTask1"});
		expected.put("style 2 below style 2", new String[] {"setTaskName0", "subTask1", "setTaskName1"});
		expected.put("style 1 as top-level monitor", new String[] {"beginTask0", "", "setTaskName0"});
		expected.put("style 2 below style 1", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 0 below style 3", new String[] {"beginTask1", "", "setTaskName1"});
		expected.put("style 2 below style 0", new String[] {"setTaskName0", "subTask1", "setTaskName1"});
		expected.put("style 0 below style 2", new String[] {"beginTask1", "subTask1", "setTaskName1"});
		expected.put("style 0 below style 1", new String[] {"beginTask1", "", "setTaskName1"});
		expected.put("style 0 below style 0", new String[] {"beginTask1", "subTask1", "setTaskName1"});
		expected.put("style 5 as top-level monitor", new String[] {"beginTask0", "", "beginTask0"});
		expected.put("style 0 as top-level monitor", new String[] {"beginTask0", "subTask0", "setTaskName0"});
		expected.put("style 4 as top-level monitor", new String[] {"beginTask0", "subTask0", "beginTask0"});
		expected.put("style 7 below style 7", new String[] {"", "", ""});
		expected.put("style 7 below style 6", new String[] {"", "subTask0", ""});
		HashMap results = new HashMap();

		for (int i = 0; i < styles.length; i++) {
			int style = styles[i];
			{
				TestProgressMonitor top = new TestProgressMonitor();
				top.beginTask("", 100);
				SubMonitor converted = SubMonitor.convert(top, 100);

				SubMonitor styled = converted.newChild(100, style);
				styled.setWorkRemaining(100);

				String testName = "style " + style + " as top-level monitor";
				results.put(testName, runChildTest(0, top, styled, 100 * styles.length));
			}

			for (int j = 0; j < styles.length; j++) {
				int innerStyle = styles[j];

				TestProgressMonitor newTop = new TestProgressMonitor();
				newTop.beginTask("", 100);
				SubMonitor newConverted = SubMonitor.convert(newTop, 100);

				SubMonitor innerStyled = newConverted.newChild(100, style);

				runChildTest(0, newTop, innerStyled, 100);

				SubMonitor innerChild = innerStyled.newChild(100, innerStyle);
				String testName = "style " + innerStyle + " below style " + style;
				results.put(testName, runChildTest(1, newTop, innerChild, 100));
				innerChild.done();
			}
		}

		String failure = null;
		// Output the code for the observed results, in case one of them has changed intentionally
		for (Iterator iter = results.entrySet().iterator(); iter.hasNext();) {
			Map.Entry next = (Map.Entry) iter.next();
			String[] expectedResult = (String[]) expected.get(next.getKey());
			if (expectedResult == null)
				expectedResult = new String[0];

			String[] value = (String[]) next.getValue();
			if (compareArray(value, expectedResult))
				continue;

			System.out.print("expected.put(\"" + next.getKey() + "\", new String[] {");
			failure = (String) next.getKey();
			String list = concatArray(value);
			System.out.println(list + "});");
		}

		if (failure != null) // Now actually throw an assertation if one of the results failed
			Assert.assertEquals(failure, concatArray((String[]) expected.get(failure)), concatArray((String[]) results.get(failure)));
	}

	private boolean compareArray(String[] value, String[] expectedResult) {
		if (value == null || expectedResult == null)
			return value == null && expectedResult == null;

		if (value.length != expectedResult.length)
			return false;
		for (int i = 0; i < expectedResult.length; i++) {
			String next = expectedResult[i];
			if (!next.equals(value[i]))
				return false;
		}
		return true;
	}

	private String concatArray(String[] value) {
		if (value == null)
			return "";

		StringBuffer buf = new StringBuffer();
		boolean isFirst = true;
		for (int i = 0; i < value.length; i++) {
			String nextValue = value[i];
			if (!isFirst)
				buf.append(", ");
			isFirst = false;
			buf.append("\"" + nextValue + "\"");
		}
		String list = buf.toString();
		return list;
	}

	/**
	 * Ensures that SubMonitor doesn't propogate redundant progress to its parent.
	 */
	public void testRedundantWork() {
		TestProgressMonitor top = new TestProgressMonitor();

		SubMonitor monitor = SubMonitor.convert(top, 10000);
		for (int i = 0; i < 10000; i++) {
			monitor.setTaskName("Task name");
			monitor.subTask("Subtask");
			monitor.worked(0);
			monitor.internalWorked(0.0);

			// Report some real work
			monitor.worked(1);
		}

		top.done();
		top.assertOptimal();
	}

	public void testCancellation() {
		TestProgressMonitor root = new TestProgressMonitor();

		SubMonitor spm = SubMonitor.convert(root, 1000);

		// Test that changes at the root propogate to the child
		root.setCanceled(true);
		Assert.assertTrue(spm.isCanceled());
		root.setCanceled(false);
		Assert.assertFalse(spm.isCanceled());

		// Test that changes to the child propogate to the root
		spm.setCanceled(true);
		Assert.assertTrue(root.isCanceled());
		spm.setCanceled(false);
		Assert.assertFalse(root.isCanceled());

		// Test a chain of depth 2

		SubMonitor spm2 = spm.newChild(1000);

		// Test that changes at the root propogate to the child
		root.setCanceled(true);
		Assert.assertTrue(spm2.isCanceled());
		root.setCanceled(false);
		Assert.assertFalse(spm2.isCanceled());

		// Test that changes to the child propogate to the root
		spm2.setCanceled(true);
		Assert.assertTrue(root.isCanceled());
		spm2.setCanceled(false);
		Assert.assertFalse(root.isCanceled());
	}

	public void testNullParent() {
		// Touch everything in the public API to ensure we don't throw an NPE
		SubMonitor mon = SubMonitor.convert(null, 1000);
		mon.setWorkRemaining(500);
		mon.worked(250);
		mon.newChild(200);

		mon.internalWorked(50.0);
		Assert.assertFalse(mon.isCanceled());
		mon.setCanceled(true);
		Assert.assertTrue(mon.isCanceled());
		mon.subTask("subtask");
		mon.setTaskName("taskname");
		mon.done();
	}

	/**
	 * Tests the automatic cleanup when progress monitors are created via their constructor
	 */
	public void testNewChild() {
		TestProgressMonitor top = new TestProgressMonitor();
		SubMonitor mon = SubMonitor.convert(top, 1000);

		Assert.assertEquals("Ensure no work has been reported yet", 0.0, top.getTotalWork(), 0.01d);

		mon.newChild(100);

		Assert.assertEquals("Ensure no work has been reported yet", 0.0, top.getTotalWork(), 0.01d);

		mon.newChild(200);

		Assert.assertEquals("Ensure monitor1 was collected", 100.0, top.getTotalWork(), 0.01d);

		// The following behavior is necessary to make it possible to pass multiple progress monitors as
		// arguments to the same method.
		Assert.assertEquals("Monitor2 should not have been collected yet (when the public constructor is used, collection should happen when beginTask() or setWorkRemaining() is called.", 100.0, top.getTotalWork(), 0.01d);

		SubMonitor monitor4 = mon.newChild(300);

		Assert.assertEquals("Now monitor2 should be collected", 300.0, top.getTotalWork(), 0.01d);

		monitor4.done();

		Assert.assertEquals("Now monitor4 should be collected", 600.0, top.getTotalWork(), 0.01d);

		mon.newChild(10);

		Assert.assertEquals("Creating a child when there are no active children should not report any work", 600.0, top.getTotalWork(), 0.01d);

		mon.worked(20);

		// Test for bug 210394
		Assert.assertEquals("Reporting work should cause the active child to be destroyed", 630.0, top.getTotalWork(), 0.01d);

		mon.newChild(10);

		Assert.assertEquals("monitor5 should have been cleaned up", 630.0, top.getTotalWork(), 0.01d);

		mon.internalWorked(60);

		Assert.assertEquals("Calling internalWorked should clean up active children", 700.0, top.getTotalWork(), 0.01d);

		// Now create a chain of undisposed children
		SubMonitor monitor7 = mon.newChild(100);

		SubMonitor monitor8 = monitor7.newChild(40);

		monitor8.newChild(10);

		mon.done();

		Assert.assertEquals("Calling done should clean up unused work", 1000.0, top.getTotalWork(), 0.01d);
	}

	/**
	 * Tests creating progress monitors under a custom progress monitor
	 * parent. This is the same as the performance test as the same name, 
	 * but it verifies correctness rather than performance.
	 */
	public void testCreateChildrenUnderCustomParent() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		createChildrenUnderParent(monitor, SubMonitorTest.PROGRESS_SIZE);

		// We don't actually expect the progress to be optimal in this case since the progress monitor wouldn't
		// know what it was rooted under and would have had to report more progress than necessary... but we
		// should be able to check that there was no redundancy.

		Assert.assertTrue(monitor.getRedundantWorkCalls() == 0);
		Assert.assertTrue(monitor.getWorkCalls() >= 100);
	}

	/**
	 * Creates a chain of n nested progress monitors. Calls beginTask on all monitors
	 * except for the innermost one.
	 * 
	 * @param parent
	 * @param depth
	 * @return the innermost SubMonitor
	 */
	public static SubMonitor createSubProgressChain(SubMonitor parent, int depth) {
		depth--;
		parent.beginTask("", 100);
		SubMonitor current = parent;
		while (depth > 0) {
			current.setWorkRemaining(100);
			current = current.newChild(100);
			depth--;
		}
		return current;
	}

	/**
	 * Creates a balanced binary tree of progress monitors, without calling worked. Tests
	 * progress monitor creation and cleanup time, and ensures that excess progress is
	 * being collected when IProgressMonitor.done() is called.
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize total size of the recursion tree
	 */
	public static void createBalancedTree(IProgressMonitor parent, int loopSize) {
		SubMonitor monitor = SubMonitor.convert(parent, 100);
		int leftBranch = loopSize / 2;
		int rightBranch = loopSize - leftBranch;

		if (leftBranch > 1) {
			createBalancedTree(monitor.newChild(50), leftBranch);
		}

		if (rightBranch > 1) {
			createBalancedTree(monitor.newChild(50), rightBranch);
		}
	}

	/**
	 * <p>The innermost loop for the create tree test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.</p>
	 * 
	 * <p>The performance test ensures that it is fast to create a lot of progress monitors.</p>
	 * 
	 * <p>The correctness test ensures that creating and destroying SubMonitors
	 * is enough to report progress, even if worked(int) and worked(double) are never called</p>
	 */
	public static void runTestCreateTree(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		SubMonitor nestedMonitor = SubMonitorTest.createSubProgressChain(progress, SubMonitorTest.CHAIN_DEPTH);

		SubMonitorTest.createBalancedTree(nestedMonitor, SubMonitorTest.PROGRESS_SIZE);

		progress.done();
		monitor.done();
	}

	/**
	 * Reports progress by creating a balanced binary tree of progress monitors. Simulates 
	 * mixed usage of IProgressMonitor in a typical usage. Calls isCanceled once each time work 
	 * is reported. Half of the work is reported using internalWorked and half is reported using worked,
	 * to simulate mixed usage of the progress monitor.
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize total size of the recursion tree
	 */
	public static void reportWorkInBalancedTree(IProgressMonitor parent, int loopSize) {
		SubMonitor monitor = SubMonitor.convert(parent, 100);
		int leftBranch = loopSize / 2;
		int rightBranch = loopSize - leftBranch;

		if (leftBranch > 1) {
			reportWorkInBalancedTree(monitor.newChild(50), leftBranch);
		} else {
			monitor.worked(25);
			monitor.internalWorked(25.0);
			monitor.isCanceled();
		}

		if (rightBranch > 1) {
			reportWorkInBalancedTree(monitor.newChild(50), rightBranch);
		} else {
			monitor.worked(25);
			monitor.internalWorked(25.0);
			monitor.isCanceled();
		}
	}

	/**
	 * The innermost loop for the recursion test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.
	 */
	public static void runTestTypicalUsage(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		SubMonitor nestedMonitor = SubMonitorTest.createSubProgressChain(progress, SubMonitorTest.CHAIN_DEPTH);

		SubMonitorTest.reportWorkInBalancedTree(nestedMonitor, SubMonitorTest.PROGRESS_SIZE);

		progress.done();
		monitor.done();
	}

	/**
	 * Tests SubMonitor.worked. This is the same
	 * as the performance test as the same name, but it verifies correctness
	 * rather than performance.
	 */
	public void testWorked() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		SubMonitor nestedMonitor = createSubProgressChain(progress, SubProgressTest.CHAIN_DEPTH);

		reportWorkInLoop(nestedMonitor, SubProgressTest.PROGRESS_SIZE);

		progress.done();
		monitor.done();

		monitor.assertOptimal();
	}

	/**
	 * Tests SubMonitor.worked. This is the same
	 * as the performance test as the same name, but it verifies correctness
	 * rather than performance.
	 */
	public void testInternalWorked() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		SubMonitor nestedMonitor = createSubProgressChain(progress, SubProgressTest.CHAIN_DEPTH);

		reportFloatingPointWorkInLoop(nestedMonitor, SubProgressTest.PROGRESS_SIZE);

		progress.done();
		monitor.done();

		monitor.assertOptimal();
	}

	/**
	 * Tests reporting of progress by sub-monitors created via newChild() 
	 */
	public void testBug252446() {
		int children = 12;
		int cyclesPerChild = 17;

		TestProgressMonitor monitor = new TestProgressMonitor();
		SubMonitor progress = SubMonitor.convert(monitor, children * cyclesPerChild);

		// At this time monitor.getExpectedWork() == SubMonitor.MINIMUM_RESOLUTION == 1000
		double expectedTicksPerIteration = (double) monitor.getExpectedWork() / children / cyclesPerChild;

		for (int i = 0; i < children; i++) {
			IProgressMonitor mon = progress.newChild(cyclesPerChild);
			for (int j = 1; j <= cyclesPerChild; j++) {
				mon.worked(1);
				double expectedTopMonitorWork = expectedTicksPerIteration * (i * cyclesPerChild + j);
				// Progress is passed to the parent monitor as integer leading to rounding 
				// errors. The parent's progress has to follow child's progress "close enough"
				// and then it will catch up when next child is created. Hence, a relatively large delta 
				// value in this check:
				assertEquals(expectedTopMonitorWork, monitor.getTotalWork(), 2.0d);
			}
		}
		monitor.done();
		monitor.assertOptimal();
	}

	/**
	 * Creates and destroys the given number of child progress monitors under the given parent.
	 * 
	 * @param parent monitor to create children under. The caller must call done on this monitor
	 * if necessary. 
	 * @param progressSize total number of children to create.
	 */
	private static void createChildrenUnderParent(IProgressMonitor parent, int progressSize) {
		SubMonitor monitor = SubMonitor.convert(parent, progressSize);

		for (int count = 0; count < progressSize; count++) {
			SubMonitor mon = monitor.newChild(1);
			mon.beginTask("", 100);
		}
	}

	static public void reportPerformance(String className, String methodName, long startTime, long endTime) {
		// enable to see performance results for the progress monitors
		//			System.out.println(className + "#" + methodName + " elapsed time: " + (endTime - startTime) / 1000.0d + "s");
	}

}
