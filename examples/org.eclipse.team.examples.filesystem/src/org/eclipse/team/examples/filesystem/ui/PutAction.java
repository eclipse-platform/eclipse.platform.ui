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
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.filesystem.FileSystemProvider;
import org.eclipse.team.examples.filesystem.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for checking in the selected resources
 */
public class PutAction extends FileSystemAction {

	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation(null) {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					Map table = getRepositoryProviderMapping();
					monitor.beginTask(null, table.size() * 1000);
					monitor.setTaskName(Policy.bind("PutAction.working")); //$NON-NLS-1$
					for (Iterator iter = table.keySet().iterator(); iter.hasNext();) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						FileSystemProvider provider = (FileSystemProvider) iter.next();
						List list = (List) table.get(provider);
						IResource[] providerResources = (IResource[]) list.toArray(new IResource[list.size()]);
						provider.getOperations().checkin(providerResources, IResource.DEPTH_INFINITE, isOverrideIncoming(), subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("PutAction.problemMessage"), PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	/**
	 * Indicate whether the put should override incoming changes.
	 * @return whether the put should override incoming changes.
	 */
	protected boolean isOverrideIncoming() {
		return false;
	}
}
