/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.text.DateFormat;
import java.util.Date;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ui.TeamUIMessages;
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
	
	private RefreshParticipantJob job;
	
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
			if (event.getParticipant() == participant) {
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
		RefreshParticipantJob.addRefreshListener(refreshSubscriberListener);
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
			job = new RefreshSubscriberParticipantJob(participant, TeamUIMessages.RefreshSchedule_14, NLS.bind(TeamUIMessages.RefreshSchedule_15, new String[] { participant.getName(), getRefreshIntervalAsString() }), participant.getResources(), new RefreshUserNotificationPolicy(getParticipant())); 
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
		RefreshParticipantJob.removeRefreshListener(refreshSubscriberListener);
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
			return TeamUIMessages.SyncViewPreferencePage_lastRefreshRunNever; 
		}
		long stopMills = event.getStopTime();
		StringBuffer text = new StringBuffer();
		if(stopMills <= 0) {
			text.append(TeamUIMessages.SyncViewPreferencePage_lastRefreshRunNever); 
		} else {
			Date lastTimeRun = new Date(stopMills);
			text.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastTimeRun));
		}
		SyncInfo[] changes = event.getChanges();
		if (changes.length == 0) {
			text.append(TeamUIMessages.RefreshSchedule_7); 
		} else if (changes.length == 1) {
			text.append(NLS.bind(TeamUIMessages.RefreshSchedule_changesSingular, new String[] { Integer.toString(changes.length) })); 
		} else {
			text.append(NLS.bind(TeamUIMessages.RefreshSchedule_changesPlural, new String[] { Integer.toString(changes.length) })); 
		}
		return text.toString();
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
			unit = (hours ? TeamUIMessages.RefreshSchedule_9 : TeamUIMessages.RefreshSchedule_10); // 
		} else {
			unit = (hours ? TeamUIMessages.RefreshSchedule_11 : TeamUIMessages.RefreshSchedule_12); // 
		}
		return NLS.bind(TeamUIMessages.RefreshSchedule_13, new String[] { Long.toString(minutes), unit }); 
	}
}
