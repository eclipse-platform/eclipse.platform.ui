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
import org.eclipse.swt.widgets.Shell;

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
		participant.refresh(resources, participant.getRefreshListenerFactory().createModalDialogListener(targetId, participant, participant.getSubscriberSyncInfoCollector().getSyncInfoTree()), participant.getName(), null);
	}
}
