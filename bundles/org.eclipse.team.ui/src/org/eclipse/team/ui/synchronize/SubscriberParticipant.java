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
package org.eclipse.team.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.progress.UIJob;

/**
 * A synchronize participant that displays synchronization information for local
 * resources that are managed via a {@link Subscriber}.
 * 
 * Participant:
 * 1. maintains a collection of all out-of-sync resources for a subscriber
 * 2. synchronize schedule
 * 3. APIs for creating specific: sync page, sync wizard, sync advisor (control ui pieces)
 * 4. allows refreshing the participant synchronization state
 *
 * @since 3.0
 */
public abstract class SubscriberParticipant extends AbstractSynchronizeParticipant implements IPropertyChangeListener {
	
	/**
	 * Collects and maintains set of all out-of-sync resources of the subscriber
	 */
	private SubscriberSyncInfoCollector collector;
	
	private SubscriberRefreshSchedule refreshSchedule;
	
	/**
	 * Key for settings in memento
	 */
	private static final String CTX_SUBSCRIBER_PARTICIPANT_SETTINGS = TeamUIPlugin.ID + ".TEAMSUBSRCIBERSETTINGS"; //$NON-NLS-1$
	
	/**
	 * Key for schedule in memento
	 */
	private static final String CTX_SUBSCRIBER_SCHEDULE_SETTINGS = TeamUIPlugin.ID + ".TEAMSUBSRCIBER_REFRESHSCHEDULE"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the schedule of a page has changed. 
	 */
	public static final String P_SYNCVIEWPAGE_SCHEDULE = TeamUIPlugin.ID  + ".P_SYNCVIEWPAGE_SCHEDULE";	 //$NON-NLS-1$
	
	public SubscriberParticipant() {
		super();
		refreshSchedule = new SubscriberRefreshSchedule(this);
	}
	
	/**
	 * Initialize the particpant sync info set in the configuration.
	 * Subclasses may override but must invoke the inherited method.
	 * @param configuration the page configuration
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		configuration.setProperty(SynchronizePageConfiguration.P_PARTICIPANT_SYNC_INFO_SET, collector.getSyncInfoSet());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeViewPage#createPage(org.eclipse.team.ui.sync.ISynchronizeView)
	 */
	public final IPageBookViewPage createPage(ISynchronizePageConfiguration configuration) {
		validateConfiguration(configuration);
		return new SubscriberParticipantPage(configuration, getSubscriberSyncInfoCollector());
	}
	
