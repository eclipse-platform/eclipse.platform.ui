/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * Action to create a new remote folder.
 */
public class CreateNewFolderAction extends TargetAction {

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length == 1;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
			createDir(getShell(), getSelectedRemoteFolders()[0]);
		} catch (TeamException e) {
			handle(e, Policy.bind("Error"), Policy.bind("CreateNewFolderAction.errorCreatingFolder")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public static IRemoteTargetResource createDir(Shell shell, final IRemoteTargetResource parent) throws TeamException {
		final String[] suggestedFolderName = new String[] {Policy.bind("CreateNewFolderAction.newFolderName")}; //$NON-NLS-1$
		try {
			TeamUIPlugin.runWithProgress(shell, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						IRemoteResource[] members;
						try {
							members = parent.members(null);
						} catch (TeamException e) {
							return;
						}
						int numNewFolders = 0;
						for (int i = 0; i < members.length; i++) {
							if(members[i].getName().equals(Policy.bind("CreateNewFolderAction.newFolderName"))) { //$NON-NLS-1$
								numNewFolders++;
							}							
						}
						if(numNewFolders != 0) {
							suggestedFolderName[0] +=  " " + numNewFolders; //$NON-NLS-1$
						}						
				}
			});
		// ignore, just use the default name
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
		
		InputDialog dialog = new InputDialog(shell, 
					Policy.bind("CreateNewFolderAction.title"),  //$NON-NLS-1$
					Policy.bind("CreateNewFolderAction.message"),  //$NON-NLS-1$
					suggestedFolderName[0], 
					null);
		if(dialog.open() == dialog.OK) {
			String folderName = dialog.getValue();
			IRemoteTargetResource newFolder = parent.getFolder(folderName);
			newFolder.mkdirs();
			return newFolder;
		}
		return null;
	}
}
