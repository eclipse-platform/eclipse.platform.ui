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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.JobTreeElement;
import org.eclipse.ui.internal.progress.ProgressInfoItem;
import org.eclipse.ui.internal.progress.TaskInfo;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.tests.TestPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProgressViewTests extends ProgressTestCase {

	@Override
	@Before
	public void doSetUp() throws Exception {
		super.doSetUp();
		FinishedJobs.getInstance().clearAll();
	}

	@Override
	@After
	public void doTearDown() throws Exception {
		FinishedJobs.getInstance().clearAll();
		super.doTearDown();
	}


	@Test
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

	@Test
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

	@Test
	public void testItemOrder() throws Exception {
		openProgressView();
		ArrayList<DummyJob> jobsToSchedule = new ArrayList<>();
		ArrayList<DummyJob> allJobs = new ArrayList<>();

		DummyJob userJob = new DummyJob("1. User Job", Status.OK_STATUS);
		userJob.setUser(true);
		jobsToSchedule.add(userJob);
		DummyJob highPrioJob = new DummyJob("2. High Priority Job", Status.OK_STATUS);
		highPrioJob.setPriority(Job.INTERACTIVE);
		jobsToSchedule.add(highPrioJob);
		DummyJob job1 = new DummyJob("3. Usual job 1", Status.OK_STATUS);
		jobsToSchedule.add(job1);
		DummyJob job2 = new DummyJob("4. Usual job 2", Status.OK_STATUS);
		jobsToSchedule.add(job2);
		DummyJob job3 = new DummyJob("5. Usual job 3", Status.OK_STATUS);
		jobsToSchedule.add(job3);
		DummyJob lowPrioJob = new DummyJob("6. Low Priority Job", Status.OK_STATUS);
		lowPrioJob.setPriority(Job.DECORATE);
		jobsToSchedule.add(lowPrioJob);

		allJobs.addAll(jobsToSchedule);
//		TODO Disabled due to other progress viewer bugs.
//		DummyJob sleepJob = new DummyJob("7. Not yet started Job", Status.OK_STATUS);
//		sleepJob.schedule(TimeUnit.MINUTES.toMillis(2));
//		allJobs.add(sleepJob);
//		DummyJob keptJob = new DummyJob("8. Finished and kept Job", Status.OK_STATUS);
//		keptJob.setProperty(IProgressConstants.KEEP_PROPERTY, true);
//		keptJob.schedule();
//		allJobs.add(keptJob);

		try {
			ArrayList<DummyJob> shuffledJobs = new ArrayList<>(jobsToSchedule);
			Collections.shuffle(shuffledJobs);
			StringBuilder scheduleOrder = new StringBuilder("Jobs schedule order: ");
			progressView.getViewer().refresh(); // order will only hold on the first time.
			Thread.sleep(200); // wait till throttled update ran.
			Job dummyJob = new Job("dummy throttled caller") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					return Status.OK_STATUS;
				}
			};
			dummyJob.schedule(); // trigger throttled update to clear ProgressViewerComparator.lastIndexes
			// now hope the loop is executed before next throttled update (could fail if VM
			// is busy otherwise):
			for (DummyJob job : shuffledJobs) {
				job.shouldFinish = false;
				job.schedule(); // if the schedule updates the progress View (throttled) the sort order is
								// affected
				scheduleOrder.append(job.getName()).append(", ");
			}
			TestPlugin.getDefault().getLog()
					.log(new Status(IStatus.OK, TestPlugin.PLUGIN_ID, scheduleOrder.toString()));

			for (DummyJob job : allJobs) {
				processEventsUntil(() -> job.inProgress, TimeUnit.SECONDS.toMillis(3));
			}
			progressView.getViewer().refresh();
			processEventsUntil(() -> progressView.getViewer().getProgressInfoItems().length == allJobs.size(),
					TimeUnit.SECONDS.toMillis(5));

			ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
			assertEquals("Not all jobs visible in progress view", allJobs.size(), progressInfoItems.length);
			Object[] expected = allJobs.toArray();
			Object[] actual = Arrays.stream(progressInfoItems).map(pi -> pi.getJobInfos()[0].getJob()).toArray();
			assertArrayEquals("Wrong job order", expected, actual);
		} finally {
			for (DummyJob job : jobsToSchedule) {
				job.shouldFinish = true;
			}
		}
	}

	private int countJobs(Job job) {
		int count = 0;
		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobTreeElement info = progressInfoItem.getInfo();
			if(info instanceof TaskInfo) {
				// if task info then get the parent and check
				JobTreeElement parent = info.getParent();
				if (parent instanceof JobInfo jobInfo) {
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
