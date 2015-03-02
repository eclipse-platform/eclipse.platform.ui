/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin W. Kirst <martin.kirst@s1998.tu-chemnitz.de> - jUnit test for Bug 361121 [Progress] DetailedProgressViewer's comparator violates its general contract
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.JobInfo;

public class JobInfoTestOrdering extends TestCase {

	private List jobinfos = new ArrayList();

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	protected void setUp() throws Exception {
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
	public void testJobStateOrdering() {
		Collections.sort(jobinfos);
		assertEquals(Job.RUNNING,  ((JobInfo)jobinfos.get(0)).getJob().getState());
		assertEquals(Job.WAITING,  ((JobInfo)jobinfos.get(1)).getJob().getState());
		assertEquals(Job.SLEEPING, ((JobInfo)jobinfos.get(2)).getJob().getState());
		assertEquals(Job.NONE,     ((JobInfo)jobinfos.get(3)).getJob().getState());
	}

}
