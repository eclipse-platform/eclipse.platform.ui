/*******************************************************************************
 * Copyright (c) 2009, 2024 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.ProgressView;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public abstract class ProgressTestCase {

	protected ProgressView progressView;
	protected IWorkbenchWindow window;

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void doSetUp() throws Exception {
		window = openTestWindow("org.eclipse.ui.resourcePerspective");

		// Remove progress info items before running the tests to prevent random
		// failings
		FinishedJobs.getInstance().clearAll();
	}

	@After
	public void doTearDown() throws Exception {
		// Remove progress info items
		FinishedJobs.getInstance().clearAll();
	}

	/**
	 * Opens the ProgresView on the specified page and process the UI events.
	 */
	public void openProgressView() throws Exception {
		progressView = (ProgressView) openView(IPageLayout.ID_PROGRESS_VIEW);
	}

	public IViewPart openView(String id) throws Exception {
		IWorkbenchPage activePage = window.getActivePage();
		IViewPart view = activePage.showView(id);
		assertNotNull("View is not created properly: " + id, view);
		processEvents();
		return view;
	}

	public void hideProgressView() {
		window.getActivePage().hideView(progressView);
		processEvents();
	}

	/**
	 * Wait until all given jobs are finished or timeout is reached.
	 *
	 * @param jobs        list of jobs to join
	 * @param timeout     timeout duration
	 * @param timeoutUnit timeout duration unit
	 * @throws OperationCanceledException if one of the jobs progress monitor was
	 *                                    canceled while joining
	 * @throws InterruptedException       if thread was interrupted while joining
	 * @throws TimeoutException           if not all jobs finished in time
	 */
	public static void joinJobs(Iterable<? extends Job> jobs, int timeout, TimeUnit timeoutUnit)
			throws OperationCanceledException, InterruptedException, TimeoutException {
		long timeoutMs = timeoutUnit.toMillis(timeout);
		long start = System.currentTimeMillis();
		for (Job job : jobs) {
			while (!job.join(100, null)) {
				if (System.currentTimeMillis() - start > timeoutMs) {
					throw new TimeoutException();
				}
				processEvents();
			}
		}
	}
}