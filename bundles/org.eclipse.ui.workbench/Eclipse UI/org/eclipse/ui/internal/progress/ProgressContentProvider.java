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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.progress.UIJob;

/**
 * The ProgressContentProvider is the content provider used for
 * classes that listen to the progress changes.
 */
public class ProgressContentProvider implements ITreeContentProvider {

	/**
	 * The UpdatesInfo is a private class for keeping track of the
	 * updates required.
	 */
	private class UpdatesInfo {

		Collection additions = new HashSet();
		Collection deletions = new HashSet();
		Collection refreshes = new HashSet();
		boolean updateAll = false;

		private UpdatesInfo() {
		}

		/**
		 * Add an add update
		 * @param addition
		 */
		void add(JobInfo addition) {
			additions.add(addition);
		}

		/**
		 * Add a remove update
		 * @param addition
		 */
		void remove(JobInfo removal) {
			deletions.add(removal);
		}
		/**
		 * Add a refresh update
		 * @param addition
		 */
		void refresh(JobInfo refresh) {
			refreshes.add(refresh);
		}
		/**
		 * Reset the caches after completion of an update.
		 */
		void reset() {
			additions.clear();
			deletions.clear();
			refreshes.clear();
		}
	}

	private TreeViewer viewer;
	private Job updateJob;
	UpdatesInfo currentInfo = new UpdatesInfo();

	public ProgressContentProvider(TreeViewer mainViewer) {
		viewer = mainViewer;
		JobProgressManager.getInstance().addProvider(this);
		createUpdateJob();
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
		return JobProgressManager.getInstance().getJobs();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		JobProgressManager.getInstance().removeProvider(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(
		Viewer updateViewer,
		Object oldInput,
		Object newInput) {
	}
	/**
	 * Refresh the viewer as a result of a change in info.
	 * @param info
	 */
	void refresh(final JobInfo info) {

		currentInfo.refresh(info);
		//Add in a 100ms delay so as to keep priority low
		updateJob.schedule(100);
	}

	/**
	 * Refresh the viewer for all jobs.
	 * @param info
	 */
	void refreshAll() {
		currentInfo.updateAll = true;

		//Add in a 100ms delay so as to keep priority low
		updateJob.schedule(100);
	}

	/**
	 * Refresh the viewer as a result of an addition of info.
	 * @param info
	 */
	void add(final JobInfo info) {
		
		currentInfo.add(info);
		updateJob.schedule(100);
	}

	/**
	 * Refresh the viewer as a result of a removal of info.
	 * @param info
	 */
	void remove(final JobInfo info) {
		
		currentInfo.remove(info);
		updateJob.schedule(100);
	}

	/**
	 * Create the update job that handles the updatesInfo.
	 */
	private void createUpdateJob() {
			updateJob = new UIJob(ProgressMessages.getString("ProgressContentProvider.UpdateProgressJob")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {

				if (viewer.getControl().isDisposed())
					return Status.CANCEL_STATUS;

				if (currentInfo.updateAll)
					viewer.refresh(true);
				else {
					Object[] updateItems = currentInfo.refreshes.toArray();
					for (int i = 0; i < updateItems.length; i++) {
						viewer.refresh(updateItems[i], true);
					}
					viewer.add(
						viewer.getInput(),
						currentInfo.additions.toArray());

					viewer.remove(currentInfo.deletions.toArray());
				}
				currentInfo.reset();
				return Status.OK_STATUS;

			}

		};
		updateJob.setSystem(true);
		updateJob.setPriority(Job.DECORATE);

	}
}
