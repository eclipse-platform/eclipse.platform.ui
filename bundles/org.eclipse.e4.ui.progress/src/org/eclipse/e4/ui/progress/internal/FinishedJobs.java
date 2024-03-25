/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
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
 *     Philipp Bumann <bumannp@gmail.com> - Bug 477602
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.progress.IDisposableAction;
import org.eclipse.e4.ui.progress.IProgressConstants;

import jakarta.inject.Singleton;

/**
 * This singleton remembers all JobTreeElements that should be preserved (e.g.
 * because their associated Jobs have the "keep" property set).
 */
@Creatable
@Singleton
public class FinishedJobs extends EventManager {

	/**
	 * Interface for notify listeners.
	 */
	static interface KeptJobsListener {

		/**
		 * A job to be kept has finished
		 *
		 * @param jte the element that finished
		 */
		void finished(JobTreeElement jte);

		/**
		 * A kept job has been removed.
		 *
		 * @param jte {@code null} if all elements were removed
		 */
		void removed(JobTreeElement jte);
	}

	final IJobProgressManagerListener listener;

	private final Set<JobTreeElement> keptjobinfos = new LinkedHashSet<>();

	private HashMap<Object, Long> finishedTime = new HashMap<>();

	private static final JobTreeElement[] EMPTY_INFOS = new JobTreeElement[0];

	public FinishedJobs() {
		listener = new IJobProgressManagerListener() {
			@Override
			public void addJob(JobInfo info) {
				removeDuplicates(info);
			}

			@Override
			public void addGroup(GroupInfo info) {
				removeDuplicates(info);
			}

			@Override
			public void refreshJobInfo(JobInfo info) {
				checkTasks(info);
			}

			@Override
			public void refreshGroup(GroupInfo info) {
				// no action
			}

			@Override
			public void refreshAll() {
				// no action
			}

			@Override
			public void removeJob(JobInfo info) {
				if (keep(info)) {
					synchronized (keptjobinfos) {
						removeDuplicates(info);
						add(info);
					}
				}
			}

			@Override
			public void removeGroup(GroupInfo group) {
				// no action
			}

			@Override
			public boolean showsDebug() {
				return false;
			}
		};
	}

