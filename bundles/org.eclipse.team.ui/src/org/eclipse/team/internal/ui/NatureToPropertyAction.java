/*******************************************************************************
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class NatureToPropertyAction extends TeamAction {

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return true;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					final Shell shell = getShell();
					IProject[] projects = getSelectedProjects();
					List statii = new ArrayList();
					for (int i = 0; i < projects.length; i++) {
						IFile file = projects[i].getFile(".project"); //$NON-NLS-1$
						IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, shell);
						if (status.getCode() == IStatus.OK) {
							RepositoryProvider.convertNatureToProperty(projects[i], true);
						} else {
							statii.add(status);
							RepositoryProvider.convertNatureToProperty(projects[i], false);
						}
					}
					if (!statii.isEmpty()) {
						final IStatus[] statusArray = (IStatus[])statii.toArray(new IStatus[statii.size()]);
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								if (statusArray.length == 1) {
									ErrorDialog.openError(shell, Policy.bind("NatureToPropertyAction.label"), Policy.bind("NatureToPropertyAction.message"), statusArray[0]); //$NON-NLS-1$ //$NON-NLS-2$
								} else {
									ErrorDialog.openError(shell, Policy.bind("NatureToPropertyAction.label"), Policy.bind("NatureToPropertyAction.message"), new MultiStatus(TeamUIPlugin.ID, 0, statusArray, Policy.bind("NatureToPropertyAction.multiMessage"), null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								}
							}
						});
						for (int i = 0; i < statusArray.length; i++) {
							TeamUIPlugin.log(statusArray[i]);
						}
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("NatureToPropertyAction.label"), this.PROGRESS_DIALOG);  //$NON-NLS-1$
	}
}

