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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * AddAction performs a 'cvs add' command on the selected resources. If a
 * container is selected, its children are recursively added.
 */
public class AddAction extends WorkspaceAction {
	
	/*
	 * @see CVSAction#execute()
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		if (!promptForAddOfIgnored()) return;
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {					
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("AddAction.adding")); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.add(providerResources, IResource.DEPTH_INFINITE, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);

	}
	
	/**
	 * Method promptForAddOfIgnored.
	 */
	private boolean promptForAddOfIgnored() {
		IResource[] resources = getSelectedResources();
		boolean prompt = false;
		for (int i = 0; i < resources.length; i++) {
			ICVSResource resource = CVSWorkspaceRoot.getCVSResourceFor(resources[i]);
			try {
				if (resource.isIgnored()) {
					prompt = true;
					break;
				} 
			} catch (CVSException e) {
				handle(e);
			}
		}
		if (prompt) {
			return MessageDialog.openQuestion(getShell(), Policy.bind("AddAction.addIgnoredTitle"), Policy.bind("AddAction.addIgnoredQuestion")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return true;
	}

	/*
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("AddAction.addFailed"); //$NON-NLS-1$
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

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForIgnoredResources()
	 */
	protected boolean isEnabledForIgnoredResources() {
		return true;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		// Add to version control should never be enabled for linked resources
		IResource resource = cvsResource.getIResource();
		if (resource.isLinked()) return false;
		return super.isEnabledForCVSResource(cvsResource);
	}

}
