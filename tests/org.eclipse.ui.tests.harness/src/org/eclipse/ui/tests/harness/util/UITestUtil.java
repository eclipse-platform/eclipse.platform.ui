/*******************************************************************************
 * Copyright (c) 2025 Vector Informatik and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public final class UITestUtil {

	private UITestUtil() {
	}

	/**
	 * Returns the workbench page input to use for newly created windows.
	 *
	 * @return the page input to use for newly created windows
	 */
	public static IAdaptable getPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
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
			IWorkbenchWindow window = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, getPageInput());
			waitOnShell(window.getShell());
			return window;
		} catch (WorkbenchException e) {
			fail("Problem opening test window", e);
			return null;
		}
	}

	/**
	 * Fails the test due to the given throwable.
	 */
	private static void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			write(status, 0);
		} else
			e.printStackTrace();
		throw new AssertionError(message, e);
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
	 * Open "n" test pages with the empty perspective in a window.
	 */
	public static IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal) {
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
	 * Open a test page with the empty perspective in a window.
	 */
	public static IWorkbenchPage openTestPage(IWorkbenchWindow win) {
		IWorkbenchPage[] pages = openTestPage(win, 1);
		if (pages != null) {
			return pages[0];
		}
		return null;
	}

	/**
	 * Close all pages within a window.
	 */
	public static void closeAllPages(IWorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		for (IWorkbenchPage page : pages) {
			page.close();
		}
	}

	public static void processEvents() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null)
			while (display.readAndDispatch())
				;
	}

	/**
	 *
	 * @param condition , or null if this should only wait
	 * @param timeout   , -1 if forever
	 * @return true if successful, false if time out or interrupted
	 */
	public static boolean processEventsUntil(Condition condition, long timeout) {
		long startTime = System.currentTimeMillis();
		Display display = PlatformUI.getWorkbench().getDisplay();
		while (condition == null || !condition.compute()) {
			if (timeout != -1 && System.currentTimeMillis() - startTime > timeout) {
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

	public static interface Condition {
		public boolean compute();
	}

	/**
	 * Utility for waiting until the execution of jobs of any family has finished or
	 * timeout is reached. If no jobs are running, the method waits given minimum
	 * wait time. While this method is waiting for jobs, UI events are processed.
	 *
	 * @param minTimeMs minimum wait time in milliseconds
	 * @param maxTimeMs maximum wait time in milliseconds
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
	private static void sleep(long millis) {
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
	 * @param shell non null
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

	/**
	 * Try and process events until the new shell is the active shell. This may
	 * never happen, so time out after a suitable period.
	 *
	 * @param shell the shell to wait on
	 * @since 3.2
	 */
	static void waitOnShell(Shell shell) {
		processEvents();
		waitForJobs(100, 5000);
	}

}
