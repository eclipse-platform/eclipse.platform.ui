/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 *     Terry Parker - Bug 454633, Report the cumulative error status of job groups in the ProgressManager
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
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
import org.eclipse.ui.statushandlers.StatusManager.INotificationListener;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;

/**
 * JobProgressManager provides the progress monitor to the job manager and
 * informs any ProgressContentProviders of changes.
 */
public class ProgressManager extends ProgressProvider implements IProgressService {
	/**
	 * A property to determine if the job was run in the dialog. Kept for
	 * backwards compatibility.
	 *
	 * @deprecated
	 * @see IProgressConstants#PROPERTY_IN_DIALOG
	 */
	@Deprecated
	public static final QualifiedName PROPERTY_IN_DIALOG = IProgressConstants.PROPERTY_IN_DIALOG;

	private static final String ERROR_JOB = "errorstate.png"; //$NON-NLS-1$

	static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

	private static ProgressManager singleton;

	final private Set<Job> managedJobs = ConcurrentHashMap.newKeySet();

	final private Map<Object, Collection<IJobBusyListener>> familyListeners = Collections
			.synchronizedMap(new LinkedHashMap<>());

	//	list of IJobProgressManagerListener
	private ListenerList<IJobProgressManagerListener> listeners = new ListenerList<>();

	final IJobChangeListener changeListener;

	static final String PROGRESS_VIEW_NAME = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

	static final String PROGRESS_FOLDER = "$nl$/icons/full/progress/"; //$NON-NLS-1$

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

	final ConcurrentMap<Job, JobMonitor> runnableMonitors = new ConcurrentHashMap<>();

	// A table that maps families to keys in the Jface image table
	private Hashtable<Object, String> imageKeyTable = new Hashtable<>();

