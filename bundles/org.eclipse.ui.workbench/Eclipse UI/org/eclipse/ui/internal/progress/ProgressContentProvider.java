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
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.*;

/**
 * The ProgressContentProvider is the content provider used for
 * classes that listen to the progress changes.
 */
public class ProgressContentProvider
	implements ITreeContentProvider, IJobChangeListener {

	private Map jobs = Collections.synchronizedMap(new HashMap());

	private TreeViewer viewer;

	public ProgressContentProvider(TreeViewer mainViewer) {
		Platform.getJobManager().addJobChangeListener(this);
		viewer = mainViewer;
		JobProgressManager.getInstance().addProvider(this);
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
		Platform.getJobManager().removeJobChangeListener(this);
		JobProgressManager.getInstance().removeProvider(this);

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
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#subTask(org.eclipse.core.runtime.jobs.Job, java.lang.String)
	 */
	public void subTask(Job job, String name) {
		if (job == null)
			return;

		if (job instanceof AnimateJob)
			return;
		if (name.length() == 0)
			return;
		JobInfo info = getInfo(job);
		
		if(info == null)
			return;
			
		info.clearChildren();
		info.addChild(new JobInfo(name));
		refreshViewer(info);

	}

	/**
	 * Get the JobInfo currently being collected for job.
	 * @param job
	 * @return
	 */
	private JobInfo getInfo(Job job) {
		return ((JobInfo) jobs.get(job));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressListener#worked(org.eclipse.core.runtime.jobs.Job, int)
	 */
	public void worked(Job job, double work) {
		if (job == null)
			return;

		if (job instanceof AnimateJob)
			return;
		JobInfo info = getInfo(job);
		info.addWork(work);
		refreshViewer(info);
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.Job, org.eclipse.core.runtime.IStatus)
	 */
	public void done(Job job, IStatus result) {
		jobs.remove(job);
		refreshViewer(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
	 */
	public void aboutToRun(Job job) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.Job)
	 */
	public void awake(Job job) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.Job)
	 */
	public void running(Job job) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.Job)
	 */
	public void scheduled(Job job) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.Job)
	 */
	public void sleeping(Job job) {

	}

}
