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
package org.eclipse.team.ui.synchronize.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.internal.ui.synchronize.RefreshUserNotificationPolicy;
import org.eclipse.team.internal.ui.synchronize.RefreshUserNotificationPolicyInModalDialog;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A synchronize participant that displays synchronization information for local
 * resources that are managed via a {@link Subscriber}.
 *
 * @since 3.0
 */
public abstract class SubscriberParticipant extends AbstractSynchronizeParticipant implements IPropertyChangeListener {
	
	private SubscriberSyncInfoCollector collector;
	
	private SubscriberRefreshSchedule refreshSchedule;
	
	private int currentMode;
	
	private IWorkingSet workingSet;
	
	private ISynchronizeView view;
	
	/**
	 * Key for settings in memento
	 */
	private static final String CTX_SUBSCRIBER_PARTICIPANT_SETTINGS = TeamUIPlugin.ID + ".TEAMSUBSRCIBERSETTINGS"; //$NON-NLS-1$
	
	/**
	 * Key for schedule in memento
	 */
	private static final String CTX_SUBSCRIBER_SCHEDULE_SETTINGS = TeamUIPlugin.ID + ".TEAMSUBSRCIBER_REFRESHSCHEDULE"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the mode of a page has changed. 
	 */
	public static final String P_SYNCVIEWPAGE_WORKINGSET = TeamUIPlugin.ID  + ".P_SYNCVIEWPAGE_WORKINGSET";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating the schedule of a page has changed. 
	 */
	public static final String P_SYNCVIEWPAGE_SCHEDULE = TeamUIPlugin.ID  + ".P_SYNCVIEWPAGE_SCHEDULE";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating the mode of a page has changed. 
	 */
	public static final String P_SYNCVIEWPAGE_MODE = TeamUIPlugin.ID  + ".P_SYNCVIEWPAGE_MODE";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating the selection has changed.
	 */
	public static final String P_SYNCVIEWPAGE_SELECTION = TeamUIPlugin.ID  + ".P_SYNCVIEWPAGE_SELECTION";	 //$NON-NLS-1$
		
	/**
	 * Modes are direction filters for the view
	 */
	public final static int INCOMING_MODE = 0x1;
	public final static int OUTGOING_MODE = 0x2;
	public final static int BOTH_MODE = 0x4;
	public final static int CONFLICTING_MODE = 0x8;
	public final static int ALL_MODES = INCOMING_MODE | OUTGOING_MODE | CONFLICTING_MODE | BOTH_MODE;
	
