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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.RTag;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.ui.actions.TeamAction;

public class MoveTagAction extends TeamAction {

	/**
	 * Returns the selected remote folders
	 */
	protected ICVSFolder[] getSelectedFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSFolder) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSFolder.class);
					if (adapter instanceof ICVSFolder) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (ICVSFolder[])resources.toArray(new ICVSFolder[resources.size()]);
		}
		return new ICVSFolder[0];
	}
	
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSFolder[] resources = getSelectedFolders();
		if (resources.length == 0) return false;
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		// Setup the holders
		final CVSTag[] tag = new CVSTag[] {null};
		final ICVSFolder[] folders = getSelectedFolders();
		
		// Show a busy cursor while display the tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {

				if(folders.length == 0) {
					// nothing to do
					return;
				}

				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), folders, 
					Policy.bind("MoveTagAction.title"), //$NON-NLS-1$
					Policy.bind("MoveTagAction.message"), false); //$NON-NLS-1$
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					return;
				}
				tag[0] = dialog.getResult();
				
			}
		}, Policy.bind("MoveTagAction.errorMessage"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$
		
		// Show progress while tagging remotely
		if (tag[0] == null) return;
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(null, 1000 * folders.length);
					for (int i = 0; i < folders.length; i++) {
						((ICVSRemoteFolder)folders[i]).tag(tag[0], new LocalOption[] {RTag.FORCE_REASSIGNMENT, RTag.CLEAR_FROM_REMOVED}, new SubProgressMonitor(monitor, 1000));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("MoveTagAction.errorMessage"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}

}
