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
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.RTag;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;

public class MoveRemoteTagAction extends TagInRepositoryAction {

	private boolean recursive;
	
	/**
	 * @see TagRemoteAction#getLocalOptions()
	 */
	protected LocalOption[] getLocalOptions() {
		LocalOption[] options;
		if(recursive) {
			options = new LocalOption[] {RTag.FORCE_REASSIGNMENT, RTag.CLEAR_FROM_REMOVED};
		} else {
			options = new LocalOption[] {RTag.FORCE_REASSIGNMENT, RTag.CLEAR_FROM_REMOVED, Command.DO_NOT_RECURSE};
		}
		return options;
	}

	/**
	 * @see TagRemoteAction#promptForTag(ICVSFolder[])
	 */
	protected CVSTag promptForTag(ICVSFolder[] folders) {
		// Allow the user to select a tag
		TagSelectionDialog dialog = new TagSelectionDialog(getShell(), folders, 
			Policy.bind("MoveTagAction.title"), //$NON-NLS-1$
			Policy.bind("MoveTagAction.message"), //$NON-NLS-1$
			TagSelectionDialog.INCLUDE_BRANCHES | TagSelectionDialog.INCLUDE_VERSIONS, 
			true, /* show recurse */
			IHelpContextIds.TAG_REMOTE_WITH_EXISTING_DIALOG);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == Dialog.CANCEL) {
			return null;
		}
		CVSTag tag = dialog.getResult();
		if (tag != null) {
			recursive = dialog.getRecursive();
		}
		return tag;
	}

}
