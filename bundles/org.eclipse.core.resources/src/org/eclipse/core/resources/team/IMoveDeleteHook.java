/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources.team;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * org.eclipse.core.internal.resources.Workspace
 * private void initialize() {
 * 	...
 * 	moveDeleteHook = initializeHook();
 * 	...
 * }
 * private IMoveDeleteHook initializeHook() {
 * 	// if there is no hook then use the default Core implementation
 * 	if (no team provider hook registered) {
 * 		return new MoveDeleteHook("core");
 * 	} else {
 * 		return new IMoveDeleteHook("team provider");
 * 	}
 * }
 * 
 * org.eclipse.core.internal.resources.Resource
 * public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
 * 	ResourceTree tree = new ResourceTree();
 * 	getWorkspace().getMoveDeleteHook().delete(tree, this, updateFlags, monitor);
 *		// if we had problems during the hook call, then throw an exception
 * 	if (!tree.getStatus().isOK()) {
 * 		throw new ResourceException(tree.getStatus());
 * 	}
 * }
 * public void move(IPath path, int updateFlags, IProgressMonitor monitor) throws CoreException {
 * 	ResourceTree tree = new ResourceTree();
 * 	IResource destination = null;
 * 	switch (getType()) {
 * 		case IResource.FILE:
 * 			destination = getWorkspace().getRoot().getFile(path);
 * 			getWorkspace().getMoveDeleteHook().moveFile(tree, (IFile) this, (IFile) destination, updateFlags, monitor);
 * 			break;
 * 		case IResource.FOLDER:
 * 			getWorkspace().getMoveDeleteHook().moveFolder(tree, (IFolder) this, (IFolder) destination, updateFlags, monitor);
 * 			destination = getWorkspace().getRoot().getFolder(path);
 * 			break;
 * 		case IResource.PROJECT:
 * 			getWorkspace().getMoveDeleteHook().moveFile(tree, (IProject) this, getWorkspace().newProjectDescription(path.lastSegment()), updateFlags, monitor);
 * 			break;
 * 		case IResource.ROOT:
 * 			throw new ResourceException("error");
 * 		}
 *		// if we had problems during the hook call, then throw an exception
 * 	if (!tree.getStatus().isOK()) {
 * 		throw new ResourceException(tree.getStatus());
 * 	}
 * }
 * 
 * 
 * @since 2.0
 */
public interface IMoveDeleteHook {

/**
 * Delete the given file. 
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>deletedFile</code>, <code>deleteFailed</code>,
 * or <code>standardDeleteFile</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree the tree
 * @param file the file
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return <code>true</code>
 */
public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor);

/**
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>deletedFolder</code>, <code>deleteFailed</code>,
 * or <code>standardDeleteFolder</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree
 * @param folder
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return
 */
public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor);

/**
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>deletedProject</code>, <code>deleteFailed</code>,
 * or <code>standardDeleteProject</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree
 * @param project
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return
 */
public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor);

/**
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>movedFile</code>, <code>moveFailed</code>,
 * or <code>standardMoveFile</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree
 * @param source
 * @param destination
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return
 */
public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor);

/**
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>movedFolder</code>, <code>moveFailed</code>,
 * or <code>standardMoveFolder</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree
 * @param source
 * @param destination
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return
 */
public boolean moveFolder(IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor);

/**
 * Return true if the hook did the work, false otherwise.
 * 
 * <p>
 * By the end of this operation the hook must have called exactly one of the following
 * methods on the given resource tree: <code>movedProject</code>, <code>moveFailed</code>,
 * or <code>standardMoveProject</code>.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param tree
 * @param source
 * @param description
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return
 */
public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor);
}
