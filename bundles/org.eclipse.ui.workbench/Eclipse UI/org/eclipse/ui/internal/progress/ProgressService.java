package org.eclipse.ui.internal.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.internal.runtime.jobs.JobManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobListener;
import org.eclipse.core.runtime.jobs.IProgressListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The ProgressService is a mock up of the progress image.
 */
public class ProgressService implements IJobListener,IProgressListener,IProgressService {

	WorkbenchWindow workbenchWindow;

	ProgressContributionItem progressItem =
		new ProgressContributionItem(this, "ProgressContributionItem");

	public ProgressService(WorkbenchWindow window) {
		this.workbenchWindow = window;
		workbenchWindow.getStatusLineManager().add(progressItem);
		JobManager.getInstance().addJobListener(this);
		JobManager.getInstance().addProgressListener(this);
	}

	public IProgressMonitor getProgressMonitor() {
		return progressItem;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnableContext.
	 * Runs the given <code>IRunnableWithProgress</code> with the progress monitor for this
	 * progress dialog.  The dialog is opened before it is run, and closed after it completes.
	 */
	public void run(
		boolean fork,
		boolean cancelable,
		IRunnableWithProgress runnable)
		throws InvocationTargetException, InterruptedException {
		//		setCancelable(cancelable);
		//		open();
		try {
			//			runningRunnables++;

			//Let the progress monitor know if they need to update in UI Thread
			//			progressMonitor.forked = fork;
			ModalContext.run(
				runnable,
				fork,
				this.getProgressMonitor(),
				workbenchWindow.getShell().getDisplay());
		} finally {
			//			runningRunnables--;
			//			close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToResume(org.eclipse.core.runtime.jobs.IJob)
	 */
	public void aboutToResume(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
	 */
	public void aboutToRun(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobListener#adding(org.eclipse.core.runtime.jobs.Job)
	 */
	public void adding(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobListener#finished(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void finished(Job job, int result) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobListener#running(org.eclipse.core.runtime.jobs.Job)
	 */
	public void running(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#beginTask(org.eclipse.core.runtime.jobs.Job, java.lang.String, int)
	 */
	public void beginTask(Job job, String name, int totalWork) {
		getProgressMonitor().beginTask(name,totalWork);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#done(org.eclipse.core.runtime.jobs.Job)
	 */
	public void done(Job job) {
		// XXX Auto-generated method stub
		getProgressMonitor().done();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#setTaskName(org.eclipse.core.runtime.jobs.Job, java.lang.String)
	 */
	public void setTaskName(Job job, String name) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#subTask(org.eclipse.core.runtime.jobs.Job, java.lang.String)
	 */
	public void subTask(Job job, String name) {
		getProgressMonitor().subTask(name);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#worked(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void worked(Job job, int work) {
		getProgressMonitor().worked(work);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToSchedule(org.eclipse.core.runtime.jobs.Job)
	 */
	public void aboutToSchedule(Job job) {
		// XXX Auto-generated method stub

	}
	
	public Object[] getInfos(){
		return progressItem.getStatuses();
	}

}