	/**
	 * Returns true if JobInfo indicates that it must be kept.
	 */
	static boolean keep(JobInfo info) {
		Job job = info.getJob();
		if (job != null) {
			Object prop = job.getProperty(ProgressManagerUtil.KEEP_PROPERTY);
			if (prop instanceof Boolean) {
				if (((Boolean) prop).booleanValue()) {
					return true;
				}
			}

			prop = job.getProperty(ProgressManagerUtil.KEEPONE_PROPERTY);
			if (prop instanceof Boolean) {
				if (((Boolean) prop).booleanValue()) {
					return true;
				}
			}

			IStatus status = job.getResult();
			if (status != null && status.getSeverity() == IStatus.ERROR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Register for notification.
	 *
	 * @param l listener to add. Not {@code null}.
	 */
	void addListener(KeptJobsListener l) {
		addListenerObject(l);
	}

	/**
	 * Deregister for notification.
	 *
	 * @param l listener to remove. Not {@code null}.
	 */
	void removeListener(KeptJobsListener l) {
		removeListenerObject(l);
	}

	private void removeDuplicates(GroupInfo info) {
		Object[] objects = info.getChildren();
		for (Object object : objects) {
			if (object instanceof JobInfo) {
				removeDuplicates((JobInfo) object);
			}
		}
	}

	private void removeDuplicates(JobTreeElement info) {
		JobTreeElement[] toBeRemoved = findJobsToRemove(info);
		if (toBeRemoved != null) {
			for (JobTreeElement element : toBeRemoved) {
				remove(element);
			}
		}
	}

	/**
	 * Add given Job to list of kept jobs.
	 */
	private void add(JobInfo info) {
		boolean fire = false;

		synchronized (keptjobinfos) {
			if (!keptjobinfos.contains(info)) {
				keptjobinfos.add(info);

				long now = System.currentTimeMillis();
				finishedTime.put(info, Long.valueOf(now));

				GroupInfo parent = info.getParent();
				if (!(parent == null || keptjobinfos.contains(parent))) {
					keptjobinfos.add(parent);
					finishedTime.put(parent, Long.valueOf(now));
				}

				fire = true;
			}
		}

		if (fire) {
			Object l[] = getListeners();
			for (Object element : l) {
				KeptJobsListener jv = (KeptJobsListener) element;
				jv.finished(info);
			}
		}
	}

	static void disposeAction(JobTreeElement jte) {
		if (jte.isJobInfo()) {
			JobInfo ji = (JobInfo) jte;
			Job job = ji.getJob();
			if (job != null) {
				Object prop = job
						.getProperty(IProgressConstants.ACTION_PROPERTY);
				if (prop instanceof IDisposableAction) {
					((IDisposableAction) prop).dispose();
				}
			}
		}
	}

	private JobTreeElement[] findJobsToRemove(JobTreeElement info) {

		if (info.isJobInfo()) {
			Job myJob = ((JobInfo) info).getJob();

			if (myJob != null) {

				Object prop = myJob
						.getProperty(ProgressManagerUtil.KEEPONE_PROPERTY);
				if (prop instanceof Boolean && ((Boolean) prop).booleanValue()) {
					ArrayList<JobTreeElement> found = null;
					JobTreeElement[] all = getKeptElements();
					for (JobTreeElement jte : all) {
						if (jte != info && jte.isJobInfo()) {
							Job job = ((JobInfo) jte).getJob();
							if (job != null && job != myJob
									&& job.belongsTo(myJob)) {
								if (found == null) {
									found = new ArrayList<>();
								}
								found.add(jte);
							}
						}
					}
					if (found != null) {
						return found
								.toArray(new JobTreeElement[found.size()]);
					}
				}
			}
		}
		return null;
	}

	private void checkTasks(JobInfo info) {
		if (keep(info)) {
			TaskInfo tinfo = info.getTaskInfo();
			if (tinfo != null) {
				JobTreeElement[] toBeRemoved = null;
				boolean fire = false;
				JobTreeElement element = (JobTreeElement) tinfo.getParent();
				synchronized (keptjobinfos) {
					if (element == info && !keptjobinfos.contains(tinfo)) {
						toBeRemoved = findJobsToRemove(element);
						keptjobinfos.add(tinfo);
						finishedTime.put(tinfo, Long.valueOf(System
								.currentTimeMillis()));
					}
				}

				if (toBeRemoved != null) {
					for (JobTreeElement jobTreeElement : toBeRemoved) {
						remove(jobTreeElement);
					}
				}

				if (fire) {
					for (Object listener : getListeners()) {
						KeptJobsListener jv = (KeptJobsListener) listener;
						jv.finished(info);
					}
				}
			}
		}
	}

	/**
	 * Remove all kept jobs with result status {@link IStatus#ERROR error}.
	 */
	public void removeErrorJobs() {
		JobTreeElement[] infos = getKeptElements();
		for (JobTreeElement info : infos) {
			if (info.isJobInfo()) {
				JobInfo info1 = (JobInfo) info;
				Job job = info1.getJob();
				if (job != null) {
					IStatus status = job.getResult();
					if (status != null && status.getSeverity() == IStatus.ERROR) {
						JobTreeElement topElement = info1.getParent();
						if (topElement == null) {
							topElement = info1;
						}
						remove(topElement);
					}
				}
			}
		}
	}

	boolean remove(JobTreeElement jte) {
		boolean fire = false;
		boolean removed = false;

		synchronized (keptjobinfos) {
			if (keptjobinfos.remove(jte)) {
				removed = true;
				finishedTime.remove(jte);
				disposeAction(jte);

				// delete all elements that have jte as their direct or indirect
				// parent
				JobTreeElement jobTreeElements[] = getKeptElements();
				for (JobTreeElement jobTreeElement : jobTreeElements) {
					JobTreeElement parent = (JobTreeElement) jobTreeElement
							.getParent();
					if (parent != null) {
						if (parent == jte || parent.getParent() == jte) {
							if (keptjobinfos.remove(jobTreeElement)) {
								disposeAction(jobTreeElement);
							}
							finishedTime.remove(jobTreeElement);
						}
					}
				}
				fire = true;
			}
		}

		if (fire) {
			// notify listeners
			Object l[] = getListeners();
			for (Object element : l) {
				KeptJobsListener jv = (KeptJobsListener) element;
				jv.removed(jte);
			}
		}
		return removed;
	}

	/**
	 * Returns all kept elements.
	 */
	JobTreeElement[] getKeptElements() {
		synchronized (keptjobinfos) {
			if (keptjobinfos.isEmpty()) {
				return EMPTY_INFOS;
			}
			return keptjobinfos.toArray(new JobTreeElement[keptjobinfos.size()]);
		}
	}

	/**
	 * Get the date that indicates the finish time.
	 *
	 * @param jte job element to get finish date for
	 * @return finish date of the requested job element or <code>null</code> if
	 *         unknown element
	 */
	public Date getFinishDate(JobTreeElement jte) {
		Long value = finishedTime.get(jte);
		if (value != null) {
			return new Date(value.longValue());
		}
		return null;
	}

	/**
	 * Return whether or not the kept infos have the element.
	 *
	 * @param element the job element to check
	 * @return <code>true</code> if requested element is configured to be kept
	 */
	public boolean isKept(JobTreeElement element) {
		synchronized (keptjobinfos) {
			return keptjobinfos.contains(element);
		}
	}

	/**
	 * Clear all kept jobs.
	 */
	public void clearAll() {
		synchronized (keptjobinfos) {
			JobTreeElement[] all = getKeptElements();
			for (JobTreeElement element : all) {
				disposeAction(element);
			}
			keptjobinfos.clear();
			finishedTime.clear();
		}

		// notify listeners
		Object l[] = getListeners();
		for (Object element : l) {
			KeptJobsListener jv = (KeptJobsListener) element;
			jv.removed(null);
		}
	}
}
