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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.*;

/**
 * A dialog that displays the option of adding the participant to the {@link org.eclipse.team.ui.synchronize.ISynchronizeManager}
 * when the dialog is closed. This can be useful for showing changes for a participant modally and allowing the
 * user to decide if the participant shown should be made available non-modally.
 * 
 * @see SaveablePartAdapter
 * @see ISynchronizeParticipant
 * @since 3.0
 */
public class ParticipantPageDialog extends SaveablePartDialog {
		
	private ISynchronizeParticipant participant;
	private Button rememberParticipantButton;

	/**
	 * Creates a dialog with the given title and input. The input is not created until the dialog
	 * is opened.
	 * 
	 * @param shell the parent shell or <code>null</code> to create a top level shell. 
	 * @param title the shell's title
	 * @param input the compare input to show in the dialog
	 */
	public ParticipantPageDialog(Shell shell, SaveablePartAdapter input, ISynchronizeParticipant participant) {
		super(shell, input);
		this.participant = participant;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent2) {
		Composite parent = (Composite) super.createDialogArea(parent2);	
		ISynchronizeParticipantReference[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();	
		if (participant != null && ! particantRegisteredWithSynchronizeManager(participant)) {
			rememberParticipantButton = new Button(parent, SWT.CHECK);
			rememberParticipantButton.setText(Policy.bind("ParticipantCompareDialog.1")); //$NON-NLS-1$
		}
		Dialog.applyDialogFont(parent2);
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if(buttonId == IDialogConstants.OK_ID && isRememberParticipant()) {
			rememberParticipant();
		}
		super.buttonPressed(buttonId);
	}
	
	private boolean isRememberParticipant() {
		return getParticipant() != null && rememberParticipantButton != null && rememberParticipantButton.getSelection();
	}
	
	private boolean particantRegisteredWithSynchronizeManager(ISynchronizeParticipant participant) {
		return TeamUI.getSynchronizeManager().get(participant.getId(), participant.getSecondaryId()) != null;
	}
	
	private void rememberParticipant() {
		if(getParticipant() != null) {
			ISynchronizeManager mgr = TeamUI.getSynchronizeManager();
			ISynchronizeView view = mgr.showSynchronizeViewInActivePage();
			mgr.addSynchronizeParticipants(new ISynchronizeParticipant[] {getParticipant()});
			view.display(participant);
		}
	}
	
	/**
	 * Returns the participant showing in this dialog.
	 * 
	 * @return the participant showing in this dialog. 
	 */
	protected ISynchronizeParticipant getParticipant() {
		return participant;
	}
}