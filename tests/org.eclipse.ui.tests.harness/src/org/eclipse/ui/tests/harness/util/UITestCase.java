/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474957
 *     Paul Pazderski <paul-eclipse@ppazderski.de> - Bug 546537: improve compatibility with BlockJUnit4ClassRunner
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import junit.framework.TestCase;

/**
 * <code>UITestCase</code> is a useful super class for most
 * UI tests cases.  It contains methods to create new windows
 * and pages.  It will also automatically close the test
 * windows when the tearDown method is called.
 */
public abstract class UITestCase extends TestCase {

	/**
	 * Rule to close windows opened during the test case, manually called to remain
	 * compatible with JUnit3
	 */
	private final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private Set<Shell> preExistingShells;

	/**
	 * Required to preserve the existing logging output when running tests with
	 * {@link BlockJUnit4ClassRunner}.
	 */
	@Rule
	public TestWatcher testWatcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			runningTest = description.getMethodName();
		}
		@Override
		protected void finished(Description description) {
			runningTest = null;
		}
	};
	/**
	 * Name of the currently executed test method. Only valid if test is executed
	 * with {@link BlockJUnit4ClassRunner}.
	 */
	private String runningTest = null;

	public UITestCase(String testName) {
		super(testName);
	}

	/**
	 * Outputs a trace message to the trace output device, if enabled.
	 * By default, trace messages are sent to <code>System.out</code>.
	 *
	 * @param msg the trace message
	 */
	protected void trace(String msg) {
		System.out.println(msg);
	}

	/**
	 * Simple implementation of setUp. Subclasses are prevented from overriding this
	 * method to maintain logging consistency. doSetUp() should be overridden
	 * instead.
	 * <p>
	 * This method is public and annotated with {@literal @}{@link Before} to setup
	 * tests which are configured to {@link RunWith} JUnit4 runner.
	 * </p>
	 */
	@Before
	@Override
	public final void setUp() throws Exception {
		super.setUp();
		closeTestWindows.before();
		this.preExistingShells = Set.of(PlatformUI.getWorkbench().getDisplay().getShells());
		String name = runningTest != null ? runningTest : this.getName();
		trace(TestRunLogUtil.formatTestStartMessage(name));
		doSetUp();

	}

	/**
	 * Sets up the fixture, for example, open a network connection.
	 * This method is called before a test is executed.
	 * The default implementation does nothing.
	 * Subclasses may extend.
	 */
	protected void doSetUp() throws Exception {
		// do nothing.
	}

	/**
	 * Simple implementation of tearDown. Subclasses are prevented from overriding
	 * this method to maintain logging consistency. doTearDown() should be
	 * overridden instead.
	 * <p>
	 * This method is public and annotated with {@literal @}{@link After} to setup
	 * tests which are configured to {@link RunWith} JUnit4 runner.
	 * </p>
	 */
	@After
	@Override
	public final void tearDown() throws Exception {
		String name = runningTest != null ? runningTest : this.getName();
		trace(TestRunLogUtil.formatTestFinishedMessage(name));
		doTearDown();

		// Check for shell leak.
		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && !preExistingShells.contains(shell)) {
				leakedModalShellTitles.add(shell.getText());
				shell.close();
			}
		}
		assertEquals("Test leaked modal shell: [" + String.join(", ", leakedModalShellTitles) + "]", 0,
				leakedModalShellTitles.size());
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 * The default implementation closes all test windows, processing events both before
	 * and after doing so.
	 * Subclasses may extend.
	 */
	protected void doTearDown() throws Exception {
		closeTestWindows.after();
	}

	/**
	 * Close all test windows.
	 */
	public void closeAllTestWindows() {
		closeTestWindows.closeAllTestWindows();
	}

	/**
	 * Set whether the window listener will manage opening and closing of created windows.
	 */
	protected void manageWindows(boolean manage) {
		closeTestWindows.setEnabled(manage);
	}


}
