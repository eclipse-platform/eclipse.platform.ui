/*******************************************************************************
 * Copyright (c) 2003, 2022 IBM Corporation and others.
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
 *     Teddy Walker <teddy.walker@googlemail.com>
 *     		- Fix for Bug 151204 [Progress] Blocked status of jobs are not applied/reported
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *     Terry Parker - Bug 454633, Report the cumulative error status of job groups in the ProgressManager
 *     Christoph Läubrich - Issue #8
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Throttler;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.dialogs.WorkbenchDialogBlockedHandler;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * JobProgressManager provides the progress monitor to the job manager and
 * informs any ProgressContentProviders of changes.
 */
public class ProgressManager extends ProgressProvider implements IProgressService {
	/**
	 * A property to determine if the job was run in the dialog. Kept for backwards
	 * compatibility.
	 *
	 * @deprecated use IProgressConstants#PROPERTY_IN_DIALOG instead
	 */
	@Deprecated
	public static final QualifiedName PROPERTY_IN_DIALOG = IProgressConstants.PROPERTY_IN_DIALOG;

	private static final String ERROR_JOB = "errorstate.svg"; //$NON-NLS-1$

	static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

	private static ProgressManager singleton;

	final private Set<Job> managedJobs = ConcurrentHashMap.newKeySet();

	final private Map<Object, Collection<IJobBusyListener>> familyListeners = Collections
			.synchronizedMap(new LinkedHashMap<>());

	// list of IJobProgressManagerListener
	private ListenerList<IJobProgressManagerListener> listeners = new ListenerList<>();

	final IJobChangeListener changeListener;

	static final String PROGRESS_VIEW_NAME = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

	static final String PROGRESS_FOLDER = "$nl$/icons/full/progress/"; //$NON-NLS-1$

	private static final String SLEEPING_JOB = "sleeping.svg"; //$NON-NLS-1$

	private static final String WAITING_JOB = "waiting.svg"; //$NON-NLS-1$

	private static final String BLOCKED_JOB = "lockedstate.svg"; //$NON-NLS-1$

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

	final Map<Job, JobMonitor> runnableMonitors = new HashMap<>();

	// A table that maps families to keys in the Jface image table
	private Hashtable<Object, String> imageKeyTable = new Hashtable<>();

	/**
	 * Lock object for synchronizing updates of {@code pendingJobUpdates},
	 * {@code pendingGroupUpdates}, {@code pendingJobRemoval},
	 * {@code pendingGroupRemoval} and {@code pendingJobAddition}.
	 */
	private final Object pendingUpdatesMutex = new Object();

	/**
	 * Modification guarded by {@link #pendingUpdatesMutex}.
	 */
	private Map<JobInfo, Set<IJobProgressManagerListener>> pendingJobUpdates = new LinkedHashMap<>();

	/**
	 * Modification guarded by {@link #pendingUpdatesMutex}.
	 */
	private Set<GroupInfo> pendingGroupUpdates = new LinkedHashSet<>();

	/**
	 * Modification guarded by {@link #pendingUpdatesMutex}.
	 */
	private Map<JobInfo, Set<IJobProgressManagerListener>> pendingJobRemoval = new LinkedHashMap<>();

	/**
	 * Modification guarded by {@link #pendingUpdatesMutex}.
	 */
	private Set<GroupInfo> pendingGroupRemoval = new LinkedHashSet<>();

	/**
	 * Modification guarded by {@link #pendingUpdatesMutex}.
	 */
	private Map<JobInfo, Set<IJobProgressManagerListener>> pendingJobAddition = new LinkedHashMap<>();

	private static final String IMAGE_KEY = "org.eclipse.ui.progress.images"; //$NON-NLS-1$

	private final Throttler uiRefreshThrottler;

	/**
	 * Returns the progress manager currently in use.
	 *
	 * @return JobProgressManager
	 */
	public static ProgressManager getInstance() {
		if (singleton == null) {
			singleton = new ProgressManager();
		}
		return singleton;
	}

