/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This class represents an action performed on a local CVS workspace
 */
public abstract class WorkspaceAction extends CVSAction {

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#beginExecution(IAction)
	 */
	protected boolean beginExecution(IAction action) throws TeamException {
		if (super.beginExecution(action)) {
			// Ensure that the required sync info is loaded
			if (requiresLocalSyncInfo()) {
				if (!ensureSyncInfoLoaded(getSelectedResources())) {
					return false;
				}
				if (!isEnabled()) {
					MessageDialog.openInformation(getShell(), Policy.bind("CVSAction.disabledTitle"), Policy.bind("CVSAction.disabledMessage")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return true if the sync info is loaded for all selected resources.
	 * The purpose of this method is to allow enablement code to be as fast
	 * as possible. If the sync info is not loaded, the menu should be enabled
	 * and, if choosen, the action will verify that it is indeed enabled before
	 * performing the associated operation
	 */
	protected boolean isSyncInfoLoaded(IResource[] resources) throws CVSException {
		return EclipseSynchronizer.getInstance().isSyncInfoLoaded(resources, getEnablementDepth());
	}

	/**
	 * Returns the resource depth of the action for use in determining if the required
	 * sync info is loaded. The default is IResource.DEPTH_INFINITE. Sunclasses can override
	 * as required.
	 */
	protected int getActionDepth() {
		return IResource.DEPTH_INFINITE;
	}

	/**
	 * Returns the resource depth of the action enablement for use in determining if the required
	 * sync info is loaded. The default is IResource.DEPTH_ZERO. Sunclasses can override
	 * as required.
	 */
	protected int getEnablementDepth() {
		return IResource.DEPTH_ZERO;
	}
	
	/**
	 * Ensure that the sync info for all the provided resources has been loaded.
	 * If an out-of-sync resource is found, prompt to refresh all the projects involved.
	 */
	protected boolean ensureSyncInfoLoaded(IResource[] resources) throws CVSException {
		boolean keepTrying = true;
		while (keepTrying) {
			try {
				EclipseSynchronizer.getInstance().ensureSyncInfoLoaded(resources, getActionDepth());
				keepTrying = false;
			} catch (CVSException e) {
				if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
					// determine the projects of the resources involved
					Set projects = new HashSet();
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						projects.add(resource.getProject());
					}
					// prompt to refresh
					if (promptToRefresh(getShell(), (IResource[]) projects.toArray(new IResource[projects.size()]), e.getStatus())) {
						for (Iterator iter = projects.iterator();iter.hasNext();) {
							IProject project = (IProject) iter.next();
							try {
								project.refreshLocal(IResource.DEPTH_INFINITE, null);
							} catch (CoreException coreException) {
								throw CVSException.wrapException(coreException);
							}
						}
					} else {
						return false;
					}
				} else {
					throw e;
				}
			}
		}
		return true;
	}
	
	/**
	 * Override to ensure that the sync info is available before performing the
	 * real <code>isEnabled()</code> test.
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#setActionEnablement(IAction)
	 */
	protected void setActionEnablement(IAction action) {
		try {
			boolean requires = requiresLocalSyncInfo();
			if (!requires || (requires && isSyncInfoLoaded(getSelectedResources()))) {
				super.setActionEnablement(action);
			} else {
				// If the sync info is not loaded, enable the menu item
				// Performing the action will ensure that the action should really
				// be enabled before anything else is done
				action.setEnabled(true);
			}
		} catch (CVSException e) {
			// We couldn't determine if the sync info was loaded.
			// Enable the action so that performing the action will
			// reveal the error to the user.
			action.setEnabled(true);
		}
	}

	/**
	 * Return true if the action requires the sync info for the selected resources.
	 * If the sync info is required, the real enablement code will only be run if
	 * the sync info is loaded from disc. Otherwise, the action is enabled and
	 * performing the action will load the sync info and verify that the action is truely
	 * enabled before doing anything else.
	 * 
	 * This implementation returns <code>true</code>. Subclasses must override if they do
	 * not require the sync info of the selected resources.
	 * 
	 * @return boolean
	 */
	protected boolean requiresLocalSyncInfo() {
		return true;
	}

	protected boolean promptToRefresh(final Shell shell, final IResource[] resources, final IStatus status) {
		final boolean[] result = new boolean[] { false};
		Runnable runnable = new Runnable() {
			public void run() {
				Shell shellToUse = shell;
				if (shell == null) {
					shellToUse = new Shell(Display.getCurrent());
				}
				String question;
				if (resources.length == 1) {
					question = Policy.bind("CVSAction.refreshQuestion", status.getMessage(), resources[0].getFullPath().toString()); //$NON-NLS-1$
				} else {
					question = Policy.bind("CVSAction.refreshMultipleQuestion", status.getMessage()); //$NON-NLS-1$
				}
				result[0] = MessageDialog.openQuestion(shellToUse, Policy.bind("CVSAction.refreshTitle"), question); //$NON-NLS-1$
			}
		};
		Display.getDefault().syncExec(runnable);
		return result[0];
	}

	/**
	 * Most CVS workspace actions modify the workspace and thus should
	 * save dirty editors.
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#needsToSaveDirtyEditors()
	 */
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}

}
