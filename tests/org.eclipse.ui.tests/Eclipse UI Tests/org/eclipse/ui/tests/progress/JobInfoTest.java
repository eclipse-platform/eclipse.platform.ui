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
 *     Red Hat Inc. - Bug 474132
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.JobInfo;
import org.junit.Before;
import org.junit.Test;

public class JobInfoTest {


	/**
	 * @see org.eclipse.core.internal.jobs.InternalJob
	 */
	static final int ABOUT_TO_RUN = 0x10;
	/**
	 * @see org.eclipse.core.internal.jobs.InternalJob
	 */
	static final int ABOUT_TO_SCHEDULE = 0x20;
	/**
	 * @see org.eclipse.core.internal.jobs.InternalJob
	 */
	static final int BLOCKED = 0x08;
	/**
	 * @see org.eclipse.core.internal.jobs.InternalJob
	 */
	static final int YIELDING = 0x40;

	private List jobinfos = new ArrayList();

	@Before
	public void setUp() throws Exception {
		int counter = 0;
		counter = createAndAddJobInfos(false, false, ABOUT_TO_RUN, counter);
		counter = createAndAddJobInfos(false, true,  ABOUT_TO_RUN, counter);
		counter = createAndAddJobInfos(true, false,  ABOUT_TO_RUN, counter);
		counter = createAndAddJobInfos(true, true,  ABOUT_TO_RUN, counter);

		counter = createAndAddJobInfos(false, false, ABOUT_TO_SCHEDULE, counter);
		counter = createAndAddJobInfos(false, true, ABOUT_TO_SCHEDULE, counter);
		counter = createAndAddJobInfos(true, false, ABOUT_TO_SCHEDULE, counter);
		counter = createAndAddJobInfos(true, true, ABOUT_TO_SCHEDULE, counter);

		counter = createAndAddJobInfos(false, false, Job.SLEEPING, counter);
		counter = createAndAddJobInfos(false, true, Job.SLEEPING, counter);
		counter = createAndAddJobInfos(true, false, Job.SLEEPING, counter);
		counter = createAndAddJobInfos(true, true, Job.SLEEPING, counter);

		counter = createAndAddJobInfos(false, false, Job.WAITING, counter);
		counter = createAndAddJobInfos(false, true, Job.WAITING, counter);
		counter = createAndAddJobInfos(true, false, Job.WAITING, counter);
		counter = createAndAddJobInfos(true, true, Job.WAITING, counter);

		counter = createAndAddJobInfos(false, false, Job.RUNNING, counter);
		counter = createAndAddJobInfos(false, true, Job.RUNNING, counter);
		counter = createAndAddJobInfos(true, false, Job.RUNNING, counter);
		counter = createAndAddJobInfos(true, true, Job.RUNNING, counter);

	}

	/**
	 * Test that {@link org.eclipse.ui.internal.progress.JobInfo#compareTo(Object)}
	 * is valid implemented and complies to the interface method contract.
	 */
	@Test
	public void testCompareToContractCompliance() {
		for(int xi = 0; xi<this.jobinfos.size(); xi++) {
			JobInfo x = (JobInfo) jobinfos.get(xi);

			for(int yi = 0; yi<this.jobinfos.size(); yi++) {
				JobInfo y = (JobInfo) jobinfos.get(yi);
				int xyResult = x.compareTo(y);
				int yxResult = y.compareTo(x);
				// sgn(compare(x, y)) == -sgn(compare(y, x)) for all x and y.
				assertEquals(String.format("sgn(compare(%1$s, %2$s)) != -sgn(compare(%2$s, %1$s))", new Object[] { x, y}),
						Math.round(Math.signum(xyResult)) , Math.round(-Math.signum(yxResult)));

				for(int zi = 0; zi<this.jobinfos.size(); zi++) {
					JobInfo z = (JobInfo) jobinfos.get(zi);
					int xzResult = x.compareTo(z);
					int yzResult = y.compareTo(z);
					// ((compare(x, y)>0) && (compare(y, z)>0)) implies compare(x, z)>0.
					if(xyResult > 0) {
						if(yzResult > 0) {
							assertTrue(String.format("((compare(%1$s, %2$s)>0) && (compare(%2$s, %3$s)>0)) but not compare(%1$s, %3$s)>0", new Object[] {x, y, z}),
									xzResult > 0);
						}
					}
					else if(xyResult == 0) {
						// compare(x, y)==0 implies that sgn(compare(x, z))==sgn(compare(y, z)) for all z.
						assertEquals(String.format("compare(%1$s, %2$s)==0 but not that sgn(compare(%1$s, %3$s))==sgn(compare(%2$s, %3$s))", new Object[]{ x, y, z}),
								Math.round(Math.signum(xzResult)) , Math.round(Math.signum(yzResult)));
					}
				}

				boolean consistentWithEquals = true;
				// Optionally (compare(x, y)==0) == (x.equals(y))
				if(consistentWithEquals && xyResult == 0) {
					assertTrue(String.format("compare(%1$s, %2$s)==0) == (%1$s.equals(%2$s)", new Object[] {x, y}),
							x.equals(y));
				}
			}
		}
	}

	/**
	 * @param user
	 * @param system
	 * @param jobstate
	 * @param counter
	 * @return
	 */
	private int createAndAddJobInfos(boolean user, boolean system, int jobstate, int counter) {
		TestJob job;
		JobInfo ji;

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.INTERACTIVE);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.SHORT);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.LONG);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.BUILD);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.DECORATE);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		job = new TestJob("Job" + (counter++));
		job.setUser(user);
		job.setSystem(system);
		job.setPriority(Job.LONG);
		job.setInternalJobState(jobstate);
		ji = new ExtendedJobInfo(job);
		jobinfos.add(ji);

		return counter;
	}

}
