/**
 * A NotifyingJob is a job that informs a listener when it completes.
 */
package org.eclipse.jface.progress;

import java.util.*;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobListener;
import org.eclipse.core.runtime.jobs.Job;

public abstract class NotifyingJob extends Job {

	Collection completionListeners = new ArrayList();

	IJobListener listener = new IJobListener() {
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
		JobManager.getInstance().addJobListener(this.listener);
	}

	public void addCompletionListener(IJobCompletionListener listener) {
		completionListeners.add(listener);
	}

	public void removeCompletionListener(IJobCompletionListener listener) {
		completionListeners.remove(listener);
	}

}
