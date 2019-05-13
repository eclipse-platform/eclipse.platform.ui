/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Lucas Bullen (Red Hat Inc.) - Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.ISafeRunnableRunner;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

public abstract class ViewerTestCase extends TestCase {

	Display fDisplay;
	protected Shell fShell;
	protected StructuredViewer fViewer;
	protected TestElement fRootElement;
	public TestModel fModel;

	protected boolean disableTestsBug347491 = false;
	protected boolean eventLoopAdjustmentBug531048 = false;
	private ILogger oldLogger;
	private ISafeRunnableRunner oldRunner;

	public ViewerTestCase(String name) {
		super(name);
		disableTestsBug347491 = Util.isCocoa();
		eventLoopAdjustmentBug531048 = Util.isGtk();
	}

	protected void assertSelectionEquals(String message, TestElement expected) {
		IStructuredSelection structuredSelection = fViewer.getStructuredSelection();
		IStructuredSelection expectedSelection = new StructuredSelection(expected);
		assertEquals("selectionEquals - " + message, structuredSelection, expectedSelection);
	}

	protected abstract StructuredViewer createViewer(Composite parent);

	public void interact() {
		Shell shell = fShell;
		if (shell != null && !shell.isDisposed()) {
			Display display = shell.getDisplay();
			if (display != null) {
				while (shell.isVisible()) {
					display.readAndDispatch();
				}
			}
		}
	}

	protected void openBrowser() {
		fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		fShell = new Shell(fDisplay, getShellStyle());
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);
		setInput();
		fShell.open();
		//processEvents();
	}

	/**
	 * @return
	 */
	protected int getShellStyle() {
		return SWT.SHELL_TRIM;
	}

	protected void setInput() {
		fViewer.setInput(fRootElement);
	}

	public void processEvents() {
		Shell shell = fShell;
		if (shell != null && !shell.isDisposed()) {
			Display display = shell.getDisplay();
			if (display != null) {
				while (display.readAndDispatch()) {
					// loop until there are no more events to dispatch
				}
			}
		}
	}

	@Override
	public void setUp() {
		oldLogger = Policy.getLog();
		oldRunner = SafeRunnable.getRunner();
		Policy.setLog(status -> fail(status.getMessage()));
		SafeRunnable.setRunner(code -> {
			try {
				code.run();
			} catch (Throwable th) {
				throw new RuntimeException(th);
			}
		});
		setUpModel();
		openBrowser();
	}

	protected void setUpModel() {
		fRootElement = TestElement.createModel(3, 10);
		fModel = fRootElement.getModel();
	}

	/**
	 * Pauses execution of the current thread
	 *
	 * @param millis
	 */
	protected static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public void tearDown() {
		Policy.setLog(oldLogger);
		SafeRunnable.setRunner(oldRunner);
		processEvents();
		fViewer = null;
		if (fShell != null) {
			fShell.dispose();
			fShell = null;
		}
		// leave the display
		fRootElement = null;
		fModel = null;
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
	public void waitForJobs(long minTimeMs, long maxTimeMs) {
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
}
