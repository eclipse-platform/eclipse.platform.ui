/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philipp Bumann <bumannp@gmail.com> - Bug 477602
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;

/**
 * The ProgressViewUpdater is the singleton that updates viewers.
 */
@Creatable
@Singleton
public class ProgressViewUpdater implements IJobProgressManagerListener {

    private IProgressUpdateCollector[] collectors;

    Job updateJob;

    UpdatesInfo currentInfo = new UpdatesInfo();

    Object updateLock = new Object();

    @Inject
    ProgressManager progressManager;

	static class MutableBoolean {
		boolean value;
	}

	/*
	 * True when update job is scheduled or running. This is used to limit the
	 * update job to no more than once every 100 ms. See bug 258352 and 395645.
	 */
	MutableBoolean updateScheduled = new MutableBoolean();


    /**
     * The UpdatesInfo is a private class for keeping track of the updates
     * required.
     */
    static class UpdatesInfo {

        Collection<JobTreeElement> additions = new HashSet<>();

        Collection<JobTreeElement> deletions = new HashSet<>();

        Collection<JobTreeElement> refreshes = new HashSet<>();

        boolean updateAll = false;

        private UpdatesInfo() {
            //Create a new instance of the info
        }

        /**
         * Add an add update
         *
         * @param addition
         */
        void add(JobTreeElement addition) {
            additions.add(addition);
        }

        /**
         * Add a remove update
         *
         * @param removal
         */
        void remove(JobTreeElement removal) {
            deletions.add(removal);
        }

        /**
         * Add a refresh update
         *
         * @param refresh
         */
        void refresh(JobTreeElement refresh) {
            refreshes.add(refresh);
        }

        /**
         * Reset the caches after completion of an update.
         */
        void reset() {
            additions.clear();
            deletions.clear();
            refreshes.clear();
            updateAll = false;
        }

        void processForUpdate() {
            HashSet<JobTreeElement> staleAdditions = new HashSet<>();

            Iterator<JobTreeElement> additionsIterator = additions.iterator();
            while (additionsIterator.hasNext()) {
				JobTreeElement treeElement = additionsIterator.next();
                if (!treeElement.isActive()) {
                    if (deletions.contains(treeElement)) {
						staleAdditions.add(treeElement);
					}
                }
            }

            additions.removeAll(staleAdditions);

            HashSet<JobTreeElement> obsoleteRefresh = new HashSet<>();
            Iterator<JobTreeElement> refreshIterator = refreshes.iterator();
            while (refreshIterator.hasNext()) {
				JobTreeElement treeElement = refreshIterator.next();
                if (deletions.contains(treeElement)
                        || additions.contains(treeElement)) {
					obsoleteRefresh.add(treeElement);
				}

                //Also check for groups that are being added
               Object parent = treeElement.getParent();
               if(parent != null && (deletions.contains(parent)
                       || additions.contains(parent))){
            	   obsoleteRefresh.add(treeElement);
               }

                if (!treeElement.isActive()) {
                    //If it is done then delete it
                    obsoleteRefresh.add(treeElement);
                    deletions.add(treeElement);
                }
            }

            refreshes.removeAll(obsoleteRefresh);

        }
    }

    /**
     * Create a new instance of the receiver.
     */
    ProgressViewUpdater() {
        createUpdateJob();
        collectors = new IProgressUpdateCollector[0];
    }

    @PostConstruct
    void init(MApplication application) {
    	progressManager.addListener(this);
    	application.getContext().set(ProgressViewUpdater.class, this);
    }

    /**
     * Add the new collector to the list of collectors.
     *
     * @param newCollector
     */
    void addCollector(IProgressUpdateCollector newCollector) {
        IProgressUpdateCollector[] newCollectors = new IProgressUpdateCollector[collectors.length + 1];
        System.arraycopy(collectors, 0, newCollectors, 0, collectors.length);
        newCollectors[collectors.length] = newCollector;
        collectors = newCollectors;
    }

    /**
     * Remove the collector from the list of collectors.
     *
     * @param provider
     */
    void removeCollector(IProgressUpdateCollector provider) {
        HashSet<IProgressUpdateCollector> newCollectors = new HashSet<>();
        for (int i = 0; i < collectors.length; i++) {
            if (!collectors[i].equals(provider)) {
				newCollectors.add(collectors[i]);
			}
        }
        IProgressUpdateCollector[] newArray = new IProgressUpdateCollector[newCollectors
                .size()];
        newCollectors.toArray(newArray);
        collectors = newArray;
        //Remove ourselves if there is nothing to update
        if (collectors.length == 0) {
        	progressManager.removeListener(this);
		}
    }

