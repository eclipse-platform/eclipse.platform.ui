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

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.*;

public class RefreshUserNotificationPolicyInModalDialog implements IRefreshSubscriberListener {

	private SubscriberParticipant participant;
	private boolean rememberInSyncView;
	private String targetId;
	private SyncInfoTree syncInfoSet;
	private Shell shell;

	public RefreshUserNotificationPolicyInModalDialog(Shell shell, String targetId, SubscriberParticipant participant, SyncInfoTree syncInfoSet) {
		this.targetId = targetId;
		this.participant = participant;
		this.syncInfoSet = syncInfoSet;
		this.shell = shell;
	}

	public void refreshStarted(IRefreshEvent event) {
	}

	public Runnable refreshDone(final IRefreshEvent event) {
		//	Ensure that this event was generated for this participant
		if (event.getSubscriber() != participant.getSubscriberSyncInfoCollector().getSubscriber())
			return null;
		//	 If the event is for a cancelled operation, there's nothing to do
		int severity = event.getStatus().getSeverity();
		if(severity == Status.CANCEL || severity == Status.ERROR) 
			return null;
		
		return new Runnable() {
			public void run() {
				try {
					// If there are no changes
					if (!areChanges()) {
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Policy.bind("OpenComparedDialog.noChangeTitle"), Policy.bind("OpenComparedDialog.noChangesMessage")); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					if (isSingleFileCompare(event.getResources())) {
						compareAndOpenEditors(event, participant);
					} else {
						compareAndOpenDialog(event, participant);
					}
				} finally {
					if (TeamUI.getSynchronizeManager().get(participant.getId(), participant.getSecondaryId()) == null) {
						participant.dispose();
					}
				}
			}
		};
	}

	private boolean areChanges() {
		return ! syncInfoSet.isEmpty();
	}

	protected boolean isSingleFileCompare(IResource[] resources) {
		return resources.length == 1 && resources[0].getType() == IResource.FILE;
	}

	protected void compareAndOpenEditors(IRefreshEvent event, SubscriberParticipant participant) {
		IResource[] resources = event.getResources();
		for (int i = 0; i < resources.length; i++) {
			SyncInfo info = participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet().getSyncInfo(resources[i]);
			if (info != null) {
				SyncInfoCompareInput input = new SyncInfoCompareInput(event.getSubscriber().getName(), info);
				CompareUI.openCompareEditor(input);
			}
		}
	}

	protected void compareAndOpenDialog(final IRefreshEvent event, final SubscriberParticipant participant) {
		TreeViewerAdvisor advisor = new TreeViewerAdvisor(targetId, null, syncInfoSet);
		CompareConfiguration cc = new CompareConfiguration();
		SynchronizeCompareInput input = new SynchronizeCompareInput(cc, advisor) {
			public String getTitle() {
				int numChanges = participant.getSubscriberSyncInfoCollector().getSyncInfoTree().size();
				if (numChanges > 1) {
					return Policy.bind("OpenComparedDialog.diffViewTitleMany", Integer.toString(numChanges)); //$NON-NLS-1$
				} else {
					return Policy.bind("OpenComparedDialog.diffViewTitleOne", Integer.toString(numChanges)); //$NON-NLS-1$
				}
			}
		};
		try {
			// model will be built in the background since we know the compare input was 
			// created with a subscriber participant
			input.run(new NullProgressMonitor());
		} catch (InterruptedException e) {
			Utils.handle(e);
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		}
		SynchronizeDialog dialog = new SynchronizeDialog(shell, participant.getName(), input);
		dialog.setSynchronizeParticipant(participant);
		dialog.setBlockOnOpen(true);
		dialog.open();
	}
}