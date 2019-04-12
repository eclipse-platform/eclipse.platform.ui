/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.jface.util.Throttler;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The ProgressViewUpdater is the singleton that updates viewers.
 */
class ProgressViewUpdater implements IJobProgressManagerListener {

	private static ProgressViewUpdater singleton;

	private Set<IProgressUpdateCollector> collectors;

	UpdatesInfo currentInfo = new UpdatesInfo();

	boolean debug;

	Throttler throttledUpdate = new Throttler(PlatformUI.getWorkbench().getDisplay(), Duration.ofMillis(100),
			this::update);

	/**
	 * The UpdatesInfo is a private class for keeping track of the updates required.
	 */
	static class UpdatesInfo {

		Collection<JobTreeElement> additions = new LinkedHashSet<>();

		Collection<JobTreeElement> deletions = new LinkedHashSet<>();

		Collection<JobTreeElement> refreshes = new LinkedHashSet<>();

		volatile boolean updateAll;

		private UpdatesInfo() {
			// Create a new instance of the info
		}

		/**
		 * Add an add update
		 *
		 * @param addition
		 */
		synchronized void add(JobTreeElement addition) {
			additions.add(addition);
		}

		/**
		 * Add a remove update
		 *
		 * @param removal
		 */
		synchronized void remove(JobTreeElement removal) {
			deletions.add(removal);
		}

		/**
		 * Add a refresh update
		 *
		 * @param refresh
		 */
		synchronized void refresh(JobTreeElement refresh) {
			refreshes.add(refresh);
		}

		/**
		 * Reset the caches after completion of an update.
		 */
		synchronized void reset() {
			additions.clear();
			deletions.clear();
			refreshes.clear();
			updateAll = false;
		}

		/**
		 * @return array containing updated, added and deleted items
		 */
		synchronized JobTreeElement[][] processForUpdate() {
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
			for (JobTreeElement treeElement : refreshes) {
				if (deletions.contains(treeElement) || additions.contains(treeElement)) {
					obsoleteRefresh.add(treeElement);
				}

				// Also check for groups that are being added
				Object parent = treeElement.getParent();
				if (parent != null && (deletions.contains(parent) || additions.contains(parent))) {
					obsoleteRefresh.add(treeElement);
				}

				if (!treeElement.isActive()) {
					// If it is done then delete it
					obsoleteRefresh.add(treeElement);
					deletions.add(treeElement);
				}
			}

			refreshes.removeAll(obsoleteRefresh);

			JobTreeElement[] updateItems = refreshes.toArray(new JobTreeElement[0]);
			JobTreeElement[] additionItems = additions.toArray(new JobTreeElement[0]);
			JobTreeElement[] deletionItems = deletions.toArray(new JobTreeElement[0]);
			return new JobTreeElement[][] { updateItems, additionItems, deletionItems };
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
	 * @return boolean <code>true</code> if there is already a singleton
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
		collectors = new LinkedHashSet<>();
		ProgressManager.getInstance().addListener(this);
		debug = PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS);
	}

	/**
	 * Add the new collector to the list of collectors.
	 *
	 * @param newCollector
	 */
	void addCollector(IProgressUpdateCollector newCollector) {
		collectors.add(newCollector);
	}

	/**
	 * Remove the collector from the list of collectors.
	 *
	 * @param provider
	 */
	void removeCollector(IProgressUpdateCollector provider) {
		collectors.remove(provider);
		// Remove ourselves if there is nothing to update
		if (collectors.isEmpty()) {
			clearSingleton();
		}
	}

	/** Running in UI thread by throttledUpdate */
	private void update() {
		// Abort the update if there isn't anything
		if (collectors.isEmpty()) {
			return;
		}

		if (currentInfo.updateAll) {
			currentInfo.reset();
			for (IProgressUpdateCollector collector : collectors) {
				collector.refresh();
			}

		} else {
			JobTreeElement[][] elements = currentInfo.processForUpdate();

			JobTreeElement[] updateItems = elements[0];
			JobTreeElement[] additionItems = elements[1];
			JobTreeElement[] deletionItems = elements[2];

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

	@Override
	public void refreshJobInfo(JobInfo info) {
		currentInfo.refresh(info);
		// Add in a 100ms delay so as to keep priority low
		throttledUpdate.throttledExec();

	}

	@Override
	public void refreshGroup(GroupInfo info) {
		currentInfo.refresh(info);
		// Add in a 100ms delay so as to keep priority low
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

		// Add in a 100ms delay so as to keep priority low
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
