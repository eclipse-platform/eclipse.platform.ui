/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.progress.UIJob;

/**
 * Job to refresh a {@link Subscriber} in the background. The job can be configured
 * to be re-scheduled and run at a specified interval.
 * <p>
 * The job supports a basic workflow for modal/non-modal usage. If the job is
 * run in the foreground (e.g. in a modal progress dialog) the refresh listeners
 * action is invoked immediately after the refresh is completed. Otherwise the refresh
 * listeners action is associated to the job as a <i>goto</i> action. This will
 * allow the user to select the action in the progress view and run it when they
 * choose.
 * </p>
 * @since 3.0
 */
public final class RefreshSubscriberJob extends WorkspaceJob {
	
	/**
	 * Uniquely identifies this type of job. This is used for cancellation.
	 */
	private final static Object FAMILY_ID = new Object();
	
	/**
	 * If true this job will be restarted when it completes 
	 */
	private boolean reschedule = false;
	
	/**
	 * If true a rescheduled refresh job should be retarted when cancelled
	 */
	private boolean restartOnCancel = true; 
	
	/**
	 * The schedule delay used when rescheduling a completed job 
	 */
	private static long scheduleDelay;
	
	/**
	 * The subscribers and resources to refresh.
	 */
	private IResource[] resources;

	/**
	 * The participant that is being refreshed.
	 */
	private SubscriberParticipant participant;
	
	/**
	 * The task name for this refresh. This is usually more descriptive than the
	 * job name.
	 */
	private String taskName;
	
	/**
	 * Refresh started/completed listener for every refresh
	 */
	private static List listeners = new ArrayList(1);
	private static final int STARTED = 1;
	private static final int DONE = 2;
	
	/**
	 * Notification for safely notifying listeners of refresh lifecycle.
	 */
	private abstract class Notification implements ISafeRunnable {
		private IRefreshSubscriberListener listener;
		public void handleException(Throwable exception) {
			// don't log the exception....it is already being logged in Platform#run
		}
		public void run(IRefreshSubscriberListener listener) {
			this.listener = listener;
			Platform.run(this);
		}
		public void run() throws Exception {
			notify(listener);
		}
		/**
		 * Subsclasses overide this method to send an event safely to a lsistener
		 * @param listener
		 */
		protected abstract void notify(IRefreshSubscriberListener listener);
	}
	
