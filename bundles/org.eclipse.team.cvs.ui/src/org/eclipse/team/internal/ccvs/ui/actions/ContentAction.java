/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.internal.ccvs.ui.AvoidableMessageDialog;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

public class ContentAction extends ActionDelegate implements IEditorActionDelegate {
	CVSSyncCompareInput syncInput;
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		if (store.getBoolean(ICVSUIConstants.PREF_PROMPT_ON_CHANGE_GRANULARITY)) {
			Shell shell = syncInput.getViewer().getControl().getShell();
			AvoidableMessageDialog dialog = new AvoidableMessageDialog(
					shell,
					Policy.bind("ContentAction.Confirm_Long_Operation_1"), //$NON-NLS-1$
					null,	// accept the default window icon
					Policy.bind("ContentAction.Changing_this_setting_will_involve_contacting_the_server_and_may_be_long-running_2"), //$NON-NLS-1$
					MessageDialog.QUESTION, 
					new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
					0);
					
			boolean result = dialog.open() == 0;
			if (result && dialog.isDontShowAgain()) {
				store.setValue(ICVSUIConstants.PREF_PROMPT_ON_CHANGE_GRANULARITY, false);
			}
			if (!result) return;
		}			
		syncInput.setSyncGranularity(action.isChecked() ? ILocalSyncElement.GRANULARITY_CONTENTS : ILocalSyncElement.GRANULARITY_TIMESTAMP);
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			IEditorInput input = targetEditor.getEditorInput();
			if (input instanceof CVSSyncCompareInput) {
				this.syncInput = (CVSSyncCompareInput)input;
				action.setEnabled(true);
				action.setChecked(syncInput.getSyncGranularity() == ILocalSyncElement.GRANULARITY_CONTENTS);
				return;
			}
		}
		syncInput = null;
		action.setEnabled(false);
	}

}
