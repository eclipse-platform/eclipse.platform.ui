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
package org.eclipse.core.resources.team;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Provides internal access to the workspace resource tree for the purposes of
 * implementing move and delete operation. Implementations of 
 * <code>IMoveDeleteHook</code> call these methods.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 2.0
 */
public interface IResourceTree {

	/**
	 * Constant indicating that no file timestamp was supplied.
	 * <p>
	 * [FIXME: make this the same as java.io.File#getLastModified
	 * when the file doesn't exist.]
	 * </p>
	 * 
	 * @see #movedFile
	 */
	public static final long NULL_TIMESTAMP = 0;

	/**
	 * Adds the current state of the given file to the local history.
	 * Does nothing if the file does not exist in the workspace resource tree,
	 * or if it exists in the workspace resource tree but not in the local file
	 * system.
	 * <p>
	 * This method is used to capture the state of a file in the workspace
	 * local history before it is overwritten or deleted.
	 * </p>
	 * 
	 * @param file the file to be captured
	 */
	public void addToLocalHistory(IFile file);

	/**
	 * Returns whether the given resource and its descendants to the given depth 
	 * are considered to be in sync with the local file system. Returns 
	 * <code>true</code> if the given resource does not exist in the workspace
	 * resource tree.
	 * 
	 * @param resource the resource of interest
	 * @param depth the depth (one of <code>IResource.DEPTH_ZERO</code>,
	 *   <code>DEPTH_ONE</code>, or <code>DEPTH_INFINITE</code>)
	 * @return <code>true</code> if the resource is synchronized or does not 
	 *   exist in the workspace resource tree, <code>false</code> otherwise
	 */
	public boolean isSynchronized(IResource resource, int depth);

	/**
	 * Returns the timestamp for the given file in the local file system.
	 * Returns <code>NULL_TIMESTAMP</code> if the file does not
	 * exist in the workspace or if its location in the local file system cannot
	 * be determined.
	 * <p>
	 * Note that the timestamp stored in the workspace resource tree is not 
	 * interchangeable with <code>java.io.File</code> last modification time
	 * (<code>computeTimestamp</code> has higher resolution).
	 * </p>
	 * 
	 * @param file the file of interest
	 * @return the local file system timestamp for the file, or 
	 *    <code>NULL_TIMESTAMP</code> if it could not be computed
	 */
	public long computeTimestamp(IFile file);

	/**
	 * Declares that the operation has failed for the specified reason.
	 * This method may be called multiple times to report multiple
	 * failures. All reasons will be accumulated and taken into consideration
	 * when deciding the outcome of the hooked operation as a whole.
	 * 
	 * @param reason the reason the operation (or sub-operation) failed
	 */
	public void failed(IStatus reason);

	/**
	 * Declares that the given file has been successfully deleted from the
	 * local file system, and requests that the corresponding deletion should
	 * now be made to the workspace resource tree. No action is taken if the
	 * given file does not exist in the workspace resource tree.
	 * <p>
	 * This method clears out any markers, session properties, and persistent
	 * properties associated with the given file.
	 * </p>
	 * 
	 * @param file the file that was just deleted from the local file system
	 */
	public void deletedFile(IFile file);

	/**
	 * Declares that the given folder and all its descendents have been 
	 * successfully deleted from the local file system, and requests that the
	 * corresponding deletion should now be made to the workspace resource tree.
	 * No action is taken if the given folder does not exist in the workspace
	 * resource tree.
	 * <p>
	 * This method clears out any markers, session properties, and persistent
	 * properties associated with the given folder or its descendents.
	 * </p>
	 * 
	 * @param folder the folder that was just deleted from the local file system
	 */
	public void deletedFolder(IFolder folder);

	/**
	 * Declares that the given project's content area in the local file system
	 * has been successfully dealt with in an appropriate manner, and requests 
	 * that the corresponding deletion should now be made to the workspace 
	 * resource tree. No action is taken if the given project does not exist in
	 * the workspace resource tree.
	 * <p>
	 * This method clears out everything associated with this project and any of 
	 * its descendent resources, including: markers; session properties; 
	 * persistent properties; local history; and project-specific plug-ins 
	 * working data areas. The project's content area is not affected.
	 * </p>
	 * 
	 * @param project the project being deleted
	 */
	public void deletedProject(IProject project);

