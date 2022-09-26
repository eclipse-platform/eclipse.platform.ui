/*******************************************************************************
 * Copyright (c) 2011, 2019 IBM Corporation and others.
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
 *     Martin W. Kirst <martin.kirst@s1998.tu-chemnitz.de> - jUnit test for Bug 361121 [Progress] DetailedProgressViewer's comparator violates its general contract
 *     Red Hat Inc. - Bug 474132
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.JobSnapshot;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class JobInfoTestOrdering {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	/**
	 * Test that checks when jobs sorted by their state, the running ones
	 * are ordered to first place
	 */
	@Test
	public void testJobStateOrdering() {
		List<JobSnapshot> jobinfos = new ArrayList<>();
		int counter = 0;
		TestJob job;
		JobInfo ji;

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.NONE);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(new JobSnapshot(ji));

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.SLEEPING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(new JobSnapshot(ji));

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.WAITING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(new JobSnapshot(ji));

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.RUNNING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(new JobSnapshot(ji));

		jobinfos.sort(null);
		assertEquals(Job.RUNNING, jobinfos.get(0).getState());
		assertEquals(Job.WAITING, jobinfos.get(1).getState());
		assertEquals(Job.SLEEPING, jobinfos.get(2).getState());
		assertEquals(Job.NONE, jobinfos.get(3).getState());
	}

	/**
	 * Test that checks when jobs sorted by their priority, the jobs with highest
	 * priority (lowest numerical value) are ordered to first place
	 */
	@Test
	public void testJobPriorityOrdering() {
		List<JobSnapshot> jobInfos = new ArrayList<>();
		Job job;

		job = new TestJob("TestJob");
		job.setPriority(Job.DECORATE);
		jobInfos.add(new JobSnapshot(new ExtendedJobInfo(job)));

		job = new TestJob("TestJob");
		job.setPriority(Job.BUILD);
		jobInfos.add(new JobSnapshot(new ExtendedJobInfo(job)));

		job = new TestJob("TestJob");
		job.setPriority(Job.LONG);
		jobInfos.add(new JobSnapshot(new ExtendedJobInfo(job)));

		job = new TestJob("TestJob");
		job.setPriority(Job.SHORT);
		jobInfos.add(new JobSnapshot(new ExtendedJobInfo(job)));

		job = new TestJob("TestJob");
		job.setPriority(Job.INTERACTIVE);
		jobInfos.add(new JobSnapshot(new ExtendedJobInfo(job)));

		Collections.shuffle(jobInfos);
		jobInfos.sort(null);
		assertEquals(Job.INTERACTIVE, jobInfos.get(0).getPriority());
		assertEquals(Job.SHORT, jobInfos.get(1).getPriority());
		assertEquals(Job.LONG, jobInfos.get(2).getPriority());
		assertEquals(Job.BUILD, jobInfos.get(3).getPriority());
		assertEquals(Job.DECORATE, jobInfos.get(4).getPriority());
	}
}