	private final static int[] INCOMING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.INCOMING};
	private final static int[] OUTGOING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.OUTGOING};
	private final static int[] BOTH_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.INCOMING, SyncInfo.OUTGOING};
	private final static int[] CONFLICTING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING};

	private IRefreshSubscriberListenerFactory refreshListenerFactory;
	
	public SubscriberParticipant() {
		super();
		refreshSchedule = new SubscriberRefreshSchedule(this);
		refreshListenerFactory = new IRefreshSubscriberListenerFactory() {
			public IRefreshSubscriberListener createModalDialogListener(String targetId, SubscriberParticipant participant, SyncInfoTree syncInfoSet) {
				return new RefreshUserNotificationPolicyInModalDialog(targetId, participant, syncInfoSet);
			}
			public IRefreshSubscriberListener createSynchronizeViewListener(SubscriberParticipant participant) {
				return new RefreshUserNotificationPolicy(participant);
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeViewPage#createPage(org.eclipse.team.ui.sync.ISynchronizeView)
	 */
	public final IPageBookViewPage createPage(ISynchronizeView view) {
		return doCreatePage(view);
	}
	
	protected SubscriberParticipantPage doCreatePage(ISynchronizeView view) {
		return new SubscriberParticipantPage(this, view,  createSynchronizeViewerAdvisor(view));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#createRefreshPage()
	 */
	public IWizard createSynchronizeWizard() {
		return new SubscriberRefreshWizard(this);
	}
	
	public void setMode(int mode) {
		int oldMode = getMode();
		if(oldMode == mode) return;
		currentMode = mode;
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_SELECTED_MODE, mode);
		updateMode(mode);
		firePropertyChange(this, P_SYNCVIEWPAGE_MODE, new Integer(oldMode), new Integer(mode));
	}
	
	public int getMode() {
		return currentMode;
	}
	
	public void setRefreshSchedule(SubscriberRefreshSchedule schedule) {
		this.refreshSchedule = schedule;
		firePropertyChange(this, P_SYNCVIEWPAGE_SCHEDULE, null, schedule);
	}
	
	public SubscriberRefreshSchedule getRefreshSchedule() {
		return refreshSchedule;
	}
	
	public void setWorkingSet(IWorkingSet set) {
		IWorkingSet oldSet = workingSet;
		if(collector != null) {
			IResource[] resources = set != null ? Utils.getResources(set.getElements()) : new IResource[0];
			collector.setWorkingSet(resources);
			firePropertyChange(this, P_SYNCVIEWPAGE_WORKINGSET, oldSet, set);
		} 
		workingSet = set;
	}
	
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
	
	public void selectResources(IResource[] resources) {
		firePropertyChange(this, P_SYNCVIEWPAGE_SELECTION, null, new StructuredSelection(resources));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#getResources()
	 */
	public IResource[] getResources() {
		return collector.getSubscriber().roots();
	}
	
	/**
	 * Will refresh a participant in the background.
	 * 
	 * @param resources the resources to be refreshed.
	 */
	public void refresh(IResource[] resources, IRefreshSubscriberListener listener, String taskName, IWorkbenchSite site) {
		ISynchronizeView view = TeamUI.getSynchronizeManager().showSynchronizeViewInActivePage();
		refreshHelper(view.getSite(), taskName, resources, getSubscriberSyncInfoCollector(), listener);
	}
	
	public IRefreshSubscriberListenerFactory getRefreshListeners() {
		return getRefreshListenerFactory();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeViewPage#dispose()
	 */
	public void dispose() {
		refreshSchedule.dispose();				
		TeamUI.removePropertyChangeListener(this);
		collector.dispose();
	}
	
	/**
	 * Return the <code>SubscriberSyncInfoCollector</code> for the participant.
	 * This collector maintains the set of all out-of-sync resources for the subscriber.
	 * @return the <code>SubscriberSyncInfoCollector</code> for this participant
	 */
	public final SubscriberSyncInfoCollector getSubscriberSyncInfoCollector() {
		return collector;
	}
	
	protected void setSubscriber(Subscriber subscriber) {
		collector = new SubscriberSyncInfoCollector(subscriber);
		
		// listen for global ignore changes
		TeamUI.addPropertyChangeListener(this);
		
		preCollectingChanges();
		
		collector.start();
		
		// start the refresh now that a subscriber has been added
		SubscriberRefreshSchedule schedule = getRefreshSchedule();
		if(schedule.isEnabled()) {
			getRefreshSchedule().startJob();
		}
	}
	
	protected IRefreshSubscriberListenerFactory getRefreshListenerFactory() {
		return refreshListenerFactory;
	}
	
	/**
	 * This method is invoked just before the collector is started. 
	 * This gives an oportunity to configure the collector parameters
	 * before collection starts. The default implementation sets the working
	 * set as returned by <code>getWorkingSet()</code> and sets the mode 
	 * as returned by <code>getMode()</code>.
	 */
	protected void preCollectingChanges() {
		if(workingSet != null) {
			setWorkingSet(workingSet);
		}
		updateMode(getMode());
	}
	
	/**
	 * Returns the viewer advisor which will be used to configure the display of the participant.
	 * @return
	 */
	protected StructuredViewerAdvisor createSynchronizeViewerAdvisor(ISynchronizeView view) {
		return new SynchronizeViewerAdvisor(view, this);
	}
	
	/**
	 * Get the <code>Subscriber</code> for this participant
	 * @return a <code>TamSubscriber</code>
	 */
	public Subscriber getSubscriber() {
		return collector.getSubscriber();
	}
		
	/* (non-Javadoc)
	 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(TeamUI.GLOBAL_IGNORES_CHANGED)) {
			collector.reset();
		}	
	}
	
	/**
	 * This method is invoked from <code>setMode</code> when the mode has changed.
	 * It sets the filter on the collector to show the <code>SyncInfo</code>
	 * appropriate for the mode.
	 * @param mode the new mode (one of <code>INCOMING_MODE_FILTER</code>,
	 * <code>OUTGOING_MODE_FILTER</code>, <code>CONFLICTING_MODE_FILTER</code>
	 * or <code>BOTH_MODE_FILTER</code>)
	 */
	protected void updateMode(int mode) {
		if(collector != null) {	
		
			int[] modeFilter = BOTH_MODE_FILTER;
			switch(mode) {
			case SubscriberParticipant.INCOMING_MODE:
				modeFilter = INCOMING_MODE_FILTER; break;
			case SubscriberParticipant.OUTGOING_MODE:
				modeFilter = OUTGOING_MODE_FILTER; break;
			case SubscriberParticipant.BOTH_MODE:
				modeFilter = BOTH_MODE_FILTER; break;
			case SubscriberParticipant.CONFLICTING_MODE:
				modeFilter = CONFLICTING_MODE_FILTER; break;
			}

			collector.setFilter(
					new FastSyncInfoFilter.AndSyncInfoFilter(
							new FastSyncInfoFilter[] {
									new FastSyncInfoFilter.SyncInfoDirectionFilter(modeFilter)
							}));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(IMemento memento) throws PartInitException {
		if(memento != null) {
			IMemento settings = memento.getChild(CTX_SUBSCRIBER_PARTICIPANT_SETTINGS);
			if(settings != null) {
				String setSetting = settings.getString(P_SYNCVIEWPAGE_WORKINGSET);
				String modeSetting = settings.getString(P_SYNCVIEWPAGE_MODE);
				SubscriberRefreshSchedule schedule = SubscriberRefreshSchedule.init(settings.getChild(CTX_SUBSCRIBER_SCHEDULE_SETTINGS), this);
				setRefreshSchedule(schedule);
				
				if(setSetting != null) {
					IWorkingSet workingSet = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(setSetting);
					if(workingSet != null) {
						setWorkingSet(workingSet);
					}
				}
				
				int mode = SubscriberParticipant.BOTH_MODE;
				if(modeSetting != null) {
					try {
						mode = Integer.parseInt(modeSetting);
					} catch (NumberFormatException e) {
						mode = SubscriberParticipant.BOTH_MODE;
					}
				}
				setMode(mode);
			}
		} else {
			setMode(BOTH_MODE);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		IMemento settings = memento.createChild(CTX_SUBSCRIBER_PARTICIPANT_SETTINGS);
		IWorkingSet set = getWorkingSet();
		if(set != null) {
			settings.putString(P_SYNCVIEWPAGE_WORKINGSET, getWorkingSet().getName());
		}
		settings.putString(P_SYNCVIEWPAGE_MODE, Integer.toString(getMode()));
		refreshSchedule.saveState(settings.createChild(CTX_SUBSCRIBER_SCHEDULE_SETTINGS));
	}
	
	public static SubscriberParticipant find(Subscriber s) {
		ISynchronizeParticipant[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant p = participants[i];
			if(p instanceof SubscriberParticipant) {
				if(((SubscriberParticipant)p).getSubscriber().equals(s)) {
					return (SubscriberParticipant)p;
				}
			}
		}
		return null;
	}
	
	private void refreshHelper(IWorkbenchSite site, String taskName, IResource[] resources, final SubscriberSyncInfoCollector collector, final IRefreshSubscriberListener listener) {
		RefreshSubscriberJob job = new RefreshSubscriberJob(taskName, resources, collector.getSubscriber());
		job.setSubscriberCollector(collector);
		IRefreshSubscriberListener autoListener = new IRefreshSubscriberListener() {
			public void refreshStarted(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshStarted(event);
				}
			}
			public void refreshDone(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshDone(event);
					RefreshSubscriberJob.removeRefreshListener(this);
				}
			}
		};
		
		if (listener != null) {
			RefreshSubscriberJob.addRefreshListener(autoListener);
		}	
		Utils.schedule(job, site);
	}
}