	/**
	 * Shuts down the singleton if there is one.
	 */
	public static void shutdownProgressManager() {
		if (singleton == null) {
			return;
		}
		singleton.shutdown();
	}

	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor
	 * integration with the ProgressMonitor.
	 */
	public class JobMonitor implements IProgressMonitor {
		Job job;
		JobInfo info;
		String currentTaskName;
		Set<IProgressMonitor> monitors = Collections.emptySet();

		/**
		 * Creates a monitor on the supplied job.
		 *
		 * @param newJob  the job this monitor is created for
		 * @param jobInfo the info object for the job
		 */
		JobMonitor(Job newJob, JobInfo jobInfo) {
			job = newJob;
			info = jobInfo;
		}

		/**
		 * Get the monitored job's information.
		 *
		 * @return job info
		 */
		public JobInfo getJobInfo() {
			return info;
		}

		/**
		 * Adds monitor as another monitor that
		 *
		 * @param monitor the listening monitor to add
		 */
		public void addProgressListener(IProgressMonitor monitor) {
			Assert.isNotNull(monitor);
			Set<IProgressMonitor> newSet = new LinkedHashSet<>(monitors);
			newSet.add(monitor);
			this.monitors = Collections.unmodifiableSet(newSet);
			Optional<TaskInfo> optionalInfo = info.getTaskInfo();
			if (optionalInfo.isPresent()) {
				TaskInfo currentTask = optionalInfo.get();
				monitor.beginTask(currentTaskName, currentTask.totalWork);
				monitor.internalWorked(currentTask.preWork);
			}
		}

		/**
		 * Removes progress listener.
		 *
		 * @param monitor the listening monitor to remove
		 */
		public void removeProgresListener(IProgressMonitor monitor) {
			Set<IProgressMonitor> newSet = new LinkedHashSet<>(monitors);
			newSet.remove(monitor);
			this.monitors = Collections.unmodifiableSet(newSet);
		}

		@Override
		public void beginTask(String taskName, int totalWork) {
			info.beginTask(taskName, totalWork);
			refreshJobInfo(info);
			currentTaskName = taskName;
			monitors.forEach(listener -> listener.beginTask(taskName, totalWork));
		}

		@Override
		public void done() {
			info.clearTaskInfo();
			info.clearChildren();
			monitors.forEach(t -> t.done());
		}

		@Override
		public void internalWorked(double work) {
			if (info.getTaskInfo().isPresent()) {
				info.addWork(work);
				refreshJobInfo(info);
			}
			monitors.forEach(listener -> listener.internalWorked(work));
		}

		@Override
		public boolean isCanceled() {
			return info.isCanceled();
		}

		@Override
		public void setCanceled(boolean value) {
			// Don't bother canceling twice.
			if (value && !info.isCanceled()) {
				info.cancel();
				// Only inform the first time
				monitors.forEach(listener -> listener.setCanceled(value));
			}
		}

		@Override
		public void setTaskName(String taskName) {
			if (info.getTaskInfo().isPresent()) {
				info.setTaskName(taskName);
			} else {
				beginTask(taskName, 100);
				return;
			}
			info.clearChildren();
			refreshJobInfo(info);
			currentTaskName = taskName;
			monitors.forEach(listener -> listener.setTaskName(taskName));
		}

		@Override
		public void subTask(String name) {
			if (name == null) {
				return;
			}
			info.clearChildren();
			info.addSubTask(name);
			refreshJobInfo(info);
			monitors.forEach(listener -> listener.subTask(name));
		}

		@Override
		public void worked(int work) {
			internalWorked(work);
		}

		@Override
		public void clearBlocked() {
			info.setBlockedStatus(null);
			refreshJobInfo(info);
			monitors.forEach(IProgressMonitor::clearBlocked);
		}

		@Override
		public void setBlocked(IStatus reason) {
			info.setBlockedStatus(reason);
			refreshJobInfo(info);
			monitors.forEach(listener -> listener.setBlocked(reason));
		}
	}

