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
import org.eclipse.core.runtime.IPath;

/**
 * Primary interface for hooking the implementation of <code>IResource.move</code>
 * and <code>IResource.delete</code>.
 * <p>
 * This interface is intended to be implemented by the team component in conjunction
 * with the <code>org.eclipse.core.resources.moveDeleteHook</code> standard extension
 * point. It is not intended to be implemented by other clients.
 * </p>
 * 
 * @since 2.0
 */
public interface IMoveDeleteHook {

	/**
	 * Implements <code>IResource.delete(int,IProgressMonitor)</code> where the receiver
	 * is a file. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * In broad terms, a full re-implementation should delete the file in the local file
	 * system and then call <code>tree.deletedFile</code> to complete the updating of the
	 * workspace resource tree to reflect this fact. If unsuccessful in deleting the file
	 * from the the local file system, it should instead call <code>tree.failed</code> to
	 * report the reason for the failure. In either case, it should return
	 * <code>true</code> to indicate that the operation was attempted.
	 * The <code>FORCE</code> update flag needs to be honored: unless <code>FORCE</code>
	 * is specified, the implementation must use <code>tree.isSynchronized</code> to 
	 * determine whether the file is in sync before attempting to delete it.
	 * The <code>KEEP_HISTORY</code> update flag needs to be honored as well; use
	 * <code>tree.addToLocalHistory</code> to capture the contents of the file
	 * (naturally, this must be before deleting the file from the local file system).
	 * </p>
	 * <p>
	 * An extending implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardDeleteFile</code> to explicitly
	 * invoke the standard file deletion behavior, which deletes both the file from the
	 * local file system and updates the workspace resource tree. It should return
	 * <code>true</code> to indicate that the operation was attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardDeleteFile</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param file the handle of the file to delete; the receiver of
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#delete(int,IProgressMonitor)
	 */
	public boolean deleteFile(
		IResourceTree tree,
		IFile file,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Implements <code>IResource.delete(int,IProgressMonitor)</code> where the receiver
	 * is a folder. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * In broad terms, a full re-implementation should delete the directory tree in the
	 * local file system and then call <code>tree.deletedFolder</code> to complete the
	 * updating of the workspace resource tree to reflect this fact. If unsuccessful 
	 * in deleting the directory or any of its descendents from the the local file system,
	 * it should instead call <code>tree.failed</code> to report each reason for failure.
	 * In either case it should return <code>true</code> to indicate that the operation
	 * was attempted.
	 * The <code>FORCE</code> update flag needs to be honored: unless <code>FORCE</code>
	 * is specified, the implementation must use <code>tree.isSynchronized</code> to 
	 * determine whether the folder subtree is in sync before attempting to delete it.
	 * The <code>KEEP_HISTORY</code> update flag needs to be honored as well; use
	 * <code>tree.addToLocalHistory</code> to capture the contents of any files being
	 * deleted.
	 * </p>
	 * <p>
	 * A partial re-implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardDeleteFolder</code> to explicitly
	 * invoke the standard folder deletion behavior, which deletes both the folder
	 * and its descendents from the local file system and updates the workspace resource
	 * tree. It should return <code>true</code> to indicate that the operation was
	 * attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardDeleteFolder</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param folder the handle of the folder to delete; the receiver of
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#delete(int,IProgressMonitor)
	 */
	public boolean deleteFolder(
		IResourceTree tree,
		IFolder folder,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Implements <code>IResource.delete(int,IProgressMonitor)</code> where the receiver
	 * is a project. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * In broad terms, a full re-implementation should delete the project content area in
	 * the local file system if required (the files of a closed project should be deleted
	 * only if the <code>IResource.ALWAYS_DELETE_PROJECT_CONTENTS</code> update flag is
	 * specified; the files of an open project should be deleted unless the
	 * the <code>IResource.NEVER_DELETE_PROJECT_CONTENTS</code> update flag is
	 * specified). It should then call <code>tree.deletedProject</code> to complete 
	 * the updating of the workspace resource tree to reflect this fact. If unsuccessful
	 * in deleting the project's files from the local file system, it should instead call
	 * <code>tree.failed</code> to report the reason for the failure. In either case, it
	 * should return <code>true</code> to indicate that the operation was attempted.
	 * The <code>FORCE</code> update flag may need to be honored if the project is open:
	 * unless <code>FORCE</code> is specified, the implementation must use
	 * <code>tree.isSynchronized</code> to  determine whether the project subtree is in
	 * sync before attempting to delete it. 
	 * Note that local history is not maintained when a project is deleted,
	 * regardless of the setting of the <code>KEEP_HISTORY</code> update flag. 
	 * </p>
	 * <p>
	 * A partial re-implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardDeleteProject</code> to explicitly
	 * invoke the standard project deletion behavior. It should return <code>true</code>
	 * to indicate that the operation was attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardDeleteProject</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param project the handle of the project to delete; the receiver of
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#delete(int,IProgressMonitor)
	 */
	public boolean deleteProject(
		IResourceTree tree,
		IProject project,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Implements <code>IResource.move(IPath,int,IProgressMonitor)</code> where the receiver
	 * is a file. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * In broad terms, a full re-implementation should move the file in the local file
	 * system and then call <code>tree.moveFile</code> to complete the updating of the
	 * workspace resource tree to reflect this fact. If unsuccessful in moving the file
	 * in the the local file system, it should instead call <code>tree.failed</code> to
	 * report the reason for the failure. In either case, it should return
	 * <code>true</code> to indicate that the operation was attempted.
	 * The <code>FORCE</code> update flag needs to be honored: unless <code>FORCE</code>
	 * is specified, the implementation must use <code>tree.isSynchronized</code> to 
	 * determine whether the file is in sync before attempting to moving it.
	 * The <code>KEEP_HISTORY</code> update flag needs to be honored as well; use
	 * <code>tree.addToLocalHistory</code> to capture the contents of the file
	 * (naturally, this must be before moving the file from the local file system).
	 * </p>
	 * <p>
	 * An extending implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardMoveFile</code> to explicitly
	 * invoke the standard file moving behavior, which moves both the file in the
	 * local file system and updates the workspace resource tree. It should return
	 * <code>true</code> to indicate that the operation was attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardMoveFile</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param source the handle of the file to move; the receiver of
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param destination the handle of where the file will move to; the handle 
	 *    equivalent of the first parameter to
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#move(IPath,int,IProgressMonitor)
	 */
	public boolean moveFile(
		IResourceTree tree,
		IFile source,
		IFile destination,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Implements <code>IResource.move(IPath,int,IProgressMonitor)</code> where the receiver
	 * is a project. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * In broad terms, a full re-implementation should move the directory tree in the
	 * local file system and then call <code>tree.movedFolder</code> to complete the
	 * updating of the workspace resource tree to reflect this fact. If unsuccessful 
	 * in moving the directory or any of its descendents in the the local file system,
	 * it should instead call <code>tree.failed</code> to report each reason for failure.
	 * In either case it should return <code>true</code> to indicate that the operation
	 * was attempted.
	 * The <code>FORCE</code> update flag needs to be honored: unless <code>FORCE</code>
	 * is specified, the implementation must use <code>tree.isSynchronized</code> to 
	 * determine whether the folder subtree is in sync before attempting to move it.
	 * The <code>KEEP_HISTORY</code> update flag needs to be honored as well; use
	 * <code>tree.addToLocalHistory</code> to capture the contents of any files being
	 * moved.
	 * </p>
	 * <p>
	 * A re-implementation that needs to undbundle a folder move into recursive moves
	 * of the descendent files should call <code>tree.beginMovingFolder</code> to
	 * start things off by creating a new folder at the destination. After is has
	 * successfully moved all descendents, it should call <code>tree.endMoving</code>
	 * to complete the updating of the workspace resource tree to reflect the move.
	 * </p>
	 * <p>
	 * A partial re-implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardMoveFolder</code> to explicitly
	 * invoke the standard folder move behavior, which move both the folder
	 * and its descendents in the local file system and updates the workspace resource
	 * tree. It should return <code>true</code> to indicate that the operation was
	 * attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardDeleteFolder</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param source the handle of the folder to move; the receiver of
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param destination the handle of where the folder will move to; the handle 
	 *    equivalent of the first parameter to
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#move(IPath,int,IProgressMonitor)
	 */
	public boolean moveFolder(
		IResourceTree tree,
		IFolder source,
		IFolder destination,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Implements <code>IResource.move(IPath,int,IProgressMonitor)</code> where the receiver
	 * is a project. Returns <code>true</code> to accept responsibility for implementing
	 * this operation as per the API contract.
	 * <p>
	 * [FIXME - There are a number of cases that must be handled...]
	 * </p>
	 * <p>
	 * A partial re-implementation should perform whatever pre-processing it needs
	 * to do and then call <code>tree.standardMoveProject</code> to explicitly
	 * invoke the standard project move behavior. It should return <code>true</code>
	 * to indicate that the operation was attempted.
	 * </p>
	 * <p>
	 * Returning <code>false</code> is the easy way for the implementation to say "pass".
	 * It is equivalent to calling <code>tree.standardMoveProject</code> and returning
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param tree the workspace resource tree
	 * @param source the handle of the project to move; the receiver of
	 *    <code>IResource.move(IProjectDescription,int,IProgressMonitor)</code>
	 * @param description the new description of the project; the first parameter to
	 *    <code>IResource.move(IProjectDescription,int,IProgressMonitor)</code>
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IProjectDescription,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IProjectDescription,int,IProgressMonitor)</code>
	 * @return <code>false</code> if this method declined to assume responsibility for
	 *   this operation, and <code>true</code> if this method attempted to carry out the
	 *   operation
	 * @see org.eclipse.core.resources.IResource#move(IProjectDescription,int,IProgressMonitor)
	 */
	public boolean moveProject(
		IResourceTree tree,
		IProject source,
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor);
}