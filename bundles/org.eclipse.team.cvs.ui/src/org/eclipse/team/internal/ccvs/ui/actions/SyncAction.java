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
package org.eclipse.team.internal.ccvs.ui.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.sync.ISyncViewer;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends WorkspaceAction {
	
	public void execute(IAction action) throws InvocationTargetException {
		IResource[] resources = getResourcesToSync();
		if (resources == null || resources.length == 0) return;
		
		ISyncViewer view = TeamUI.showSyncViewInActivePage(null);
		if(view != null) {
			CVSWorkspaceSubscriber cvsWorkspaceSubscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
			view.setSelection(cvsWorkspaceSubscriber, resources, view.getCurrentViewType());
			view.refreshWithRemote(cvsWorkspaceSubscriber, resources);
		}
	}
	
	protected IResource[] getResourcesToSync() {
		return getSelectedResources();
	}
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a
	 * CVS folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return super.isEnabledForCVSResource(cvsResource) || cvsResource.getParent().isCVSFolder();
	}

}
