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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IProgressProvider;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.IProgressManager;
import org.eclipse.ui.progress.UIJob;

/**
 * JobProgressManager provides the progress monitor to the job manager and
 * informs any ProgressContentProviders of changes.
 */
public class ProgressManager extends JobChangeAdapter implements IProgressProvider, IProgressManager {

	private static ProgressManager singleton;
	private Map jobs = Collections.synchronizedMap(new HashMap());
	private Collection listeners = Collections.synchronizedList(new ArrayList());
	private WorkbenchMonitorProvider monitorProvider;
	private ProgressFeedbackManager feedbackManager = new ProgressFeedbackManager();

	static final String PROGRESS_VIEW_NAME = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
	static final String PROGRESS_FOLDER = "icons/full/progress/"; //$NON-NLS-1$

	private static final String PROGRESS_20 = "progress20.gif"; //$NON-NLS-1$
	private static final String PROGRESS_40 = "progress40.gif"; //$NON-NLS-1$
	private static final String PROGRESS_60 = "progress60.gif"; //$NON-NLS-1$
	private static final String PROGRESS_80 = "progress80.gif"; //$NON-NLS-1$
	private static final String PROGRESS_100 = "progress100.gif"; //$NON-NLS-1$

	private static final String SLEEPING_JOB = "sleeping.gif"; //$NON-NLS-1$
	private static final String WAITING_JOB = "waiting.gif"; //$NON-NLS-1$
	private static final String ERROR_JOB = "errorstate.gif"; //$NON-NLS-1$
	private static final String BLOCKED_JOB = "lockedstate.gif"; //$NON-NLS-1$

	private static final String PROGRESS_20_KEY = "PROGRESS_20"; //$NON-NLS-1$
	private static final String PROGRESS_40_KEY = "PROGRESS_40"; //$NON-NLS-1$
	private static final String PROGRESS_60_KEY = "PROGRESS_60"; //$NON-NLS-1$
	private static final String PROGRESS_80_KEY = "PROGRESS_80"; //$NON-NLS-1$
	private static final String PROGRESS_100_KEY = "PROGRESS_100"; //$NON-NLS-1$

	private static final String SLEEPING_JOB_KEY = "SLEEPING_JOB"; //$NON-NLS-1$
	private static final String WAITING_JOB_KEY = "WAITING_JOB"; //$NON-NLS-1$
	private static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$
	private static final String BLOCKED_JOB_KEY = "LOCKED_JOB"; //$NON-NLS-1$

	//A list of keys for looking up the images in the image registry
	static String[] keys = new String[] { PROGRESS_20_KEY, PROGRESS_40_KEY, PROGRESS_60_KEY, PROGRESS_80_KEY, PROGRESS_100_KEY };

	Hashtable runnableMonitors = new Hashtable();

	/**
	 * Get the progress manager currently in use.
	 * @return JobProgressManager
	 */
	public static ProgressManager getInstance() {
		if (singleton == null)
			singleton = new ProgressManager();
		return singleton;
	}

	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor
	 * integration with the ProgressMonitor.
	 */
	private class JobMonitor implements IProgressMonitorWithBlocking {
		Job job;
		IProgressMonitor workbenchMonitor;
		String currentTaskName;

