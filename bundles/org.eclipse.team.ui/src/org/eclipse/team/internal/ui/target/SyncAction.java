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
package org.eclipse.team.internal.ui.target;

 
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.core.target.TargetProvider;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncView;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends TeamAction {
	
	public void run(IAction action) {
		IResource[] resources = getSelectedResources();
		SyncView view = SyncView.findViewInActivePage(getTargetPage());
		if (view != null) {
			view.showSync(getCompareInput(resources), getTargetPage());
		}
	}
	
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!resource.isAccessible()) return false;
			TargetProvider provider = TargetManager.getProvider(resource.getProject());
			if(provider == null) return false;
		}
		return true;
	}

	protected SyncCompareInput getCompareInput(IResource[] resources) {
		return new TargetSyncCompareInput(resources, IRemoteSyncElement.GRANULARITY_TIMESTAMP);
	}
}
