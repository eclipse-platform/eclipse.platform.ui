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

import java.util.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 *
 */
public class SubProgressTest extends TestCase {

	private long startTime;
	/**
	 * <p>Depth of the chain chain of progress monitors. In all of the tests, we create
	 * a nested chain of progress monitors rathar than a single monitor, to test its
	 * scalability under recursion. We pick a number representing a moderately deep
	 * recursion, but is still small enough that it could correspond to a real call stack
	 * without causing overflow.</p>
	 * 
	 * <p>Note: changing this constant will invalidate comparisons with old performance data.</p>
	 */
	public static final int CHAIN_DEPTH = 100;
	/**
	 * <p>Number of calls to worked() within each test. This was chosen to be significantly larger 
	 * than 1000 to test how well the monitor can optimize unnecessary resolution
	 * in reported progress, but small enough that the test completes in a reasonable
	 * amount of time.</p>
	 * 
	 * <p>Note: changing this constant will invalidate comparisons with old performance data.</p>
	 */
	public static final int PROGRESS_SIZE = 100000;

	public SubProgressTest() {
		super();
	}

	public SubProgressTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		startTime = System.currentTimeMillis();
		super.setUp();
	}

	protected void tearDown() throws Exception {
		long endTime = System.currentTimeMillis();
		SubMonitorTest.reportPerformance(getClass().getName(), getName(), startTime, endTime);
		super.tearDown();
	}

	/**
	 * Calls done on the given progress monitor and all of its parents, to a maximum
	 * of the given depth. 
	 * 
	 * @deprecated to suppress deprecation warnings
	 * 
	 * @param monitor
	 * @param depth
	 */
	public static void callDoneOnChain(IProgressMonitor monitor, int depth) {
		IProgressMonitor current = monitor;
		for (int count = 0; count < depth; count++) {
			current.done();
			if (!(current instanceof SubProgressMonitor))
				return;
			SubProgressMonitor cur = (SubProgressMonitor) current;
			current = cur.getWrappedProgressMonitor();
		}
	}

	/**
	 * Test behaviors that subclasses of SubProgressMonitor will expect from their base class.
	 * @deprecated to suppress deprecation warnings
	 */
	public void testCustomSubclass() {
		TestProgressMonitor top = new TestProgressMonitor();
		top.beginTask("", 1000);

		SubProgressSubclass customSubclass = new SubProgressSubclass(top, 1000);
		customSubclass.beginTask("", 10000);

		for (int count = 0; count < 10000; count++)
			customSubclass.worked(1);

		Assert.assertEquals("If there is a custom subclass of SubProgressMonitor, all calls to worked() should delegate to internalWorked", 10000, customSubclass.internalWorkedCalls);
		customSubclass.done();
		top.done();
	}

	/**
	 * Tests the style bits in SubProgressMonitor 
	 * @deprecated to suppress deprecation warnings
	 */
	public void testStyles() {

		int[] styles = new int[] {0, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK | SubProgressMonitor.SUPPRESS_SUBTASK_LABEL};

		HashMap expected = new HashMap();
		expected.put("style 0 below style 2", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 2 below style 0", new String[] {"setTaskName1", "beginTask1 ", "setTaskName1"});
		expected.put("style 6 below style 0", new String[] {"setTaskName1", "beginTask1 ", "setTaskName1"});
		expected.put("style 2 below style 4", new String[] {"setTaskName1", "beginTask0 beginTask1 ", "setTaskName1"});
		expected.put("style 0 below style 0", new String[] {"setTaskName0", "subTask1", "setTaskName1"});
		expected.put("style 6 as top-level monitor", new String[] {"", "", "setTaskName0"});
		expected.put("style 6 below style 2", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 6 below style 6", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 0 below style 6", new String[] {"setTaskName0", "", "setTaskName1"});
		expected.put("style 4 below style 2", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 0 as top-level monitor", new String[] {"", "subTask0", "setTaskName0"});
		expected.put("style 0 below style 4", new String[] {"setTaskName0", "beginTask0 subTask1", "setTaskName1"});
		expected.put("style 4 below style 0", new String[] {"setTaskName1", "beginTask1 subTask1", "setTaskName1"});
		expected.put("style 4 as top-level monitor", new String[] {"", "beginTask0 subTask0", "setTaskName0"});
		expected.put("style 2 below style 6", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 4 below style 6", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 2 below style 2", new String[] {"setTaskName1", "", "setTaskName1"});
		expected.put("style 2 as top-level monitor", new String[] {"", "", "setTaskName0"});
		expected.put("style 6 below style 4", new String[] {"setTaskName1", "beginTask0 beginTask1 ", "setTaskName1"});
		expected.put("style 4 below style 4", new String[] {"setTaskName1", "beginTask0 beginTask1 subTask1", "setTaskName1"});
		HashMap results = new HashMap();

		for (int i = 0; i < styles.length; i++) {
			int style = styles[i];
			TestProgressMonitor top = new TestProgressMonitor();
			top.beginTask("", 100);
			SubProgressMonitor child = new SubProgressMonitor(top, 100, style);

			String testName = "style " + style + " as top-level monitor";
			results.put(testName, runChildTest(0, top, child, 100 * styles.length));

			for (int j = 0; j < styles.length; j++) {
				int innerStyle = styles[j];
				SubProgressMonitor innerChild = new SubProgressMonitor(child, 100, innerStyle);
				testName = "style " + innerStyle + " below style " + style;
				results.put(testName, runChildTest(1, top, innerChild, 100));
				innerChild.done();
			}
			child.done();
		}

		String failure = null;
		// Output the code for the observed results, in case one of them has changed intentionally
		for (Iterator iter = results.entrySet().iterator(); iter.hasNext();) {
			Map.Entry next = (Map.Entry) iter.next();
			String[] expectedResult = (String[]) expected.get(next.getKey());
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
	 * Tests SubProgressMonitor nesting when using the default constructor. (Tests
	 * parents in floating point mode)
	 * @deprecated to suppress deprecation warnings
	 */
	public void testConstructorNestingFP() {
		TestProgressMonitor top = new TestProgressMonitor();
		top.beginTask("", 2000);

		// Create an SPM, put it in floating-point mode, and consume half its work
		SubProgressMonitor fpMonitor = new SubProgressMonitor(top, 1000);
		fpMonitor.beginTask("", 100);
		fpMonitor.internalWorked(50.0);
		fpMonitor.internalWorked(-10.0); // should have no effect

		Assert.assertEquals(500.0, top.getTotalWork(), 0.01d);

		// Create a child monitor, and ensure that it grabs the correct amount of work
		// from the parent.
		SubProgressMonitor childMonitor = new SubProgressMonitor(fpMonitor, 20);
		childMonitor.beginTask("", 100);
		childMonitor.worked(100);
		childMonitor.done();

		Assert.assertEquals(700.0, top.getTotalWork(), 0.01d);

		// Create a child monitor, and ensure that it grabs the correct amount of work
		// from the parent.
		SubProgressMonitor childMonitor2 = new SubProgressMonitor(fpMonitor, 30);
		childMonitor2.beginTask("", 100);
		childMonitor2.worked(100);
		childMonitor2.done();

		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);

		// Ensure that creating another child will have no effect
		SubProgressMonitor childMonitor3 = new SubProgressMonitor(fpMonitor, 10);
		childMonitor3.beginTask("", 100);
		childMonitor3.worked(100);
		childMonitor3.done();

		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
		fpMonitor.worked(100);
		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
		fpMonitor.done();
		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
	}

	/**
	 * Tests SubProgressMonitor nesting when using the default constructor. Tests constructors
	 * in int mode.
	 * @deprecated to suppress deprecation warnings
	 */
	public void testConstructorNestingInt() {
		TestProgressMonitor top = new TestProgressMonitor();
		top.beginTask("", 2000);

		// Create an SPM leave it in int mode, and consume half its work
		SubProgressMonitor fpMonitor = new SubProgressMonitor(top, 1000);
		fpMonitor.beginTask("", 100);
		fpMonitor.worked(50);

		Assert.assertEquals(500.0, top.getTotalWork(), 0.01d);

		// Create a child monitor, and ensure that it grabs the correct amount of work
		// from the parent.
		SubProgressMonitor childMonitor = new SubProgressMonitor(fpMonitor, 20);
		childMonitor.beginTask("", 100);
		childMonitor.worked(100);
		childMonitor.done();

		Assert.assertEquals(700.0, top.getTotalWork(), 0.01d);

		// Create a child monitor, and ensure that it grabs the correct amount of work
		// from the parent.
		SubProgressMonitor childMonitor2 = new SubProgressMonitor(fpMonitor, 30);
		childMonitor2.beginTask("", 100);
		childMonitor2.worked(100);
		childMonitor2.done();

		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);

		// Ensure that creating another child will have no effect
		SubProgressMonitor childMonitor3 = new SubProgressMonitor(fpMonitor, 10);
		childMonitor3.beginTask("", 100);
		childMonitor3.worked(100);
		childMonitor3.done();

		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
		fpMonitor.worked(100);
		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
		fpMonitor.done();
		Assert.assertEquals(1000.0, top.getTotalWork(), 0.01d);
	}

	/**
	 * Tests the automatic cleanup when progress monitors are created via their constructor
	 * @deprecated to suppress deprecation warnings
	 */
	public void testParallelChildren() {
		TestProgressMonitor top = new TestProgressMonitor();
		top.beginTask("", 1000);
		SubProgressMonitor mon = new SubProgressMonitor(top, 1000);
		mon.beginTask("", 1000);

		SubProgressMonitor monitor1 = new SubProgressMonitor(mon, 200);
		SubProgressMonitor monitor2 = new SubProgressMonitor(mon, 200);

		Assert.assertEquals("Ensure no work has been reported yet", 0.0, top.getTotalWork(), 0.01d);
		monitor1.beginTask("", 1000);
		Assert.assertEquals("Ensure no work has been reported yet", 0.0, top.getTotalWork(), 0.01d);
		monitor2.beginTask("", 1000);
		Assert.assertEquals("Should not have cleaned up monitor 1", 0.0, top.getTotalWork(), 0.01d);
		monitor1.done();

		Assert.assertEquals("Should have cleaned up monitor 1", 200.0, top.getTotalWork(), 0.01d);
		monitor1.worked(1000);
		Assert.assertEquals("Monitor1 shouldn't report work once it's complete", 200.0, top.getTotalWork(), 0.01d);
		monitor2.worked(500);
		Assert.assertEquals(300.0, top.getTotalWork(), 0.01d);

		// Create a monitor that will leak - monitors won't be auto-completed until their done methods are
		// called
		SubProgressMonitor monitor3 = new SubProgressMonitor(mon, 300);
		Assert.assertEquals("Monitor2 should not have been cleaned up yet", 300.0, top.getTotalWork(), 0.01d);
		SubProgressMonitor monitor4 = new SubProgressMonitor(mon, 300);
		monitor4.beginTask("", 100);
		mon.done();
		Assert.assertNotNull(monitor3);

		Assert.assertEquals("All leaked work should have been collected", 1000.0, top.getTotalWork(), 0.01d);
	}

	/**
	 * @deprecated to suppress deprecation warnings
	 */
	public void testCancellation() {
		TestProgressMonitor root = new TestProgressMonitor();
		root.beginTask("", 1000);

		SubProgressMonitor spm = new SubProgressMonitor(root, 1000);

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
		spm.beginTask("", 1000);
		SubProgressMonitor spm2 = new SubProgressMonitor(spm, 1000);

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

	/**
	 * Tests creating progress monitors under a custom progress monitor
	 * parent. This is the same as the performance test as the same name, 
	 * but it verifies correctness rather than performance.
	 */
	public void testCreateChildrenUnderCustomParent() {
		TestProgressMonitor monitor = new TestProgressMonitor();
		createChildrenUnderParent(monitor, SubProgressTest.PROGRESS_SIZE);

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
	 * @deprecated to suppress deprecation warnings
	 * 
	 * @param parent
	 * @param depth
	 * @return the innermost SubProgressMonitor
	 */
	private static SubProgressMonitor createSubProgressChain(IProgressMonitor parent, int depth) {
		SubProgressMonitor current;
		do {
			parent.beginTask("", 100);
			current = new SubProgressMonitor(parent, 100);
			parent = current;
			depth--;
		} while (depth > 0);
		return current;
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
		for (int i = 0; i < loopSize; i++)
			monitor.worked(1);
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
		for (int i = 0; i < loopSize; i++)
			monitor.internalWorked(1.0d);
	}

	/**
	 * Reports progress by creating a balanced binary tree of progress monitors. Simulates 
	 * mixed usage of IProgressMonitor in a typical usage. Calls isCanceled once each time work 
	 * is reported. Half of the work is reported using internalWorked and half is reported using worked,
	 * to simulate mixed usage of the progress monitor.
	 * 
	 * @deprecated to suppress deprecation warnings
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize total size of the recursion tree
	 */
	public static void reportWorkInBalancedTree(IProgressMonitor monitor, int loopSize) {
		monitor.beginTask("", 100);
		int leftBranch = loopSize / 2;
		int rightBranch = loopSize - leftBranch;

		if (leftBranch > 1) {
			SubProgressMonitor leftProgress = new SubProgressMonitor(monitor, 50);
			reportWorkInBalancedTree(leftProgress, leftBranch);
			leftProgress.done();
		} else {
			monitor.worked(25);
			monitor.internalWorked(25.0);
			monitor.isCanceled();
		}

		if (rightBranch > 1) {
			SubProgressMonitor rightProgress = new SubProgressMonitor(monitor, 50);
			reportWorkInBalancedTree(rightProgress, rightBranch);
			rightProgress.done();
		} else {
			monitor.worked(25);
			monitor.internalWorked(25.0);
			monitor.isCanceled();
		}
	}

	/**
	 * Creates a balanced binary tree of progress monitors, without calling worked. Tests
	 * progress monitor creation and cleanup time, and ensures that excess progress is
	 * being collected when IProgressMonitor.done() is called.
	 * 
	 * @deprecated to suppress deprecation warnings
	 * 
	 * @param monitor progress monitor (callers are responsible for calling done() if necessary)
	 * @param loopSize total size of the recursion tree
	 */
	public static void createBalancedTree(IProgressMonitor monitor, int loopSize) {
		monitor.beginTask("", 100);
		int leftBranch = loopSize / 2;
		int rightBranch = loopSize - leftBranch;

		if (leftBranch > 1) {
			SubProgressMonitor leftProgress = new SubProgressMonitor(monitor, 50);
			createBalancedTree(leftProgress, leftBranch);
			leftProgress.done();
		}

		if (rightBranch > 1) {
			SubProgressMonitor rightProgress = new SubProgressMonitor(monitor, 50);
			createBalancedTree(rightProgress, rightBranch);
			rightProgress.done();
		}
	}

	/**
	 * The innermost loop for the looping test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.
	 * 
	 * @deprecated to suppress deprecation warnings
	 */
	public static void runTestWorked(IProgressMonitor monitor) {
		SubProgressMonitor nestedMonitor = createSubProgressChain(monitor, SubProgressTest.CHAIN_DEPTH);
		reportWorkInLoop(nestedMonitor, SubProgressTest.PROGRESS_SIZE);
		callDoneOnChain(nestedMonitor, SubProgressTest.CHAIN_DEPTH + 1);
	}

	/**
	 * The innermost loop for the looping test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.
	 * 
	 * @deprecated to suppress deprecation warnings
	 */
	public static void runTestInternalWorked(IProgressMonitor monitor) {
		SubProgressMonitor nestedMonitor = createSubProgressChain(monitor, SubProgressTest.CHAIN_DEPTH);
		reportFloatingPointWorkInLoop(nestedMonitor, SubProgressTest.PROGRESS_SIZE);
		callDoneOnChain(nestedMonitor, SubProgressTest.CHAIN_DEPTH + 1);
	}

	/**
	 * The innermost loop for the recursion test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.
	 * 
	 * @deprecated to suppress deprecation warnings
	 */
	public static void runTestTypicalUsage(IProgressMonitor monitor) {
		SubProgressMonitor nestedMonitor = createSubProgressChain(monitor, SubProgressTest.CHAIN_DEPTH);
		reportWorkInBalancedTree(nestedMonitor, SubProgressTest.PROGRESS_SIZE);
		callDoneOnChain(nestedMonitor, SubProgressTest.CHAIN_DEPTH + 1);
	}

	/**
	 * <p>The innermost loop for the create tree test. We make this a static method so
	 * that it can be used both in this performance test and in the correctness test.</p>
	 * 
	 * <p>The performance test ensures that it is fast to create a lot of progress monitors.</p>
	 * 
	 * <p>The correctness test ensures that creating and destroying SubProgressMonitors
	 * is enough to report progress, even if worked(int) and worked(double) are never called</p>
	 * 
	 * @deprecated to suppress deprecation warnings
	 */
	public static void runTestCreateTree(IProgressMonitor monitor) {
		SubProgressMonitor nestedMonitor = createSubProgressChain(monitor, SubProgressTest.CHAIN_DEPTH);
		createBalancedTree(nestedMonitor, SubProgressTest.PROGRESS_SIZE);
		callDoneOnChain(nestedMonitor, SubProgressTest.CHAIN_DEPTH + 1);
	}

	/**
	 * Creates and destroys the given number of child progress monitors under the given parent.
	 * 
	 * @param monitor monitor to create children under. The caller must call done on this monitor
	 * if necessary. 
	 * @param progressSize total number of children to create.
	 * 
	 * @deprecated to suppress deprecation warnings
	 */
	private static void createChildrenUnderParent(IProgressMonitor monitor, int progressSize) {
		monitor.beginTask("", progressSize);

		for (int count = 0; count < progressSize; count++) {
			SubProgressMonitor mon = new SubProgressMonitor(monitor, 1);
			mon.beginTask("", 100);
			mon.done();
		}
	}

	/**
	 * Test SubProgressMonitor's created with negative a work value.
	 */
	public void testNegativeWorkValues() {
		TestProgressMonitor top = new TestProgressMonitor();
		top.beginTask("", 10);

		SubProgressMonitor childMonitor = new SubProgressMonitor(top, IProgressMonitor.UNKNOWN); // -1
		childMonitor.beginTask("", 10);
		childMonitor.worked(5);
		Assert.assertEquals(0.0, top.getTotalWork(), 0.01d);
		childMonitor.done();
		Assert.assertEquals(0.0, top.getTotalWork(), 0.01d);

		top.done();
	}

}
