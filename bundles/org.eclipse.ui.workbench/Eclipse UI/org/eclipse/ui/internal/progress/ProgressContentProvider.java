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
package org.eclipse.ui.internal.progress;

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.viewers.*;

/**
 * The ProgressContentProvider is the content provider used for
 * classes that listen to the progress changes.
 */
public class ProgressContentProvider implements ITreeContentProvider {

	private Map jobs = Collections.synchronizedMap(new HashMap());
	IJobChangeListener listener;
	private TreeViewer viewer;

	public ProgressContentProvider(TreeViewer mainViewer) {
		listener = new JobChangeAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void scheduled(IJobChangeEvent event) {
				if (!isNonDisplayableJob(event.getJob())) {
					jobs.put(event.getJob(), new JobInfo(event.getJob()));
					refreshViewer(null);
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void aboutToRun(IJobChangeEvent event) {
				if (!isNonDisplayableJob(event.getJob())) {
					JobInfo info = getJobInfo(event.getJob());
					info.setRunning();
					refreshViewer(null);
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				if (!isNonDisplayableJob(event.getJob())) {
					if (event.getResult().getCode() == IStatus.ERROR) {
						JobInfo info = getJobInfo(event.getJob());
						info.setError(event.getResult());
					} else {
						jobs.remove(event.getJob());
					}
					refreshViewer(null);
				}

			}

		};
		Platform.getJobManager().addJobChangeListener(listener);
		viewer = mainViewer;
		JobProgressManager.getInstance().addProvider(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return ((JobTreeElement) parentElement).getChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return ((JobTreeElement) element).getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return ((JobTreeElement) element).hasChildren();
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
		Platform.getJobManager().removeJobChangeListener(listener);
		JobProgressManager.getInstance().removeProvider(this);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * A task has begun on job so create a new JobInfo for it.
	 * @param job
	 * @param taskName
	 * @param totalWork
	 */
	public void beginTask(Job job, String taskName, int totalWork) {
		if (isNonDisplayableJob(job))
			return;
		JobInfo info = getJobInfo(job);
		info.beginTask(taskName, totalWork);

		refreshViewer(null);
	}

	/**
	 * Get the JobInfo for the job. If it does not exist
	 * create it.
	 * @param job
	 * @return
	 */
	private JobInfo getJobInfo(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info == null) {
			info = new JobInfo(job);
			jobs.put(job, info);
		}
		return info;
	}
	/**
	 * Return whether or not this job is displayable.
	 * @param job
	 * @return
	 */
	private boolean isNonDisplayableJob(Job job) {
		return job == null || job.isSystem();
	}
	/**
	 * Reset the name of the task to task name.
	 * @param job
	 * @param taskName
	 * @param totalWork
	 */
	public void setTaskName(Job job, String taskName, int totalWork) {
		if (isNonDisplayableJob(job))
			return;

		JobInfo info = getJobInfo(job);
		if (info.taskInfo == null) {
			beginTask(job, taskName, totalWork);
			return;
		} else
			info.taskInfo.setTaskName(taskName);

		info.clear();
		refreshViewer(info);
	}

	/**
	 * Create a new subtask on jg
	 * @param job
	 * @param name
	 */
	public void subTask(Job job, String name) {
		if (isNonDisplayableJob(job))
			return;
		if (name.length() == 0)
			return;
		JobInfo info = getJobInfo(job);

		info.clear();
		info.addSubTask(name);
		refreshViewer(info);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#worked(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void worked(Job job, double work) {
		if (isNonDisplayableJob(job))
			return;

		JobInfo info = getJobInfo(job);
		if (info.taskInfo != null) {
			info.addWork(work);
			refreshViewer(info);
		}
	}

	/**
	 * Refresh the viewer as a result of a change in info.
	 * @param info
	 */
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

	/**
	 * Clear the job out of the list of those being displayed.
	 * Only do this for jobs that are an error.
	 * @param job
	 */
	void clearJob(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info != null && info.status.getCode() == IStatus.ERROR) {
			jobs.remove(job);
			viewer.refresh(null);
		}
	}
}
