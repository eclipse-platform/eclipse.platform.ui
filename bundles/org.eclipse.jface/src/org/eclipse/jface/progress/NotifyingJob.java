/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.progress;
/**
 * A NotifyingJob is a job that informs a listener when it completes.
 */
import java.util.*;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

public abstract class NotifyingJob extends Job {

	Collection completionListeners = new ArrayList();

	IJobChangeListener listener = new IJobChangeListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
		 */
		public void aboutToRun(Job job) {
				// XXX Auto-generated method stub

	}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#awake(org.eclipse.core.runtime.jobs.Job)
		 */
		public void awake(Job job) {
				// XXX Auto-generated method stub

	}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#running(org.eclipse.core.runtime.jobs.Job)
		 */
		public void running(Job job) {
				// XXX Auto-generated method stub

	}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#scheduled(org.eclipse.core.runtime.jobs.Job)
		 */
		public void scheduled(Job job) {
				// XXX Auto-generated method stub

	}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#sleeping(org.eclipse.core.runtime.jobs.Job)
		 */
		public void sleeping(Job job) {
				// XXX Auto-generated method stub

	}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, org.eclipse.core.runtime.IStatus)
		 */
		public void done(Job job, IStatus result) {
			if (job == NotifyingJob.this) {
				Iterator listeners = completionListeners.iterator();
				while (listeners.hasNext()) {
					IJobCompletionListener listener =
						(IJobCompletionListener) listeners.next();
					listener.finished(result);
				}
			}
		}
	};

	public NotifyingJob() {
		super();
		JobManager.getInstance().addJobChangeListener(this.listener);
	}

	public void addCompletionListener(IJobCompletionListener listener) {
		completionListeners.add(listener);
	}

	public void removeCompletionListener(IJobCompletionListener listener) {
		completionListeners.remove(listener);
	}

}
