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

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.internal.progress.ProgressMessages;

/**
 * The ProgressContentProvider is the content provider used for
 * classes that listen to the progress changes.
 */
public class ProgressContentProvider implements ITreeContentProvider {

	private Map jobs = Collections.synchronizedMap(new HashMap());
	IJobChangeListener listener;
	private TreeViewer viewer;
	private List updates = Collections.synchronizedList(new ArrayList());
	private boolean updateAll = false;
	private Job updateJob;

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
	public void inputChanged(Viewer updateViewer, Object oldInput, Object newInput) {
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
		refreshViewer(info);
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
		if (info.hasTaskInfo()) 
			info.setTaskName(taskName);
		else{
			beginTask(job, taskName, totalWork);
			return;
		}

		info.clearChildren();
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

		info.clearChildren();
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
		if (info.hasTaskInfo()) {
			info.addWork(work);
			refreshViewer(info);
		}
	}

	/**
	 * Refresh the viewer as a result of a change in info.
	 * @param info
	 */
	private void refreshViewer(final JobInfo info) {
		
		if(updateJob == null)
			createUpdateJob();

		if(info == null)
			updateAll = true;
		else
			updates.add(info);
			
		//Add in a 100ms delay so as to keep priority low
		updateJob.schedule(100);			
	}

	/**
	 * Clear the job out of the list of those being displayed.
	 * Only do this for jobs that are an error.
	 * @param job
	 */
	void clearJob(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info != null && info.getStatus().getCode() == IStatus.ERROR) {
			jobs.remove(job);
			viewer.refresh(null);
		}
	}
	
	private void createUpdateJob(){
		updateJob = new UIJob(ProgressMessages.getString("ProgressContentProvider.UpdateProgressJob")){ //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if(updateAll){
					viewer.refresh(null,true);
					updateAll = false;
					updates.clear();
				}
				else{
					Object[] updateItems = updates.toArray();
					updates.clear();
					for(int i = 0; i < updateItems.length; i++){
						viewer.refresh(updateItems[i],true);
					}
				}
				return Status.OK_STATUS;
					
			}
			
		};
		updateJob.setSystem(true);
		updateJob.setPriority(Job.DECORATE);
		
	}
}