	/**
	 * This method is invoked before the given configuration is used to
	 * create the page (see <code>createPage(ISynchronizePageConfiguration)</code>).
	 * The configuration would have been initialized by 
	 * <code>initializeConfiguration(ISynchronizePageConfiguration)</code>
	 * but may have also been tailored further. This method gives the particpant 
	 * a chance to validate those changes before the page is created.
	 * @param configuration the page configuration that is about to be used to create a page.
	 */
	protected void validateConfiguration(ISynchronizePageConfiguration configuration) {
		// Do nothing by default
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#createRefreshPage()
	 */
	public IWizard createSynchronizeWizard() {
		return new SubscriberRefreshWizard(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#getResources()
	 */
	public IResource[] getResources() {
		return collector.getSubscriber().roots();
	}
	
	private void internalRefresh(IResource[] resources, final IRefreshSubscriberListener listener, String taskName, IWorkbenchSite site) {
		final IWorkbenchAction[] gotoAction = new IWorkbenchAction[] {null};
		final RefreshSubscriberJob job = new RefreshSubscriberJob(taskName, resources, collector.getSubscriber());
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		group.beginTask(taskName + " " + getName(), 100);
		job.setProgressGroup(group, 80);
		collector.setProgressGroup(group, 20);
		job.setUser(true);
		job.setSubscriberCollector(collector);
		job.setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "icon"), getImageDescriptor());
		job.setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "goto"), new WorkbenchAction() {
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
		});
		// Listener delagate
		IRefreshSubscriberListener autoListener = new IRefreshSubscriberListener() {
			public void refreshStarted(IRefreshEvent event) {
				if(listener != null) {
					listener.refreshStarted(event);
				}
			}
			public ActionFactory.IWorkbenchAction refreshDone(IRefreshEvent event) {
				if(listener != null) {
					// Update the progress properties. Only keep the synchronize if the operation is non-modal.
					Boolean modelProperty = (Boolean)job.getProperty(ProgressManager.PROPERTY_IN_DIALOG);
					boolean isModal = true;
					if(modelProperty != null) {
						isModal = modelProperty.booleanValue();
					}

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
						gotoAction[0].setEnabled(runnable.isEnabled());
						runnable.addPropertyChangeListener(new IPropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent event) {
								if(event.getProperty().equals(IAction.ENABLED)) {
									Boolean bool = (Boolean) event.getNewValue();
									gotoAction[0].setEnabled(bool.booleanValue());
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
		Utils.schedule(job, site);
	}
	
	/**
	 * Refresh this participants synchronization state and displays the result in a model dialog. 
	 * @param resources
	 * @param taskName
	 * @param site
	 */
	public final void refreshInDialog(Shell shell, IResource[] resources, String taskName, ISynchronizePageConfiguration configuration, IWorkbenchSite site) {
		IRefreshSubscriberListener listener =  new RefreshUserNotificationPolicyInModalDialog(shell, configuration, this);
		internalRefresh(resources, listener, taskName, site);
	}
	
	/**
	 * Will refresh a participant in the background.
	 * 
	 * @param resources the resources to be refreshed.
	 */
	public final void refresh(IResource[] resources, String taskName, IWorkbenchSite site) {
		IRefreshSubscriberListener listener = new RefreshUserNotificationPolicy(this);
		internalRefresh(resources, listener, taskName, site);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeViewPage#dispose()
	 */
	public void dispose() {
		refreshSchedule.dispose();				
		TeamUI.removePropertyChangeListener(this);
		collector.dispose();
	}
	
	public SyncInfoTree getSyncInfoSet() {
		return getSubscriberSyncInfoCollector().getSyncInfoSet();
	}
	
	protected void setSubscriber(Subscriber subscriber) {
		collector = new SubscriberSyncInfoCollector(subscriber);
		
		// listen for global ignore changes
		TeamUI.addPropertyChangeListener(this);
		
		// Start collecting changes
		collector.start();
		
		// Start the refresh now that a subscriber has been added
		SubscriberRefreshSchedule schedule = getRefreshSchedule();
		if(schedule.isEnabled()) {
			getRefreshSchedule().startJob();
		}
	}
	
	/**
	 * Get the <code>Subscriber</code> for this participant
	 * @return a <code>TamSubscriber</code>
	 */
	public Subscriber getSubscriber() {
		if (collector == null) return null;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(String secondaryId, IMemento memento) throws PartInitException {
		if(memento != null) {
			IMemento settings = memento.getChild(CTX_SUBSCRIBER_PARTICIPANT_SETTINGS);
			if(settings != null) {
				SubscriberRefreshSchedule schedule = SubscriberRefreshSchedule.init(settings.getChild(CTX_SUBSCRIBER_SCHEDULE_SETTINGS), this);
				setRefreshSchedule(schedule);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		IMemento settings = memento.createChild(CTX_SUBSCRIBER_PARTICIPANT_SETTINGS);
		refreshSchedule.saveState(settings.createChild(CTX_SUBSCRIBER_SCHEDULE_SETTINGS));
	}

	/*
	 * Reset the sync set of the particpant by repopulating it from scratch.
	 */
	public void reset() {
		getSubscriberSyncInfoCollector().reset();
	}
	
	/*
	 * Return the <code>SubscriberSyncInfoCollector</code> for the participant.
	 * This collector maintains the set of all out-of-sync resources for the subscriber.
	 * @return the <code>SubscriberSyncInfoCollector</code> for this participant
	 */
	public SubscriberSyncInfoCollector getSubscriberSyncInfoCollector() {
		return collector;
	}
	
	public void setRefreshSchedule(SubscriberRefreshSchedule schedule) {
		this.refreshSchedule = schedule;
		firePropertyChange(this, P_SYNCVIEWPAGE_SCHEDULE, null, schedule);
	}
	
	public SubscriberRefreshSchedule getRefreshSchedule() {
		return refreshSchedule;
	}
	
	/**
	 * Provide a filter that is used to filter the contents of the
	 * sync info set for the participant. Normally, all out-of-sync
	 * resources from the subscriber will be included in the 
	 * participant's set. However, a filter can be used to exclude
	 * some of these out-of-sync resources, if desired.
	 * <p>
	 * Subsclasses can invoke this method any time after 
	 * <code>setSubscriber</code> has been invoked.
	 * @param filter a sync info filter
	 */
	protected void setSyncInfoFilter(SyncInfoFilter filter) {
		collector.setFilter(filter);
	}
}