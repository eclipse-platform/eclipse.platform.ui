/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.core.synchronize.*;

/**
 * Provides the context for an <code>IResourceMappingMerger</code> or a model
 * specific synchronization view that supports merging.
 * 
 * TODO: Need to have a story for folder merging (see bug 113898) TODO: How are
 * merge/markasMerge changes batched? IWorkspace#run? (see bug 113928)
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass {@link MergeContext}.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceMappingMerger
 * @see MergeContext
 * @since 3.2
 */
public interface IMergeContext extends ISynchronizationContext {

	/**
	 * Method that allows the model merger to signal that the file in question
	 * has been completely merged. Model mergers can call this method if they
	 * have transfered all changes from a remote file to a local file and wish
	 * to signal that the merge is done. This will allow repository providers to
	 * update the synchronization state of the file to reflect that the file is
	 * up-to-date with the repository.
	 * <p>
	 * For two-way merging, this method can be used to reject any change. For
	 * three-way merging, this method should only be used when remote content in
	 * being merged with local content (i.e. the file exists both locally and
	 * remotely) with the exception that it can also be used to keep local
	 * changes to a file that has been deleted. See the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method for more
	 * details. For other cases in which either the local file or remote file
	 * does not exist, one of the <code>merge</code> methods should be used.
	 * This is done to accommodate repositories that have special handling for
	 * file additions, removals and moves.
	 * <p>
	 * The <code>inSyncHint</code> allows a client to indicate to the context
	 * that the model persisted in the file is in-sync. If the hint is
	 * <code>true</code>, the context should compare the local and remote
	 * file at the content level and make the local file in-sync with the remote
	 * if the contents are the same.
	 * </p>
	 * 
	 * @param file the file that has been merged
	 * @param inSyncHint a hint to the context that the model persisted in the
	 *            file is in-sync.
	 * @param monitor a progress monitor
	 * @return a status indicating the results of the operation
	 * @throws CoreException if errors occur
	 */
	public abstract IStatus markAsMerged(IFile file, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Method that can be called by the model merger to attempt a file-system
	 * level merge. This is useful for cases where the model merger does not
	 * need to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate <code>IStreamMerger</code> to
	 * merge the files covered by the provided traversals. If a stream merger
	 * cannot be found, the text merger is used. If this behavior is not
	 * desired, sub-classes may override this method.
	 * <p>
	 * This method does a best-effort attempt to merge all the files covered by
	 * the provided traversals. Files that could not be merged will be indicated
	 * in the returned status. If the status returned has the code
	 * <code>MergeStatus.CONFLICTS</code>, the list of failed files can be
	 * obtained by calling the <code>MergeStatus#getConflictingFiles()</code>
	 * method.
	 * <p>
	 * Any resource changes triggered by this merge will be reported through the
	 * resource delta mechanism and the sync-info tree associated with this
	 * context.
	 * 
	 * TODO: How do we handle folder additions/removals generically? (see bug
	 * 113898)
	 * <p>
	 * 
	 * @see SyncInfoSet#addSyncSetChangedListener(ISyncInfoSetChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param infos the sync infos to be merged
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 * @throws CoreException if an error occurs
	 */
	public IStatus merge(ISyncInfoSet infos, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Method that can be called by the model merger to attempt a file-system
	 * level merge. This is useful for cases where the model merger does not
	 * need to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate {@link IStreamMerger} to merge the
	 * files covered by the provided traversals. If a stream merger cannot be
	 * found, the text merger is used. If this behavior is not desired,
	 * sub-classes of {@link MergeContext} may override this method.
	 * <p>
	 * This method does a best-effort attempt to merge all the files covered by
	 * the provided traversals. Files that could not be merged will be indicated
	 * in the returned status. If the status returned has the code
	 * <code>MergeStatus.CONFLICTS</code>, the list of failed files can be
	 * obtained by calling the <code>MergeStatus#getConflictingFiles()</code>
	 * method. TODO: Should report conflicts as paths to be consistent with
	 * deltas!
	 * <p>
	 * It is not expected that clients of this API will associate special
	 * meaning with the existence of a folder other than the fact that it
	 * contains files. The sync delta tree should still include folder changes
	 * so that clients that have a one-to-one correspondence between their model
	 * objects and folders can decorate these elements appropriately. However,
	 * clients of this API will only be expected to perform operations on file
	 * deltas and will expect folders to be created as needed to contain the
	 * files (i.e. implementations of this method should ignore any folder
	 * deltas in the provided deltas). Clients will also expect local folders
	 * that have incoming folder deletions to be removed once all the folder's
	 * children have been removed using merge.
	 * <p>
	 * It is not expected that clients of this API will be capable of dealing
	 * with namespace conflicts. Implementors should ensure that any namespace
	 * conflicts are dealt with before the merger is invoked.
	 * <p>
	 * The deltas provided to this method should be those obtained from the tree 
	 * ({@link ISynchronizationContext#getSyncDeltaTree()})
	 * of this context. Any resource changes triggered by this merge will be
	 * reported through the resource delta mechanism and the change notification
	 * mechanisms of the delta tree associated with this context.
	 * <p>
	 * For two-way merging, clients can either accept changes using the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method or reject
	 * them using {@link #markAsMerged(IFile, boolean, IProgressMonitor) }.
	 * Three-way changes are a bit more complicated. The following list
	 * summarizes how particular remote file changes can be handled. The delta
	 * kind and flags mentioned in the descriptions are obtained the remote
	 * change (see {@link IThreeWayDelta#getRemoteChange()}), whereas conflicts
	 * are indicated by the three-way delta itself.
	 * <ul>
	 * 
	 * <li> When the delta kind is {@link IDelta#ADDED} and the delta is
	 * also a move (i.e. the {@link ITwoWayDelta#MOVED_FROM} is set). The merge
	 * can either use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method to accept
	 * the rename or perform an
	 * {@link IFile#move(IPath, boolean, boolean, IProgressMonitor) } where the
	 * source file is obtained using {@link ITwoWayDelta#getMovedFromPath()} and
	 * the destination is the path of the delta ({@link IDelta#getPath()}).
	 * This later approach is helpful in the case where the local file and
	 * remote file both contain content changes (i.e. the file can be moved by
	 * the model and then the contents can be merged by the model). </li>
	 * 
	 * <li> When the delta kind is {@link IDelta#REMOVED} and the delta is
	 * also a move (i.e. the {@link ITwoWayDelta#MOVED_TO} is set). The merge
	 * can either use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method to accept
	 * the rename or perform an
	 * {@link IFile#move(IPath, boolean, boolean, IProgressMonitor) } where the
	 * source file is obtained using {@link IDelta#getPath()} and the
	 * destination is obtained from {@link ITwoWayDelta#getMovedToPath()}. This
	 * later approach is helpful in the case where the local file and remote
	 * file both contain content changes (i.e. the file can be moved by the
	 * model and then the contents can be merged by the model). </li>
	 * 
	 * <li> When the delta kind is {@link IDelta#ADDED} and it is not part
	 * of a move, the merger must use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method to accept
	 * this change. If there is a conflicting addition, the force flag can be
	 * set to override the local change. If the model wishes to keep the local
	 * changes, they can overwrite the file after merging it. Models should
	 * consult the flags to see if the remote change is a rename 
	 * ({@link ITwoWayDelta#MOVED_FROM}).
	 * </li>
	 * 
	 * <li>When the delta kind is {@link IDelta#REMOVED} and it is not part
	 * of a move, the merger can use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method but could
	 * also perform the delete manually using any of the {@link IFile} delete
	 * methods. In the case where there are local changes to the file being
	 * deleted, the model may either choose to merge using the force flag (thus
	 * removing the file and the local changes) or call
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) } on the file
	 * which will convert the incoming deletion to an outgoing addition.</li>
	 * 
	 * <li>When the delta kind is {@link IDelta#CHANGED} and there is no
	 * conflict, the model is advised to use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method to merge
	 * these changes as this is the most efficient means to do so. However, the
	 * model can choose to perform the merge themselves and then invoke
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) } with the
	 * <code>inSyncHint</code> set to <code>true</code> but this will be
	 * less efficient. </li>
	 * 
	 * <li>When the delta kind is {@link IDelta#CHANGED} and there is a
	 * conflict, the model can use the
	 * {@link #merge(IDelta[], boolean, IProgressMonitor) } method to merge
	 * these changes. If the force flag is not set, an auto-merge is attempted
	 * using an appropriate {@link IStreamMerger}. If the force flag is set,
	 * the local changes are discarded. The model can choose to attempt the
	 * merge themselves and, if it is successful, invoke
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) } with the
	 * <code>inSyncHint</code> set to <code>false</code> which will make the
	 * file an outgoing change. </li>
	 * </ul>
	 * 
	 * TODO: need to talk about ITwoWayDelta CONTENT and REPLACED
	 * 
	 * @see IDeltaTree#addSyncDeltaChangeListener(org.eclipse.team.core.delta.ISyncDeltaChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param deltas the deltas to be merged
	 * @param force ignore any local changes when performing the merge.
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 * @throws CoreException if an error occurs
	 */
	public IStatus merge(IDelta[] deltas, boolean force,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Method that can be called by the model merger to attempt a merge of a
	 * particular resource.
	 * 
	 * <p>
	 * For files, this is useful for cases where the model merger does not need
	 * to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate <code>IStreamMerger</code> to
	 * perform the merge on a file. If a stream merger cannot be found, the text
	 * merger is used. If this behavior is not desired, sub-classes may override
	 * this method.
	 * 
	 * @param file the file to be merged
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 */
	public IStatus merge(SyncInfo info, IProgressMonitor monitor);

}
