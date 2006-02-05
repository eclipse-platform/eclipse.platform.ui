/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.ui.SaveablePartAdapter;
import org.eclipse.team.ui.synchronize.*;

public final class ModelParticipantPageDialog extends ParticipantPageDialog {
	
	/*
	 * Ids for custom buttons when previewing a merge/replace
	 */
	private static final int DONE_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int REPLACE_ID = IDialogConstants.CLIENT_ID + 2;

	private final ModelSynchronizeParticipant participant;
	private Button doneButton;
	private Button replaceButton;

	public ModelParticipantPageDialog(Shell shell, ModelSynchronizeParticipant participant, CompareConfiguration cc, ISynchronizePageConfiguration pc) {
		super(shell, createInput(shell, participant, cc, pc), participant);
		this.participant = participant;
	}

	private static SaveablePartAdapter createInput(Shell shell, ModelSynchronizeParticipant participant, CompareConfiguration cc, ISynchronizePageConfiguration pc) {
		ParticipantPageSaveablePart input = new ParticipantPageSaveablePart(shell, cc, pc, participant);
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.SaveablePartDialog#close()
	 */
	public boolean close() {
		boolean close = super.close();
		if (super.close()) {
			getInput().dispose();
		}
		return close;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ParticipantPageDialog#isOfferToRememberParticipant()
	 */
	protected boolean isOfferToRememberParticipant() {
		if (isReplace())
			return false;
		return super.isOfferToRememberParticipant();
	}

	private boolean isReplace() {
		return ((IMergeContext)participant.getContext()).getMergeType() == ISynchronizationContext.TWO_WAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.SaveablePartDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		boolean isReplace = isReplace();
		isReplace = false; // TODO: Disbled for now
		if (isReplace) {
			replaceButton = createButton(parent, REPLACE_ID, "&Replace", true); 
			replaceButton.setEnabled(true); 
		}
		doneButton = createButton(parent, DONE_ID, TeamUIMessages.ResourceMappingMergeOperation_2, !isReplace); 
		doneButton.setEnabled(true); 
		// Don't call super because we don't want the OK button to appear
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ParticipantPageDialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == DONE_ID) {
			super.buttonPressed(IDialogConstants.OK_ID);
		} else if (buttonId == REPLACE_ID) {
			// TODO: Disabled for now
//			try {
//				// Do this inline so we don't have to manage disposing of the context
//				PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
//					public void run(IProgressMonitor monitor) throws InvocationTargetException,
//							InterruptedException {
//						try {
//							
//							ModelParticipantPageDialog.this.operation.performMerge(monitor);
//						} catch (CoreException e) {
//							throw new InvocationTargetException(e);
//						}
//					}
//				});
//			} catch (InvocationTargetException e) {
//				Throwable t = e.getTargetException();
//				IStatus status;
//				if (t instanceof CoreException) {
//					CoreException ce = (CoreException) t;
//					status = ce.getStatus();
//				} else {
//					status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, TeamUIMessages.internal, t);
//					TeamUIPlugin.log(status);
//				}
//				ErrorDialog.openError(getShell(), null, null, status);
//				return;
//			} catch (InterruptedException e) {
//				// Operation was cancelled. Leave the dialog open
//				return;
//			}
			super.buttonPressed(IDialogConstants.OK_ID);
		} else {
			super.buttonPressed(buttonId);
		}
	}
}