		/**
		 * Create a monitor on the supplied job.
		 * 
		 * 
		 * @param newJob
		 */
		JobMonitor(Job newJob) {
			job = newJob;
			workbenchMonitor = monitorProvider.getMonitor(job);
		}
		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
		 * int)
		 */
		public void beginTask(String taskName, int totalWork) {
			JobInfo info = getJobInfo(job);
			info.beginTask(taskName, totalWork);
			refresh(info);
			currentTaskName = taskName;
			workbenchMonitor.beginTask(taskName, totalWork);
		}
		/* (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#done() */
		public void done() {
			JobInfo info = getJobInfo(job);
			info.clearTaskInfo();
			info.clearChildren();
			workbenchMonitor.done();
			runnableMonitors.remove(this);
		}

		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo()) {
				info.addWork(work);
				refresh(info);
			}
			workbenchMonitor.internalWorked(work);
		}
		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			JobInfo info = getJobInfo(job);
			return info.isCanceled();
		}

		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
			workbenchMonitor.setCanceled(value);
			JobInfo info = getJobInfo(job);
			
			//Don't bother cancelling twice
			if(value && !info.isCanceled())
				info.cancel();
		}

		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
		public void setTaskName(String taskName) {

			JobInfo info = getJobInfo(job);
			if (info.hasTaskInfo())
				info.setTaskName(taskName);
			else {
				beginTask(taskName, 100);
				return;
			}

			info.clearChildren();
			refresh(info);
			currentTaskName = taskName;
			workbenchMonitor.setTaskName(taskName);
		}

		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
		public void subTask(String name) {

			if (name.length() == 0)
				return;
			JobInfo info = getJobInfo(job);

			info.clearChildren();
			info.addSubTask(name);
			refresh(info);
			workbenchMonitor.subTask(name);
		}

		/*
		 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			internalWorked(work);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
		 */
		public void clearBlocked() {
			JobInfo info = getJobInfo(job);
			info.setBlockedStatus(null);
			refresh(info);

		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
		 */
		public void setBlocked(IStatus reason) {
			JobInfo info = getJobInfo(job);
			info.setBlockedStatus(null);
			refresh(info);
		}
	}

	/**
	 * Create a new instance of the receiver.
	 */
	ProgressManager() {
		Platform.getJobManager().setProgressProvider(this);
		Platform.getJobManager().addJobChangeListener(this);
		monitorProvider = new WorkbenchMonitorProvider();
		URL iconsRoot = Platform.getPlugin(PlatformUI.PLUGIN_ID).find(new Path(ProgressManager.PROGRESS_FOLDER));

		try {
			setUpImage(iconsRoot, PROGRESS_20, PROGRESS_20_KEY);
			setUpImage(iconsRoot, PROGRESS_40, PROGRESS_40_KEY);
			setUpImage(iconsRoot, PROGRESS_60, PROGRESS_60_KEY);
			setUpImage(iconsRoot, PROGRESS_80, PROGRESS_80_KEY);
			setUpImage(iconsRoot, PROGRESS_100, PROGRESS_100_KEY);

			setUpImage(iconsRoot, SLEEPING_JOB, SLEEPING_JOB_KEY);
			setUpImage(iconsRoot, WAITING_JOB, WAITING_JOB_KEY);
			setUpImage(iconsRoot, ERROR_JOB, ERROR_JOB_KEY);
			setUpImage(iconsRoot, BLOCKED_JOB, BLOCKED_JOB_KEY);

		} catch (MalformedURLException e) {
			ProgressUtil.logException(e);
		}

	}

	/**
	 * Set up the image in the image regsitry.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param iconsRoot
	 * @param fileName
	 * @param key
	 * @throws MalformedURLException
	 */
	private void setUpImage(URL iconsRoot, String fileName, String key) throws MalformedURLException {
		JFaceResources.getImageRegistry().put(key, ImageDescriptor.createFromURL(new URL(iconsRoot, fileName)));

	}

	/*
	 * (non-Javadoc) @see org.eclipse.core.runtime.jobs.IProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job)
	 */
	public IProgressMonitor createMonitor(Job job) {
		return progressFor(job);
	}

	/**
	 * Return a monitor for the job. Check if we cached a monitor for this job
	 * previously for a long operation timeout check.
	 * @param job
	 * @return IProgressMonitor
	 */
	private JobMonitor progressFor(Job job) {
		if (runnableMonitors.containsKey(job))
			return (JobMonitor) runnableMonitors.get(job);
		else
			return new JobMonitor(job);
	}

	/**
	 * Add an IJobProgressManagerListener to listen to the changes.
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

	/*
	 * (non-Javadoc) @see org.eclipse.core.runtime.jobs.JobChangeAdapter#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void scheduled(IJobChangeEvent event) {
		if (isNeverDisplayedJob(event.getJob()))
			return;
		JobInfo info = new JobInfo(event.getJob());
		jobs.put(event.getJob(), info);
		add(info);
	}

	/*
	 * (non-Javadoc) @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void aboutToRun(IJobChangeEvent event) {
		JobInfo info = getJobInfo(event.getJob());
		refresh(info);
	}

	/*
	 * (non-Javadoc) @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void done(IJobChangeEvent event) {

		JobInfo info = getJobInfo(event.getJob());
		if (event.getResult().getSeverity() == IStatus.ERROR) {
			info.setError(event.getResult());
				UIJob job = new UIJob(ProgressMessages.getString("JobProgressManager.OpenProgressJob")) {//$NON-NLS-1$
	/*
	 * (non-Javadoc) @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IWorkbenchWindow window = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

					if (window == null)
						return Status.CANCEL_STATUS;
					ProgressUtil.openProgressView(window);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			refresh(info);

		} else {
			jobs.remove(event.getJob());
			//Only refresh if we are showing it
			remove(info);
		}
	}

	/**
	 * Get the JobInfo for the job. If it does not exist create it.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param job
	 * @return
	 */
	JobInfo getJobInfo(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info == null) {
			info = new JobInfo(job);
			jobs.put(job, info);
		}
		return info;
	}

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in
	 * info.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param info
	 */
	public void refresh(JobInfo info) {

		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				IJobProgressManagerListener listener = (IJobProgressManagerListener) iterator.next();
				if (!isNonDisplayableJob(info.getJob(), listener.showsDebug()))
					listener.refresh(info);
			}
		}
	}

	/**
	 * Refresh all the IJobProgressManagerListener as a result of a change in
	 * the whole model.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param info
	 */
	public void refreshAll() {
		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				IJobProgressManagerListener listener = (IJobProgressManagerListener) iterator.next();
				listener.refreshAll();
			}
		}

	}

	/**
	 * Refresh the content providers as a result of a deletion of info.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param info
	 */
	public void remove(JobInfo info) {

		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				IJobProgressManagerListener listener = (IJobProgressManagerListener) iterator.next();
				if (!isNonDisplayableJob(info.getJob(), listener.showsDebug()))
					listener.remove(info);
			}
		}
	}

	/**
	 * Refresh the content providers as a result of an addition of info.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param info
	 */
	public void add(JobInfo info) {
		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				IJobProgressManagerListener listener = (IJobProgressManagerListener) iterator.next();
				if (!isNonDisplayableJob(info.getJob(), listener.showsDebug()))
					listener.add(info);
			}
		}

	}

	/**
	 * Return whether or not this job is currently displayable.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param job
	 * @param debug
	 *           If the listener is in debug mode.
	 * @return
	 */
	boolean isNonDisplayableJob(Job job, boolean debug) {
		if (isNeverDisplayedJob(job))
			return true;
		if (debug) //Always display in debug mode
			return false;
		else
			return job.isSystem() || job.getState() == Job.SLEEPING;
	}

	/**
	 * Return whether or not this job is ever displayable.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param job
	 * @return
	 */
	private boolean isNeverDisplayedJob(Job job) {
		return job == null;
	}

	/**
	 * Get the jobs currently being displayed.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return JobInfo[]
	 */
	public JobInfo[] getJobInfos() {
		synchronized (jobs) {
			Iterator iterator = jobs.keySet().iterator();
			Collection result = new ArrayList();
			while (iterator.hasNext()) {
				Job next = (Job) iterator.next();
				result.add(jobs.get(next));
			}
			JobInfo[] infos = new JobInfo[result.size()];
			result.toArray(infos);
			return infos;
		}
	}

	/**
	 * Return whether or not there are any jobs being displayed.
	 * 
	 * 
	 * 
	 * 
	 * 
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
	 * Clear the job out of the list of those being displayed. Only do this for
	 * jobs that are an error.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param job
	 */
	void clearJob(Job job) {
		JobInfo info = (JobInfo) jobs.get(job);
		if (info != null && info.getErrorStatus() != null) {
			jobs.remove(job);
			remove(info);
		}
	}

	/**
	 * Clear all of the errors from the list.
	 */
	void clearAllErrors() {
		Collection jobsToDelete = new ArrayList();
		synchronized (jobs) {
			Iterator keySet = jobs.keySet().iterator();
			while (keySet.hasNext()) {
				Object job = keySet.next();
				JobInfo info = (JobInfo) jobs.get(job);
				if (info.getErrorStatus() != null)
					jobsToDelete.add(job);
			}
		}

		Iterator deleteSet = jobsToDelete.iterator();
		while (deleteSet.hasNext()) {
			jobs.remove(deleteSet.next());
		}
		refreshAll();
	}

	/**
	 * Return whether or not there are any errors displayed.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return
	 */
	boolean hasErrorsDisplayed() {

		synchronized (jobs) {
			Iterator keySet = jobs.keySet().iterator();
			while (keySet.hasNext()) {
				Object job = keySet.next();
				JobInfo info = (JobInfo) jobs.get(job);
				if (info.getErrorStatus() != null)
					return true;
			}
		}

		return false;
	}

	/**
	 * Returns the image descriptor with the given relative path.
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
	 *           The URL for the file system to the image.
	 * @param loader -
	 *           the loader used to get this data
	 * @return ImageData[]
	 */
	ImageData[] getImageData(URL fileSystemPath, ImageLoader loader) {
		try {
			InputStream stream = fileSystemPath.openStream();
			ImageData[] result = loader.load(stream);
			stream.close();
			return result;
		} catch (FileNotFoundException exception) {
			ProgressUtil.logException(exception);
			return null;
		} catch (IOException exception) {
			ProgressUtil.logException(exception);
			return null;
		}
	}

	/**
	 * Get the current image for the receiver. If there is no progress yet
	 * return null.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param element
	 * @return
	 */
	Image getDisplayImage(JobTreeElement element) {
		if (element.isJobInfo()) {
			JobInfo info = (JobInfo) element;
			int done = info.getPercentDone();
			if (done > 0) {
				int index = Math.min(4, (done / 20));
				return JFaceResources.getImage(keys[index]);
			} else {
				if (info.isBlocked())
					return JFaceResources.getImage(BLOCKED_JOB_KEY);
				if (info.getErrorStatus() != null)
					return JFaceResources.getImage(ERROR_JOB_KEY);
				int state = info.getJob().getState();
				if (state == Job.SLEEPING)
					return JFaceResources.getImage(SLEEPING_JOB_KEY);
				if (state == Job.WAITING)
					return JFaceResources.getImage(WAITING_JOB_KEY);
				
				//By default return the 0 progress image
				return JFaceResources.getImage(keys[0]);
			}
		}
		return null;
	}

	/**
	 * Block the current thread until UIJob is served. The message is used to
	 * announce to the user a pending UI Job.
	 * 
	 * Note: This is experimental API and subject to change at any time.
	 * @param job
	 * @param message
	 * @return IStatus
	 * @since 3.0
	 */
	public IStatus requestInUI(UIJob job, String message) {
		return feedbackManager.requestInUI(job, message);
	}

	/**
	 * Return the ProgressFeedbackManager for the receiver.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return ProgressFeedbackManager
	 */
	ProgressFeedbackManager getFeedbackManager() {
		return feedbackManager;
	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.progress.IProgressManager#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void busyCursorWhile(final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException
	{

		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
		dialog.setOpenOnRun(false);
		final boolean[] busy = {true};
		
		UIJob updateJob = new UIJob(ProgressMessages.getString("ProgressManager.openJobName")) { //$NON-NLS-1$
			/*
			 * (non-Javadoc) @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				busy[0] = false;
				dialog.open();
				if(monitor.isCanceled())
					return Status.CANCEL_STATUS;
				else
					return Status.OK_STATUS;
			}
		};

		updateJob.schedule(IProgressManager.LONG_OPERATION_MILLISECONDS);
		
		final InvocationTargetException[] invokes = new InvocationTargetException[1];
		final InterruptedException[] interrupt = new InterruptedException[1];
		
		invokes[0] = null;
		interrupt[0] = null;

		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			/*
			 * (non-Javadoc) @see java.lang.Runnable#run()
			 */
			public void run() {
				try {
					dialog.run(true /* fork */ ,true /* cancelable */,runnable);
					
					//Run the event loop until the progress job wakes up
					Display display = Display.getCurrent();
					while(busy[0]){
						if (!display.readAndDispatch())
							display.sleep();
					}
						
				} catch (InvocationTargetException e) {
					invokes[0] = e;
				} catch (InterruptedException e) {
					interrupt[0] = e;
				}
			}
		});
		
		if(invokes[0] != null)
			throw invokes[0];
		
		if(interrupt[0] != null)
			throw interrupt[0];

	}

}
