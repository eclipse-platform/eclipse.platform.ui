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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.team.core.DeploymentProvider;
import org.eclipse.team.core.IDeploymentProviderManager;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IActionDelegate;

public class UnDeployAction extends TeamAction implements IActionDelegate {

	public void run(IAction action) {
		IContainer container = (IContainer)getSelectedResources()[0];
		IDeploymentProviderManager manager = Team.getDeploymentManager();
		try {
			DeploymentProvider[] providers = manager.getMappings(container, FileSystemDeploymentProvider.ID);
			for (int i = 0; i < providers.length; i++) {
				DeploymentProvider provider = providers[i];
				manager.unmap(container, provider);
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), "Error", "Un-Mapping", e.getStatus());
		}
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