	/*
	 * A listener that allows for removing error jobs & indicators when errors
	 * are handled.
	 */
	private final INotificationListener notificationListener;

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
		StatusManager.getManager().removeListener(singleton.notificationListener);
		singleton.shutdown();
	}

	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor
	 * integration with the ProgressMonitor.
	 */
	public class JobMonitor implements IProgressMonitorWithBlocking {
		Job job;
		JobInfo info;
		String currentTaskName;
		Set<IProgressMonitorWithBlocking> monitors = Collections.emptySet();

		/**
		 * Creates a monitor on the supplied job.
		 *
		 * @param newJob
		 */
		JobMonitor(Job newJob, JobInfo jobInfo) {
			job = newJob;
			info = jobInfo;
		}

		public JobInfo getJobInfo() {
			return info;
		}

		/**
		 * Adds monitor as another monitor that
		 *
		 * @param monitor
		 */
		public void addProgressListener(IProgressMonitorWithBlocking monitor) {
			Assert.isNotNull(monitor);
			Set<IProgressMonitorWithBlocking> newSet = new LinkedHashSet<>(monitors);
			newSet.add(monitor);
			this.monitors = Collections.unmodifiableSet(newSet);
			TaskInfo currentTask = info.getTaskInfo();
			if (currentTask != null) {
				monitor.beginTask(currentTaskName, currentTask.totalWork);
				monitor.internalWorked(currentTask.preWork);
			}
		}

		public void removeProgresListener(IProgressMonitorWithBlocking monitor) {
			Set<IProgressMonitorWithBlocking> newSet = new LinkedHashSet<>(monitors);
			newSet.remove(monitor);
			this.monitors = Collections.unmodifiableSet(newSet);
		}

		@Override
		public void beginTask(String taskName, int totalWork) {
			info.beginTask(taskName, totalWork);
			refreshJobInfo(info);
			currentTaskName = taskName;
			monitors.stream().forEach(listener -> listener.beginTask(taskName, totalWork));
		}

		@Override
		public void done() {
			info.clearTaskInfo();
			info.clearChildren();
			monitors.stream().forEach(IProgressMonitorWithBlocking::done);
		}

		@Override
		public void internalWorked(double work) {
			if (info.hasTaskInfo()) {
				info.addWork(work);
				refreshJobInfo(info);
			}
			monitors.stream().forEach(listener -> listener.internalWorked(work));
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
				monitors.stream().forEach(listener -> listener.setCanceled(value));
			}
		}

		@Override
		public void setTaskName(String taskName) {
			if (info.hasTaskInfo()) {
				info.setTaskName(taskName);
			} else {
				beginTask(taskName, 100);
				return;
			}
			info.clearChildren();
			refreshJobInfo(info);
			currentTaskName = taskName;
			monitors.stream().forEach(listener -> listener.setTaskName(taskName));
		}

		@Override
		public void subTask(String name) {
			if (name == null) {
				return;
			}
			info.clearChildren();
			info.addSubTask(name);
			refreshJobInfo(info);
			monitors.stream().forEach(listener -> listener.subTask(name));
		}

		@Override
		public void worked(int work) {
			internalWorked(work);
		}

		@Override
		public void clearBlocked() {
			info.setBlockedStatus(null);
			refreshJobInfo(info);
			monitors.stream().forEach(IProgressMonitorWithBlocking::clearBlocked);
		}

		@Override
		public void setBlocked(IStatus reason) {
			info.setBlockedStatus(reason);
			refreshJobInfo(info);
			monitors.stream().forEach(listener -> listener.setBlocked(reason));
		}
	}

	/**
	 * Creates a new instance of the receiver.
	 */
	ProgressManager() {
		Dialog.setBlockedHandler(new WorkbenchDialogBlockedHandler());

		setUpImages();

		changeListener = createChangeListener();

		notificationListener = createNotificationListener();

		Job.getJobManager().setProgressProvider(this);
		Job.getJobManager().addJobChangeListener(this.changeListener);
		StatusManager.getManager().addListener(notificationListener);

		uiRefreshThrottler = new Throttler(Display.getDefault(), Duration.ofMillis(100), this::notifyListeners);
	}

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

			ImageDescriptor errorImage = ImageDescriptor
					.createFromURL(new URL(iconsRoot, ERROR_JOB));
			JFaceResources.getImageRegistry().put(ERROR_JOB_KEY, errorImage);

		} catch (MalformedURLException e) {
			ProgressManagerUtil.logException(e);
		}
	}

	private INotificationListener createNotificationListener() {
		return (type, adapters) -> {
			if(type == INotificationTypes.HANDLED){
				FinishedJobs.getInstance().removeErrorJobs();
				StatusAdapterHelper.getInstance().clear();
			}
		};
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

				final JobInfo info = progressFor(event.getJob()).getJobInfo();
				removeJobInfo(info);

				/*
				 * Only report severe errors to the StatusManager if the error
				 * is not part of a job group, or if the job is the last job in
				 * a job group. For job groups, the JobManager accumulates the
				 * status of jobs belonging to the group, suppresses the status
				 * reporting of the individual jobs and reports a single
				 * MultiStatus for the group, so mirror that behavior here.
				 */
				StatusAdapter statusAdapter = null;
				if (event.getJobGroupResult() != null && event.getJobGroupResult().getSeverity() == IStatus.ERROR) {
					statusAdapter = new StatusAdapter(event.getJobGroupResult());
				} else if (event.getResult() != null
						&& event.getResult().getSeverity() == IStatus.ERROR
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
			 *
			 * @param event
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
	 * @param info
	 */
	protected void sleepJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			sleepGroup(group,info);
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
	 *
	 * @param group
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
	 *
	 * @param iconsRoot
	 * @param fileName
	 * @param key
	 * @throws MalformedURLException
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
		// only need a default monitor for operations the UI thread
		// and only if there is a display
		Display display;
		if (PlatformUI.isWorkbenchRunning()
				&& !PlatformUI.getWorkbench().isStarting()) {
			display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()
					&& (display.getThread() == Thread.currentThread())) {
				return new EventLoopProgressMonitor(new NullProgressMonitor());
			}
		}
		return super.getDefaultMonitor();
	}

	/**
	 * Returns a monitor for the job. Checks if we cached a monitor for this job
	 * previously for a long operation timeout check.
	 *
	 * @param job
	 * @return IProgressMonitor
	 */
	public JobMonitor progressFor(Job job) {
		return runnableMonitors.computeIfAbsent(job, (j) -> {
			return new JobMonitor(j, new JobInfo(j));
		});
	}

	/**
	 * Adds an IJobProgressManagerListener to listen to the changes.
	 *
	 * @param listener
	 */
	void addListener(IJobProgressManagerListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the supplied IJobProgressManagerListener from the list of
	 * listeners.
	 *
	 * @param listener
	 */
	void removeListener(IJobProgressManagerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refreshes the IJobProgressManagerListeners as a result of a change in
	 * info.
	 *
	 * @param info
	 */
	public void refreshJobInfo(JobInfo info) {
		synchronized (pendingUpdatesMutex) {
			rememberListenersForJob(info, pendingJobUpdates);
		}
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Refreshes the IJobProgressManagerListeners as a result of a change in
	 * info.
	 *
	 * @param info
	 */
	public void refreshGroup(GroupInfo info) {
		synchronized (pendingUpdatesMutex) {
			pendingGroupUpdates.add(info);
		}
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Refreshes the content providers as a result of a deletion of info.
	 *
	 * @param info
	 *            the info to remove
	 */
	public void removeJobInfo(JobInfo info) {
		Job job = info.getJob();
		managedJobs.remove(job);
		synchronized (pendingUpdatesMutex) {
			rememberListenersForJob(info, pendingJobRemoval);
		}
		runnableMonitors.remove(job);
		uiRefreshThrottler.throttledExec();
	}

	/**
	 * Removes the group from the roots and inform the listeners.
	 *
	 * @param group
	 *            GroupInfo
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
	 * @param info
	 */
	public void addJobInfo(JobInfo info) {
		GroupInfo group = info.getGroupInfo();
		if (group != null) {
			refreshGroup(group);
		}

		managedJobs.add(info.getJob());
		synchronized (pendingUpdatesMutex) {
			rememberListenersForJob(info, pendingJobAddition);
		}
		uiRefreshThrottler.throttledExec();
	}

	private void rememberListenersForJob(JobInfo info, Map<JobInfo, Set<IJobProgressManagerListener>> listenersMap) {
		Set<IJobProgressManagerListener> localListeners = listenersMap.computeIfAbsent(info,
				k -> new LinkedHashSet<>());
		StreamSupport.stream(listeners.spliterator(), false)
				.filter(listener -> !isCurrentDisplaying(info.getJob(), listener.showsDebug()))
				.forEach(localListeners::add);
	}

	/**
	 * Returns whether or not this job is currently displayable.
	 *
	 * @param job
	 * @param debug
	 *            if the listener is in debug mode
	 * @return boolean <code>true</code> if the job is not displayed
	 */
	boolean isCurrentDisplaying(Job job, boolean debug) {
		return isNeverDisplaying(job, debug) || job.getState() == Job.SLEEPING;
	}

	/**
	 * Returns whether or not we even display this job with debug mode set to
	 * debug.
	 *
	 * @param job
	 * @param debug
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
	 * @param debug
	 * @return JobInfo[]
	 */
	public JobInfo[] getJobInfos(boolean debug) {
		return managedJobs.stream().filter(job -> !isCurrentDisplaying(job, debug))
				.map(job -> progressFor(job).getJobInfo())
				.toArray(JobInfo[]::new);
	}

	/**
	 * Returns the current root elements filtered on the debug mode.
	 *
	 * @param debug
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
	 * @param loader
	 *            the loader used to get this data
	 * @return ImageData[]
	 */
	ImageData[] getImageData(URL fileSystemPath, ImageLoader loader) {
		try {
			InputStream stream = fileSystemPath.openStream();
			ImageData[] result = loader.load(stream);
			stream.close();
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
	 *
	 * @param dialogWaitRunnable
	 * @param dialog
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
	 * @param dialog
	 *            the dialog to open
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
	public IProgressMonitor createMonitor(Job job, IProgressMonitor group,
			int ticks) {
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
	 *
	 * @param family
	 * @param listener
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
	 *
	 * @param listener
	 */
	void removeListener(IJobBusyListener listener) {
		synchronized (familyListeners) {
			Iterator<Object> families = familyListeners.keySet().iterator();
			while (families.hasNext()) {
				Object next = families.next();
				Collection<IJobBusyListener> currentListeners = familyListeners.get(next);
				currentListeners.remove(listener);

				// Remove any empty listeners.
				if (currentListeners.isEmpty()) {
					families.remove();
				}
			}
		}
	}

	/**
	 * Returns the listeners for the job.
	 *
	 * @param job
	 * @return Collection of IJobBusyListener
	 */
	private Collection<IJobBusyListener> busyListenersForJob(Job job) {
		if (job.isSystem()) {
			return Collections.EMPTY_LIST;
		}
		synchronized (familyListeners) {
			if (familyListeners.isEmpty()) {
				return Collections.EMPTY_LIST;
			}

			Iterator<Object> families = familyListeners.keySet().iterator();
			Collection<IJobBusyListener> returnValue = new LinkedHashSet<>();
			while (families.hasNext()) {
				Object next = families.next();
				if (job.belongsTo(next)) {
					Collection<IJobBusyListener> currentListeners = familyListeners.get(next);
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
		String key = IMAGE_KEY + String.valueOf(imageKeyTable.size());
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
	 * Iterates through all of the windows and set them to be disabled or
	 * enabled as appropriate.
	 *
	 * @param active
	 *            the state the windows will be set to
	 */
	private void setUserInterfaceActive(boolean active) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell[] shells = workbench.getDisplay().getShells();
		if (active) {
			for (int i = 0; i < shells.length; i++) {
				if (!shells[i].isDisposed()) {
					shells[i].setEnabled(active);
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
	 * @param job
	 * @return boolean
	 */
	boolean checkForStaleness(Job job) {
		if (job.getState() == Job.NONE) {
			removeJobInfo(progressFor(job).getJobInfo());
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
		return WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				IPreferenceConstants.RUN_IN_BACKGROUND);
	}

	/**
	 * Sets whether or not the ProgressViewUpdater should show system jobs.
	 *
	 * @param showSystem
	 */
	public void setShowSystemJobs(boolean showSystem) {
		ProgressViewUpdater updater = ProgressViewUpdater.getSingleton();
		updater.debug = showSystem;
		updater.refreshAll();
	}

	private class RunnableWithStatus implements Runnable {
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
			} catch (InvocationTargetException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} catch (InterruptedException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} catch (OperationCanceledException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} finally {
				manager.endRule(rule);
			}
		}

		/**
		 * Returns a progress monitor that forwards to an event loop monitor.
		 * Overrides #setBlocked() so that we always open the blocked dialog.
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
					Dialog.getBlockedHandler().showBlocked(
							ProgressManagerUtil.getDefaultParent(), this, reason, getTaskName());
				}
			};
		}

		public IStatus getStatus() {
			return status;
		}
	}
}
