/*
 * Created on May 1, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.internal.progress;

import java.util.*;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.viewers.*;

public class ProgressContentProvider
	implements ITreeContentProvider, IJobListener, IProgressListener {

	private Map jobs = Collections.synchronizedMap(new HashMap());

	private TreeViewer viewer;

	public ProgressContentProvider(TreeViewer mainViewer) {
		JobManager.getInstance().addJobListener(this);
		JobManager.getInstance().addProgressListener(this);
		viewer = mainViewer;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#beginTask(org.eclipse.core.runtime.jobs.Job, java.lang.String, int)
	 */
	public void beginTask(Job job, String name, int totalWork) {
		if (job == null || job instanceof AnimateJob)
			return;
		if (totalWork == IProgressMonitor.UNKNOWN)
			jobs.put(job, new JobInfo(name));
		else
			jobs.put(job, new JobInfoWithProgress(name, totalWork));
		refreshViewer(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#done(org.eclipse.core.runtime.jobs.Job)
	 */
	public void done(Job job) {

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
		if(job == null)
			return;
			
		if (job instanceof AnimateJob)
			return;
		if (name.length() == 0)
			return;
		JobInfo info = getInfo(job);
		info.clearChildren();
		info.addChild(new JobInfo(name));
		refreshViewer(info);

	}
	private JobInfo getInfo(Job job) {
		return ((JobInfo) jobs.get(job));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#worked(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void worked(Job job, double work) {
		if(job == null)
			return;
			
		if (job instanceof AnimateJob)
			return;
		JobInfo info = getInfo(job);
		info.addWork(work);
		refreshViewer(info);
	}

	private void refreshViewer(final JobInfo info) {
		if (viewer.getControl().isDisposed())
			return;

		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if (viewer.getControl().isDisposed())
					return;
				viewer.refresh(info);
			}
		});
	}

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
	 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, org.eclipse.core.runtime.IStatus)
	 */
	public void done(Job job, IStatus result) {
		jobs.remove(job);
		refreshViewer(null);

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

}
