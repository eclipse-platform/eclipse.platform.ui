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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.synchronize.viewers.*;

/**
 * Dialog that supports synchronizing a participant and displaying the results in a dialog. The user can edit the changes
 * in the dialog and will be prompted to save the changes when the dialog is closed. This provides a modal helper for
 * showing a subscriber participant to the user.
 * <p>
 * Example usage of this dialog:
 * <pre>
 * SubscriberParticipant = new MyParticipant();
 * SubscriberParticipantDialog dialog = new SubscriberParticipantDialog(shell, participant.getId(), participant, resources);
 * dialog.run();
 * </pre>
 * </p>
 * TODO: mention the remembering of the participant
 * 
 * @see SubscriberParticipant
 * @since 3.0
 */
public class SubscriberParticipantDialog {

	private SubscriberParticipant participant;
	private Shell shell;
	private IResource[] resources;
	private boolean rememberInSyncView;
	private String targetId;

	/**
	 * 
	 * @param shell shell to use to open the compare dialog
	 * @param participant the participant to use as a basis for the comparison
	 * @param resources
	 */
	public SubscriberParticipantDialog(Shell shell, String targetId, SubscriberParticipant participant, IResource[] resources) {
		this.shell = shell;
		this.targetId = targetId;
		this.participant = participant;
		this.resources = resources;
	}

	public void run() {
		Subscriber s = participant.getSubscriber();
		RefreshAction.run(null, participant.getName(), s.roots(), participant.getSubscriberSyncInfoCollector(), new IRefreshSubscriberListener() {
			public void refreshStarted(IRefreshEvent event) {
			}
			public void refreshDone(final IRefreshEvent event) {
				TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						if (participant.getSubscriberSyncInfoCollector().getSyncInfoTree().isEmpty()) {
							MessageDialog.openInformation(getShell(), Policy.bind("OpenComparedDialog.noChangeTitle"), Policy.bind("OpenComparedDialog.noChangesMessage")); //$NON-NLS-1$ //$NON-NLS-2$
							return;
						}
						if (isSingleFileCompare(resources)) {
							compareAndOpenEditors(event, participant);
						} else {
							compareAndOpenDialog(event, participant);
						}
					}
				});
			}
		});
	}

	protected Shell getShell() {
		return shell;
	}
	
	protected boolean isSingleFileCompare(IResource[] resources) {
		return resources.length == 1 && resources[0].getType() == IResource.FILE;
	}

	protected void compareAndOpenEditors(IRefreshEvent event, SubscriberParticipant participant) {
		for (int i = 0; i < resources.length; i++) {
			SyncInfo info = participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet().getSyncInfo(resources[i]);
			if(info != null) {
				CompareUI.openCompareEditor(new SyncInfoCompareInput(event.getSubscriber().getName(), info));
			}
		}
	}

	protected void compareAndOpenDialog(final IRefreshEvent event, final SubscriberParticipant participant) {
		TreeViewerAdvisor advisor = new TreeViewerAdvisor(targetId, null, participant.getSubscriberSyncInfoCollector().getSyncInfoTree());
		CompareConfiguration cc = new CompareConfiguration();
		SynchronizeCompareInput input = new SynchronizeCompareInput(cc, advisor) {
			public String getTitle() {
				int numChanges = participant.getSubscriberSyncInfoCollector().getSyncInfoTree().size();
				if(numChanges > 1) {
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
		SynchronizeDialog dialog = createCompareDialog(getShell(), participant.getName(), input);
		if(isRememberInSyncView()) {
			dialog.setSynchronizeParticipant(participant);
		}
		dialog.setBlockOnOpen(true);
		dialog.open();
	}
	
	protected SynchronizeDialog createCompareDialog(Shell shell, String title, CompareEditorInput input) {
		return new SynchronizeDialog(shell, title, input);
	}
	
	/**
	 * @return Returns the rememberInSyncView.
	 */
	public boolean isRememberInSyncView() {
		return rememberInSyncView;
	}
	/**
	 * @param rememberInSyncView The rememberInSyncView to set.
	 */
	public void setRememberInSyncView(boolean rememberInSyncView) {
		this.rememberInSyncView = rememberInSyncView;
	}
}
