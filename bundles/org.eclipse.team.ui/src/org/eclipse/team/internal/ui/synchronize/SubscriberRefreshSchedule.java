/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.text.DateFormat;
import java.util.Date;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Schedule to refresh a subscriber at a specified interval. The schedule can be disabled or enabled
 * and will create the refresh job.
 * 
 * @since 3.0
 */
public class SubscriberRefreshSchedule {
	private long refreshInterval = 3600; // 1 hour default
	
	private boolean enabled = false;
	
	private RefreshSubscriberJob job;
	
	private SubscriberParticipant participant;
	
	private IRefreshEvent lastRefreshEvent;
	
	/**
	 * Key for settings in memento
	 */
	private static final String CTX_REFRESHSCHEDULE_INTERVAL = TeamUIPlugin.ID + ".CTX_REFRESHSCHEDULE_INTERVAL"; //$NON-NLS-1$
	
	/**
	 * Key for schedule in memento
	 */
	private static final String CTX_REFRESHSCHEDULE_ENABLED = TeamUIPlugin.ID + ".CTX_REFRESHSCHEDULE_ENABLED"; //$NON-NLS-1$
		
	private IRefreshSubscriberListener refreshSubscriberListener = new IRefreshSubscriberListener() {
		public void refreshStarted(IRefreshEvent event) {
		}
		public ActionFactory.IWorkbenchAction refreshDone(final IRefreshEvent event) {
			if (event.getSubscriber() == participant.getSubscriber()) {
				lastRefreshEvent = event;
				if(enabled && event.getRefreshType() == IRefreshEvent.SCHEDULED_REFRESH) {
					RefreshUserNotificationPolicy policy = new RefreshUserNotificationPolicy(participant);
					policy.refreshDone(event);
				}
			}
			return null;
		}
	};
	
	
	public SubscriberRefreshSchedule(SubscriberParticipant participant) {
		this.participant = participant;
		RefreshSubscriberJob.addRefreshListener(refreshSubscriberListener);
	}

	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled, boolean allowedToStart) {
		boolean wasEnabled = isEnabled();
		this.enabled = enabled;
		if(enabled && ! wasEnabled) { 
			if(allowedToStart) {
				startJob();
			}
		} else {
			stopJob();
		}
	}
	
	/**
	 * @return Returns the refreshInterval in seconds.
	 */
	public long getRefreshInterval() {
		return refreshInterval;
	}

	public SubscriberParticipant getParticipant() {
		return participant;
	}
	
	/**
	 * @param refreshInterval The refreshInterval to set.
	 */
	public void setRefreshInterval(long refreshInterval) {
		if(refreshInterval != getRefreshInterval()) {
			stopJob();
			this.refreshInterval = refreshInterval;
			if(isEnabled()) {
				startJob();
			}
		}
	}
	
	public void startJob() {
		SyncInfoSet set = participant.getSubscriberSyncInfoCollector().getSyncInfoSet();
		if(set == null) { 
			return;
		}
		if(job == null) {
			SubscriberParticipant participant = getParticipant();
			job = new RefreshSubscriberJob(participant, Policy.bind("RefreshSchedule.14"), Policy.bind("RefreshSchedule.15", participant.getName(), getRefreshIntervalAsString()), participant.getResources(), new RefreshUserNotificationPolicy(getParticipant())); //$NON-NLS-1$ //$NON-NLS-2$
			job.setUser(false);
		} else if(job.getState() != Job.NONE){
			stopJob();
		}
		job.setRefreshInterval(getRefreshInterval());
		job.setRestartOnCancel(true);
		job.setReschedule(true);
		// Schedule delay is in mills.
		job.schedule(getRefreshInterval() * 1000);		
	}
	
	protected void stopJob() {
		if(job != null) {
			job.setRestartOnCancel(false /* don't restart the job */);
			job.setReschedule(false);
			job.cancel();
			job = null;
		}
	}

	public void dispose() {
		stopJob();
		RefreshSubscriberJob.removeRefreshListener(refreshSubscriberListener);
	}
	
	public void saveState(IMemento memento) {
		memento.putString(CTX_REFRESHSCHEDULE_ENABLED, Boolean.toString(enabled));
		memento.putInteger(CTX_REFRESHSCHEDULE_INTERVAL, (int)refreshInterval);
	}

	public static SubscriberRefreshSchedule init(IMemento memento, SubscriberParticipant participant) {
		SubscriberRefreshSchedule schedule = new SubscriberRefreshSchedule(participant);
		if(memento != null) {
			String enabled = memento.getString(CTX_REFRESHSCHEDULE_ENABLED);
			int interval = memento.getInteger(CTX_REFRESHSCHEDULE_INTERVAL).intValue();
			schedule.setRefreshInterval(interval);
			schedule.setEnabled("true".equals(enabled) ? true : false, false /* don't start job */); //$NON-NLS-1$
		}
		// Use the defaults if a schedule hasn't been saved or can't be found.
		return schedule;
	}

	public static String refreshEventAsString(IRefreshEvent event) {
		if(event == null) {
			return Policy.bind("SyncViewPreferencePage.lastRefreshRunNever"); //$NON-NLS-1$
		}
		long stopMills = event.getStopTime();
		long startMills = event.getStartTime();
		StringBuffer text = new StringBuffer();
		if(stopMills <= 0) {
			text.append(Policy.bind("SyncViewPreferencePage.lastRefreshRunNever")); //$NON-NLS-1$
		} else {
			Date lastTimeRun = new Date(stopMills);
			text.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastTimeRun));
		}
		SyncInfo[] changes = event.getChanges();
		if (changes.length == 0) {
			text.append(Policy.bind("RefreshSchedule.7")); //$NON-NLS-1$
		} else if (changes.length == 1) {
			text.append(Policy.bind("RefreshSchedule.changesSingular", Integer.toString(changes.length))); //$NON-NLS-1$
		} else {
			text.append(Policy.bind("RefreshSchedule.changesPlural", Integer.toString(changes.length))); //$NON-NLS-1$
		}
		return text.toString();
	} 
	
	public String getScheduleAsString() {
		if(! isEnabled()) {
			return Policy.bind("RefreshSchedule.8"); //$NON-NLS-1$
		}		
		return getRefreshIntervalAsString();
	}
	
	public IRefreshEvent getLastRefreshEvent() {
		return lastRefreshEvent;
	}
	
	private String getRefreshIntervalAsString() {
		boolean hours = false;
		long seconds = getRefreshInterval();
		if(seconds <= 60) {
			seconds = 60;
		}
		long minutes = seconds / 60;		
		if(minutes >= 60) {
			minutes = minutes / 60;
			hours = true;
		}		
		String unit;
		if(minutes >= 1) {
			unit = (hours ? Policy.bind("RefreshSchedule.9") : Policy.bind("RefreshSchedule.10")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			unit = (hours ? Policy.bind("RefreshSchedule.11") : Policy.bind("RefreshSchedule.12")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return Policy.bind("RefreshSchedule.13", Long.toString(minutes), unit); //$NON-NLS-1$
	}
}