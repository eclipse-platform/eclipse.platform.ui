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
package org.eclipse.team.examples.filesystem.deployment;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.IDeploymentProviderManager;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IActionDelegate;

public class NullAction extends TeamAction implements IActionDelegate {

	public void run(IAction action) {
		MessageDialog.openQuestion(getShell(), "Action Run", "Action Run");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if(resources.length == 1) {
			IResource resource = resources[0];
			IDeploymentProviderManager manager = Team.getDeploymentManager();
			if(! manager.getMappedTo(resource, FileSystemDeploymentProvider.ID)) {
				return false;
			}
		}
		return true;
	}
}