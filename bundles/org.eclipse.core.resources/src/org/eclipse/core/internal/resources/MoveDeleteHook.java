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
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.FileSystemStore;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;

/**
 * @since 2.0
 */
public class MoveDeleteHook implements IMoveDeleteHook {

/**
 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
 */
public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

/**
 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
 */
public boolean deleteFolder(IResourceTree tree, IFolder folder, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

/**
 * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
 */
public boolean deleteProject(IResourceTree tree, IProject project, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

/**
 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
 */
public boolean moveFile(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

/**
 * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
 */
public boolean moveFolder(final IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

/**
 * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
 */
public boolean moveProject(IResourceTree tree, IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
	// Let someone else do the work.
	return false;
}

}
