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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.IgnoreResourcesDialog;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class IgnoreAction extends WorkspaceAction {
	
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.PROJECT) return false;
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			if (cvsResource.isManaged()) return false;
			if (cvsResource.isIgnored()) return false;
		}
		return super.isEnabled();
	}
	
	protected void execute(final IAction action) throws InvocationTargetException, InterruptedException {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IResource[] resources = getSelectedResources();
				IgnoreResourcesDialog dialog = new IgnoreResourcesDialog(getShell(), resources);
				if (dialog.open() != IgnoreResourcesDialog.OK) return;
				
				try {
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						String pattern = dialog.getIgnorePatternFor(resource);
						ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
						cvsResource.setIgnoredAs(pattern);
					}
					// fix the action enablement
					if (action != null) action.setEnabled(isEnabled());
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("IgnoreAction.ignore"); //$NON-NLS-1$
	}

}