	/**
	 * Declares that the given source file has been successfully moved to the
	 * given destination in the local file system, and requests that the
	 * corresponding changes should now be made to the workspace resource tree.
	 * No action is taken if the given source file does not exist in the 
	 * workspace resource tree. The given timestamp is that  of the file after
	 * the move, as computed by <code>computeTimestamp</code>. If the timestamp
	 * is <code>NULL_TIMESTAMP</code> then the  destination file will be queried
	 * for its timestamp.
	 * <p>
	 * The destination file must not already exist in the workspace resource
	 * tree.
	 * </p>
	 * 
	 * @param source the handle of the source file that was moved
	 * @param destination the handle of where the file moved to
	 * @param timestamp the timestamp of the file in the local file system
	 *    after the move, as determined by  <code>computeTimestamp</code>; 
	 *    if the timestamp is <code>NULL_TIMESTAMP</code> then the destination
	 *    file will be queried for its timestamp
	 * @see #computeTimestamp
	 */
	public void movedFile(IFile source, IFile destination, long timestamp);

	/**
	 * Declares that the given source folder and its descendents have been
	 * successfully moved to the given destination in the local file system,
	 * and requests that the corresponding changes should now be made to the
	 * workspace resource tree for the folder and all its descendents. No action
	 * is taken if the given source folder does not exist in the workspace
	 * resource tree.
	 * <p>
	 * This operation assumes the file timestamps are unchanged. Consequently,
	 * this operation cannot be used if some file timestamps changed in the
	 * process of moving the files in the local file system.
	 * </p>
	 * <p>
	 * The destination folder must not already exist in the workspace resource
	 * tree.
	 * </p>
	 * 
	 * @param source the handle of the source folder that was moved
	 * @param destination the handle of where the folder moved to
	 */
	public void movedFolderSubtree(IFolder source, IFolder destination);

	/**
	 * Declares the start of a move of the given source folder to the given
	 * destination folder.
	 * <p>
	 * Call this method after the destination folder has been successfully
	 * created in the local file system; it will create the corresponding folder
	 * in the workspace  resource tree. No action is taken if the given source
	 * folder does not exist in the workspace resource tree.
	 * </p>
	 * <p>
	 * Calling this method enables a folder move to be decomposed into a series
	 * of moves of the folder's members.  After all members have been
	 * successfully moved,  call <code>endMoveFolder</code> to complete the
	 * transaction.
	 * </p>
	 * <p>
	 * The destination folder must not already exist in the workspace resource
	 * tree.
	 * </p>
	 * 
	 * @param source the handle of the source folder that is being moved
	 * @param destination the handle of where the folder is being moved to
	 */
	public void beginMoveFolder(IFolder source, IFolder destination);

	/**
	 * Declares the end of a successful move of the given source folder to the
	 * given destination folder that began with an earlier call to
	 * <code>beginMoveFolder</code>.
	 * <p>
	 * Call this method after the members of the source folder have been
	 * successfully moved to their appropriate spots in the destination folder.
	 * This method makes the appropriate changes to the workspace resource tree
	 * required to complete the transaction. These changes affect the
	 * destination folder (not its descendents), and the source folder.
	 * No action is taken if either the given source folder or the given
	 * destination folder does not exist in the workspace resource tree.
	 * </p>
	 * <p>
	 * This method moves any markers, session properties, and persistent
	 * properties associated with the given source folder to the destination
	 * source folder. Any markers, session properties, and persistent properties
	 * associated with any remaining descendents of the given source folder are
	 * discarded.
	 * </p>
	 * 
	 * @param source the handle of the source folder that is being moved
	 * @param destination the handle of where the folder is being moved to
	 */
	public void endMoveFolder(IFolder source, IFolder destination);

	/**
	 * Declares that the given source project and its files and folders have 
	 * been successfully relocated in the local file system if required, and
	 * requests that the rename and/or relocation should now be made to the
	 * workspace resource tree for the project and all its descendents. No
	 * action is taken if the given project does not exist in the workspace
	 * resource tree.
	 * <p>
	 * This operation assumes the file timestamps are unchanged. Consequently,
	 * this operation cannot be used if some file timestamps changed in the
	 * process of moving the files in the local file system.
	 * <p>
	 * If the project is being renamed, the destination project must not
	 * already exist in the workspace resource tree.
	 * </p>
	 * <p>
	 * Local history is not preserved if the project is renamed. It is preserved
	 * when the project's content area is relocated without renaming the
	 * project.
	 * </p>
	 * 
	 * @param source the handle of the source project that was moved
	 * @param description the new project description
	 */
	public void movedProjectSubtree(
		IProject source,
		IProjectDescription description);

