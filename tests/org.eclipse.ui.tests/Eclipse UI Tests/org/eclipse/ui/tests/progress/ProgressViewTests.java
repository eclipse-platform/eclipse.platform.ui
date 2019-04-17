/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.JobTreeElement;
import org.eclipse.ui.internal.progress.ProgressInfoItem;
import org.eclipse.ui.internal.progress.TaskInfo;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @since 3.6
 * @author Prakash G.R.
 *
 */
public class ProgressViewTests extends ProgressTestCase {

	/**
	 * @param testName
	 */
	public ProgressViewTests(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		FinishedJobs.getInstance().clearAll();
	}

	@Override
	protected void doTearDown() throws Exception {
		FinishedJobs.getInstance().clearAll();
		super.doTearDown();
	}


	public void testClearTaskInfo() throws Exception {
		// test for
		openProgressView();

		// run the jobs, hide & show the view
		Job job1 = runDummyJob();
		Job job2 = runDummyJob();

		hideProgressView();
		openProgressView();

		// now check the items in the view. The job should be listed only once
		int count1 = countJobs(job1);
		assertEquals(1, count1);
		int count2 = countJobs(job2);
		assertEquals(1, count2);
	}

	public void testNoUpdatesIfHidden() throws Exception {
		// test for
		openProgressView();
		// minimize progress view to reliably hide it
		IWorkbenchPage activePage = window.getActivePage();
		activePage.setPartState(activePage.getActivePartReference(), IWorkbenchPage.STATE_MINIMIZED);
		processEvents();
		assertFalse("Progress view still visible.", activePage.isPartVisible(progressView));

		// run the jobs, view is hidden
		Job job1 = runDummyJob();
		Job job2 = runDummyJob();

		// make sure we see "throttled" updates too
		processEventsUntil(() -> false, 1000);

		// now check the items in the view. The job should not be listed
		int count1 = countJobs(job1);
		assertEquals(0, count1);
		int count2 = countJobs(job2);
		assertEquals(0, count2);

		openProgressView();

		// we should see "kept" jobs now
		count1 = countJobs(job1);
		assertEquals(1, count1);
		count2 = countJobs(job2);
		assertEquals(1, count2);

		FinishedJobs.getInstance().clearAll();

		// make sure we see "throttled" updates too
		processEventsUntil(() -> false, 1000);

		count1 = countJobs(job1);
		assertEquals(0, count1);
		count2 = countJobs(job2);
		assertEquals(0, count2);
	}

	private int countJobs(Job job) {
		int count = 0;
		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobTreeElement info = progressInfoItem.getInfo();
			if(info instanceof TaskInfo) {
				// if task info then get the parent and check
				JobTreeElement parent = info.getParent();
				if(parent instanceof JobInfo) {
					JobInfo jobInfo = (JobInfo) parent;
					if (containsJob(jobInfo, job)) {
						count++;
					}
				}
			} else {
				JobInfo[] jobInfos = progressInfoItem.getJobInfos();
				for (JobInfo jobInfo : jobInfos) {
					if (containsJob(jobInfo, job)) {
						count++;
					}
				}
			}
		}
		return count;
	}

	protected boolean containsJob(JobInfo jobInfo, Job job) {
		return job.equals(jobInfo.getJob());
	}

	protected Job runDummyJob() throws InterruptedException {
		DummyJob dummyJob = new DummyJob("Dummy Job", Status.OK_STATUS);
		dummyJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);

		dummyJob.schedule();
		processEvents();

		dummyJob.join();
		processEvents();

		return dummyJob;
	}

}
