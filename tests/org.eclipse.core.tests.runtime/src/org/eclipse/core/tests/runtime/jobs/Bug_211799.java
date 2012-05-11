/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Regression test for bug 211799
 */
public class Bug_211799 extends AbstractJobManagerTest {

	public class BugJob extends Job {
		private long id;

		public BugJob(long id) {
			super("Test Job"); //$NON-NLS-1$
			this.id = id;
			setRule(rule);
			setPriority(Job.DECORATE);
		}

		public Long getId() {
			return new Long(this.id);
		}

		protected IStatus run(IProgressMonitor monitor) {
			synchronized (list) {
				Long val = (Long) list.getFirst();
				if (val.longValue() != id) 
					failure = new RuntimeException("We broke, running should have been: " + val.longValue());
				list.remove(new Long(id));
			}

			synchronized (runList) {
				runList.add(new Long(id));
			}

			return Status.OK_STATUS;
		}
	}
	static final ISchedulingRule rule = new IdentityRule();
	long counter = 0;
	Exception failure = null;
	final int JOBS_TO_SCHEDULE = 500;
	LinkedList list = new LinkedList();
	
	List runList = new ArrayList(JOBS_TO_SCHEDULE);

	public void testBug() {
		for (int i = 0; i < JOBS_TO_SCHEDULE; i++) {
			synchronized (list) {
				counter++;
				list.addLast(new Long(counter));
				new BugJob(counter).schedule(0L);
			}
		}

		// Wait until all the jobs are done
		while (true) {
			synchronized (runList) {
				if (runList.size() == JOBS_TO_SCHEDULE) {
					break;
				}
			}
		}
		if (failure != null)
			fail("1.0", failure);

	}
}
