/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Teddy Walker <teddy.walker@googlemail.com> 
 *     		- Fix for Bug 151204 [Progress] Blocked status of jobs are not applied/reported
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.dialogs.WorkbenchDialogBlockedHandler;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * JobProgressManager provides the progress monitor to the job manager and
 * informs any ProgressContentProviders of changes.
 */
public class ProgressManager extends ProgressProvider implements
		IProgressService {
	/**
	 * A property to determine if the job was run in the dialog. Kept for
	 * backwards compatability.
	 * 
	 * @deprecated
	 * @see IProgressConstants#PROPERTY_IN_DIALOG
	 */
	public static final QualifiedName PROPERTY_IN_DIALOG = IProgressConstants.PROPERTY_IN_DIALOG;

	private static final String ERROR_JOB = "errorstate.gif"; //$NON-NLS-1$

	static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

	private static ProgressManager singleton;

	final private Map jobs = Collections.synchronizedMap(new HashMap());

	final private Map familyListeners = Collections
			.synchronizedMap(new HashMap());

	final Object familyKey = new Object();

	private IJobProgressManagerListener[] listeners = new IJobProgressManagerListener[0];

	final Object listenersKey = new Object();

	IJobChangeListener changeListener;

	static final String PROGRESS_VIEW_NAME = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

	static final String PROGRESS_FOLDER = "$nl$/icons/full/progress/"; //$NON-NLS-1$

	private static final String SLEEPING_JOB = "sleeping.gif"; //$NON-NLS-1$

	private static final String WAITING_JOB = "waiting.gif"; //$NON-NLS-1$

	private static final String BLOCKED_JOB = "lockedstate.gif"; //$NON-NLS-1$

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

	final Map runnableMonitors = Collections.synchronizedMap(new HashMap());

	final Object monitorKey = new Object();

	FinishedJobs finishedJobs;

	// A table that maps families to keys in the Jface image
	// table
	private Hashtable imageKeyTable = new Hashtable();

	private static final String IMAGE_KEY = "org.eclipse.ui.progress.images"; //$NON-NLS-1$

	/**
	 * Get the progress manager currently in use.
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
	 * Shutdown the singleton if there is one.
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
		 *      int)
		 */
		public void beginTask(String taskName, int totalWork) {
			JobInfo info = getJobInfo(job);
			info.beginTask(taskName, totalWork);
			refreshJobInfo(info);
			currentTaskName = taskName;
			if (listener != null) {
				listener.beginTask(taskName, totalWork);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#done()
		 */
		public void done() {
			JobInfo info = getJobInfo(job);
			info.clearTaskInfo();
			info.clearChildren();
			runnableMonitors.remove(job);
			if (listener != null) {
				listener.done();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			// Use the internal get so we don't create a Job Info for
			// a job that is not running (see bug 149857)
			JobInfo info = internalGetJobInfo(job);
			if (info == null)
				return false;
			return info.isCanceled();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			internalWorked(work);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
		 */
		public void clearBlocked() {
			JobInfo info = getJobInfo(job);
			info.setBlockedStatus(null);
			refreshJobInfo(info);
			if (listener != null) {
				listener.clearBlocked();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
		 */
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
	 * Create a new instance of the receiver.
	 */
	ProgressManager() {
		Job.getJobManager().setProgressProvider(this);
		Dialog.setBlockedHandler(new WorkbenchDialogBlockedHandler());
		createChangeListener();
		Job.getJobManager().addJobChangeListener(this.changeListener);
		URL iconsRoot = ProgressManagerUtil.getIconsRoot();
		try {
			setUpImage(iconsRoot, SLEEPING_JOB, SLEEPING_JOB_KEY);
			setUpImage(iconsRoot, WAITING_JOB, WAITING_JOB_KEY);
			setUpImage(iconsRoot, BLOCKED_JOB, BLOCKED_JOB_KEY);

			// Let the error manager set up its own icons
			setUpImages(iconsRoot);
		} catch (MalformedURLException e) {
			ProgressManagerUtil.logException(e);
		}
	}

	/**
	 * Set up any images the error management needs.
	 * 
	 * @param iconsRoot
	 * @throws MalformedURLException
	 */
	void setUpImages(URL iconsRoot) throws MalformedURLException {
		// TODO see ErrorNotificationManager - this method isn't currently used
		// In the ErrorNotificationManager it is invoked by ProgressManager
		JFaceResources.getImageRegistry().put(ERROR_JOB_KEY,
				ImageDescriptor.createFromURL(new URL(iconsRoot, ERROR_JOB)));
	}

	/**
	 * Create the IJobChangeListener registered with the Job manager.
	 */
	private void createChangeListener() {
		changeListener = new JobChangeAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void aboutToRun(IJobChangeEvent event) {
				JobInfo info = getJobInfo(event.getJob());
				refreshJobInfo(info);
				Iterator startListeners = busyListenersForJob(event.getJob())
						.iterator();
				while (startListeners.hasNext()) {
					IJobBusyListener next = (IJobBusyListener) startListeners
							.next();
					next.incrementBusy(event.getJob());
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}
				Iterator startListeners = busyListenersForJob(event.getJob())
						.iterator();
				while (startListeners.hasNext()) {
					IJobBusyListener next = (IJobBusyListener) startListeners
							.next();
					next.decrementBusy(event.getJob());
				}

				final JobInfo info = getJobInfo(event.getJob());
				removeJobInfo(info);

				if (event.getResult() != null
						&& event.getResult().getSeverity() == IStatus.ERROR) {
					StatusAdapter statusAdapter = new StatusAdapter(event
							.getResult());
					statusAdapter.addAdapter(Job.class, event.getJob());

					if (event
							.getJob()
							.getProperty(
									IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) == Boolean.TRUE) {
						statusAdapter
								.setProperty(
										IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
										Boolean.TRUE);
						StatusAdapterHelper.getInstance().putStatusAdapter(
								info, statusAdapter);
					}

					StatusManager.getManager().handle(statusAdapter,
							StatusManager.SHOW);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void scheduled(IJobChangeEvent event) {
				updateFor(event);
				if (event.getJob().isUser()) {
					boolean noDialog = shouldRunInBackground();
					if (!noDialog) {
						final IJobChangeEvent finalEvent = event;
						WorkbenchJob showJob = new WorkbenchJob(
								ProgressMessages.ProgressManager_showInDialogName) {
							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
							 */
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
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
					addJobInfo(new JobInfo(event.getJob()));
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void awake(IJobChangeEvent event) {
				updateFor(event);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
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

		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				IJobProgressManagerListener listener = listeners[i];
				// Is this one the user never sees?
				if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
					continue;
				if (listener.showsDebug())
					listener.refreshJobInfo(info);
				else
					listener.removeJob(info);

			}
		}

	}

	/**
	 * Refresh the group when info is sleeping.
	 * @param group
	 */
	private void sleepGroup(GroupInfo group, JobInfo info) {
		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				
				IJobProgressManagerListener listener = listeners[i];
				if (isNeverDisplaying(info.getJob(), listener.showsDebug()))
					continue;
		
				if (listener.showsDebug() || group.isActive())
					listener.refreshGroup(group);
				else
					listener.removeGroup(group);
			}
		}
		
	}

	/**
	 * Set up the image in the image regsitry.
	 * 
	 * @param iconsRoot
	 * @param fileName
	 * @param key
	 * @throws MalformedURLException
	 */
	private void setUpImage(URL iconsRoot, String fileName, String key)
			throws MalformedURLException {
		JFaceResources.getImageRegistry().put(key,
				ImageDescriptor.createFromURL(new URL(iconsRoot, fileName)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job)
	 */
	public IProgressMonitor createMonitor(Job job) {
		return progressFor(job);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ProgressProvider#getDefaultMonitor()
	 */
	public IProgressMonitor getDefaultMonitor() {
		// only need a default monitor for operations the UI thread
		// and only if there is a display
		Display display;
		if (PlatformUI.isWorkbenchRunning()
				&& !((Workbench) PlatformUI.getWorkbench()).isStarting()) {
			display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()
					&& (display.getThread() == Thread.currentThread())) {
				return new EventLoopProgressMonitor(new NullProgressMonitor());
			}
		}
		return super.getDefaultMonitor();
	}

	/**
	 * Return a monitor for the job. Check if we cached a monitor for this job
	 * previously for a long operation timeout check.
	 * 
	 * @param job
	 * @return IProgressMonitor
	 */
	public JobMonitor progressFor(Job job) {

		synchronized (monitorKey) {
			if (runnableMonitors.containsKey(job)) {
				return (JobMonitor) runnableMonitors.get(job);
			}
			JobMonitor monitor = new JobMonitor(job);
			runnableMonitors.put(job, monitor);
			return monitor;
		}

	}

	/**
	 * Add an IJobProgressManagerListener to listen to the changes.
	 * 
	 * @param listener
	 */
	void addListener(IJobProgressManagerListener listener) {

		synchronized (listenersKey) {
			ArrayList newListeners = new ArrayList(listeners.length + 1);
			for (int i = 0; i < listeners.length; i++) {
				newListeners.add(listeners[i]);
			}
			newListeners.add(listener);
			listeners = new IJobProgressManagerListener[newListeners.size()];
			newListeners.toArray(listeners);
		}

	}

	/**
	 * Remove the supplied IJobProgressManagerListener from the list of
	 * listeners.
	 * 
	 * @param listener
	 */
	void removeListener(IJobProgressManagerListener listener) {
		synchronized (listenersKey) {
			ArrayList newListeners = new ArrayList();
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i].equals(listener)) {
					continue;
				}
				newListeners.add(listeners[i]);
			}
			listeners = new IJobProgressManagerListener[newListeners.size()];
			newListeners.toArray(listeners);
		}
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
			info = new JobInfo(job);
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
		return (JobInfo) jobs.get(job);
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

		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				IJobProgressManagerListener listener = listeners[i];
				if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
					listener.refreshJobInfo(info);
				}
			}
		}
	}

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in info.
	 * 
	 * @param info
	 */
	public void refreshGroup(GroupInfo info) {

		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].refreshGroup(info);
			}
		}
	}

	/**
	 * Refresh all the IJobProgressManagerListener as a result of a change in
	 * the whole model.
	 */
	public void refreshAll() {

		pruneStaleJobs();
		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].refreshAll();
			}
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
		synchronized (monitorKey) {
			if (runnableMonitors.containsKey(job)) {
				runnableMonitors.remove(job);
			}
		}

		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				IJobProgressManagerListener listener = listeners[i];
				if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
					listener.removeJob(info);
				}
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

		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].removeGroup(group);
			}
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
		synchronized (listenersKey) {
			for (int i = 0; i < listeners.length; i++) {
				IJobProgressManagerListener listener = listeners[i];
				if (!isCurrentDisplaying(info.getJob(), listener.showsDebug())) {
					listener.addJob(info);
				}
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
			Iterator iterator = jobs.keySet().iterator();
			Collection result = new ArrayList();
			while (iterator.hasNext()) {
				Job next = (Job) iterator.next();
				if (!isCurrentDisplaying(next, debug)) {
					result.add(jobs.get(next));
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
			Iterator iterator = jobs.keySet().iterator();
			Collection result = new HashSet();
			while (iterator.hasNext()) {
				Job next = (Job) iterator.next();
				if (!isCurrentDisplaying(next, debug)) {
					JobInfo jobInfo = (JobInfo) jobs.get(next);
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
			Iterator iterator = jobs.keySet().iterator();
			while (iterator.hasNext()) {
				return true;
			}
			return false;
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
		try {
			InputStream stream = fileSystemPath.openStream();
			ImageData[] result = loader.load(stream);
			stream.close();
			return result;
		} catch (FileNotFoundException exception) {
			ProgressManagerUtil.logException(exception);
			return null;
		} catch (IOException exception) {
			ProgressManagerUtil.logException(exception);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void busyCursorWhile(final IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(
				ProgressManagerUtil.getDefaultParent());
		dialog.setOpenOnRun(false);
		final InvocationTargetException[] invokes = new InvocationTargetException[1];
		final InterruptedException[] interrupt = new InterruptedException[1];
		// show a busy cursor until the dialog opens
		Runnable dialogWaitRunnable = new Runnable() {
			public void run() {
				try {
					dialog.setOpenOnRun(false);
					setUserInterfaceActive(false);
					dialog.run(true, true, runnable);
				} catch (InvocationTargetException e) {
					invokes[0] = e;
				} catch (InterruptedException e) {
					interrupt[0] = e;
				} finally {
					setUserInterfaceActive(true);
				}
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
	 * Show the busy cursor while the runnable is running. Schedule a job to
	 * replace it with a progress dialog.
	 * 
	 * @param dialogWaitRunnable
	 * @param dialog
	 */
	private void busyCursorWhile(Runnable dialogWaitRunnable,
			ProgressMonitorJobsDialog dialog) {
		// create the job that will open the dialog after a delay
		scheduleProgressMonitorJob(dialog);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display == null) {
			return;
		}
		// show a busy cursor until the dialog opens
		BusyIndicator.showWhile(display, dialogWaitRunnable);
	}

	/**
	 * Schedule the job that will open the progress monitor dialog
	 * 
	 * @param dialog
	 *            the dialog to open
	 */
	private void scheduleProgressMonitorJob(
			final ProgressMonitorJobsDialog dialog) {

		final WorkbenchJob updateJob = new WorkbenchJob(
				ProgressMessages.ProgressManager_openJobName) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
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
	 * Shutdown the receiver.
	 */
	private void shutdown() {
		synchronized (listenersKey) {
			this.listeners = new IJobProgressManagerListener[0];
		}
		Job.getJobManager().setProgressProvider(null);
		Job.getJobManager().removeJobChangeListener(this.changeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ProgressProvider#createProgressGroup()
	 */
	public IProgressMonitor createProgressGroup() {
		return new GroupInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job,
	 *      org.eclipse.core.runtime.IProgressMonitor, int)
	 */
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
	 * Add the listener to the family.
	 * 
	 * @param family
	 * @param listener
	 */
	void addListenerToFamily(Object family, IJobBusyListener listener) {
		synchronized (familyKey) {
			Collection currentListeners;
			if (familyListeners.containsKey(family)) {
				currentListeners = (Collection) familyListeners.get(family);
			} else {
				currentListeners = new HashSet();
			}
			currentListeners.add(listener);
			familyListeners.put(family, currentListeners);
		}
	}

	/**
	 * Remove the listener from all families.
	 * 
	 * @param listener
	 */
	void removeListener(IJobBusyListener listener) {
		synchronized (familyKey) {
			Collection keysToRemove = new HashSet();
			Iterator families = familyListeners.keySet().iterator();
			while (families.hasNext()) {
				Object next = families.next();
				Collection currentListeners = (Collection) familyListeners
						.get(next);
				if (currentListeners.contains(listener)) {
					currentListeners.remove(listener);
				}
				if (currentListeners.isEmpty()) {
					keysToRemove.add(next);
				} else {
					familyListeners.put(next, currentListeners);
				}
			}
			// Remove any empty listeners
			Iterator keysIterator = keysToRemove.iterator();
			while (keysIterator.hasNext()) {
				familyListeners.remove(keysIterator.next());
			}
		}
	}

	/**
	 * Return the listeners for the job.
	 * 
	 * @param job
	 * @return Collection of IJobBusyListener
	 */
	private Collection busyListenersForJob(Job job) {
		if (job.isSystem()) {
			return Collections.EMPTY_LIST;
		}
		synchronized (familyKey) {

			if (familyListeners.isEmpty()) {
				return Collections.EMPTY_LIST;
			}

			Iterator families = familyListeners.keySet().iterator();
			Collection returnValue = new ArrayList();
			while (families.hasNext()) {
				Object next = families.next();
				if (job.belongsTo(next)) {
					Collection currentListeners = (Collection) familyListeners
							.get(next);
					returnValue.addAll(currentListeners);
				}
			}
			return returnValue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#showInDialog(org.eclipse.swt.widgets.Shell,
	 *      org.eclipse.core.runtime.jobs.Job)
	 */
	public void showInDialog(Shell shell, Job job) {
		if (shouldRunInBackground()) {
			return;
		}

		final ProgressMonitorFocusJobDialog dialog = new ProgressMonitorFocusJobDialog(
				shell);
		dialog.show(job, shell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean,
	 *      org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		if (fork == false || cancelable == false) {
			// backward compatible code
			final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(
					null);
			dialog.run(fork, cancelable, runnable);
			return;
		}

		busyCursorWhile(runnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#runInUI(org.eclipse.jface.operation.IRunnableWithProgress,
	 *      org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public void runInUI(final IRunnableContext context,
			final IRunnableWithProgress runnable, final ISchedulingRule rule)
			throws InvocationTargetException, InterruptedException {
		final IJobManager manager = Job.getJobManager();
		final InvocationTargetException[] exception = new InvocationTargetException[1];
		final InterruptedException[] canceled = new InterruptedException[1];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					manager.beginRule(rule,
							((Workbench) PlatformUI.getWorkbench())
									.isStarting() ? new NullProgressMonitor()
									: getEventLoopMonitor());
					context.run(false, false, runnable);
				} catch (InvocationTargetException e) {
					exception[0] = e;
				} catch (InterruptedException e) {
					canceled[0] = e;
				} catch (OperationCanceledException e) {
					canceled[0] = new InterruptedException(e.getMessage());
				} finally {
					manager.endRule(rule);
				}
			}

			/**
			 * Get a progress monitor that forwards to an event loop monitor.
			 * Override #setBlocked() so that we always open the blocked dialog.
			 * 
			 * @return the monitor on the event loop
			 */
			private IProgressMonitor getEventLoopMonitor() {
				return new EventLoopProgressMonitor(new NullProgressMonitor()) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor#setBlocked(org.eclipse.core.runtime.IStatus)
					 */
					public void setBlocked(IStatus reason) {

						// Set a shell to open with as we want to create this
						// even if there is a modal shell.
						Dialog.getBlockedHandler().showBlocked(
								ProgressManagerUtil.getDefaultParent(), this,
								reason, getTaskName());
					}
				};
			}
		});
		if (exception[0] != null) {
			throw exception[0];
		}
		if (canceled[0] != null) {
			throw canceled[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#getLongOperationTime()
	 */
	public int getLongOperationTime() {
		return 800;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#registerIconForFamily(org.eclipse.jface.resource.ImageDescriptor,
	 *      java.lang.Object)
	 */
	public void registerIconForFamily(ImageDescriptor icon, Object family) {
		String key = IMAGE_KEY + String.valueOf(imageKeyTable.size());
		imageKeyTable.put(family, key);
		ImageRegistry registry = JFaceResources.getImageRegistry();

		// Avoid registering twice
		if (registry.getDescriptor(key) == null) {
			registry.put(key, icon);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#getIconFor(org.eclipse.core.runtime.jobs.Job)
	 */
	public Image getIconFor(Job job) {
		Enumeration families = imageKeyTable.keys();
		while (families.hasMoreElements()) {
			Object next = families.nextElement();
			if (job.belongsTo(next)) {
				return JFaceResources.getImageRegistry().get(
						(String) imageKeyTable.get(next));
			}
		}
		return null;
	}

	/**
	 * Iterate through all of the windows and set them to be disabled or enabled
	 * as appropriate.'
	 * 
	 * @param active
	 *            The set the windows will be set to.
	 */
	private void setUserInterfaceActive(boolean active) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell[] shells = workbench.getDisplay().getShells();
		if (active) {
			for (int i = 0; i < shells.length; i++) {
				shells[i].setEnabled(active);
			}
		} else {
			// Deactive shells in reverse order
			for (int i = shells.length - 1; i >= 0; i--) {
				shells[i].setEnabled(active);
			}
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
		for (int i = 0; i < jobsToCheck.length; i++) {
			Job job = (Job) jobsToCheck[i];
			if (checkForStaleness(job)) {
				if (Policy.DEBUG_STALE_JOBS) {
					WorkbenchPlugin.log("Stale Job " + job.getName()); //$NON-NLS-1$
				}
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
	private boolean shouldRunInBackground() {
		return WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				IPreferenceConstants.RUN_IN_BACKGROUND);
	}

	/**
	 * Set whether or not the ProgressViewUpdater should show system jobs.
	 * 
	 * @param showSystem
	 */
	public void setShowSystemJobs(boolean showSystem) {
		ProgressViewUpdater updater = ProgressViewUpdater.getSingleton();
		updater.debug = showSystem;
		updater.refreshAll();

	}
}
