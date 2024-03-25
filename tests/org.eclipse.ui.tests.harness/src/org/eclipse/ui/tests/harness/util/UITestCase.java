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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceMemento;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
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
	 * Returns the workbench page input to use for newly created windows.
	 *
	 * @return the page input to use for newly created windows
	 * @since 3.1
	 */
	public static IAdaptable getPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Rule to close windows opened during the test case, manually called to remain
	 * compatible with JUnit3
	 */
	private final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	protected IWorkbench fWorkbench;

	/** Preference helper to restore changed preference values after test run. */
	private final PreferenceMemento prefMemento = new PreferenceMemento();

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
	 * Fails the test due to the given throwable.
	 */
	public static void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			write(status, 0);
		} else
			e.printStackTrace();
		fail(message + ": " + e);
	}

	private static void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("\t".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}

	private static void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());

		if (status.getException() != null) {
			indent(output, indent);
			output.print("Exception: ");
			status.getException().printStackTrace(output);
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (IStatus child : children) {
				write(child, indent + 1);
			}
		}
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
		fWorkbench = PlatformUI.getWorkbench();
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
		prefMemento.resetPreferences();
		doTearDown();
		fWorkbench = null;

		// Check for modal shell leak.
		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && shell.isVisible()
					&& (shell.getStyle() & (SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) != 0) {
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

	public static void processEvents() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null)
			while (display.readAndDispatch())
				;
	}

	/**
	 * Utility for waiting until the execution of jobs of any family has
	 * finished or timeout is reached. If no jobs are running, the method waits
	 * given minimum wait time. While this method is waiting for jobs, UI events
	 * are processed.
	 *
	 * @param minTimeMs
	 *            minimum wait time in milliseconds
	 * @param maxTimeMs
	 *            maximum wait time in milliseconds
	 */
	public static void waitForJobs(long minTimeMs, long maxTimeMs) {
		if (maxTimeMs < minTimeMs) {
			throw new IllegalArgumentException("Max time is smaller as min time!");
		}
		final long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < minTimeMs) {
			processEvents();
			sleep(10);
		}
		while (!Job.getJobManager().isIdle() && System.currentTimeMillis() - start < maxTimeMs) {
			processEvents();
			sleep(10);
		}
	}

	/**
	 * Pauses execution of the current thread
	 */
	protected static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Tries to make given shell active.
	 *
	 * <p>
	 * Note: the method runs at least 1000 milliseconds to make sure the active
	 * window is really active, see
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417258#c27
	 *
	 * @param shell
	 *            non null
	 * @return true if the given shell is active for the current display
	 */
	public static boolean forceActive(Shell shell) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell[] shells = display.getShells();
		for (Shell s : shells) {
			if (s.isVisible()) {
				s.setMinimized(true);
			}
			processEvents();
		}
		waitForJobs(200, 3000);
		for (Shell s : shells) {
			if (s.isVisible()) {
				s.setMinimized(false);
			}
			processEvents();
		}
		waitForJobs(200, 3000);
		shell.setVisible(false);
		processEvents();
		shell.setMinimized(true);
		processEvents();
		waitForJobs(200, 3000);
		shell.setVisible(true);
		processEvents();
		shell.setMinimized(false);
		processEvents();
		shell.forceActive();
		processEvents();
		shell.forceFocus();
		processEvents();
		waitForJobs(400, 3000);
		return display.getActiveShell() == shell;
	}

	public static class ShellStateListener implements ShellListener {
		private final AtomicBoolean shellIsActive;

		public ShellStateListener(AtomicBoolean shellIsActive) {
			this.shellIsActive = shellIsActive;
		}

		@Override
		public void shellIconified(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellDeiconified(ShellEvent e) {
			shellIsActive.set(true);
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellClosed(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellActivated(ShellEvent e) {
			shellIsActive.set(true);
		}
	}

	public static interface Condition {
		public boolean compute();
	}

	/**
	 *
	 * @param condition
	 *            , or null if this should only wait
	 * @param timeout
	 *            , -1 if forever
	 * @return true if successful, false if time out or interrupted
	 */
	public static boolean processEventsUntil(Condition condition, long timeout) {
		long startTime = System.currentTimeMillis();
		Display display = PlatformUI.getWorkbench().getDisplay();
		while (condition == null || !condition.compute()) {
			if (timeout != -1
					&& System.currentTimeMillis() - startTime > timeout) {
				return false;
			}
			while (display.readAndDispatch())
				;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}

	/**
	 * Open a test window with the empty perspective.
	 */
	public static IWorkbenchWindow openTestWindow() {
		return openTestWindow(EmptyPerspective.PERSP_ID);
	}

	/**
	 * Open a test window with the provided perspective.
	 */
	public static IWorkbenchWindow openTestWindow(String perspectiveId) {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().openWorkbenchWindow(
					perspectiveId, getPageInput());
			waitOnShell(window.getShell());
			return window;
		} catch (WorkbenchException e) {
			fail("Problem opening test window", e);
			return null;
		}
	}

	/**
	 * Try and process events until the new shell is the active shell. This may
	 * never happen, so time out after a suitable period.
	 *
	 * @param shell
	 *            the shell to wait on
	 * @since 3.2
	 */
	private static void waitOnShell(Shell shell) {
		processEvents();
		waitForJobs(100, 5000);
	}

	/**
	 * Close all test windows.
	 */
	public void closeAllTestWindows() {
		closeTestWindows.closeAllTestWindows();
	}

	/**
	 * Open a test page with the empty perspective in a window.
	 */
	public IWorkbenchPage openTestPage(IWorkbenchWindow win) {
		IWorkbenchPage[] pages = openTestPage(win, 1);
		if (pages != null) {
			return pages[0];
		}
		return null;
	}

	/**
	 * Open "n" test pages with the empty perspective in a window.
	 */
	public IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal) {
		try {
			IWorkbenchPage[] pages = new IWorkbenchPage[pageTotal];
			IAdaptable input = getPageInput();

			for (int i = 0; i < pageTotal; i++) {
				pages[i] = win.openPage(EmptyPerspective.PERSP_ID, input);
			}
			return pages;
		} catch (WorkbenchException e) {
			fail("Problem opening test page", e);
			return null;
		}
	}

	/**
	 * Close all pages within a window.
	 */
	public void closeAllPages(IWorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage page : pages) {
			page.close();
		}
	}

	/**
	 * Set whether the window listener will manage opening and closing of created windows.
	 */
	protected void manageWindows(boolean manage) {
		closeTestWindows.setEnabled(manage);
	}

	/**
	 * Returns the workbench.
	 *
	 * @return the workbench
	 * @since 3.1
	 */
	protected IWorkbench getWorkbench() {
		return fWorkbench;
	}

	/**
	 * Change a preference value for this test run. The preference will be reset to
	 * its value before test started automatically on {@link #tearDown()}.
	 *
	 * @param <T>   preference value type. The type must have a corresponding
	 *              {@link IPreferenceStore} setter.
	 * @param store preference store to manipulate (must not be <code>null</code>)
	 * @param name  preference to change
	 * @param value new preference value
	 * @throws IllegalArgumentException when setting a type which is not supported
	 *                                  by {@link IPreferenceStore}
	 *
	 * @see IPreferenceStore#setValue(String, double)
	 * @see IPreferenceStore#setValue(String, float)
	 * @see IPreferenceStore#setValue(String, int)
	 * @see IPreferenceStore#setValue(String, long)
	 * @see IPreferenceStore#setValue(String, boolean)
	 * @see IPreferenceStore#setValue(String, String)
	 */
	protected <T> void setPreference(IPreferenceStore store, String name, T value) {
		prefMemento.setValue(store, name, value);
	}
}