    /**
     * Schedule an update.
     */
    void scheduleUpdate() {
        if (PlatformUI.isWorkbenchRunning()) {
            // make sure we don't schedule too often
			boolean scheduleUpdate = false;
			synchronized (updateScheduled) {
				if (!updateScheduled.value || updateJob.getState() == Job.NONE) {
					updateScheduled.value = scheduleUpdate = true;
				}
        	}
			if (scheduleUpdate)
				updateJob.schedule(100);
        }
    }

    /**
     * Create the update job that handles the updatesInfo.
     */
    private void createUpdateJob() {
        updateJob = new UIJob(ProgressMessages.ProgressContentProvider_UpdateProgressJob) {
            @Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				synchronized (updateScheduled) {
					// updates requested while we are running should cause it to
					// be rescheduled
					updateScheduled.value = false;
				}
				// Abort the job if there isn't anything
				if (collectors.length == 0) {
					return Status.CANCEL_STATUS;
				}

				if (currentInfo.updateAll) {
					synchronized (updateLock) {
						currentInfo.reset();
					}
					for (IProgressUpdateCollector collector : collectors) {
						collector.refresh();
					}

				} else {
					// Lock while getting local copies of the caches.
					Object[] updateItems;
					Object[] additionItems;
					Object[] deletionItems;
					synchronized (updateLock) {
						currentInfo.processForUpdate();

						updateItems = currentInfo.refreshes.toArray();
						additionItems = currentInfo.additions.toArray();
						deletionItems = currentInfo.deletions.toArray();

						currentInfo.reset();
					}

					for (IProgressUpdateCollector collector : collectors) {
						if (updateItems.length > 0) {
							collector.refresh(updateItems);
						}
						if (additionItems.length > 0) {
							collector.add(additionItems);
						}
						if (deletionItems.length > 0) {
							collector.remove(deletionItems);
						}
					}
				}

				return Status.OK_STATUS;
			}

			@Override
			protected void canceling() {
				synchronized (updateScheduled) {
					updateScheduled.value = false;
				}
            }
        };
        updateJob.setSystem(true);
        updateJob.setPriority(Job.DECORATE);
        updateJob.setProperty(ProgressManagerUtil.INFRASTRUCTURE_PROPERTY, new Object());

    }

    /**
     * Get the updates info that we are using in the receiver.
     *
     * @return Returns the currentInfo.
     */
    UpdatesInfo getCurrentInfo() {
        return currentInfo;
    }

    /**
     * Refresh the supplied JobInfo.
     * @param info
     */
    public void refresh(JobInfo info) {

        if (isUpdateJob(info.getJob())) {
			return;
		}

        synchronized (updateLock) {
            currentInfo.refresh(info);
            GroupInfo group = info.getGroupInfo();
            if (group != null) {
				currentInfo.refresh(group);
			}
        }
        //Add in a 100ms delay so as to keep priority low
        scheduleUpdate();

    }

    @Override
	public void refreshJobInfo(JobInfo info) {

        if (isUpdateJob(info.getJob())) {
			return;
		}

        synchronized (updateLock) {
            currentInfo.refresh(info);
        }
        //Add in a 100ms delay so as to keep priority low
        scheduleUpdate();

    }

    @Override
	public void refreshGroup(GroupInfo info) {
        synchronized (updateLock) {
            currentInfo.refresh(info);
        }
        //Add in a 100ms delay so as to keep priority low
        scheduleUpdate();

    }

    @Override
	public void addGroup(GroupInfo info) {

        synchronized (updateLock) {
            currentInfo.add(info);
        }
        scheduleUpdate();

    }

    @Override
	public void refreshAll() {

        synchronized (updateLock) {
            currentInfo.updateAll = true;
        }

        //Add in a 100ms delay so as to keep priority low
        scheduleUpdate();

    }

    @Override
	public void addJob(JobInfo info) {

        if (isUpdateJob(info.getJob())) {
			return;
		}

        synchronized (updateLock) {
            GroupInfo group = info.getGroupInfo();

            if (group == null) {
				currentInfo.add(info);
			} else {
                currentInfo.refresh(group);
            }
        }
        scheduleUpdate();

    }

    @Override
	public void removeJob(JobInfo info) {

        if (isUpdateJob(info.getJob())) {
			return;
		}

        synchronized (updateLock) {
            GroupInfo group = info.getGroupInfo();
            if (group == null) {
				currentInfo.remove(info);
			} else {
                currentInfo.refresh(group);
            }
        }
        scheduleUpdate();
    }

    @Override
	public void removeGroup(GroupInfo group) {
        synchronized (updateLock) {
            currentInfo.remove(group);
        }
        scheduleUpdate();

    }

    @Override
	public boolean showsDebug() {
    	return Preferences.getBoolean(IProgressConstants.SHOW_SYSTEM_JOBS);
    }

    /**
     * Return whether or not this is the update job. This is used to determine
     * if a final refresh is required.
     *
     * @param job
     * @return boolean <code>true</true> if this is the
     * update job
     */
    boolean isUpdateJob(Job job) {
        return job.equals(updateJob);
    }
}
