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
			ErrorDialog.openError(getShell(), Policy.bind("DisconnectAction.errorTitle"), null, e.getStatus());
		} 
	}
	
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return true;
	}
}