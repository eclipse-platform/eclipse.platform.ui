/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.examples.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.examples.filesystem.FileSystemProvider;
import org.eclipse.team.examples.filesystem.FileSystemRemoteResource;
import org.eclipse.team.examples.filesystem.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for replacing the contents of the selected resources with whatever is in the repository
 */
public class ReplaceAction extends FileSystemAction {
	
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					Map table = getRepositoryProviderMapping();
					monitor.beginTask(null, table.size() * 1000);
					monitor.setTaskName(Policy.bind("ReplaceAction.working")); //$NON-NLS-1$
					for (Iterator iter = table.keySet().iterator(); iter.hasNext();) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						FileSystemProvider provider = (FileSystemProvider) iter.next();
						List list = (List) table.get(provider);
						IResource[] providerResources = (IResource[]) list.toArray(new IResource[list.size()]);
						//Grab the remote counterparts of 'providerResources':
						FileSystemRemoteResource[] remote = new FileSystemRemoteResource[list.size()];
						for (int i = 0; i < remote.length; i++) {
							remote[i] = new FileSystemRemoteResource(provider.getRoot().append(providerResources[i].getProjectRelativePath()));
						}
						//copy the entire tree structure:
						IPath dropSpot = null;
						for (int i = 0; i < providerResources.length; i++) {
							if (providerResources[i].getType() == IResource.FILE) {
								IFile localFile = (IFile) providerResources[i];
								dropSpot = localFile.getLocation().removeLastSegments(1);
							} else if (providerResources[i].getType() == IResource.FOLDER||providerResources[i].getType() == IResource.PROJECT) {
								IContainer localDir = (IContainer) providerResources[i];
								dropSpot = localDir.getLocation().removeLastSegments(1);
							} 
							if (remote[i].isContainer())
								remote[i].copyOver(dropSpot);
							else 
								provider.getSimpleAccess().get(new IResource[] { providerResources[i] }, IResource.DEPTH_ONE, subMonitor);
						}
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("ReplaceAction.problemMessage"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
}