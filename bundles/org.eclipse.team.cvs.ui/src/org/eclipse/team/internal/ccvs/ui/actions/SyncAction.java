/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncView;

/**
 * Action for catchup/release in popup menus.
 */
public class SyncAction extends WorkspaceAction {
	
	public void execute(IAction action) throws InvocationTargetException {
		try {
			IResource[] resources = getResourcesToSync();
			if (resources == null || resources.length == 0) return;
			SyncCompareInput input = getCompareInput(resources);
			if (input == null) return;
			SyncView view = SyncView.findViewInActivePage(getTargetPage());
			if (view != null) {
				view.showSync(input, getTargetPage());
			}
		} catch (CVSException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	protected SyncCompareInput getCompareInput(IResource[] resources) throws CVSException {
		return new CVSSyncCompareInput(resources);
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
