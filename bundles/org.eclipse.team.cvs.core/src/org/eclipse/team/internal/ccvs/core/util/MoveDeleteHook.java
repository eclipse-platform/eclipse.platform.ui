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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFileModificationValidator;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This hook exists to ensure that folders deletions will be recorded so that outgoing file
 * deletions can be properly communicated to the server.
 */
public class MoveDeleteHook implements IMoveDeleteHook {

	private boolean withinCVSOperation = false;
	
	/*
	 * Delete the file and return true if an outgoing deletion will result
	 * and the parent folder sync info needs to remain 
	 */
	private void makeFileOutgoingDeletion(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// Delete or move the file
		if (destination == null) {
			tree.standardDeleteFile(source, updateFlags, monitor);
		} else {
			tree.standardMoveFile(source, destination, updateFlags, monitor);
		}
		// Indicate whether the parent folder sync info must remain for outgoing deletions
		// and update the file sync info to be a deletion
		ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(source);
		try {
			ResourceSyncInfo info = cvsFile.getSyncInfo();
			if (!withinCVSOperation && info != null && ! info.isAdded()) {
				MutableResourceSyncInfo newInfo = info.cloneMutable();
				newInfo.setDeleted(true);
				cvsFile.setSyncInfo(newInfo);
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
	}
	
	/*
	 * Delete the files contained in the folder structure rooted at source.
	 * Return true if at least one file has been marked as an outgoing deletion and the parent folder must remain
	 */
	private void makeFolderOutgoingDeletion(IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

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
				makeFolderOutgoingDeletion(tree, (IFolder)child, destFolder, updateFlags, monitor);
			} else if (child.getType() == IResource.FILE) {
				IFile destFile = null;
				if (destination != null) {
					destFile = destination.getFile(child.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
				}
				makeFileOutgoingDeletion(tree, (IFile)child, destFile, updateFlags, monitor);
			}
		}
		
		// Remember the folder's sync info so future operations will know about it
		try {
			EclipseSynchronizer.getInstance().prepareForDeletion(source);
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
		
		// Move the CVS folder if appropriate (delta listeners will handle removal if required)
		if (destination != null) {
			IFolder cvsFolder = source.getFolder(SyncFileWriter.CVS_DIRNAME);
			if (cvsFolder.exists()) {
				tree.standardMoveFolder(cvsFolder, destination.getFolder(SyncFileWriter.CVS_DIRNAME), updateFlags, monitor);
			}
		}
		
		// delete the orginal folder
		tree.standardDeleteFolder(source, updateFlags, monitor);
				
		return;
	}
	
	/**
	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
	 */
	public boolean deleteFile(
		IResourceTree tree,
		IFile file,
		int updateFlags,
		IProgressMonitor monitor) {
		
		// Return the opposite of the checkout request.
		// If the file is deleted, the AddDeleteMoveListener will update the sync info of the file
		return !checkOutFiles(tree, new IFile[] {file}, monitor);
	}
	
	/**
	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
	 */
	public boolean deleteFolder(
		final IResourceTree tree,
		final IFolder folder,
		final int updateFlags,
		IProgressMonitor monitor) {
		
		monitor.beginTask(null, 100);
		try {
			if (!ensureCheckedOut(folder, tree, Policy.subMonitorFor(monitor, 30))) return true;
			final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
			if (cvsFolder.isManaged()) {
				EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							makeFolderOutgoingDeletion(tree, folder, null, updateFlags, monitor);
						} catch (CoreException e) {
							tree.failed(e.getStatus());
						}
					}
				}, Policy.subMonitorFor(monitor, 70));
				return true;
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		} finally {
			monitor.done();
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
		// todo: Perform a "cvs release" if there are any edits on the project
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
		
		// See comment in deleteFile
		if (destination.exists())
			// Ensure that we can write to the destination
		 	return !checkOutFiles(tree, new IFile[] {source, destination}, monitor);
		else
			return !checkOutFiles(tree, new IFile[] {source}, monitor);
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
		
		monitor.beginTask(null, 100);
		try {
			if (!ensureCheckedOut(source, tree, Policy.subMonitorFor(monitor, 30))) return true;
			if (!ensureCheckedOut(destination, tree, Policy.subMonitorFor(monitor, 10))) return true;
			final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(source);
			if (cvsFolder.isManaged()) {
				EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							makeFolderOutgoingDeletion(tree, source, destination, updateFlags, monitor);
						} catch (CoreException e) {
							tree.failed(e.getStatus());
						}
					}
				}, Policy.subMonitorFor(monitor, 60));
				return true;
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		} finally {
			monitor.done();
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
		// project will mean that the file deletions will be lost. It also means that phantom
		// folders are lost.
		try {
			EclipseSynchronizer.getInstance().prepareForDeletion(source);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
		return false;
	}

	/**
	 * Returns the recordOutgoingDeletions.
	 * @return boolean
	 */
	public boolean isWithinCVSOperation() {
		return withinCVSOperation;
	}

	/**
	 * Sets the recordOutgoingDeletions.
	 * @param recordOutgoingDeletions The recordOutgoingDeletions to set
	 */
	public void setWithinCVSOperation(boolean withinCVSOperation) {
		this.withinCVSOperation = withinCVSOperation;
	}
	
	/**
	 * Ensure that the given file is checked out (i.e. not read only). Return
	 * true if it is OK to procede and false otherwise.
	 * 
	 * @param tree
	 * @param file
	 * @return boolean
	 */
	private boolean checkOutFiles(IResourceTree tree, IFile[] files, IProgressMonitor monitor) {
		// Ensure that the file is "checked out" (i.e. not read-only
		IFileModificationValidator validator = getFileModificationValidator(files);
		if (validator instanceof ICVSFileModificationValidator) {
			IStatus status = ((ICVSFileModificationValidator)validator).validateMoveDelete(files, monitor);
			if (status.isOK()) {
				return true;
			} else {
				tree.failed(status);
				return false;
			}
		}
		return true;
	}

	private boolean ensureCheckedOut(IFolder folder, IResourceTree tree, IProgressMonitor monitor) {
		if (!folder.exists()) return true;
		try {
			final List readOnlyFiles = new ArrayList();
			// First, find any read-only files
			folder.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FILE) {
						IFile file = (IFile) resource;
						if (file.isReadOnly()) {
							readOnlyFiles.add(file);
						}
					}
					return true;
				}
			});
			if (readOnlyFiles.isEmpty()) return true;
			// Next, ensure they are checked out
			return checkOutFiles(tree, (IFile[]) readOnlyFiles.toArray(new IFile[readOnlyFiles.size()]), monitor);
		} catch (CoreException e) {
			tree.failed(e.getStatus());
			return false;
		}
	}

	private IFileModificationValidator getFileModificationValidator(IFile[] files) {
		return getProvider(files).getFileModificationValidator();
	}

	private CVSTeamProvider getProvider(IFile[] files) {
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(files[0].getProject(), CVSProviderPlugin.getTypeId());
		return provider;
	}
}
