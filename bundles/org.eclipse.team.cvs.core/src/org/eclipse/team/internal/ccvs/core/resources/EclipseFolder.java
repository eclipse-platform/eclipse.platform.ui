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
package org.eclipse.team.internal.ccvs.core.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Implements the ICVSFolder interface on top of an 
 * instance of the ICVSFolder interface
 * 
 * @see ICVSFolder
 */
class EclipseFolder extends EclipseResource implements ICVSFolder {

	protected EclipseFolder(IContainer container) {
		super(container);		
	}

	/**
	 * 
	 * @see ICVSFolder#getFolders()
	 */
	public ICVSFolder[] getFolders() throws CVSException {
		IContainer folder = (IContainer)resource;			
		final List folders = new ArrayList();
		
		IResource[] resources = EclipseSynchronizer.getInstance().members(folder);
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(resource.getType()!=IResource.FILE) {
				ICVSResource cvsResource = new EclipseFolder((IContainer)resource);
				if(!cvsResource.isIgnored()) {
					folders.add(cvsResource);
				}
			}			
		}	
		return (ICVSFolder[]) folders.toArray(new ICVSFolder[folders.size()]);
	}
	
	/**
	 * @see ICVSFolder#getFiles()
	 */
	public ICVSFile[] getFiles() throws CVSException {
		IContainer folder = (IContainer)resource;			
		final List files = new ArrayList();
		
		IResource[] resources = EclipseSynchronizer.getInstance().members(folder);
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(resource.getType()==IResource.FILE) {
				ICVSResource cvsResource = new EclipseFile((IFile)resource);
				if(!cvsResource.isIgnored()) {
					files.add(cvsResource);
				}
			}			
		}	
		return (ICVSFile[]) files.toArray(new ICVSFile[files.size()]);
	}

	/**
	 * @see ICVSFolder#createFolder(String)
	 */
	public ICVSFolder getFolder(String name) throws CVSException {
		if ((CURRENT_LOCAL_FOLDER.equals(name)) || ((CURRENT_LOCAL_FOLDER + SEPARATOR).equals(name)))
			return this;
		IPath path = new Path(name);
		if(resource.getType()==IResource.ROOT && path.segmentCount()==1) {
			return new EclipseFolder(((IWorkspaceRoot)resource).getProject(name));
		} else {
			return new EclipseFolder(((IContainer)resource).getFolder(new Path(name)));
		}
	}

	/**
	 * @see ICVSFolder#createFile(String)
	 */
	public ICVSFile getFile(String name) throws CVSException {
		return new EclipseFile(((IContainer)resource).getFile(new Path(name)));
	}

	/**
	 * @see ICVSFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		try {
			if(resource.getType()==IResource.PROJECT) {
				IProject project = (IProject)resource;
				project.create(null);
				project.open(null);				
			} else {
				((IFolder)resource).create(false /*don't force*/, true /*make local*/, null);
			}				
		} catch (CoreException e) {
			throw CVSException.wrapException(resource, Policy.bind("EclipseFolder_problem_creating", resource.getFullPath().toString(), e.getStatus().getMessage()), e); //$NON-NLS-1$
		} 
	}
		
	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
		
	/**
	 * @see ICVSFolder#acceptChildren(ICVSResourceVisitor)
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
		
		ICVSResource[] subFiles;
		ICVSResource[] subFolders;
		
		subFiles = getFiles();
		subFolders = getFolders();
		
		for (int i=0; i<subFiles.length; i++) {
			subFiles[i].accept(visitor);
		}
		
		for (int i=0; i<subFolders.length; i++) {
			subFolders[i].accept(visitor);
		}
	}

	/**
	 * @see ICVSResource#accept(ICVSResourceVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFolder(this);
	}

	/**
	 * @see ICVSResource#getRemoteLocation(ICVSFolder)
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
				
		if (getFolderSyncInfo() != null) {
			return getFolderSyncInfo().getRemoteLocation();
		}			

		ICVSFolder parent = getParent();
		if(parent!=null && !equals(stopSearching)) {
			String parentLocation;
			parentLocation = parent.getRemoteLocation(stopSearching);
			if (parentLocation!=null) {
				return parentLocation + SEPARATOR + getName();
			}		
		}
		return null;
	}

	/*
	 * @see ICVSFolder#getFolderInfo()
	 */
	public FolderSyncInfo getFolderSyncInfo() throws CVSException {
		return EclipseSynchronizer.getInstance().getFolderSync((IContainer)resource);
	}

	/*
	 * @see ICVSFolder#setFolderInfo(FolderSyncInfo)
	 */
	public void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException {
		EclipseSynchronizer.getInstance().setFolderSync((IContainer)resource, folderInfo);
		// the server won't add directories as sync info, therefore it must be done when
		// a directory is shared with the repository.
		setSyncInfo(new ResourceSyncInfo(getName()));
	}

	/*
	 * @see ICVSFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() {
		try {
			return EclipseSynchronizer.getInstance().getFolderSync((IContainer)resource) != null;
		} catch(CVSException e) {
			return false;
		}
	}

	/*
	 * @see ICVSResource#unmanage()
	 */
	public void unmanage(IProgressMonitor monitor) throws CVSException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", 100); //$NON-NLS-1$
			run(new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					recursiveUnmanage((IContainer) resource, monitor);				
				}
			}, Policy.subMonitorFor(monitor, 99));
			// unmanaged from parent
			super.unmanage(Policy.subMonitorFor(monitor, 1));
		} finally {
			monitor.done();
		}
	}
	
	private static void recursiveUnmanage(IContainer container, IProgressMonitor monitor) throws CVSException {
		try {
			monitor.beginTask("", 10); //$NON-NLS-1$
			monitor.subTask(container.getFullPath().toOSString());
			EclipseSynchronizer.getInstance().deleteFolderSync(container);						
			IResource[] members = container.members();
			for (int i = 0; i < members.length; i++) {
				monitor.worked(1);
				IResource resource = members[i];
				if (members[i].getType() != IResource.FILE) {
					recursiveUnmanage((IContainer) resource, monitor);
				}
			}
		} catch (CoreException e) {
		}
	}
	
	/*
	 * @see ICVSResource#isIgnored()
	 */
	public boolean isIgnored() {
		if(isCVSFolder()) {
			return false;
		}		
		return super.isIgnored();
	}
	
	/*
	 * @see ICVSFolder#getChild(String)
	 */
	public ICVSResource getChild(String namedPath) throws CVSException {
		IPath path = new Path(namedPath);
		if(path.segmentCount()==0) {
			 return this;
		}
		IResource child = ((IContainer)resource).findMember(path);
		if(child!=null) {
			if(child.getType()==IResource.FILE) {
				return new EclipseFile((IFile)child);
			} else {
				return new EclipseFolder((IContainer)child);
			}
		}
		return null;
	}
	
	/*
	 * @see ICVSFolder#run(ICVSRunnable, IProgressMonitor)
	 */
	public void run(final ICVSRunnable job, IProgressMonitor monitor) throws CVSException {
		final CVSException[] error = new CVSException[1];
		// Remove the registered Move/Delete hook, assuming that the cvs runnable will keep sync info up-to-date
		final IMoveDeleteHook oldHook = CVSTeamProvider.getRegisteredMoveDeleteHook();
		CVSTeamProvider.setMoveDeleteHook(null);
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					monitor = Policy.monitorFor(monitor);
					try {
						monitor.beginTask(null, 100);
						try {
							EclipseSynchronizer.getInstance().beginOperation(Policy.subMonitorFor(monitor, 5));
							job.run(Policy.subMonitorFor(monitor, 60));
						} finally {
							EclipseSynchronizer.getInstance().endOperation(Policy.subMonitorFor(monitor, 35));
						}
					} catch(CVSException e) {
						error[0] = e; 
					} finally {						
						monitor.done();
					}
				}
			}, monitor);
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			CVSTeamProvider.setMoveDeleteHook(oldHook);
		}
		if(error[0]!=null) {
			throw error[0];
		}
	}
	/**
	 * @see ICVSFolder#fetchChildren(IProgressMonitor)
	 */
	public ICVSResource[] fetchChildren(IProgressMonitor monitor) throws CVSException {
		List children = new ArrayList();
		children.addAll(Arrays.asList(getFolders()));
		children.addAll(Arrays.asList(getFiles()));
		return (ICVSResource[]) children.toArray(new ICVSResource[children.size()]);
	}
}