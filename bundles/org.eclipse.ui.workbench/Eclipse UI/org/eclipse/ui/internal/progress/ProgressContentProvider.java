/*
 * Created on May 1, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.internal.progress;

import java.util.Hashtable;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ProgressContentProvider
	implements ITreeContentProvider, IJobListener, IProgressListener {

	private Hashtable jobs = new Hashtable();

	public ProgressContentProvider() {
		JobManager.getInstance().addJobListener(this);
		JobManager.getInstance().addProgressListener(this);

	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return ((JobInfo) parentElement).getChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return ((JobInfo) element).getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return ((JobInfo) element).hasChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return jobs.values().toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
	 */
	public void aboutToRun(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToSchedule(org.eclipse.core.runtime.jobs.Job)
	 */
	public void aboutToSchedule(Job job) {
		jobs.put(job, new JobInfo(job.toString()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void finished(Job job, IStatus result) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#paused(org.eclipse.core.runtime.jobs.Job)
	 */
	public void paused(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#running(org.eclipse.core.runtime.jobs.Job)
	 */
	public void running(Job job) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#beginTask(org.eclipse.core.runtime.jobs.Job, java.lang.String, int)
	 */
	public void beginTask(Job job, String name, int totalWork) {
		getInfo(job).addToLeafChild(new JobInfoWithProgress(name, totalWork));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#done(org.eclipse.core.runtime.jobs.Job)
	 */
	public void done(Job job) {
		jobs.remove(job);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#setTaskName(org.eclipse.core.runtime.jobs.Job, java.lang.String)
	 */
	public void setTaskName(Job job, String name) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#subTask(org.eclipse.core.runtime.jobs.Job, java.lang.String)
	 */
	public void subTask(Job job, String name) {
		getInfo(job).addToLeafChild(new JobInfo(name));

	}
	private JobInfo getInfo(Job job) {
		return ((JobInfo) jobs.get(job));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#worked(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void worked(Job job, int work) {
		getInfo(job).addWork(work);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobListener#resumed(org.eclipse.core.runtime.jobs.Job)
	 */
	public void resumed(Job job) {
		// XXX Auto-generated method stub

	}

}
