/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.Action;

/**
 * OrganizeFavoritesAction
 */
public class OrganizeFavoritesAction extends Action {
	
	// launch group identifier
	private String fGroupId; 
	
	/**
	 * @param text
	 */
	public OrganizeFavoritesAction(String launchGroupId) {
		super(LaunchConfigurationsMessages.OrganizeFavoritesAction_0); 
		fGroupId = launchGroupId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(fGroupId);
		FavoritesDialog dialog = new FavoritesDialog(DebugUIPlugin.getShell(), history);
		dialog.open();
	}

}
