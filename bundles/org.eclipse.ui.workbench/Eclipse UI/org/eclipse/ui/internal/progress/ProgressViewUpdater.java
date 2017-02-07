/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The ProgressViewUpdater is the singleton that updates viewers.
 */
class ProgressViewUpdater implements IJobProgressManagerListener {

    private static ProgressViewUpdater singleton;

    private IProgressUpdateCollector[] collectors;

    UpdatesInfo currentInfo = new UpdatesInfo();

    boolean debug;

	Throttler throttledUpdate = new Throttler(PlatformUI.getWorkbench().getDisplay(), Duration.ofMillis(100),
			this::update);

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
            HashSet staleAdditions = new HashSet();

            Iterator additionsIterator = additions.iterator();
            while (additionsIterator.hasNext()) {
                JobTreeElement treeElement = (JobTreeElement) additionsIterator
                        .next();
                if (!treeElement.isActive()) {
                    if (deletions.contains(treeElement)) {
						staleAdditions.add(treeElement);
					}
                }
            }

            additions.removeAll(staleAdditions);

            HashSet obsoleteRefresh = new HashSet();
            Iterator refreshIterator = refreshes.iterator();
            while (refreshIterator.hasNext()) {
                JobTreeElement treeElement = (JobTreeElement) refreshIterator
                        .next();
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
     * Return a new instance of the receiver.
     *
     * @return ProgressViewUpdater
     */
   static ProgressViewUpdater getSingleton() {
        if (singleton == null) {
			singleton = new ProgressViewUpdater();
		}
        return singleton;
    }

    /**
     * Return whether or not there is a singleton for updates to avoid creating
     * extra listeners.
     *
     * @return boolean <code>true</code> if there is already
     * a singleton
     */
    static boolean hasSingleton() {
        return singleton != null;
    }

    static void clearSingleton() {
        if (singleton != null) {
			ProgressManager.getInstance().removeListener(singleton);
		}
        singleton = null;
    }

    /**
     * Create a new instance of the receiver.
     */
    private ProgressViewUpdater() {
        collectors = new IProgressUpdateCollector[0];
        ProgressManager.getInstance().addListener(this);
        debug =
        	PrefUtil.getAPIPreferenceStore().
        		getBoolean(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS);
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
        HashSet newCollectors = new HashSet();
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
			clearSingleton();
		}
    }

	private void update() {
		// Abort the update if there isn't anything
		if (collectors.length == 0) {
			return;
		}

		if (currentInfo.updateAll) {
			currentInfo.reset();
			for (IProgressUpdateCollector collector : collectors) {
				collector.refresh();
			}

		} else {
			// Lock while getting local copies of the caches.
			Object[] updateItems;
			Object[] additionItems;
			Object[] deletionItems;
			currentInfo.processForUpdate();

			updateItems = currentInfo.refreshes.toArray();
			additionItems = currentInfo.additions.toArray();
			deletionItems = currentInfo.deletions.toArray();

			currentInfo.reset();

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
		currentInfo.refresh(info);
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			currentInfo.refresh(group);
		}
        //Add in a 100ms delay so as to keep priority low
		throttledUpdate.throttledExec();

    }

    @Override
	public void refreshJobInfo(JobInfo info) {
		currentInfo.refresh(info);
        //Add in a 100ms delay so as to keep priority low
		throttledUpdate.throttledExec();

    }

    @Override
	public void refreshGroup(GroupInfo info) {
		currentInfo.refresh(info);
        //Add in a 100ms delay so as to keep priority low
		throttledUpdate.throttledExec();

    }

    @Override
	public void addGroup(GroupInfo info) {

		currentInfo.add(info);
		throttledUpdate.throttledExec();

    }

    @Override
	public void refreshAll() {

		currentInfo.updateAll = true;

        //Add in a 100ms delay so as to keep priority low
		throttledUpdate.throttledExec();

    }

    @Override
	public void addJob(JobInfo info) {
		GroupInfo group = info.getGroupInfo();

		if (group == null) {
			currentInfo.add(info);
		} else {
			currentInfo.refresh(group);
        }
		throttledUpdate.throttledExec();

    }

    @Override
	public void removeJob(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group == null) {
			currentInfo.remove(info);
		} else {
			currentInfo.refresh(group);
        }
		throttledUpdate.throttledExec();
    }

    @Override
	public void removeGroup(GroupInfo group) {
		currentInfo.remove(group);
		throttledUpdate.throttledExec();

    }

    @Override
	public boolean showsDebug() {
        return debug;
    }

}
