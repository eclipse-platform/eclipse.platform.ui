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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ProgressViewUpdater is the singleton that updates viewers.
 */
class ProgressViewUpdater implements IJobProgressManagerListener {

	private static ProgressViewUpdater singleton;
	private ProgressContentProvider[] contentProviders;

	Job updateJob;
	UpdatesInfo currentInfo = new UpdatesInfo();
	Object updateLock = new Object();
	boolean debug = false;

	/**
	 * The UpdatesInfo is a private class for keeping track of the updates
	 * required.
	 */
	class UpdatesInfo {

		Collection additions = new HashSet();
		Collection deletions = new HashSet();
		Collection refreshes = new HashSet();
		boolean updateAll = false;

		private UpdatesInfo() {
			//Create a new instance of the info
		}

		/**
		 * Add an add update
		 * 
		 * @param addition
		 */
		void add(JobInfo addition) {
			additions.add(addition);
		}

		/**
		 * Add a remove update
		 * 
		 * @param addition
		 */
		void remove(JobInfo removal) {
			deletions.add(removal);
		}
		/**
		 * Add a refresh update
		 * 
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

		void processForUpdate() {
			HashSet staleAdditions = new HashSet();

			Iterator additionsIterator = additions.iterator();
			while (additionsIterator.hasNext()) {
				JobInfo next = (JobInfo) additionsIterator.next();
				if (deletions.contains(next) || next.getJob().getState() == Job.NONE)
					staleAdditions.add(next);
			}

			additions.removeAll(staleAdditions);

			HashSet obsoleteRefresh = new HashSet();
			Iterator refreshIterator = refreshes.iterator();
			while (refreshIterator.hasNext()) {
				JobInfo next = (JobInfo) refreshIterator.next();
				if (deletions.contains(next) || additions.contains(next))
					obsoleteRefresh.add(next);
				if (next.getJob().getState() == Job.NONE) {
					//If it is done then delete it
					obsoleteRefresh.add(next);
					deletions.add(next);
				}
			}

			refreshes.removeAll(obsoleteRefresh);

		}
	}

	/**
	 * Return a new instance of the receiver.
	 * 
	 * @return
	 */
	static ProgressViewUpdater getSingleton() {
		if (singleton == null)
			singleton = new ProgressViewUpdater();
		return singleton;
	}
	
	/**
	 * Return whether or not there is a singleton for updates
	 * to avoid creating extra listeners.
	 * @return
	 */
	static boolean hasSingleton(){
		return singleton != null;
	}

	static void clearSingleton() {
		if (singleton != null)
			ProgressManager.getInstance().removeListener(singleton);
		singleton = null;
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @return
	 */
	private ProgressViewUpdater() {
		createUpdateJob();
		contentProviders = new ProgressContentProvider[0];
		ProgressManager.getInstance().addListener(this);
		
	}

	/**
	 * Add the new provider to the list of content providers.
	 * 
	 * @param newProvider
	 */
	void addContentProvider(ProgressContentProvider newProvider) {
		ProgressContentProvider[] newProviders = new ProgressContentProvider[contentProviders.length + 1];
		System.arraycopy(contentProviders, 0, newProviders, 0, contentProviders.length);
		newProviders[contentProviders.length] = newProvider;
		contentProviders = newProviders;
	}

	/**
	 * Remove the provider from the list of content providers.
	 * 
	 * @param provider
	 */
	void removeContentProvider(ProgressContentProvider provider) {
		HashSet newProviders = new HashSet();
		for (int i = 0; i < contentProviders.length; i++) {
			if (!contentProviders[i].equals(provider))
				newProviders.add(contentProviders[i]);
		}
		ProgressContentProvider[] newArray = new ProgressContentProvider[newProviders.size()];
		newProviders.toArray(newArray);
		contentProviders = newArray;
		//Remove ourselves if there is nothing to update
		if (contentProviders.length == 0)
			clearSingleton();
	}

	/**
	 * Schedule an update.
	 */
	void scheduleUpdate() {
		if(PlatformUI.isWorkbenchRunning()){
			//Add in a 100ms delay so as to keep priority low
			updateJob.schedule(100);
		}
	}

	/**
	 * Create the update job that handles the updatesInfo.
	 */
	private void createUpdateJob() {
			updateJob = new WorkbenchJob(ProgressMessages.getString("ProgressContentProvider.UpdateProgressJob")) {//$NON-NLS-1$
	/*
	 * (non-Javadoc) @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
			public IStatus runInUIThread(IProgressMonitor monitor) {

					//Abort the job if there isn't anything
				if (contentProviders.length == 0)
					return Status.CANCEL_STATUS;

				if (currentInfo.updateAll) {
					for (int i = 0; i < contentProviders.length; i++) {
						contentProviders[i].viewer.refresh(true);
					}

				} else {
					//Lock while getting local copies of the caches.
					Object[] updateItems;
					Object[] additionItems;
					Object[] deletionItems;
					synchronized (updateLock) {
						currentInfo.processForUpdate();

						updateItems = currentInfo.refreshes.toArray();
						additionItems = currentInfo.additions.toArray();
						deletionItems = currentInfo.deletions.toArray();

					}

					for (int v = 0; v < contentProviders.length; v++) {
						ProgressTreeViewer viewer = contentProviders[v].viewer;

						for (int i = 0; i < updateItems.length; i++) {
							viewer.refresh(updateItems[i], true);
						}
						viewer.add(viewer.getInput(), additionItems);

						viewer.remove(deletionItems);
					}
				}

				synchronized (updateLock) {
					currentInfo.reset();
				}

				return Status.OK_STATUS;

			}

		};
		updateJob.setSystem(true);
		updateJob.setPriority(Job.DECORATE);

	}
	/**
	 * Get the updates info that we are using in the receiver.
	 * 
	 * @return Returns the currentInfo.
	 */
	UpdatesInfo getCurrentInfo() {
		return currentInfo;
	}
	/*
	 * (non-Javadoc) @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refresh(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void refresh(JobInfo info) {

		if (isUpdateJob(info.getJob()))
			return;

		synchronized (updateLock) {
			currentInfo.refresh(info);
		}
		//Add in a 100ms delay so as to keep priority low
		scheduleUpdate();

	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshAll()
	 */
	public void refreshAll() {

		synchronized (updateLock) {
			currentInfo.updateAll = true;
		}

		//Add in a 100ms delay so as to keep priority low
		scheduleUpdate();

	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#add(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void add(JobInfo info) {

		if (isUpdateJob(info.getJob()))
			return;
		
		synchronized (updateLock) {
			currentInfo.add(info);
		}
		scheduleUpdate();
		

	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#remove(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void remove(JobInfo info) {

		if (isUpdateJob(info.getJob()))
			return;
		
		synchronized (updateLock) {
			currentInfo.remove(info);
		}
		scheduleUpdate();
	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#showsDebug()
	 */
	public boolean showsDebug() {
		return debug;
	}

	/**
	 * Return whether or not this is the update job. This is used
	 * to determine if a final refresh is required.
	 * @param job
	 * @return
	 */
	boolean isUpdateJob(Job job){
		return job.equals(updateJob);
	}
}