	/**
	 * Creates a new instance of the receiver.
	 */
	ProgressManager() {
		Dialog.setBlockedHandler(new WorkbenchDialogBlockedHandler());

		setUpImages();

		uiRefreshThrottler = new Throttler(Display.getDefault(), Duration.ofMillis(100), this::notifyListeners);
		changeListener = createChangeListener();

		Job.getJobManager().setProgressProvider(this);
		Job.getJobManager().addJobChangeListener(this.changeListener);
	}

	/**
	 * Send pending notifications to listeners.
	 */
	/* Visible for testing */ public void notifyListeners() {
		Set<GroupInfo> localPendingGroupUpdates, localPendingGroupRemoval;
		Map<JobInfo, Set<IJobProgressManagerListener>> localPendingJobUpdates, localPendingJobAddition,
				localPendingJobRemoval;
		synchronized (pendingUpdatesMutex) {
			localPendingJobUpdates = pendingJobUpdates;
			pendingJobUpdates = new LinkedHashMap<>();
			localPendingGroupUpdates = pendingGroupUpdates;
			pendingGroupUpdates = new LinkedHashSet<>();
			localPendingJobRemoval = pendingJobRemoval;
			pendingJobRemoval = new LinkedHashMap<>();
			localPendingGroupRemoval = pendingGroupRemoval;
			pendingGroupRemoval = new LinkedHashSet<>();
			localPendingJobAddition = pendingJobAddition;
			pendingJobAddition = new LinkedHashMap<>();
		}

		localPendingJobAddition.entrySet().forEach(e -> e.getValue().forEach(listener -> listener.addJob(e.getKey())));

		// Adds all non null JobInfo#getGroupInfo to the list of groups to
		// be refreshed
		localPendingJobUpdates.entrySet().stream().map(e -> e.getKey().getGroupInfo()).filter(Objects::nonNull)
				.forEach(localPendingGroupUpdates::add);

		localPendingJobUpdates.entrySet()
				.forEach(e -> e.getValue().forEach(listener -> listener.refreshJobInfo(e.getKey())));

		// refresh groups
		localPendingGroupUpdates.forEach(groupInfo -> listeners.forEach(listener -> listener.refreshGroup(groupInfo)));

		localPendingJobRemoval.entrySet()
				.forEach(e -> e.getValue().forEach(listener -> listener.removeJob(e.getKey())));

		localPendingGroupRemoval.forEach(group -> {
			listeners.forEach(listener -> listener.removeGroup(group));
		});
	}

	private void setUpImages() {
		URL iconsRoot = ProgressManagerUtil.getIconsRoot();
		try {
			setUpImage(iconsRoot, SLEEPING_JOB, SLEEPING_JOB_KEY);
			setUpImage(iconsRoot, WAITING_JOB, WAITING_JOB_KEY);
			setUpImage(iconsRoot, BLOCKED_JOB, BLOCKED_JOB_KEY);

			ImageDescriptor errorImage = ImageDescriptor.createFromURL(new URL(iconsRoot, ERROR_JOB));
			JFaceResources.getImageRegistry().put(ERROR_JOB_KEY, errorImage);

		} catch (MalformedURLException e) {
			ProgressManagerUtil.logException(e);
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
				JobInfo info = progressFor(event.getJob()).getJobInfo();
				refreshJobInfo(info);
				Iterator<IJobBusyListener> startListeners = busyListenersForJob(event.getJob()).iterator();
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
				Iterator<IJobBusyListener> startListeners = busyListenersForJob(event.getJob()).iterator();
				while (startListeners.hasNext()) {
					IJobBusyListener next = startListeners.next();
					next.decrementBusy(event.getJob());
				}

				final JobInfo info = removeJob(event.getJob());

				/*
				 * Only report severe errors to the StatusManager if the error is not part of a
				 * job group, or if the job is the last job in a job group. For job groups, the
				 * JobManager accumulates the status of jobs belonging to the group, suppresses
				 * the status reporting of the individual jobs and reports a single MultiStatus
				 * for the group, so mirror that behavior here.
				 */
				StatusAdapter statusAdapter = null;
				if (event.getJobGroupResult() != null && event.getJobGroupResult().getSeverity() == IStatus.ERROR) {
					statusAdapter = new StatusAdapter(event.getJobGroupResult());
				} else if (event.getResult() != null && event.getResult().getSeverity() == IStatus.ERROR
						&& (event.getJob() == null || event.getJob().getJobGroup() == null)) {
					statusAdapter = new StatusAdapter(event.getResult());
					statusAdapter.addAdapter(Job.class, event.getJob());
				}
				if (statusAdapter != null) {
					if (event.getJob()
							.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) == Boolean.TRUE) {
						statusAdapter.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
						StatusAdapterHelper.getInstance().putStatusAdapter(info, statusAdapter);
					}

					StatusManager.getManager().handle(statusAdapter, StatusManager.SHOW);
				}
			}

