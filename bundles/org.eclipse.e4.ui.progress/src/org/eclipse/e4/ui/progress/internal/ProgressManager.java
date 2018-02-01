/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Teddy Walker <teddy.walker@googlemail.com>
 *     		- Fix for Bug 151204 [Progress] Blocked status of jobs are not applied/reported
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *     Philipp Bumann <bumannp@gmail.com> - Bug 477602
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.e4.ui.progress.internal.legacy.EventLoopProgressMonitor;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;
import org.eclipse.e4.ui.progress.internal.legacy.Policy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/**
 * JobProgressManager provides the progress monitor to the job manager and
 * informs any ProgressContentProviders of changes.
 */
public class ProgressManager extends ProgressProvider {
	/**
	 * A property to determine if the job was run in the dialog. Kept for
	 * backwards compatability.
	 *
	 * @deprecated
	 * @see IProgressConstants#PROPERTY_IN_DIALOG
	 */
	@Deprecated
	public static final QualifiedName PROPERTY_IN_DIALOG = IProgressConstants.PROPERTY_IN_DIALOG;

	private static final String ERROR_JOB = "errorstate.png"; //$NON-NLS-1$

	static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

	private static ProgressManager singleton;

	final private Map<Job, JobInfo> jobs = Collections
	        .synchronizedMap(new HashMap<Job, JobInfo>());

	final Map<Job, JobMonitor> runnableMonitors = Collections
	        .synchronizedMap(new HashMap<Job, JobMonitor>());

	final private Map<Object, Collection<IJobBusyListener>> familyListeners = Collections
	        .synchronizedMap(new HashMap<Object, Collection<IJobBusyListener>>());

	//	list of IJobProgressManagerListener
	private ListenerList<IJobProgressManagerListener> listeners = new ListenerList<>();

	IJobChangeListener changeListener;

	static final String PROGRESS_VIEW_NAME = "org.eclipse.e4.ui.progress.ProgressView"; //$NON-NLS-1$

	static final String PROGRESS_FOLDER = "progress/"; //$NON-NLS-1$

	private static final String SLEEPING_JOB = "sleeping.png"; //$NON-NLS-1$

	private static final String WAITING_JOB = "waiting.png"; //$NON-NLS-1$

	private static final String BLOCKED_JOB = "lockedstate.png"; //$NON-NLS-1$

	/**
	 * The key for the sleeping job icon.
	 */
	public static final String SLEEPING_JOB_KEY = "SLEEPING_JOB"; //$NON-NLS-1$

	/**
	 * The key for the waiting job icon.
	 */
	public static final String WAITING_JOB_KEY = "WAITING_JOB"; //$NON-NLS-1$

	/**
	 * The key for the locked job icon.
	 */
	public static final String BLOCKED_JOB_KEY = "LOCKED_JOB"; //$NON-NLS-1$


	@Inject
	@Optional
	IProgressService progressService;

	@Inject
	JobInfoFactory jobInfoFactory;

	@Optional
	@Inject
	FinishedJobs finishedJobs;

	/**
	 * Shutdown the singleton if there is one.
	 */
	public static void shutdownProgressManager() {
		if (singleton == null) {
			return;
		}
		singleton.shutdown();
	}

//	/**
//	 * Create a new instance of the receiver.
//	 */
//	protected ProgressManager() {
//	}

	@PostConstruct
	protected void init(WorkbenchDialogBlockedHandler dialogBlockedHandler) {
		Dialog.setBlockedHandler(dialogBlockedHandler);

		setUpImages();

		changeListener = createChangeListener();

		Job.getJobManager().setProgressProvider(this);
		Job.getJobManager().addJobChangeListener(this.changeListener);
	}

	private void setUpImages() {
		ImageTools imageTools = ImageTools.getInstance();

		imageTools.putIntoRegistry(SLEEPING_JOB_KEY, PROGRESS_FOLDER
		        + SLEEPING_JOB);
		imageTools.putIntoRegistry(WAITING_JOB_KEY, PROGRESS_FOLDER
		        + WAITING_JOB);
		imageTools.putIntoRegistry(BLOCKED_JOB_KEY, PROGRESS_FOLDER
		        + BLOCKED_JOB);
		imageTools.putIntoRegistry(ERROR_JOB_KEY, PROGRESS_FOLDER + ERROR_JOB);
	}

	@Override
	public IProgressMonitor createMonitor(Job job) {
		return progressFor(job);
	}

