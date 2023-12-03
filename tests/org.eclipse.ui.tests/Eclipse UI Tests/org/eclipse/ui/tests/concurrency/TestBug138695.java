/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.concurrency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.progress.UIJob;
import org.junit.Test;

/**
 * @since 3.2
 */
public class TestBug138695 {
	static class SampleJob extends UIJob {

		public SampleJob() {
			super("Sample");
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

	}

	static class SerialPerObjectRule implements ISchedulingRule {

		private Object fObject = null;

		public SerialPerObjectRule(Object lock) {
			fObject = lock;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule instanceof SerialPerObjectRule vup) {
				return fObject == vup.fObject;
			}
			return false;
		}

	}

	@Test
	public void testManyThreads() {
		for (int i = 0; i < 1000; i++) {
			SampleJob job = new SampleJob();
			job.setRule(new SerialPerObjectRule(this));
			job.schedule();
		}

	}

}