			@Override
			public void scheduled(IJobChangeEvent event) {
				updateFor(event);
				if (event.getJob().isUser()) {
					boolean noDialog = shouldRunInBackground();
					if (!noDialog) {
						final IJobChangeEvent finalEvent = event;
						WorkbenchJob showJob = new WorkbenchJob(ProgressMessages.ProgressManager_showInDialogName) {
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								showInDialog(null, finalEvent.getJob());
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
			 * Updates the listeners for the receiver for the event.
			 */
			private void updateFor(IJobChangeEvent event) {
				if (managedJobs.contains(event.getJob())) {
					refreshJobInfo(progressFor(event.getJob()).getJobInfo());
				} else {
					addJobInfo(progressFor(event.getJob()).getJobInfo());
				}
			}

			@Override
			public void awake(IJobChangeEvent event) {
				updateFor(event);
			}

			@Override
			public void sleeping(IJobChangeEvent event) {
				if (managedJobs.contains(event.getJob()))// Are we showing this?
					sleepJobInfo(progressFor(event.getJob()).getJobInfo());
			}
		};
	}

	/**
	 * The job in JobInfo is now sleeping. Refreshes it if we are showing it,
	 * removes it if not.
	 *
	 * @param info the job going to sleep
	 */
	protected void sleepJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			sleepGroup(group, info);
		}

		for (IJobProgressManagerListener listener : listeners) {
			// Is this one the user never sees?
			if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
				continue;
			if (listener.showsDebug()) {
				listener.refreshJobInfo(info);
			} else {
				listener.removeJob(info);
			}
		}
	}

	/**
	 * Refreshes the group when info is sleeping.
	 */
	private void sleepGroup(GroupInfo group, JobInfo info) {
		for (IJobProgressManagerListener listener : listeners) {
			if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
				continue;

			if (listener.showsDebug() || group.isActive()) {
				listener.refreshGroup(group);
			} else {
				listener.removeGroup(group);
			}
		}
	}

	/**
	 * Sets up the image in the image registry.
	 */
	private void setUpImage(URL iconsRoot, String fileName, String key) throws MalformedURLException {
		JFaceResources.getImageRegistry().put(key, ImageDescriptor.createFromURL(new URL(iconsRoot, fileName)));
	}

	@Override
	public IProgressMonitor createMonitor(Job job) {
		return progressFor(job);
	}

	@Override
	public IProgressMonitor getDefaultMonitor() {
		return monitorFor(null);
	}

	@Override
	public IProgressMonitor monitorFor(IProgressMonitor monitor) {
		// only need a default monitor for operations the UI thread
		// and only if there is a display
		Display display;
		if (PlatformUI.isWorkbenchRunning() && !PlatformUI.getWorkbench().isStarting()) {
			display = PlatformUI.getWorkbench().getDisplay();
			boolean isDisplayThread;
			try {
				isDisplayThread = !display.isDisposed() && (display.getThread() == Thread.currentThread());
			} catch (SWTException deviceDisposed) {
				// Maybe disposed after .isDisposed() check.
				isDisplayThread = false;
			}
			if (isDisplayThread) {
				return new EventLoopProgressMonitor(IProgressMonitor.nullSafe(monitor));
			}
		}
		return IProgressMonitor.nullSafe(monitor);
	}

