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
			TeamUIPlugin.runWithProgressDialog(getShell(), true /* cancelable */, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
						try {
							createDir(getShell(), getSelectedRemoteFolders()[0], new String());
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
				}
			});
		} catch (InvocationTargetException e) {
			handle(e, Policy.bind("Error"), Policy.bind("CreateNewFolderAction.errorCreatingFolder")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Throws a TeamException if one occured.
	 * Returns null if the operation was cancelled or an exception occured
	 */
	public static IRemoteTargetResource createDir(final Shell shell, final IRemoteTargetResource parent, final String defaultName) throws TeamException {
		final IRemoteTargetResource[] newFolder = new IRemoteTargetResource[] {null};
		try {				
			TeamUIPlugin.runWithProgressDialog(shell, true, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(Policy.bind("CreateNewFolderAction.creatingFolder"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
						final String[] folderName = new String[] {null};
						final String suggestedName = getSuggestedFolderName(parent, Policy.subMonitorFor(monitor, 0), defaultName);
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								InputDialog dialog = new InputDialog(shell, 
									Policy.bind("CreateNewFolderAction.title"),  //$NON-NLS-1$
									Policy.bind("CreateNewFolderAction.message"),  //$NON-NLS-1$
									suggestedName,
									null);
								Policy.checkCanceled(monitor);
								if(dialog.open() == dialog.OK) {
									folderName[0] = dialog.getValue();
								}
							}
						});
						if(folderName[0] != null) {
							newFolder[0] = parent.getFolder(folderName[0]);
							newFolder[0].mkdirs(Policy.subMonitorFor(monitor, 0));
						}
					} catch(TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch(InvocationTargetException e) {
			if (e.getTargetException() instanceof TeamException) {
				throw (TeamException)e.getTargetException();
			}
			TeamUIPlugin.handle(e);
		} catch(InterruptedException e) {
		}
		return newFolder[0];
	}
	
	protected static String getSuggestedFolderName(IRemoteTargetResource parent, IProgressMonitor monitor, String defaultName) throws TeamException {		
		String suggestedFolderName = defaultName;
		IRemoteResource[] members;
		monitor.subTask(Policy.bind("CreateNewFolderAction.suggestedNameProgress")); //$NON-NLS-1$
		members = parent.members(monitor);
		int numNewFolders = 0;
		for (int i = 0; i < members.length; i++) {
			if(members[i].getName().equals(defaultName)) {
				numNewFolders++;
			}							
		}
		if(numNewFolders != 0) {
			suggestedFolderName = Policy.bind("CreateNewFolderAction.suggestedNameConcat", defaultName, String.valueOf(numNewFolders)); //$NON-NLS-1$
		}
		return suggestedFolderName;
	}
}