/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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


	public void testClearTaskInfo() throws Exception {

		// test for
		openProgressView();

		// run the jobs, hide & show the view
		Job job1 = runDummyJob();
		Job job2 = runDummyJob();

		hideProgressView();
		openProgressView();

		// now check the items in the view. The job should be listed only once
		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		boolean job1Found = false;
		boolean job2Found = false;
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobTreeElement info = progressInfoItem.getInfo();
			if(info instanceof TaskInfo) {
				// if task info then get the parent and check
				Object parent = info.getParent();
				if(parent instanceof JobInfo) {
					JobInfo jobInfo = (JobInfo) parent;
					job1Found = checkJob(job1, job1Found, jobInfo);
					job2Found = checkJob(job2, job2Found, jobInfo);
				}
			}else {
				JobInfo[] jobInfos = progressInfoItem.getJobInfos();
				for (JobInfo jobInfo : jobInfos) {
					job1Found = checkJob(job1, job1Found, jobInfo);
					job2Found = checkJob(job2, job2Found, jobInfo);
				}
			}
		}
	}

	protected boolean checkJob(Job job, boolean found, JobInfo jobInfo) {
		if(job.equals(jobInfo.getJob())) {
			if(found) {
				fail("The job is listed twice");
			} else {
				found = true;
			}
		}
		return found;
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
