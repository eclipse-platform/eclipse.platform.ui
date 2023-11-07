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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.eclipse.jface.util.Throttler;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The ProgressViewUpdater is the singleton that updates viewers.
 */
class ProgressViewUpdater implements IJobProgressManagerListener {

	private static ProgressViewUpdater singleton;

	/**
	 * Registered collectors to be feed with throttled updates. The value remembers
	 * if the collector wants to collect updates for finished jobs
	 * (<code>true</code>) or not.
	 */
	private Map<IProgressUpdateCollector, Boolean> collectors;

	final UpdatesInfo currentInfo = new UpdatesInfo();

	boolean debug;

	Throttler throttledUpdate = new Throttler(PlatformUI.getWorkbench().getDisplay(), Duration.ofMillis(100),
			this::update);

	final KeptJobsListener finishedJobsListener = new FinishedJobsListener();

	/**
	 * The UpdatesInfo is a private class for keeping track of the updates required.
	 */
	static class UpdatesInfo {

		Collection<JobTreeElement> additions = new LinkedHashSet<>();

		Collection<JobTreeElement> deletions = new LinkedHashSet<>();

		Collection<JobTreeElement> refreshes = new LinkedHashSet<>();

		Collection<JobTreeElement> keptFinished = new LinkedHashSet<>();

		Collection<JobTreeElement> keptRemoved = new LinkedHashSet<>();

		volatile boolean updateAll;

		private UpdatesInfo() {
			// Create a new instance of the info
		}

		/**
		 * Add an add update
		 */
		synchronized void add(JobTreeElement addition) {
			additions.add(addition);
		}

		/**
		 * Add a remove update
		 */
		synchronized void remove(JobTreeElement removal) {
			deletions.add(removal);
		}

		/**
		 * Add a refresh update
		 */
		synchronized void refresh(JobTreeElement refresh) {
			refreshes.add(refresh);
		}

		/**
		 * Add an update for a job which has finished and should be kept
		 */
		synchronized void keptFinished(JobTreeElement finished) {
			keptFinished.add(finished);
		}

		/**
		 * Add an update for a job which was kept and is removed now
		 */
		synchronized void keptRemoved(JobTreeElement removed) {
			keptRemoved.add(removed);
		}

		/**
		 * Reset the caches after completion of an update.
		 */
		synchronized void reset() {
			additions.clear();
			deletions.clear();
			refreshes.clear();
			keptFinished.clear();
			keptRemoved.clear();
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
			JobTreeElement[] keptFinishedItems = keptFinished.toArray(new JobTreeElement[0]);
			JobTreeElement[] keptRemovedItems = keptRemoved.toArray(new JobTreeElement[0]);
			return new JobTreeElement[][] { updateItems, additionItems, deletionItems, keptFinishedItems,
					keptRemovedItems };
		}

	}

	class FinishedJobsListener implements KeptJobsListener {
		@Override
		public void finished(JobTreeElement jte) {
			currentInfo.keptFinished(jte);
			throttledUpdate.throttledExec();
		}

		@Override
		public void removed(JobTreeElement jte) {
			if (jte == null) {
				currentInfo.updateAll = true;
			} else {
				currentInfo.keptRemoved(jte);
			}
			throttledUpdate.throttledExec();
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
		collectors = new LinkedHashMap<>();
		ProgressManager.getInstance().addListener(this);
		debug = PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS);
	}

	/**
	 * Add the new collector to the list of collectors. Collector will not receive
	 * updates from {@link FinishedJobs}.
	 */
	void addCollector(IProgressUpdateCollector newCollector) {
		addCollector(newCollector, false);
	}

	/**
	 * Add the new collector to the list of collectors.
	 *
	 * @param includeFinished if <code>true</code> the collector will receive
	 *                        updates from {@link FinishedJobs}.
	 */
	void addCollector(IProgressUpdateCollector newCollector, boolean includeFinished) {
		collectors.put(newCollector, includeFinished);
		if (includeFinished) {
			FinishedJobs.getInstance().addListener(finishedJobsListener);
		}
	}

	/**
	 * Remove the collector from the list of collectors.
	 */
	void removeCollector(IProgressUpdateCollector provider) {
		collectors.remove(provider);
		// Remove listener if there is no more collector interested in finished jobs
		if (!collectors.containsValue(Boolean.TRUE)) {
			FinishedJobs.getInstance().removeListener(finishedJobsListener);
		}
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
			for (IProgressUpdateCollector collector : collectors.keySet()) {
				collector.refresh();
			}

		} else {
			JobTreeElement[][] elements;
			synchronized (currentInfo) {
				elements = currentInfo.processForUpdate();
				currentInfo.reset();
			}

			JobTreeElement[] updateItems = elements[0];
			JobTreeElement[] additionItems = elements[1];
			JobTreeElement[] deletionItems = elements[2];
			JobTreeElement[] keptFinsihedItems = elements[3];
			JobTreeElement[] keptRemovedItems = elements[4];

			for (IProgressUpdateCollector collector : collectors.keySet()) {
				if (updateItems.length > 0) {
					collector.refresh(updateItems);
				}
				if (additionItems.length > 0) {
					collector.add(additionItems);
				}
				if (deletionItems.length > 0) {
					collector.remove(deletionItems);
				}
				if (keptFinsihedItems.length > 0) {
					collector.refresh(keptFinsihedItems);
				}
				if (keptRemovedItems.length > 0) {
					collector.remove(keptRemovedItems);
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
