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
package org.eclipse.team.examples.filesystem.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.filesystem.Policy;
import org.eclipse.team.internal.ui.actions.TeamAction;

/**
 * Action for getting the contents of the selected resources
 */
public class DisconnectAction extends TeamAction {
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IProject projects[] = getSelectedProjects();
		try {
			for (int i = 0; i < projects.length; i++) {
				RepositoryProvider.unmap(projects[i]);
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), Policy.bind("DisconnectAction.errorTitle"), null, e.getStatus()); //$NON-NLS-1$
		} 
	}
	
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return true;
	}
}
