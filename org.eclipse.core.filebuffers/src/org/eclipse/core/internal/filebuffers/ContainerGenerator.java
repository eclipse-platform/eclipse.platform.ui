/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

public class ContainerGenerator {
	
	private IPath fContainerFullPath;
	private IContainer fContainer;
	private IWorkspace fWorkspace;

	public ContainerGenerator(IWorkspace workspace, IPath containerPath) {
		fWorkspace= workspace;
		fContainerFullPath = containerPath;
	}

	private IFolder createFolder(IFolder folderHandle, IProgressMonitor monitor) throws CoreException {
		folderHandle.create(false, true, monitor);	
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		return folderHandle;
	}
	
	private IFolder createFolderHandle(IContainer container, String folderName) {
		return container.getFolder(new Path(folderName));
	}
	
	private IProject createProject(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("",2000);//$NON-NLS-1$
		
			projectHandle.create(new SubProgressMonitor(monitor, 1000));
			if (monitor.isCanceled())
				throw new OperationCanceledException();
	
			projectHandle.open(new SubProgressMonitor(monitor, 1000));
			if (monitor.isCanceled())
				throw new OperationCanceledException();
				
		} finally {
			monitor.done();
		}
	
		return projectHandle;
	}

	private IProject createProjectHandle(IWorkspaceRoot root, String projectName) {
		return root.getProject(projectName);
	}

	public IContainer generateContainer(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("creating container", 1000 * fContainerFullPath.segmentCount());
				if (fContainer != null)
					return;
		
				// Does the container exist already?
				IWorkspaceRoot root= fWorkspace.getRoot();
				fContainer= (IContainer) root.findMember(fContainerFullPath);
				if (fContainer != null)
					return;
		
				// Create the container for the given path
				fContainer= root;
				for (int i = 0; i < fContainerFullPath.segmentCount(); i++) {
					String currentSegment = fContainerFullPath.segment(i);
					IResource resource = fContainer.findMember(currentSegment);
					if (resource != null) {
						fContainer= (IContainer) resource;
						monitor.worked(1000);
					}
					else {
						if (i == 0) {
							IProject projectHandle= createProjectHandle(root, currentSegment);
							fContainer= createProject(projectHandle, new SubProgressMonitor(monitor,1000));
						}
						else {
							IFolder folderHandle= createFolderHandle(fContainer, currentSegment);
							fContainer= createFolder(folderHandle, new SubProgressMonitor(monitor,1000));
						}
					}
				}
			}
		};
		
		fWorkspace.run(runnable, monitor);
		return fContainer;
	}
}
