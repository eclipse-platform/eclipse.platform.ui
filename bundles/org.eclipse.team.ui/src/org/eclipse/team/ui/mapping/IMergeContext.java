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
package org.eclipse.team.ui.mapping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.ui.operations.MergeContext;

/**
 * Provides the context for an <code>IResourceMappingMerger</code>
 * or a model specific synchronization view that supports merging.
 * 
 * TODO: Need to have a story for folder merging (see bug 113898)
 * TODO: How are merge/markasMerge changes batched? IWorkspace#run? (see bug 113928)
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
	 * This method should only be used when remote content in being merged with
	 * local content. For cases in which either the local file or remote file
	 * dos not exist, one of the <code>merge</code> methods should be used.
	 * This is done to accomodate repositories that have special handling
	 * for file additions and removals.
	 * </p>
	 * <p>
	 * Clients should not implement this interface but should instead subclass 
	 * MergeContext.
	 * 
	 * @see MergeContext
	 * 
	 * @param file the file that has been merged
	 * @param monitor a progress monitor
	 * @return a status indicating the results of the operation
	 */
	public abstract IStatus markAsMerged(IFile file, IProgressMonitor monitor);
	
	/**
	 * Method that can be called by the model merger to attempt a file-system
	 * level merge. This is useful for cases where the model merger does not
	 * need to do any special processing to perform the merge. By default, this
	 * method attempts to use an appropriate <code>IStreamMerger</code> to
	 * merge the files covered by the provided traversals. If a stream merger
	 * cannot be found, the text merger is used. If this behavior is not
	 * desired, sub-classes may override this method.
	 * <p>
	 * This method does a best-effort attempt to merge all the files covered
	 * by the provided traversals. Files that could not be merged will be 
	 * indicated in the returned status. If the status returned has the code
	 * <code>MergeStatus.CONFLICTS</code>, the list of failed files can be 
	 * obtained by calling the <code>MergeStatus#getConflictingFiles()</code>
	 * method.
	 * <p>
	 * Any resource changes triggered by this merge will be reported through the 
	 * resource delta mechanism and the sync-info tree associated with this context.
	 * 
	 * TODO: How do we handle folder additions/removals generically? (see bug 113898)
	 * 
	 * @see SyncInfoSet#addSyncSetChangedListener(ISyncInfoSetChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param infos
	 *            the sync infos to be merged
	 * @param monitor
	 *            a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
	 * @throws CoreException if an error occurs
	 */
	public IStatus merge(ISyncInfoSet infos, IProgressMonitor monitor) throws CoreException;

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
	 * @see org.eclipse.team.ui.mapping.IMergeContext#merge(org.eclipse.core.resources.IFile,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus merge(SyncInfo info, IProgressMonitor monitor);
	
}