	/**
	 * Create a job to refresh the specified resources with the subscriber.
	 * 
	 * @param participant the subscriber participant 
	 * @param name
	 * @param resources
	 * @param subscriber
	 */
	public RefreshSubscriberJob(SubscriberParticipant participant, String jobName, String taskName, IResource[] resources, IRefreshSubscriberListener listener) {
		super(taskName);
		Assert.isNotNull(resources);
		Assert.isNotNull(participant);
		Assert.isNotNull(resources);
		this.resources = resources;
		this.participant = participant;
		this.taskName = jobName;
		setPriority(Job.DECORATE);
		setRefreshInterval(3600 /* 1 hour */);
		
		// Handle restarting of job if it is configured as a scheduled refresh job.
		addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if(shouldReschedule()) {
					if(event.getResult().getSeverity() == IStatus.CANCEL && ! restartOnCancel) {					
						return;
					}
					RefreshSubscriberJob.this.schedule(scheduleDelay);
					restartOnCancel = true;
				}
			}
		});		
		
		initialize(listener);
	}
	
	/**
	 * If a collector is available then run the refresh and the background event processing 
	 * within the same progess group.
	 */
	public boolean shouldRun() {
		// Ensure that any progress shown as a result of this refresh occurs hidden in a progress group.
		return getSubscriber() != null;
	}

	public boolean belongsTo(Object family) {	
		if(family instanceof RefreshSubscriberJob) {
			return ((RefreshSubscriberJob)family).getSubscriber() == getSubscriber();
		} else {
			return (family == getFamily() || family == ISynchronizeManager.FAMILY_SYNCHRONIZE_OPERATION);
		}
	}
	
	public static Object getFamily() {
		return FAMILY_ID;
	}
	
	/**
	 * This is run by the job scheduler. A list of subscribers will be refreshed, errors will not stop the job 
	 * and it will continue to refresh the other subscribers.
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		// Only allow one refresh job at a time
		// NOTE: It would be cleaner if this was done by a scheduling
		// rule but at the time of writting, it is not possible due to
		// the scheduling rule containment rules.
		// Synchronized to ensure only one refresh job is running at a particular time
		synchronized (getFamily()) {	
			Subscriber subscriber = getSubscriber();
			IResource[] roots = getResources();
			MultiStatus status = new MultiStatus(TeamPlugin.ID, TeamException.UNABLE, subscriber.getName(), null); //$NON-NLS-1$
			
			// if there are no resources to refresh, just return
			if(subscriber == null || roots == null) {
				return Status.OK_STATUS;
			}
			SubscriberSyncInfoCollector collector = getCollector();
			RefreshEvent event = new RefreshEvent(reschedule ? IRefreshEvent.SCHEDULED_REFRESH : IRefreshEvent.USER_REFRESH, roots, collector.getSubscriber());
			RefreshChangeListener changeListener = new RefreshChangeListener(collector);
			try {
				event.setStartTime(System.currentTimeMillis());
				if(monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				try {
					// Set-up change listener so that we can determine the changes found
					// during this refresh.						
					subscriber.addListener(changeListener);
					// Pre-Notify
					notifyListeners(STARTED, event);
					// Perform the refresh		
					monitor.setTaskName(getName());
					subscriber.refresh(roots, IResource.DEPTH_INFINITE, monitor);					
				} catch(TeamException e) {
					status.merge(e.getStatus());
				}
			} catch(OperationCanceledException e2) {
				subscriber.removeListener(changeListener);
				event.setStatus(Status.CANCEL_STATUS);
				event.setStopTime(System.currentTimeMillis());
				notifyListeners(DONE, event);
				return Status.CANCEL_STATUS;
			} finally {
				monitor.done();
			}
			
			setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "keep"), Boolean.valueOf(! isJobModal()));
			setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "keepone"), Boolean.valueOf(! isJobModal()));
			
			// Post-Notify
			event.setChanges(changeListener.getChanges());
			event.setStopTime(System.currentTimeMillis());
			event.setStatus(status.isOK() ? calculateStatus(event) : (IStatus) status);
			notifyListeners(DONE, event);
			changeListener.clear();
			return event.getStatus();
		}
	}
	
	private IStatus calculateStatus(IRefreshEvent event) {
		StringBuffer text = new StringBuffer();
		int code = IStatus.OK;
		SyncInfo[] changes = event.getChanges();
		IResource[] resources = event.getResources();
		SubscriberSyncInfoCollector collector = getCollector();
		if (collector != null) {
			SyncInfoSet set = collector.getSyncInfoSet();
			int numChanges = refreshedResourcesContainChanges(event);
			if (numChanges > 0) {
				code = IRefreshEvent.STATUS_CHANGES;
				String outgoing = Long.toString(set.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK));
				String incoming = Long.toString(set.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK));
				String conflicting = Long.toString(set.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK));
				if (changes.length > 0) {
				// New changes found
					String numNewChanges = Integer.toString(event.getChanges().length);
					text.append(Policy.bind("RefreshCompleteDialog.5a", new Object[]{getName(), numNewChanges})); //$NON-NLS-1$
				} else {
				// Refreshed resources contain changes
					text.append(Policy.bind("RefreshCompleteDialog.5", new Object[]{getName(), new Integer(numChanges)})); //$NON-NLS-1$
				}
			} else {
				// No changes found
				code = IRefreshEvent.STATUS_NO_CHANGES;
				text.append(Policy.bind("RefreshCompleteDialog.6", getName())); //$NON-NLS-1$
			}
			return new Status(IStatus.OK, TeamUIPlugin.ID, code, text.toString(), null);
		}
		return Status.OK_STATUS;
	}
	
	private int refreshedResourcesContainChanges(IRefreshEvent event) {
		int numChanges = 0;
		SubscriberSyncInfoCollector collector = getCollector();
		if (collector != null) {
			SyncInfoTree set = collector.getSyncInfoSet();
			IResource[] resources = event.getResources();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				SyncInfo[] infos = set.getSyncInfos(resource, IResource.DEPTH_INFINITE);
				if(infos != null && infos.length > 0) {
					numChanges += infos.length;
				}
			}
		}
		return numChanges;
	}
	
	private void initialize(final IRefreshSubscriberListener listener) {
		final IWorkbenchAction[] gotoAction = new IWorkbenchAction[] {null};
		final IWorkbenchAction actionWrapper = new WorkbenchAction() {
			public void run() {
				if(gotoAction[0] != null) {
					gotoAction[0].run();
				}
			}
			public boolean isEnabled() {
				if(gotoAction[0] != null) {
					return gotoAction[0].isEnabled();
				}
				return false;
			}
			
			public void dispose() {
				super.dispose();
				if(gotoAction[0] != null) {
					gotoAction[0].dispose();
				}
			}
		};
		
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		group.beginTask(taskName, 100);
		setProgressGroup(group, 80);
		getCollector().setProgressGroup(group, 20);
		setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "icon"), participant.getImageDescriptor());
		setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "goto"), actionWrapper);
		// Listener delagate
		IRefreshSubscriberListener autoListener = new IRefreshSubscriberListener() {
			public void refreshStarted(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshStarted(event);
				}
			}
			public ActionFactory.IWorkbenchAction refreshDone(IRefreshEvent event) {
				if(listener != null) {
					boolean isModal = isJobModal();
					ActionFactory.IWorkbenchAction runnable = listener.refreshDone(event);
					if(runnable != null) {
					// If the job is being run modally then simply prompt the user immediatly
					if(isModal) {
						if(runnable != null) {
							final IAction[] r = new IAction[] {runnable};
							Job update = new UIJob("") {
								public IStatus runInUIThread(IProgressMonitor monitor) {
									r[0].run();
									return Status.OK_STATUS;
								}
							};
							update.setSystem(true);
							update.schedule();
						}
					// If the job is being run in the background, don't interrupt the user and simply update the goto action
					// to perform the results.
					} else {
						gotoAction[0] = runnable;
						actionWrapper.setEnabled(runnable.isEnabled());
						runnable.addPropertyChangeListener(new IPropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent event) {
								if(event.getProperty().equals(IAction.ENABLED)) {
									Boolean bool = (Boolean) event.getNewValue();
									actionWrapper.setEnabled(bool.booleanValue());
								}
							}
						});
					}
					}
					RefreshSubscriberJob.removeRefreshListener(this);
				}
				return null;
			}
		};
		
		if (listener != null) {
			RefreshSubscriberJob.addRefreshListener(autoListener);
		}	
	}
	
	protected IResource[] getResources() {
		return resources;
	}
	
	protected Subscriber getSubscriber() {
		return participant.getSubscriber();
	}
	
	protected SubscriberSyncInfoCollector getCollector() {
		return participant.getSubscriberSyncInfoCollector();
	}
	
	public long getScheduleDelay() {
		return scheduleDelay;
	}
	
	protected void start() {
		if(getState() == Job.NONE) {
			if(shouldReschedule()) {
				schedule(getScheduleDelay());
			}
		}
	}
	
	/**
	 * Specify the interval in seconds at which this job is scheduled.
	 * @param seconds delay specified in seconds
	 */
	public void setRefreshInterval(long seconds) {
		boolean restart = false;
		if(getState() == Job.SLEEPING) {
			restart = true;
			cancel();
		}
		scheduleDelay = seconds * 1000;
		if(restart) {
			start();
		}
	}
	
	/**
	 * Returns the interval of this job in seconds. 
	 * @return
	 */
	public long getRefreshInterval() {
		return scheduleDelay / 1000;
	}
	
	public void setRestartOnCancel(boolean restartOnCancel) {
		this.restartOnCancel = restartOnCancel;
	}
	
	public void setReschedule(boolean reschedule) {
		this.reschedule = reschedule;
	}
	
	public boolean shouldReschedule() {
		return reschedule;
	}
	
	public static void addRefreshListener(IRefreshSubscriberListener listener) {
		synchronized(listeners) {
			if(! listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}
	
	public static void removeRefreshListener(IRefreshSubscriberListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	protected void notifyListeners(final int state, final IRefreshEvent event) {
		// Get a snapshot of the listeners so the list doesn't change while we're firing
		IRefreshSubscriberListener[] listenerArray;
		synchronized (listeners) {
			listenerArray = (IRefreshSubscriberListener[]) listeners.toArray(new IRefreshSubscriberListener[listeners.size()]);
		}
		// Notify each listener in a safe manner (i.e. so their exceptions don't kill us)
		for (int i = 0; i < listenerArray.length; i++) {
			IRefreshSubscriberListener listener = listenerArray[i];
			Notification notification = new Notification() {
				protected void notify(IRefreshSubscriberListener listener) {
					switch (state) {
						case STARTED:
							listener.refreshStarted(event);
							break;
						case DONE:
							listener.refreshDone(event);
							break;
						default:
							break;
					}
				}
			};
			notification.run(listener);
		}
	}
	
	private boolean isJobModal() {
		Boolean isModal = (Boolean)getProperty(ProgressManager.PROPERTY_IN_DIALOG);
		if(isModal == null) return false;
		return isModal.booleanValue();
	}
}