	/**
	 * Declares the start of a rename/relocate of the given project.
	 * <p>
	 * Call this method after the destination project's content area has been
	 * successfully created in the local file system if required; it will create
	 * the corresponding project in the workspace resource tree. No action is
	 * taken if the given project does not exist in the workspace resource tree.
	 * </p>
	 * <p>
	 * Calling this method enables a project rename/relocate to be decomposed
	 * into a series of moves of its member folders and files.  After all 
	 * members have been successfully moved,  call <code>endMoveProject</code>
	 * to complete the transaction.
	 *</p>
	 * <p>
	 * If the project is being renamed, the destination project must not already
	 * exist in the workspace resource tree.
	 * </p>
	 * 
	 * @param project the handle of the project that is being renamed/relocated
	 * @param description the new project description
	 */
	public void beginMoveProject(
		IProject project,
		IProjectDescription description);

	/**
	 * Declares the end of a successful rename/relocate of the given project that
	 * began with an earlier call to <code>beginMoveProject</code> .
	 * <p>
	 * Call this method after the members of the project have been successfully
	 * moved to their appropriate spots in the destination project. This method
	 * makes the appropriate changes to the workspace resource tree to complete
	 * the transaction. These changes affect the destination project only (not
	 * its descendents). No action is taken if either the given project or the
	 * destination folder does not exist in the workspace resource tree.
	 * </p>
	 * <p>
	 * This method moves any markers, session properties, and persistent
	 * properties associated with the given project to the destination project. 
	 * Any markers, session properties, and persistent properties associated 
	 * with any remaining descendents of the given project are discarded.
	 * </p>
	 * <p>
	 * Local history is not preserved when a project is renamed. It is preserved
	 * when the project's content area is relocated without renaming the
	 * project.
	 * </p>
	 * 
	 * @param project the handle of the project that is being renamed/relocated
	 * @param description the new project description
	 */
	public void endMoveProject(
		IProject project,
		IProjectDescription description);

	/**
	 * Deletes the given file in the standard manner from both the local file 
	 * system and from the workspace resource tree.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of  <code>file.delete(updateFlags, monitor)</code> because all
	 * regular API operations that modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param file the file to delete
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 */
	public void standardDeleteFile(
		IFile file,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Deletes the given folder and its descendents in the standard manner from
	 * both the local file system and from  the workspace resource tree.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of  <code>folder.delete(updateFlags, monitor)</code> because all
	 * regular API operations that modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param folder the folder to delete
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 */
	public void standardDeleteFolder(
		IFolder folder,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Deletes the given project and its descendents in the standard manner from
	 * both the local file system and from the workspace resource tree.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of  <code>project.delete(updateFlags, monitor)</code> because all
	 * regular API operations that modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically 
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param project the project to delete
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.delete(int,IProgressMonitor)</code>
	 */
	public void standardDeleteProject(
		IProject project,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Moves the given file in the standard manner from both the local file 
	 * system and from the workspace resource tree.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of <code>source.move(destination.getProjectRelativePath(),
	 * updateFlags, monitor)</code> because all regular API  operations that
	 * modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically 
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param source the handle of the source file to move
	 * @param destination the handle of where the file will move to
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 */
	public void standardMoveFile(
		IFile source,
		IFile destination,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Moves the given folder and its descendents in the standard manner from
	 * both the local file system and from the workspace resource tree.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of <code>source.move(destination.getProjectRelativePath(),
	 * updateFlags, monitor)</code> because all regular API  operations that
	 * modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param source the handle of the source folder to move
	 * @param destination the handle of where the folder will move to
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 */
	public void standardMoveFolder(
		IFolder source,
		IFolder destination,
		int updateFlags,
		IProgressMonitor monitor);

	/**
	 * Renames and/or relocates the given project in the standard manner.
	 * <p>
	 * Implementations of <code>IMoveDeleteHook</code> must invoke this method
	 * in lieu of <code>source.move(description, updateFlags, monitor)</code>
	 * because all regular API  operations that modify resources are off limits.
	 * </p>
	 * <p>
	 * If the operation fails, the reason for the failure is automatically
	 * collected by an internal call to <code>failed</code>.
	 * </p>
	 * 
	 * @param source the handle of the source folder to move
	 * @param description the new project description
	 * @param updateFlags bit-wise or of update flag constants as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 * @param monitor the progress monitor, or <code>null</code> as per 
	 *    <code>IResource.move(IPath,int,IProgressMonitor)</code>
	 */
	public void standardMoveProject(
		IProject source,
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor);
}