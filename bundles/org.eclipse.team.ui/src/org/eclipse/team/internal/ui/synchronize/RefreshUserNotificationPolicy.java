package org.eclipse.team.internal.ui.synchronize;

import java.util.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.actions.OpenInCompareAction;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.actions.ActionFactory;

/**
 * This class manages the notification and setup that occurs after a refresh is completed.
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
	public void refreshStarted(final IRefreshEvent event) {
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				if (event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
					ISynchronizeView view = TeamUI.getSynchronizeManager().showSynchronizeViewInActivePage();
					if (view != null) {
						view.display(participant);
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.jobs.IRefreshSubscriberListener#refreshDone(org.eclipse.team.internal.ui.jobs.IRefreshEvent)
	 */
	public ActionFactory.IWorkbenchAction refreshDone(final IRefreshEvent event) {
		// Ensure that this event was generated for this participant
		if (event.getSubscriber() != participant.getSubscriberSyncInfoCollector().getSubscriber()) return null;
		// If the event is for a cancelled operation, there's nothing to do
		int severity = event.getStatus().getSeverity();
		if(severity == Status.CANCEL || severity == Status.ERROR) return null;
		// Decide on what action to take after the refresh is completed
		return new WorkbenchAction() {
			public void run() {
				boolean prompt = (event.getStatus().getCode() == IRefreshEvent.STATUS_NO_CHANGES);
				
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
					SyncInfo info = participant.getSubscriberSyncInfoCollector().getSyncInfoSet().getSyncInfo(file);
					if(info != null) {
						OpenInCompareAction.openCompareEditor(participant.getName(), info, false, null);
						prompt = false;
					}
				}
				
				// Prompt user if preferences are set for this type of refresh.
				if (prompt) {
					notifyIfNeededModal(event);
				}
				setToolTipText(getToolTipText());
			}
			
			public String getToolTipText() {
				boolean prompt = (event.getStatus().getCode() == IRefreshEvent.STATUS_NO_CHANGES);
				if(prompt) {
					return Policy.bind("RefreshSubscriberJob.2a"); //$NON-NLS-1$
				} else {
					return Policy.bind("RefreshSubscriberJob.2b", participant.getName()); //$NON-NLS-1$
				}
			}
		};
	}
	
	private void notifyIfNeededModal(final IRefreshEvent event) {
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				String title = (event.getRefreshType() == IRefreshEvent.SCHEDULED_REFRESH ?
						Policy.bind("RefreshCompleteDialog.4a", Utils.getTypeName(participant)) : //$NON-NLS-1$
							Policy.bind("RefreshCompleteDialog.4", Utils.getTypeName(participant)) //$NON-NLS-1$
							);
				MessageDialog.openInformation(Utils.getShell(null), title, event.getStatus().getMessage());
			}
		});
	}
}