	/**
	 * Returns a monitor for the job. Checks if we cached a monitor for this job
	 * previously for a long operation timeout check.
	 *
	 * @param job the job to (progress) monitor
	 * @return a monitor for the job. Might be an existing monitor for this job.
	 */
	public JobMonitor progressFor(Job job) {
		synchronized (runnableMonitors) {
			return runnableMonitors.computeIfAbsent(job, j -> new JobMonitor(j, new JobInfo(j)));
		}
	}

	/**
	 * Adds an IJobProgressManagerListener to listen to the changes.
	 */
	void addListener(IJobProgressManagerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the supplied IJobProgressManagerListener from the list of listeners.
	 */
	void removeListener(IJobProgressManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refreshes the IJobProgressManagerListeners as a result of a change in info.
	 *
	 * @param info the updated job info
	 */
	public void refreshJobInfo(JobInfo info) {
		checkForStaleness(info.getJob());
		synchronized (pendingUpdatesMutex) {
			Predicate<IJobProgressManagerListener> predicate = listener -> !isNeverDisplaying(info.getJob(), listener.showsDebug());
			rememberListenersForJob(info, pendingJobUpdates, predicate);
		}
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Refreshes the IJobProgressManagerListeners as a result of a change in info.
	 *
	 * @param info the updated job group
	 */
	public void refreshGroup(GroupInfo info) {
		synchronized (pendingUpdatesMutex) {
			pendingGroupUpdates.add(info);
		}
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Refreshes the content providers as a result of a deletion of job.
	 *
	 * @param job the job to remove information about
	 * @return the removed job info
	 */
	public JobInfo removeJob(Job job) {
		JobInfo info;
		synchronized (runnableMonitors) {
			info = progressFor(job).getJobInfo();
			managedJobs.remove(job);
			synchronized (pendingUpdatesMutex) {
				Predicate<IJobProgressManagerListener> predicate = listener -> !isNeverDisplaying(info.getJob(), listener.showsDebug());
				rememberListenersForJob(info, pendingJobRemoval, predicate);
			}
			runnableMonitors.remove(job);
		}
		uiRefreshThrottler.throttledExec();
		return info;
	}

	/**
	 * Refreshes the content providers as a result of a deletion of info.
	 *
	 * @param info the info to remove
	 * @deprecated use the more thread safe {@link #removeJob(Job)} instead. See bug
	 *             558655.
	 */
	@Deprecated
	public void removeJobInfo(JobInfo info) {
		removeJob(info.getJob());
	}

	/**
	 * Removes the group from the roots and inform the listeners.
	 *
	 * @param group GroupInfo
	 */
	public void removeGroup(GroupInfo group) {
		synchronized (pendingUpdatesMutex) {
			pendingGroupRemoval.add(group);
		}
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Refreshes the content providers as a result of an addition of info.
	 *
	 * @param info the added job info
	 */
	public void addJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			refreshGroup(group);
		}

		managedJobs.add(info.getJob());
		synchronized (pendingUpdatesMutex) {
			Predicate<IJobProgressManagerListener> predicate = listener -> !isCurrentDisplaying(info.getJob(), listener.showsDebug());
			rememberListenersForJob(info, pendingJobAddition, predicate);
		}
		uiRefreshThrottler.throttledExec();
	}

	private void rememberListenersForJob(JobInfo info, Map<JobInfo, Set<IJobProgressManagerListener>> listenersMap, Predicate<IJobProgressManagerListener> predicate) {
		Set<IJobProgressManagerListener> localListeners = listenersMap.computeIfAbsent(info,
				k -> new LinkedHashSet<>());
		listeners.stream().filter(predicate).forEach(localListeners::add);
	}

	/**
	 * Returns whether or not this job is currently displayable.
	 *
	 * @param debug if the listener is in debug mode
	 * @return boolean <code>true</code> if the job is not displayed
	 */
	boolean isCurrentDisplaying(Job job, boolean debug) {
		return isNeverDisplaying(job, debug) || job.getState() == Job.SLEEPING;
	}

	/**
	 * Returns whether or not we even display this job with debug mode set to debug.
	 *
	 * @return boolean
	 */
	boolean isNeverDisplaying(Job job, boolean debug) {
		if (debug)
			return false;

		return job.isSystem();
	}

	/**
	 * Returns the current job infos filtered on debug mode.
	 *
	 * @param debug if the listener is in debug mode
	 * @return JobInfo[]
	 */
	public JobInfo[] getJobInfos(boolean debug) {
		return managedJobs.stream().filter(job -> !isCurrentDisplaying(job, debug))
				.map(job -> progressFor(job).getJobInfo()).toArray(JobInfo[]::new);
	}

	/**
	 * Returns the current root elements filtered on the debug mode.
	 *
	 * @param debug if the listener is in debug mode
	 * @return JobTreeElement[]
	 */
	public JobTreeElement[] getRootElements(boolean debug) {
		return managedJobs.stream().filter(job -> !isCurrentDisplaying(job, debug)).map(job -> {
			JobInfo jobInfo = progressFor(job).getJobInfo();
			GroupInfo group = jobInfo.getGroupInfo();
			if (group == null) {
				return jobInfo;
			}
			return group;
		}).distinct().toArray(JobTreeElement[]::new);
	}

	/**
	 * Returns whether or not there are any jobs being displayed.
	 *
	 * @return boolean
	 */
	public boolean hasJobInfos() {
		return !managedJobs.isEmpty();
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 *
	 * @return Image
	 */
	Image getImage(ImageData source) {
		ImageData mask = source.getTransparencyMask();
		return new Image(null, source, mask);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 *
	 * @param fileSystemPath The URL for the file system to the image.
	 * @param loader         the loader used to get this data
	 * @return ImageData[]
	 */
	ImageData[] getImageData(URL fileSystemPath, ImageLoader loader) {
		try {
			ImageData[] result;
			try (InputStream stream = fileSystemPath.openStream()) {
				result = loader.load(stream);
			}
			return result;
		} catch (IOException exception) {
			ProgressManagerUtil.logException(exception);
			return null;
		}
	}

	@Override
	public void busyCursorWhile(final IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(ProgressManagerUtil.getDefaultParent());
		dialog.setOpenOnRun(false);
		final InvocationTargetException[] invokes = new InvocationTargetException[1];
		final InterruptedException[] interrupt = new InterruptedException[1];
		// Show a busy cursor until the dialog opens.
		Runnable dialogWaitRunnable = () -> {
			try {
				dialog.setOpenOnRun(false);
				setUserInterfaceActive(false);
				dialog.run(true, true, runnable);
			} catch (InvocationTargetException e1) {
				invokes[0] = e1;
			} catch (InterruptedException e2) {
				interrupt[0] = e2;
			} finally {
				setUserInterfaceActive(true);
			}
		};
		busyCursorWhile(dialogWaitRunnable, dialog);
		if (invokes[0] != null) {
			throw invokes[0];
		}
		if (interrupt[0] != null) {
			throw interrupt[0];
		}
	}

	/**
	 * Shows the busy cursor while the runnable is running. Schedule a job to
	 * replace it with a progress dialog.
	 */
	private void busyCursorWhile(Runnable dialogWaitRunnable, ProgressMonitorJobsDialog dialog) {
		// Create the job that will open the dialog after a delay.
		scheduleProgressMonitorJob(dialog);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display == null) {
			return;
		}
		// Show a busy cursor until the dialog opens.
		BusyIndicator.showWhile(display, dialogWaitRunnable);
	}

	/**
	 * Schedules the job that will open the progress monitor dialog.
	 *
	 * @param dialog the dialog to open
	 */
	private void scheduleProgressMonitorJob(final ProgressMonitorJobsDialog dialog) {
		final WorkbenchJob updateJob = new WorkbenchJob(ProgressMessages.ProgressManager_openJobName) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				setUserInterfaceActive(true);
				if (ProgressManagerUtil.safeToOpen(dialog, null)) {
					dialog.open();
				}
				return Status.OK_STATUS;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule(getLongOperationTime());

	}

	/**
	 * Shuts down the receiver.
	 */
	private void shutdown() {
		listeners.clear();
		Job.getJobManager().setProgressProvider(null);
		Job.getJobManager().removeJobChangeListener(this.changeListener);
	}

	@Override
	public IProgressMonitor createProgressGroup() {
		return new GroupInfo();
	}

	@Override
	public IProgressMonitor createMonitor(Job job, IProgressMonitor group, int ticks) {
		JobMonitor monitor = progressFor(job);
		if (group instanceof GroupInfo) {
			GroupInfo groupInfo = (GroupInfo) group;
			JobInfo jobInfo = monitor.getJobInfo();
			jobInfo.setGroupInfo(groupInfo);
			jobInfo.setTicks(ticks);
			groupInfo.addJobInfo(jobInfo);
		}
		return monitor;
	}

	/**
	 * Adds the listener to the family.
	 */
	void addListenerToFamily(Object family, IJobBusyListener listener) {
		synchronized (familyListeners) {
			Collection<IJobBusyListener> currentListeners = familyListeners.get(family);
			if (currentListeners == null) {
				currentListeners = new LinkedHashSet<>();
				familyListeners.put(family, currentListeners);
			}
			currentListeners.add(listener);
		}
	}

	/**
	 * Removes the listener from all families.
	 */
	void removeListener(IJobBusyListener listener) {
		synchronized (familyListeners) {
			Iterator<Collection<IJobBusyListener>> familyListeners = this.familyListeners.values().iterator();
			while (familyListeners.hasNext()) {
				Collection<IJobBusyListener> currentListeners = familyListeners.next();
				currentListeners.remove(listener);

				// Remove any empty listeners.
				if (currentListeners.isEmpty()) {
					familyListeners.remove();
				}
			}
		}
	}

	/**
	 * Returns the listeners for the job.
	 *
	 * @return Collection of IJobBusyListener
	 */
	private Collection<IJobBusyListener> busyListenersForJob(Job job) {
		if (job.isSystem()) {
			return Collections.emptyList();
		}
		synchronized (familyListeners) {
			if (familyListeners.isEmpty()) {
				return Collections.emptyList();
			}

			Collection<IJobBusyListener> returnValue = new LinkedHashSet<>();
			for (Entry<Object, Collection<IJobBusyListener>> entry : familyListeners.entrySet()) {
				if (job.belongsTo(entry.getKey())) {
					Collection<IJobBusyListener> currentListeners = entry.getValue();
					returnValue.addAll(currentListeners);
				}
			}
			return returnValue;
		}
	}

	@Override
	public void showInDialog(Shell shell, Job job) {
		if (shouldRunInBackground()) {
			return;
		}

		final ProgressMonitorFocusJobDialog dialog = new ProgressMonitorFocusJobDialog(shell);
		dialog.show(job, shell);
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		if (!fork || !cancelable) {
			// Backward compatible code.
			final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(null);
			dialog.run(fork, cancelable, runnable);
			return;
		}

		busyCursorWhile(runnable);
	}

	@Override
	public void runInUI(final IRunnableContext context, final IRunnableWithProgress runnable,
			final ISchedulingRule rule) throws InvocationTargetException, InterruptedException {
		final RunnableWithStatus runnableWithStatus = new RunnableWithStatus(context, runnable, rule);
		final Display display = Display.getDefault();
		display.syncExec(() -> BusyIndicator.showWhile(display, runnableWithStatus));

		IStatus status = runnableWithStatus.getStatus();
		if (!status.isOK()) {
			Throwable exception = status.getException();
			if (exception instanceof InvocationTargetException) {
				throw (InvocationTargetException) exception;
			} else if (exception instanceof InterruptedException) {
				throw (InterruptedException) exception;
			} else { // should be OperationCanceledException
				throw new InterruptedException(exception.getMessage());
			}
		}
	}

	@Override
	public int getLongOperationTime() {
		return 800;
	}

	@Override
	public void registerIconForFamily(ImageDescriptor icon, Object family) {
		String key = IMAGE_KEY + imageKeyTable.size();
		imageKeyTable.put(family, key);
		ImageRegistry registry = JFaceResources.getImageRegistry();

		// Avoid registering twice.
		if (registry.getDescriptor(key) == null) {
			registry.put(key, icon);
		}

	}

	@Override
	public Image getIconFor(Job job) {
		Enumeration<Object> families = imageKeyTable.keys();
		while (families.hasMoreElements()) {
			Object next = families.nextElement();
			if (job.belongsTo(next)) {
				return JFaceResources.getImageRegistry().get(imageKeyTable.get(next));
			}
		}
		return null;
	}

	/**
	 * Iterates through all of the windows and set them to be disabled or enabled as
	 * appropriate.
	 *
	 * @param active the state the windows will be set to
	 */
	private void setUserInterfaceActive(boolean active) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell[] shells = workbench.getDisplay().getShells();
		if (active) {
			for (Shell shell : shells) {
				if (!shell.isDisposed()) {
					shell.setEnabled(active);
				}
			}
		} else {
			// Deactivate shells in reverse order.
			for (int i = shells.length - 1; i >= 0; i--) {
				if (!shells[i].isDisposed()) {
					shells[i].setEnabled(active);
				}
			}
		}
	}

	/**
	 * Checks the if the job should be removed from the list as it may be stale.
	 *
	 * @return boolean
	 */
	boolean checkForStaleness(Job job) {
		if (job.getState() == Job.NONE) {
			removeJob(job);
			return true;
		}
		return false;
	}

	/**
	 * Returns whether or not dialogs should be run in the background
	 *
	 * @return <code>true</code> if the dialog should not be shown
	 */
	private boolean shouldRunInBackground() {
		return WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND);
	}

	/**
	 * Sets whether or not the ProgressViewUpdater should show system jobs.
	 *
	 * @param showSystem <code>true</code> to show system jobs
	 */
	public void setShowSystemJobs(boolean showSystem) {
		ProgressViewUpdater updater = ProgressViewUpdater.getSingleton();
		updater.debug = showSystem;
		updater.refreshAll();
	}

	private static class RunnableWithStatus implements Runnable {
		IStatus status = Status.OK_STATUS;
		private final IRunnableContext context;
		private final IRunnableWithProgress runnable;
		private final ISchedulingRule rule;

		public RunnableWithStatus(IRunnableContext context, IRunnableWithProgress runnable, ISchedulingRule rule) {
			this.context = context;
			this.runnable = runnable;
			this.rule = rule;
		}

		@Override
		public void run() {
			IJobManager manager = Job.getJobManager();
			try {
				manager.beginRule(rule, getEventLoopMonitor());
				context.run(false, false, runnable);
			} catch (InvocationTargetException | InterruptedException | OperationCanceledException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} finally {
				manager.endRule(rule);
			}
		}

		/**
		 * Returns a progress monitor that forwards to an event loop monitor. Overrides
		 * #setBlocked() so that we always open the blocked dialog.
		 *
		 * @return the monitor on the event loop
		 */
		private IProgressMonitor getEventLoopMonitor() {
			if (PlatformUI.getWorkbench().isStarting())
				return new NullProgressMonitor();

			return new EventLoopProgressMonitor(new NullProgressMonitor()) {
				@Override
				public void setBlocked(IStatus reason) {
					// Set a shell to open with as we want to create this even
					// if there is a modal shell.
					Dialog.getBlockedHandler().showBlocked(ProgressManagerUtil.getDefaultParent(), this, reason,
							getTaskName());
				}
			};
		}

		public IStatus getStatus() {
			return status;
		}
	}
}
