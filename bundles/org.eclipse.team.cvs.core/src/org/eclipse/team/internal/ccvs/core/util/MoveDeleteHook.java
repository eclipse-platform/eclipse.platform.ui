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
package org.eclipse.team.internal.ccvs.core.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This hook exists to ensure that folders deletions will be recorded so that outgoing file
 * deletions can be properly communicated to the server.
 */
public class MoveDeleteHook implements IMoveDeleteHook {

	/*
	 * Delete the file and return true if an outgoing deletion will result
	 * and the parent folder sync info needs to remain 
	 */
	private boolean makeFileOutgoingDeletion(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// Delete or move the file
		if (destination == null) {
			tree.standardDeleteFile(source, updateFlags, monitor);
		} else {
			tree.standardMoveFile(source, destination, updateFlags, monitor);
		}
		// Indicate whether the parent folder sync info must remain for outgoing deletions
		// and update the file sync info to be a deletion
		ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(source);
		boolean mustRemain = false;
		try {
			ResourceSyncInfo info = cvsFile.getSyncInfo();
			mustRemain = (info != null && ! info.isAdded());
			if (mustRemain) {
				MutableResourceSyncInfo newInfo = info.cloneMutable();
				newInfo.setDeleted(true);
				cvsFile.setSyncInfo(newInfo);
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
		return mustRemain;
	}
	
	/*
	 * Delete the files contained in the folder structure rooted at source.
	 * Return true if at least one file has been marked as an outgoing deletion and the parent folder must remain
	 */
	private boolean makeFolderOutgoingDeletion(IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		boolean fileFound = false;

		// Create the destination for a move
		if (destination != null && ! destination.exists()) {
			destination.create(false, true, monitor);
		}
		
		// Move or delete the children
		IResource[] members = source.members();
		for (int i = 0; i < members.length; i++) {
			IResource child = members[i];
			if (child.getType() == IResource.FOLDER) {
				// Determine the corresponding destination folder
				IFolder destFolder = null;
				if (destination != null) {
					destFolder = destination.getFolder(child.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
				}
				
				// Try to delete/move the child
				if (makeFolderOutgoingDeletion(tree, (IFolder)child, destFolder, updateFlags, monitor)) {
					fileFound = true;
				}
			} else if (child.getType() == IResource.FILE) {
				IFile destFile = null;
				if (destination != null) {
					destFile = destination.getFile(child.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
				}
				fileFound = makeFileOutgoingDeletion(tree, (IFile)child, destFile, updateFlags, monitor);
			}
		}

		// If there were no files, we need to check if the folder already has outgoing deletions
		if ( ! fileFound) {
			try {
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(source);
				ICVSResource[] files = folder.members(ICVSFolder.FILE_MEMBERS | ICVSFolder.MANAGED_MEMBERS);
				for (int i = 0; i < files.length; i++) {
					ICVSFile cvsFile = (ICVSFile)files[i];
					if (cvsFile.isManaged() && ! cvsFile.getSyncInfo().isAdded()) {
						fileFound = true;
						break;
					}		
				}
				// If there is still no file, we can delete the folder without any special handling
				if ( ! fileFound) {
					tree.standardDeleteFolder(source, updateFlags, monitor);
					folder.unmanage(null);
				}
			} catch (CVSException e) {
				tree.failed(e.getStatus());
			}
		}
		
		// If there were managed files found, we must remember the folder's sync info
		if (fileFound) {
			try {
				EclipseSynchronizer.getInstance().prepareForDeletion(source);
			} catch (CVSException e) {
				tree.failed(e.getStatus());
			}
			tree.standardDeleteFolder(source, updateFlags, monitor);
		}
				
		return fileFound;
	}
	
	/**
	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
	 */
	public boolean deleteFile(
		IResourceTree tree,
		IFile file,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// No special action is required here. 
		// The AddDeleteMoveListener will update the sync info of the file
		return false;
	}

	/**
	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
	 */
	public boolean deleteFolder(
		final IResourceTree tree,
		final IFolder folder,
		final int updateFlags,
		IProgressMonitor monitor) {
		
		final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
		try {
			if (cvsFolder.isManaged()) {
				cvsFolder.run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							makeFolderOutgoingDeletion(tree, folder, null, updateFlags, monitor);
						} catch (CoreException e) {
							tree.failed(e.getStatus());
						}
					}
				}, monitor);
				return true;
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
		return false;
	}

	/**
	 * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
	 */
	public boolean deleteProject(
		IResourceTree tree,
		IProject project,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// We need to flush any remembered folder deletions for the deleted project
		try {
			EclipseSynchronizer.getInstance().prepareForDeletion(project);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
		return false;
	}

	/**
	 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
	 */
	public boolean moveFile(
		IResourceTree tree,
		IFile source,
		IFile destination,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// No special action is required here. 
		// The AddDeleteMoveListener will update the sync info of the source
		return false;
	}

	/**
	 * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
	 */
	public boolean moveFolder(
		final IResourceTree tree,
		final IFolder source,
		final IFolder destination,
		final int updateFlags,
		IProgressMonitor monitor) {
		
		final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(source);
		try {
			if (cvsFolder.isManaged()) {
				cvsFolder.run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							makeFolderOutgoingDeletion(tree, source, destination, updateFlags, monitor);
						} catch (CoreException e) {
							tree.failed(e.getStatus());
						}
					}
				}, monitor);
				return true;
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
			
		return false;
	}

	/**
	 * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
	 */
	public boolean moveProject(
		IResourceTree tree,
		IProject source,
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// We need to move (or flush) any remembered folder deletions for the deleted project
		// XXX We flush for now. This means that deleting a managed folder and then moving the
		// project will mean that the file deletions will be lost.
		try {
			EclipseSynchronizer.getInstance().prepareForDeletion(source);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
		return false;
	}

}
