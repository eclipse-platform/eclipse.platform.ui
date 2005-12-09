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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.diff.*;

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
	 * Method that allows the model merger to signal that the file associated
	 * with the given diff node has been completely merged. Model mergers can
	 * call this method if they have transfered all changes from a remote file
	 * to a local file and wish to signal that the merge is done. This will
	 * allow repository providers to update the synchronization state of the
	 * file to reflect that the file is up-to-date with the repository.
	 * <p>
	 * For two-way merging, this method can be used to reject any change. For
	 * three-way merging, this method should only be used when remote content in
	 * being merged with local content (i.e. the file exists both locally and
	 * remotely) with the exception that it can also be used to keep local
	 * changes to a file that has been deleted. See the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method for more
	 * details. For other cases in which either the local file or remote file
	 * does not exist, one of the <code>merge</code> methods should be used.
	 * This is done to accommodate repositories that have special handling for
	 * file additions, removals and moves. Invoking this method with a diff node
	 * associated with a folder will have no effect.
	 * <p>
	 * The <code>inSyncHint</code> allows a client to indicate to the context
	 * that the model persisted in the file is in-sync. If the hint is
	 * <code>true</code>, the context should compare the local and remote
	 * file at the content level and make the local file in-sync with the remote
	 * if the contents are the same.
	 * </p>
	 * 
	 * @param node the diff node whose file has been merged
	 * @param inSyncHint a hint to the context that the model persisted in the
	 *            file is in-sync.
	 * @param monitor a progress monitor
	 * @throws CoreException if errors occur
	 */
	public void markAsMerged(IDiffNode node, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Mark the files associated with the given diff nodes as being merged.
	 * This method is equivalent to calling {@link #markAsMerged(IDiffNode, boolean, IProgressMonitor) }
	 * for each diff but gives the context the opportunity to optimize the 
	 * operation for multiple files.
	 * <p>
	 * This method will batch change notification by using the
	 * {@link #run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor) }
	 * method. The rule for he method will be obtained using
	 * {@link #getMergeRule(IResource) } and the flags will be
	 * <code>IResource.NONE</code> meaning that intermittent change events may
	 * occur. Clients may wrap the call in an outer run that either uses a
	 * broader scheduling rule or the <code>IWorkspace.AVOID_UPDATES</code>
	 * flag.
	 * 
	 * @param nodes the nodes to be marked as merged
	 * @param inSyncHint a hint to the context that the model persisted in the
	 *            file is in-sync.
	 * @param monitor a progress monitor
	 * @throws CoreException if errors occur
	 */
	public void markAsMerged(IDiffNode[] nodes, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Method that can be called by the model merger to attempt a file-system
	 * level merge. This is useful for cases where the model merger does not
	 * need to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate {@link IStreamMerger} to merge the
	 * files covered by the provided traversals. If a stream merger cannot be
	 * found, the text merger is used. If this behavior is not desired,
	 * sub-classes of {@link MergeContext} may override this method.
	 * <p>
	 * This method does a best-effort attempt to merge of the file associated
	 * with the given diff. A file that could not be merged will be indicated in
	 * the returned status. If the status returned has the code
	 * <code>MergeStatus.CONFLICTS</code>, the list of failed files can be
	 * obtained by calling the <code>MergeStatus#getConflictingFiles()</code>
	 * method.
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
	 * The deltas provided to this method should be those obtained from the tree ({@link ISynchronizationContext#getDiffTree()})
	 * of this context. Any resource changes triggered by this merge will be
	 * reported through the resource delta mechanism and the change notification
	 * mechanisms of the delta tree associated with this context.
	 * <p>
	 * For two-way merging, clients can either accept changes using the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method or reject
	 * them using {@link #markAsMerged(IFile, boolean, IProgressMonitor) }.
	 * Three-way changes are a bit more complicated. The following list
	 * summarizes how particular remote file changes can be handled. The delta
	 * kind and flags mentioned in the descriptions are obtained the remote
	 * change (see {@link IThreeWayDiff#getRemoteChange()}), whereas conflicts
	 * are indicated by the three-way delta itself.
	 * <ul>
	 * 
	 * <li> When the delta kind is {@link IDiffNode#ADDED} and the delta is also
	 * a move (i.e. the {@link ITwoWayDiff#MOVED_FROM} is set). The merge can
	 * either use the {@link #merge(IDiffNode[], boolean, IProgressMonitor) }
	 * method to accept the rename or perform an
	 * {@link IFile#move(IPath, boolean, boolean, IProgressMonitor) } where the
	 * source file is obtained using {@link ITwoWayDiff#getMovedFromPath()} and
	 * the destination is the path of the delta ({@link IDiffNode#getPath()}).
	 * This later approach is helpful in the case where the local file and
	 * remote file both contain content changes (i.e. the file can be moved by
	 * the model and then the contents can be merged by the model). </li>
	 * 
	 * <li> When the delta kind is {@link IDiffNode#REMOVED} and the delta is
	 * also a move (i.e. the {@link ITwoWayDiff#MOVED_TO} is set). The merge can
	 * either use the {@link #merge(IDiffNode[], boolean, IProgressMonitor) }
	 * method to accept the rename or perform an
	 * {@link IFile#move(IPath, boolean, boolean, IProgressMonitor) } where the
	 * source file is obtained using {@link IDiffNode#getPath()} and the
	 * destination is obtained from {@link ITwoWayDiff#getMovedToPath()}. This
	 * later approach is helpful in the case where the local file and remote
	 * file both contain content changes (i.e. the file can be moved by the
	 * model and then the contents can be merged by the model). </li>
	 * 
	 * <li> When the delta kind is {@link IDiffNode#ADDED} and it is not part of
	 * a move, the merger must use the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method to accept
	 * this change. If there is a conflicting addition, the force flag can be
	 * set to override the local change. If the model wishes to keep the local
	 * changes, they can overwrite the file after merging it. Models should
	 * consult the flags to see if the remote change is a rename ({@link ITwoWayDiff#MOVED_FROM}).
	 * </li>
	 * 
	 * <li>When the delta kind is {@link IDiffNode#REMOVED} and it is not part
	 * of a move, the merger can use the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method but could
	 * also perform the delete manually using any of the {@link IFile} delete
	 * methods. In the case where there are local changes to the file being
	 * deleted, the model may either choose to merge using the force flag (thus
	 * removing the file and the local changes) or call
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) } on the file
	 * which will convert the incoming deletion to an outgoing addition.</li>
	 * 
	 * <li>When the delta kind is {@link IDiffNode#CHANGED} and there is no
	 * conflict, the model is advised to use the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method to merge
	 * these changes as this is the most efficient means to do so. However, the
	 * model can choose to perform the merge themselves and then invoke
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) } with the
	 * <code>inSyncHint</code> set to <code>true</code> but this will be
	 * less efficient. </li>
	 * 
	 * <li>When the delta kind is {@link IDiffNode#CHANGED} and there is a
	 * conflict, the model can use the
	 * {@link #merge(IDiffNode[], boolean, IProgressMonitor) } method to merge
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
	 * @see IDiffTree#addDiffChangeListener(org.eclipse.team.core.diff.ISyncDeltaChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param diff the difference to be merged
	 * @param force ignore any local changes when performing the merge.
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 * @throws CoreException if an error occurs
	 */
	public IStatus merge(IDiffNode diff, boolean force, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Attempt to merge any files associated with the given diffs. This method
	 * is equivalent to calling
	 * {@link #merge(IDiffNode, boolean, IProgressMonitor) } for each diff
	 * individually but gives the context a chance to perform a more optimal
	 * merge involving multiple resources.
	 * <p>
	 * This method will batch change notification by using the
	 * {@link #run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor) }
	 * method. The rule for he method will be obtained using
	 * {@link #getMergeRule(IResource) } and the flags will be
	 * <code>IResource.NONE</code> meaning that intermittent change events may
	 * occur. Clients may wrap the call in an outer run that either uses a
	 * broader scheduling rule or the <code>IWorkspace.AVOID_UPDATES</code>
	 * flag.
	 * 
	 * @param diffs the differences to be merged
	 * @param force ignore any local changes when performing the merge.
	 * @param monitor a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 * @throws CoreException if an error occurs
	 */
	public IStatus merge(IDiffNode[] diffs, boolean force,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Runs the given action as an atomic workspace operation. It has the same
	 * semantics as
	 * {@link IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor)}
	 * with the added behavior that any synchronization state updates are
	 * batched or deferred until the end of the operation (depending on the
	 * {@link IWorkspace#AVOID_UPDATE } flag.
	 * 
	 * @param runnable a workspace runnable
	 * @param rule a scheduling rule to be obtained while the runnable is run
	 * @param flags flags indicating when updates occur (either
	 *            <code>IResource.NONE</code> or
	 *            <code>IWorkspace.AVOID_UPDATE</code>.
	 * @param monitor a progress monitor
	 * @throws CoreException if an error occurs
	 */
	public void run(IWorkspaceRunnable runnable, ISchedulingRule rule,
			int flags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Return the scheduling rule that is required to merge the resource
	 * associated with the given diff. If a resource being merged is a folder or
	 * project, the returned rule will be sufficient to merge any files
	 * contained in the folder or project. The returned rule also applies to
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) }.
	 * 
	 * @param node the node to be merged
	 * @return the scheduling rule that is required to merge the resource of the
	 *         given diff
	 */
	public ISchedulingRule getMergeRule(IDiffNode node);

	/**
	 * Return the scheduling rule that is required to merge the resources
	 * associated with the given diffs. If a resource being merged is a folder
	 * or project, the returned rule will be sufficient to merge any files
	 * contained in the folder or project. The returned rule also applies to
	 * {@link #markAsMerged(IFile, boolean, IProgressMonitor) }.
	 * 
	 * @param nodes the nodes being merged
	 * @return the scheduling rule that is required to merge the resources of
	 *         the given diffs
	 */
	public ISchedulingRule getMergeRule(IDiffNode[] nodes);

}
