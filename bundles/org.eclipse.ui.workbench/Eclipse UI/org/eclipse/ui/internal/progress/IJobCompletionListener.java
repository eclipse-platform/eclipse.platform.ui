package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;

/**
 * IJobCompletionListener is a class that listens to the result
 * of a job.
 */
public interface IJobCompletionListener {

	/**
	 * The job has finished with a result of status.
	 * @param status
		 */
	public void finished(IStatus status);

	/**
	 * The job never ran for a reason indicated by status.
	 * @param status
	 */
	public void aborted(IStatus status);

}
