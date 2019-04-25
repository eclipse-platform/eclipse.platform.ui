/*******************************************************************************
 * Copyright (c) 2011, 2017 IBM Corporation and others.
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
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class JobInfoTestOrdering {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	private List<JobInfo> jobinfos = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		jobinfos.clear();
		int counter = 0;
		TestJob job;
		JobInfo ji;

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.NONE);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.SLEEPING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.WAITING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(true);
		job.setSystem(false);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(Job.RUNNING);  // JOB STATE
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

	}

	/**
	 * Test that checks when jobs sorted by their state, the running ones
	 * are ordered to first place
	 */
	@Test
	public void testJobStateOrdering() {
		Collections.sort(jobinfos);
		assertEquals(Job.RUNNING,  jobinfos.get(0).getJob().getState());
		assertEquals(Job.WAITING,  jobinfos.get(1).getJob().getState());
		assertEquals(Job.SLEEPING, jobinfos.get(2).getJob().getState());
		assertEquals(Job.NONE,     jobinfos.get(3).getJob().getState());
	}

}