	/**
	 * Return a monitor for the job. Check if we cached a monitor for this job
	 * previously for a long operation timeout check.
	 *
	 * @param job
	 * @return IProgressMonitor
	 */
	public JobMonitor progressFor(Job job) {

		synchronized (runnableMonitors) {
			JobMonitor monitor = runnableMonitors.get(job);
			if (monitor == null) {
				monitor = new JobMonitor(job);
				runnableMonitors.put(job, monitor);
			}

			return monitor;
		}

	}

	@Override
	public IProgressMonitor createProgressGroup() {
		return new GroupInfo(this, finishedJobs);
	}

	@Override
	public IProgressMonitor createMonitor(Job job, IProgressMonitor group,
			int ticks) {
		JobMonitor monitor = progressFor(job);
		if (group instanceof GroupInfo) {
			GroupInfo groupInfo = (GroupInfo) group;
			JobInfo jobInfo = getJobInfo(job);
			jobInfo.setGroupInfo(groupInfo);
			jobInfo.setTicks(ticks);
			groupInfo.addJobInfo(jobInfo);
		}
		return monitor;
	}

	/**
	 * Get the JobInfo for the job. If it does not exist create it.
	 *
	 * @param job
	 * @return JobInfo
	 */
	JobInfo getJobInfo(Job job) {
		JobInfo info = internalGetJobInfo(job);
		if (info == null) {
			info = jobInfoFactory.getJobInfo(job);
			jobs.put(job, info);
		}
		return info;
	}

	/**
	 * Return an existing job info for the given Job or <code>null</code> if
	 * there isn't one.
	 *
	 * @param job
	 * @return JobInfo
	 */
	JobInfo internalGetJobInfo(Job job) {
		return jobs.get(job);
	}


	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor
	 * integration with the ProgressMonitor.
	 */
	class JobMonitor implements IProgressMonitorWithBlocking {
		Job job;

		String currentTaskName;

		IProgressMonitorWithBlocking listener;

		/**
		 * Create a monitor on the supplied job.
		 *
		 * @param newJob
		 */
		JobMonitor(Job newJob) {
			job = newJob;
		}

		/**
		 * Add monitor as another monitor that
		 *
		 * @param monitor
		 */
		void addProgressListener(IProgressMonitorWithBlocking monitor) {
			listener = monitor;
			JobInfo info = getJobInfo(job);
			TaskInfo currentTask = info.getTaskInfo();
			if (currentTask != null) {
				listener.beginTask(currentTaskName, currentTask.totalWork);
				listener.internalWorked(currentTask.preWork);
			}
		}

		@Override
		public void beginTask(String taskName, int totalWork) {
			JobInfo info = getJobInfo(job);
			info.beginTask(taskName, totalWork);
			refreshJobInfo(info);
			currentTaskName = taskName;
			if (listener != null) {
				listener.beginTask(taskName, totalWork);
			}
		}

		@Override
		public void done() {
			JobInfo info = getJobInfo(job);
			info.clearTaskInfo();
			info.clearChildren();
			runnableMonitors.remove(job);
			if (listener != null) {
				listener.done();
			}
		}

		@Override
		public void internalWorked(double work) {
			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo()) {
				info.addWork(work);
				refreshJobInfo(info);
			}
			if (listener != null) {
				listener.internalWorked(work);
			}
		}

		@Override
		public boolean isCanceled() {
			// Use the internal get so we don't create a Job Info for
			// a job that is not running (see bug 149857)
			JobInfo info = internalGetJobInfo(job);
			if (info == null)
				return false;
			return info.isCanceled();
		}

		@Override
		public void setCanceled(boolean value) {
			JobInfo info = getJobInfo(job);
			// Don't bother cancelling twice
			if (value && !info.isCanceled()) {
				info.cancel();
				// Only inform the first time
				if (listener != null) {
					listener.setCanceled(value);
				}
			}
		}

