/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
 *     Manumitting Technologies - bug 394036
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 *
 * A job that completes very quickly and returns the specified status
 *
 * @since 3.6
 * @author Prakash G.R. (grprakash@in.ibm.com)
 */
public class DummyJob extends Job {

	private final IStatus status;

	public boolean inProgress = false;
	/** if false, infinite until changed or job is cancelled */
	public volatile boolean shouldFinish = true;

	public DummyJob(String name, IStatus status) {
		super(name);
		this.status = status;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		inProgress = true; // spare us from registering a job change listener
		monitor.beginTask(getName() + " starts now", 10);
		try {
			for (int i = 0; i < 10 || !shouldFinish; i++) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignore
				}
				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
			return status;
		} finally {
			monitor.done();
		}
	}

}
