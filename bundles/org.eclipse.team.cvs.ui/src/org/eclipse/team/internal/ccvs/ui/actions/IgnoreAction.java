/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.IgnoreResourcesDialog;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class IgnoreAction extends WorkspaceAction {
	
	protected void execute(final IAction action) throws InvocationTargetException, InterruptedException {
		run(new WorkspaceModifyOperation(null) {
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

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForManagedResources()
	 */
	protected boolean isEnabledForManagedResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
	 */
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		if (super.isEnabledForCVSResource(cvsResource)) {
			// Perform an extra check against the subscriberto ensue there is no conflict
			CVSWorkspaceSubscriber subscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
			IResource resource = cvsResource.getIResource();
			if (resource == null) return false;
			try {
				SyncInfo info = subscriber.getSyncInfo(resource);
				return ((info.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.OUTGOING);
			} catch (TeamException e) {
				// Let the enablement happen
				return true;
			}
		}
		return false;
	}
	
}