		@Override
		public void setTaskName(String taskName) {
			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo()) {
				info.setTaskName(taskName);
			} else {
				beginTask(taskName, 100);
				return;
			}
			info.clearChildren();
			refreshJobInfo(info);
			currentTaskName = taskName;
			if (listener != null) {
				listener.setTaskName(taskName);
			}
		}

		@Override
		public void subTask(String name) {
			if (name == null) {
				return;
			}
			JobInfo info = getJobInfo(job);
			info.clearChildren();
			info.addSubTask(name);
			refreshJobInfo(info);
			if (listener != null) {
				listener.subTask(name);
			}
		}

		@Override
		public void worked(int work) {
			internalWorked(work);
		}

		@Override
		public void clearBlocked() {
			JobInfo info = getJobInfo(job);
			info.setBlockedStatus(null);
			refreshJobInfo(info);
			if (listener != null) {
				listener.clearBlocked();
			}
		}

		@Override
		public void setBlocked(IStatus reason) {
			JobInfo info = getJobInfo(job);
			info.setBlockedStatus(reason);
			refreshJobInfo(info);
			if (listener != null) {
				listener.setBlocked(reason);
			}
		}
	}



	/**
	 * Create and return the IJobChangeListener registered with the Job manager.
	 *
	 * @return the created IJobChangeListener
	 */
	private IJobChangeListener createChangeListener() {
		return new JobChangeAdapter() {

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				JobInfo info = getJobInfo(event.getJob());
				refreshJobInfo(info);
				Iterator<IJobBusyListener> startListeners = busyListenersForJob(event.getJob())
						.iterator();
				while (startListeners.hasNext()) {
					IJobBusyListener next = startListeners.next();
					next.incrementBusy(event.getJob());
				}
			}

			@Override
			public void done(IJobChangeEvent event) {
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}
				Iterator<IJobBusyListener> startListeners = busyListenersForJob(event.getJob())
						.iterator();
				while (startListeners.hasNext()) {
					IJobBusyListener next = startListeners
							.next();
					next.decrementBusy(event.getJob());
				}

				final JobInfo info = getJobInfo(event.getJob());
				removeJobInfo(info);
				//TODO E4
//				if (event.getResult() != null
//						&& event.getResult().getSeverity() == IStatus.ERROR
//						&& event
//							.getJob()
//							.getProperty(
//									IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) != Boolean.TRUE) {
//
//					ExternalServices.getStatusReporter().report(event.getResult(), StatusReporter.SHOW, new Object[0]);
//					}
			}

			@Override
			public void scheduled(IJobChangeEvent event) {
				updateFor(event);
				if (event.getJob().isUser()) {
					boolean noDialog = shouldRunInBackground();
					if (!noDialog) {
						final IJobChangeEvent finalEvent = event;
						Job showJob = new UIJob(
								ProgressMessages.ProgressManager_showInDialogName) {
							@Override
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								progressService.showInDialog(null, finalEvent.getJob());
								return Status.OK_STATUS;
							}
						};
						showJob.setSystem(true);
						showJob.schedule();
						return;
					}
				}
			}

			/**
			 * Update the listeners for the receiver for the event.
			 *
			 * @param event
			 */
			private void updateFor(IJobChangeEvent event) {
				if (isInfrastructureJob(event.getJob())) {
					return;
				}
				if (jobs.containsKey(event.getJob())) {
					refreshJobInfo(getJobInfo(event.getJob()));
				} else {
					addJobInfo(jobInfoFactory.getJobInfo(event.getJob()));
				}
			}

			@Override
			public void awake(IJobChangeEvent event) {
				updateFor(event);
			}

			@Override
			public void sleeping(IJobChangeEvent event) {

				if (jobs.containsKey(event.getJob()))// Are we showing this?
					sleepJobInfo(getJobInfo(event.getJob()));
			}
		};
	}

	/**
	 * The job in JobInfo is now sleeping. Refresh it if we are showing it,
	 * remove it if not.
	 *
	 * @param info
	 */
	protected void sleepJobInfo(JobInfo info) {
		if (isInfrastructureJob(info.getJob()))
			return;

		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			sleepGroup(group,info);
		}

		for (IJobProgressManagerListener listener : listeners) {
			// Is this one the user never sees?
			if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
				continue;
			if (listener.showsDebug())
				listener.refreshJobInfo(info);
			else
				listener.removeJob(info);

		}
	}

	@Override
	public IProgressMonitor getDefaultMonitor() {
		// only need a default monitor for operations the UI thread
		// and only if there is a display
		Display display;
		if (PlatformUI.isWorkbenchRunning() && !PlatformUI.isWorkbenchStarting()) {
			display = getDisplay();
			if (!display.isDisposed() && (display.getThread() == Thread.currentThread())) {
				return new EventLoopProgressMonitor(new NullProgressMonitor());
			}
		}
		return super.getDefaultMonitor();
	}

	/**
	 * Refresh the group when info is sleeping.
	 * @param group
	 */
	private void sleepGroup(GroupInfo group, JobInfo info) {
		for (IJobProgressManagerListener listener : listeners) {

			if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
				continue;

			if (listener.showsDebug() || group.isActive())
				listener.refreshGroup(group);
			else
				listener.removeGroup(group);
		}
	}



	/**
	 * Add an IJobProgressManagerListener to listen to the changes.
	 *
	 * @param listener
	 */
	void addListener(IJobProgressManagerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the supplied IJobProgressManagerListener from the list of
	 * listeners.
	 *
	 * @param listener
	 */
	void removeListener(IJobProgressManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in info.
	 *
	 * @param info
	 */
	public void refreshJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			refreshGroup(group);
		}

		for (IJobProgressManagerListener listener : listeners) {
			if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
				listener.refreshJobInfo(info);
			}
		}
	}

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in info.
	 *
	 * @param info
	 */
	public void refreshGroup(GroupInfo info) {

		for (IJobProgressManagerListener element : listeners) {
			element.refreshGroup(info);
		}
	}

	/**
	 * Refresh all the IJobProgressManagerListener as a result of a change in
	 * the whole model.
	 */
	public void refreshAll() {

		pruneStaleJobs();
		for (IJobProgressManagerListener element : listeners) {
			element.refreshAll();
		}

	}

	/**
	 * Refresh the content providers as a result of a deletion of info.
	 *
	 * @param info
	 *            JobInfo
	 */
	public void removeJobInfo(JobInfo info) {

		Job job = info.getJob();
		jobs.remove(job);
		runnableMonitors.remove(job);

		for (IJobProgressManagerListener listener : listeners) {
			if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
				listener.removeJob(info);
			}
		}
	}

	/**
	 * Remove the group from the roots and inform the listeners.
	 *
	 * @param group
	 *            GroupInfo
	 */
	public void removeGroup(GroupInfo group) {

		for (IJobProgressManagerListener element : listeners) {
			element.removeGroup(group);
		}
	}

	/**
	 * Refresh the content providers as a result of an addition of info.
	 *
	 * @param info
	 */
	public void addJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			refreshGroup(group);
		}

		jobs.put(info.getJob(), info);
		for (IJobProgressManagerListener listener : listeners) {
			if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
				listener.addJob(info);
			}
		}
	}

	/**
	 * Return whether or not this job is currently displayable.
	 *
	 * @param job
	 * @param debug
	 *            If the listener is in debug mode.
	 * @return boolean <code>true</code> if the job is not displayed.
	 */
	boolean isCurrentDisplaying(Job job, boolean debug) {
		return isNeverDisplaying(job, debug) || job.getState() == Job.SLEEPING;
	}

	/**
	 * Return whether or not we even display this job with debug mode set to
	 * debug.
	 *
	 * @param job
	 * @param debug
	 * @return boolean
	 */
	boolean isNeverDisplaying(Job job, boolean debug) {
		if (isInfrastructureJob(job)) {
			return true;
		}
		if (debug)
			return false;

		return job.isSystem();
	}

	/**
	 * Return whether or not this job is an infrastructure job.
	 *
	 * @param job
	 * @return boolean <code>true</code> if it is never displayed.
	 */
	private boolean isInfrastructureJob(Job job) {
		if (Policy.DEBUG_SHOW_ALL_JOBS)
			return false;
		return job.getProperty(ProgressManagerUtil.INFRASTRUCTURE_PROPERTY) != null;
	}

	/**
	 * Return the current job infos filtered on debug mode.
	 *
	 * @param debug
	 * @return JobInfo[]
	 */
	public JobInfo[] getJobInfos(boolean debug) {
		synchronized (jobs) {
			Collection<JobInfo> result = new ArrayList<>();
			for (Entry<Job, JobInfo> entry : jobs.entrySet()) {
				if (!isCurrentDisplaying(entry.getKey(), debug)) {
					result.add(entry.getValue());
				}
			}
			JobInfo[] infos = new JobInfo[result.size()];
			result.toArray(infos);
			return infos;
		}
	}

	/**
	 * Return the current root elements filtered on the debug mode.
	 *
	 * @param debug
	 * @return JobTreeElement[]
	 */
	public JobTreeElement[] getRootElements(boolean debug) {
		synchronized (jobs) {
			Collection<JobTreeElement> result = new HashSet<>();
			for (Entry<Job, JobInfo> entry : jobs.entrySet()) {
				if (!isCurrentDisplaying(entry.getKey(), debug)) {
					JobInfo jobInfo = entry.getValue();
					GroupInfo group = jobInfo.getGroupInfo();
					if (group == null) {
						result.add(jobInfo);
					} else {
						result.add(group);
					}
				}
			}
			JobTreeElement[] infos = new JobTreeElement[result.size()];
			result.toArray(infos);
			return infos;
		}
	}

	/**
	 * Return whether or not there are any jobs being displayed.
	 *
	 * @return boolean
	 */
	public boolean hasJobInfos() {
		synchronized (jobs) {
			return !jobs.isEmpty();
		}
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 *
	 * @param source
	 * @return Image
	 */
	Image getImage(ImageData source) {
		ImageData mask = source.getTransparencyMask();
		return new Image(null, source, mask);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 *
	 * @param fileSystemPath
	 *            The URL for the file system to the image.
	 * @param loader -
	 *            the loader used to get this data
	 * @return ImageData[]
	 */
	ImageData[] getImageData(URL fileSystemPath, ImageLoader loader) {
		try (InputStream stream = fileSystemPath.openStream()) {
			ImageData[] result = loader.load(stream);
			return result;
		} catch (FileNotFoundException exception) {
			ProgressManagerUtil.logException(exception);
			return null;
		} catch (IOException exception) {
			ProgressManagerUtil.logException(exception);
			return null;
		}
	}

	/**
	 * Shutdown the receiver.
	 */
	private void shutdown() {
		listeners.clear();
		Job.getJobManager().setProgressProvider(null);
		Job.getJobManager().removeJobChangeListener(this.changeListener);
	}

	/**
	 * Add the listener to the family.
	 *
	 * @param family
	 * @param listener
	 */
	void addListenerToFamily(Object family, IJobBusyListener listener) {
		synchronized (familyListeners) {
			Collection<IJobBusyListener> currentListeners = familyListeners.get(family);
			if (currentListeners == null) {
				currentListeners = new HashSet<>();
				familyListeners.put(family, currentListeners);
			}
			currentListeners.add(listener);
		}
	}

	/**
	 * Remove the listener from all families.
	 *
	 * @param listener
	 */
	void removeListener(IJobBusyListener listener) {
		synchronized (familyListeners) {
			Iterator<Collection<IJobBusyListener>> familyListeners = this.familyListeners.values().iterator();
			while (familyListeners.hasNext()) {
				Collection<IJobBusyListener> currentListeners = familyListeners.next();
				currentListeners.remove(listener);

				// Remove any empty listeners
				if (currentListeners.isEmpty()) {
					familyListeners.remove();
				}
			}
		}
	}

	/**
	 * Return the listeners for the job.
	 *
	 * @param job
	 * @return Collection of IJobBusyListener
	 */
	@SuppressWarnings("unchecked")
    private Collection<IJobBusyListener> busyListenersForJob(Job job) {
		if (job.isSystem()) {
			return Collections.EMPTY_LIST;
		}
		synchronized (familyListeners) {

			if (familyListeners.isEmpty()) {
				return Collections.EMPTY_LIST;
			}

			Collection<IJobBusyListener> returnValue = new HashSet<>();
			for (Entry<Object, Collection<IJobBusyListener>> entry : familyListeners.entrySet()) {
				if (job.belongsTo(entry.getKey())) {
					Collection<IJobBusyListener> currentListeners = entry.getValue();
					returnValue.addAll(currentListeners);
				}
			}
			return returnValue;
		}
	}

	/**
	 * Check to see if there are any stale jobs we have not cleared out.
	 *
	 * @return <code>true</code> if anything was pruned
	 */
	private boolean pruneStaleJobs() {
		Object[] jobsToCheck = jobs.keySet().toArray();
		boolean pruned = false;
		for (Object element : jobsToCheck) {
			Job job = (Job) element;
			if (checkForStaleness(job)) {
				pruned = true;
			}
		}

		return pruned;
	}

	/**
	 * Check the if the job should be removed from the list as it may be stale.
	 *
	 * @param job
	 * @return boolean
	 */
	boolean checkForStaleness(Job job) {
		if (job.getState() == Job.NONE) {
			removeJobInfo(getJobInfo(job));
			return true;
		}
		return false;
	}

	/**
	 * Return whether or not dialogs should be run in the background
	 *
	 * @return <code>true</code> if the dialog should not be shown.
	 */
	protected boolean shouldRunInBackground() {
		return Preferences.getBoolean(IProgressConstants.RUN_IN_BACKGROUND);
	}

	protected Display getDisplay() {
		return Services.getInstance().getDisplay();
	}

}
