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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.RTag;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.internal.ui.actions.TeamAction;

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
			Policy.bind("MoveTagAction.message"), false, true); //$NON-NLS-1$
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
