package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.team.ui.synchronize.subscribers.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * This class manages the notification and setup that occurs after a refresh is completed.
 * 
 * 
 */
public class RefreshUserNotificationPolicy implements IRefreshSubscriberListener {

	private SubscriberParticipant participant;

	public RefreshUserNotificationPolicy(SubscriberParticipant participant) {
		this.participant = participant;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.jobs.IRefreshSubscriberListener#refreshStarted(org.eclipse.team.internal.ui.jobs.IRefreshEvent)
	 */
	public void refreshStarted(IRefreshEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.jobs.IRefreshSubscriberListener#refreshDone(org.eclipse.team.internal.ui.jobs.IRefreshEvent)
	 */
	public void refreshDone(final IRefreshEvent event) {
		// Ensure that this event was generated for this participant
		if (event.getSubscriber() != participant.getSubscriberSyncInfoCollector().getSubscriber()) return;
		// If the event is for a cancelled operation, there's nothing to do
		if(! event.getStatus().isOK()) return;
		// Decide on what action to take after the refresh is completed
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
					boolean prompt = true;
					if(event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
						prompt = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCHRONIZING_COMPLETE_SHOW_DIALOG);
					} else {
						prompt = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCHRONIZING_SCHEDULED_COMPLETE_SHOW_DIALOG);
					}
				
					SyncInfo[] infos = event.getChanges();
					List selectedResources = new ArrayList();
					selectedResources.addAll(Arrays.asList(event.getResources()));
					for (int i = 0; i < infos.length; i++) {
						selectedResources.add(infos[i].getLocal());
					}
					IResource[] resources = (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);
					
					// If it's a file, simply show the compare editor
					if (resources.length == 1 && resources[0].getType() == IResource.FILE) {
						IResource file = resources[0];
						SyncInfo info = participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet().getSyncInfo(file);
						if(info != null) {
							SyncInfoCompareInput input = new SyncInfoCompareInput(participant.getName(), info);
							CompareUI.openCompareEditor(input);
							input.setCompareEditor(TeamUIPlugin.getActivePage().getActiveEditor());
							prompt = false;
						}
					}
						
					// Prompt user if preferences are set for this type of refresh.
					if (prompt) {
						notifyIfNeededModal(event);
					}
				}	
		});
	}
	
	private void notifyIfNeededModal(final IRefreshEvent event) {
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				RefreshCompleteDialog d = new RefreshCompleteDialog(new Shell(TeamUIPlugin.getStandardDisplay()), event, participant);
				d.setBlockOnOpen(false);
				d.open();
			}
		});
	}

	private void notifyIfNeededNonModal(final IRefreshEvent event) {
		String message = Policy.bind("RefreshUserNotificationPolicy.0", event.getSubscriber().getName()); //$NON-NLS-1$
		PlatformUI.getWorkbench().getProgressService().requestInUI(new UIJob(message) {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				RefreshCompleteDialog d = new RefreshCompleteDialog(new Shell(TeamUIPlugin.getStandardDisplay()), event, participant);
				d.setBlockOnOpen(false);
				d.open();
				return Status.OK_STATUS;
			}
		}, message);
	}
}