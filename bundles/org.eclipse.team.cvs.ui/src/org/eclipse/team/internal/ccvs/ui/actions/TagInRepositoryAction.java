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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class TagInRepositoryAction extends TagAction {
	/**
	 * Returns the selected remote folders
	 */
	protected ICVSRemoteFolder[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRemoteFolder) {
					if (!Checkout.ALIAS.isElementOf(((ICVSRemoteFolder)next).getLocalOptions())) {
						resources.add(next);
					}
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRemoteFolder.class);
					if (adapter instanceof ICVSRemoteFolder) {
						if (!Checkout.ALIAS.isElementOf(((ICVSRemoteFolder)adapter).getLocalOptions())) {
							resources.add(adapter);
						}
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (ICVSRemoteFolder[])resources.toArray(new ICVSRemoteFolder[resources.size()]);
		}
		return new ICVSRemoteFolder[0];
	}
	
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					final ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
					final String[] result = new String[1];
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = promptForTag(folders[0]);
						}
					});
					if (result[0] == null) return;

					monitor.beginTask(null, 1000 * folders.length);
					CVSTag tag = new CVSTag(result[0], CVSTag.VERSION);
					
					for (int i = 0; i < folders.length; i++) {
						folders[i].tag(tag, Command.NO_LOCAL_OPTIONS, new SubProgressMonitor(monitor, 1000));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("TagAction.tagProblemsMessage"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] resources = getSelectedRemoteFolders();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof ICVSRepositoryLocation) return false;
		}
		return true;
	}
}