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
package org.eclipse.team.internal.ccvs.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;

/**
 * This hook exists to ensure that folders deletions will be recorded so that outgoing file
 * deletions can be properly communicated to the server.
 */
public class MoveDeleteHook implements IMoveDeleteHook {
	
	/**
	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
	 */
	public boolean deleteFile(
		final IResourceTree tree,
		final IFile file,
		final int updateFlags,
		IProgressMonitor monitor) {
		
		try {
			// No special handling required for team-private members
			if (file.isTeamPrivateMember()) return false;
			// If the file is ignored by CVS then we can just delete it.
			ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
			if (cvsFile.isIgnored()) return false;
			// Otherwise, we need to prepare properly for the delete
			EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					try {
						monitor.beginTask(null, 100);
						if (checkOutFiles(tree, new IFile[] {file}, Policy.subMonitorFor(monitor, 35))) {
							EclipseSynchronizer.getInstance().prepareForMoveDelete(file, Policy.subMonitorFor(monitor, 30));
							tree.standardDeleteFile(file, updateFlags, Policy.subMonitorFor(monitor, 35));
						}
					} finally {
						monitor.done();
					}
				}
			}, monitor);
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		} 
		return true;
	}
	
	/**
	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
	 */
	public boolean deleteFolder(
		final IResourceTree tree,
		final IFolder folder,
		final int updateFlags,
		IProgressMonitor monitor) {
		
		// No special handling required for team-private members
		if (folder.isTeamPrivateMember()) return false;
		monitor.beginTask(null, 100);
		try {
			final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
			if (cvsFolder.isManaged() && ensureCheckedOut(new IFolder[] {folder}, tree, Policy.subMonitorFor(monitor, 30))) {
				EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							monitor.beginTask(null, 100);
							EclipseSynchronizer.getInstance().prepareForMoveDelete(folder, Policy.subMonitorFor(monitor, 50));
							tree.standardDeleteFolder(folder, updateFlags, Policy.subMonitorFor(monitor, 50));
						} finally {
							monitor.done();
						}
					}
				}, Policy.subMonitorFor(monitor, 70));
				return true;
			} else if (!cvsFolder.isIgnored()) {
				prepareToDelete(cvsFolder);
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
			
		// We need to flush any remembered folder deletions for the deleted project.
		// All other sync info is stored in session and persistant properties, which
		// are deleted when the associated resources are deleted
		try {
			EclipseSynchronizer.getInstance().prepareForDeletion(project);
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
		// todo: Perform a "cvs release" if there are any edits on the project
		return false;
	}

	/**
	 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
	 */
	public boolean moveFile(
			final IResourceTree tree,
			final IFile source,
			final IFile destination,
			final int updateFlags,
			IProgressMonitor monitor) {
		
		try {
			EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
				public void run(IProgressMonitor monitor) throws CVSException {
					try {
						monitor.beginTask(null, 100);
						// ensure we can write to both the source and the destination
						IFile[] filesToCheckOut;
						if (destination.exists()) {
							filesToCheckOut = new IFile[] {source, destination};
						} else {
							filesToCheckOut = new IFile[] {source};
						}
						if (checkOutFiles(tree, filesToCheckOut, Policy.subMonitorFor(monitor, 30))) {
							EclipseSynchronizer.getInstance().prepareForMoveDelete(source, Policy.subMonitorFor(monitor, 20));
							if (destination.exists()) {
								EclipseSynchronizer.getInstance().prepareForMoveDelete(destination, Policy.subMonitorFor(monitor, 20));
							}
							tree.standardMoveFile(source, destination, updateFlags, Policy.subMonitorFor(monitor, 30));
							EclipseSynchronizer.getInstance().created(destination);
						}
					} finally {
						monitor.done();
					}
				}
			}, monitor);
		} catch (CVSException e) {
			tree.failed(e.getStatus());
		}
		return true;
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
			final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(source);
			if (cvsFolder.isManaged()) {
				if (!ensureCheckedOut(new IFolder[] {source, destination}, tree, Policy.subMonitorFor(monitor, 20))) return true;
				EclipseSynchronizer.getInstance().run(new ICVSRunnable() {
					public void run(IProgressMonitor monitor) throws CVSException {
						EclipseSynchronizer.getInstance().prepareForMoveDelete(source, Policy.subMonitorFor(monitor, 20));
						if (destination.exists()) {
							EclipseSynchronizer.getInstance().prepareForMoveDelete(destination, Policy.subMonitorFor(monitor, 20));
						}
						tree.standardMoveFolder(source, destination, updateFlags, Policy.subMonitorFor(monitor, 30));
						purgeCVSFolders(destination, Policy.subMonitorFor(monitor, 20));
					}
					private void purgeCVSFolders(IFolder destination, final IProgressMonitor monitor) throws CVSException {
						// Delete any CVS folders
						try {
							destination.accept(new IResourceVisitor() {
								public boolean visit(IResource resource) throws CoreException {
									if (resource.getType() == IResource.FOLDER && resource.getName().equals(SyncFileWriter.CVS_DIRNAME)) {
										tree.standardDeleteFolder((IFolder)resource, updateFlags, monitor);
										return false;
									}
									return true;
								}
							}, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
						} catch (CoreException e) {
							throw CVSException.wrapException(e);
						}
						// Signal that the destination has been created so any previous
						// sync info will be restored
						EclipseSynchronizer.getInstance().created(destination);
					}
				}, Policy.subMonitorFor(monitor, 60));
				return true;
			} else if (!cvsFolder.isIgnored()) {
				// XXX Should be inside cvs runnable
			   prepareToDelete(cvsFolder);
			}
		} catch (CVSException e) {
			tree.failed(e.getStatus());
			return true;
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
			CVSProviderPlugin.log(e);
		}
		return false;
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

	private boolean ensureCheckedOut(IFolder[] folders, IResourceTree tree, IProgressMonitor monitor) {
		final List readOnlyFiles = new ArrayList();
		try {
			// Find any read-only files
			for (int i = 0; i < folders.length; i++) {
				IFolder folder = folders[i];
				if (folder.exists()) {
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
				}
			}
			if (readOnlyFiles.isEmpty()) return true;
			// Ensure read-only files are checked out
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
	
	/*
	 * Signal that the unmanaged resource is about to be deleted so that the
	 * dirty count of it's parent can be reduced if appropriate.
	 */
	private void prepareToDelete(ICVSResource resource) throws CVSException {
		EclipseSynchronizer.getInstance().prepareForDeletion(resource.getIResource());
	}
}
