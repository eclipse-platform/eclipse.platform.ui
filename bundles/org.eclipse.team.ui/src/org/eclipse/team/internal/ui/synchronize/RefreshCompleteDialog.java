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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.dialogs.DetailsDialog;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * A dialog that is displayed at the end of a synchronize. The dialog shows the result of
 * the synchronize operation. A details area is shown if there are new changes found, 
 * otherwise a message displays the current changes in the given participant.
 *
 * @since 3.0
 */
public class RefreshCompleteDialog extends DetailsDialog {
	private IRefreshEvent event;
	private SubscriberParticipant participant;
	private Button dontShowAgainButton;
	private SyncInfoTree syncInfoSet = new SyncInfoTree();
	
	public RefreshCompleteDialog(Shell parentShell, IRefreshEvent event, SubscriberParticipant participant) {
		super(parentShell, 
				event.getRefreshType() == IRefreshEvent.SCHEDULED_REFRESH ?
						Policy.bind("RefreshCompleteDialog.4a", participant.getName()) : //$NON-NLS-1$
						Policy.bind("RefreshCompleteDialog.4", participant.getName()) //$NON-NLS-1$
						);
		this.participant = participant;
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX);
		this.event = event;
		setImageKey(DLG_IMG_INFO);
		IDialogSettings workbenchSettings = TeamUIPlugin.getPlugin().getDialogSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createDropDownDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createDropDownDialogArea(Composite parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		IStatus status = event.getStatus();
		if(status.getSeverity() == IStatus.INFO) {
			createLabel(parent, status.getMessage(), 2);
		}
			
		dontShowAgainButton = new Button(parent, SWT.CHECK);
		dontShowAgainButton.setText(Policy.bind("RefreshCompleteDialog.22")); //$NON-NLS-1$
	
		initializeSettings();
		Dialog.applyDialogFont(parent);
	}

	protected SyncInfoSet getSubscriberSyncInfoSet() {
		return participant.getSubscriberSyncInfoCollector().getSyncInfoSet();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeCancelButton()
	 */
	protected boolean includeCancelButton() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeDetailsButton()
	 */
	protected boolean includeDetailsButton() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#includeErrorMessage()
	 */
	protected boolean includeErrorMessage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if(dontShowAgainButton != null) {
			if(event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
				TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_COMPLETE_SHOW_DIALOG, ! dontShowAgainButton.getSelection());
			} else {
				TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_SCHEDULED_COMPLETE_SHOW_DIALOG, ! dontShowAgainButton.getSelection());
			}
		}
		TeamUIPlugin.getPlugin().savePluginPreferences();		
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.dialogs.DetailsDialog#updateEnablements()
	 */
	protected void updateEnablements() {	
	}

	private Label createLabel(Composite parent, String text, int columns) {
		Label label = new Label(parent, SWT.WRAP);
		label.setText(text);
		GridData data =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint =
			convertHorizontalDLUsToPixels(
				IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		data.horizontalSpan = columns;
		label.setLayoutData(data);
		return label;
	}

	private IResource[] getResources() {
		SyncInfo[] changes = event.getChanges();
		IResource[] resources = new IResource[changes.length];
		for (int i = 0; i < changes.length; i++) {
			SyncInfo info = changes[i];
			resources[i] = info.getLocal();
		}
		return resources;
	}
	
	private void initializeSettings() {
		if(dontShowAgainButton != null) {
			if(event.getRefreshType() == IRefreshEvent.USER_REFRESH) {
				dontShowAgainButton.setSelection(! TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCHRONIZING_COMPLETE_SHOW_DIALOG));
			} else {
				dontShowAgainButton.setSelection(! TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCHRONIZING_SCHEDULED_COMPLETE_SHOW_DIALOG));
			}
		}
	}
}