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

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.delta.DeltaTree;
import org.eclipse.team.internal.core.delta.SyncInfoToDeltaConverter;

/**
 * Provides the context for an <code>IResourceMappingMerger</code>.
 * It provides access to the ancestor and remote resource mapping contexts
 * so that resource mapping mergers can attempt head-less auto-merges.
 * The ancestor context is only required for merges while the remote
 * is required for both merge and replace.
 * 
 * TODO: Need to have a story for folder merging (see bug 113898)
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceMappingMerger
 * @since 3.2
 */
public abstract class MergeContext extends SynchronizationContext implements IMergeContext {

	/**
     * Create a merge context.
	 * @param type 
     */
    protected MergeContext(IResourceMappingScope input, String type, SyncInfoTree tree, DeltaTree deltaTree) {
    	super(input, type, tree, deltaTree);
    }

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
	 * @see SyncInfoSet#addSyncSetChangedListener(ISyncInfoSetChangeListener)
	 * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 * 
	 * @param infos
	 *            the array of sync info to be merged
	 * @param monitor
	 *            a progress monitor
	 * @return a status indicating success or failure. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that the file
	 *         contain non-mergable conflicts and must be merged manually.
     * @throws CoreException if an error occurs
	 */
    public IStatus merge(ISyncInfoSet infos, IProgressMonitor monitor) throws CoreException {
		List failedFiles = new ArrayList();
		for (Iterator iter = infos.iterator(); iter.hasNext();) {
			SyncInfo info = (SyncInfo) iter.next();
			IStatus s = merge(info, monitor);
			if (!s.isOK()) {
				if (s.getCode() == IMergeStatus.CONFLICTS) {
					failedFiles.addAll(Arrays.asList(((IMergeStatus)s).getConflictingFiles()));
				} else {
					return s;
				}
			}
		}
		if (failedFiles.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			return new MergeStatus(TeamPlugin.ID, "Could not merge all files", (IFile[]) failedFiles.toArray(new IFile[failedFiles.size()]));
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.mapping.IMergeContext#merge(org.eclipse.team.core.delta.ISyncDelta[], boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus merge(IDiffNode[] deltas, boolean force, IProgressMonitor monitor) throws CoreException {
		List failedFiles = new ArrayList();
		for (int i = 0; i < deltas.length; i++) {
			IDiffNode delta = deltas[i];
			IStatus s = merge(delta, force, monitor);
			if (!s.isOK()) {
				if (s.getCode() == IMergeStatus.CONFLICTS) {
					failedFiles.addAll(Arrays.asList(((IMergeStatus)s).getConflictingFiles()));
				} else {
					return s;
				}
			}
		}
		if (failedFiles.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			return new MergeStatus(TeamPlugin.ID, "Could not merge all files", (IFile[]) failedFiles.toArray(new IFile[failedFiles.size()]));
		}
    }
    
    /**
	 * @param delta
	 * @param monitor
	 * @return
	 */
	public IStatus merge(IDiffNode delta, boolean force, IProgressMonitor monitor) {
		if (!isFileDiff(delta))
			return Status.OK_STATUS;
        try {
        	if (delta instanceof IThreeWayDiff && !force) {
				IThreeWayDiff twDelta = (IThreeWayDiff) delta;
	        	int direction = twDelta.getDirection();
	    		if (direction == IThreeWayDiff.OUTGOING) {
	        		// There's nothing to do so return OK
	        		return Status.OK_STATUS;
	        	}
	    		if (direction == IThreeWayDiff.INCOMING) {
	    			// Just copy the stream since there are no conflicts
	    			return performReplace(delta, monitor);
	    		}
				// direction == SyncInfo.CONFLICTING
	    		int type = twDelta.getKind();
	    		if (type == IDiffNode.REMOVED) {
	    			// TODO: either we need to spec mark as merged to work in this case
	    			// or somehow involve the subclass (or just ignore it)
	    			markAsMerged(getLocalFile(delta), false, monitor);
	    			return Status.OK_STATUS;
	    		}
				// type == SyncInfo.CHANGE
				ITwoWayDiff remoteChange = twDelta.getRemoteChange();
				IResourceVariant base = null;
	        	IResourceVariant remote = null;
	        	if (remoteChange != null) {
					base = (IResourceVariant)remoteChange.getBeforeState();
		        	remote = (IResourceVariant)remoteChange.getAfterState();
	        	}
				if (base == null || remote == null || !getLocalFile(delta).exists()) {
					// Nothing we can do so return a conflict status
					// TODO: Should we handle the case where the local and remote have the same contents for a conflicting addition?
					return new MergeStatus(TeamPlugin.ID, NLS.bind("Conflicting change could not be merged: {0}", new String[] { delta.getPath().toString() }), new IFile[] { getLocalFile(delta) });
				}
				// We have a conflict, a local, base and remote so we can do 
				// a three-way merge
	            return performThreeWayMerge(twDelta, monitor);
        	} else {
        		return performReplace(delta, monitor);
        	}
        } catch (CoreException e) {
            return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Merge of {0} failed due to an internal error.", new String[] { delta.getPath().toString() }), e);
        }
	}

	/**
	 * Perform a three-way merge on the given trhee-way delta that contains a content conflict.
	 * @param delta the delta
	 * @param monitor a progress monitor
	 * @return a status indicating the results of the merge
	 */
	protected abstract IStatus performThreeWayMerge(IThreeWayDiff delta, IProgressMonitor monitor) throws CoreException;

	private IFile getLocalFile(IDiffNode delta) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getPath());
	}

	/**
     * Method that can be called by the model merger to attempt a file level
     * merge. This is useful for cases where the model merger does not need to
     * do any special processing to perform the merge. By default, this method
     * attempts to use an appropriate <code>IStreamMerger</code> to perform the
     * merge. If a stream merger cannot be found, the text merger is used. If this behavior
     * is not desired, sub-classes may override this method.
     * 
     * @param file the file to be merged
     * @param monitor a progress monitor
     * @return a status indicating success or failure. A code of
     *         <code>MergeStatus.CONFLICTS</code> indicates that the file contain
     *         non-mergable conflicts and must be merged manually.
     * @see org.eclipse.team.core.mapping.MergeContext#merge(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus merge(SyncInfo info, IProgressMonitor monitor) {
		IResource r = info.getLocal();
		if (r.getType() != IResource.FILE)
			return Status.OK_STATUS;
		IFile file = (IFile)r;
        try {
        	if (info.getComparator().isThreeWay()) {
	        	int direction = SyncInfo.getDirection(info.getKind());
	    		if (direction == SyncInfo.OUTGOING) {
	        		// There's nothing to do so return OK
	        		return Status.OK_STATUS;
	        	}
	    		if (direction == SyncInfo.INCOMING) {
	    			// Just copy the stream since there are no conflicts
	    			return performReplace(SyncInfoToDeltaConverter.getDeltaFor(info), monitor);
	    		}
				// direction == SyncInfo.CONFLICTING
	    		int type = SyncInfo.getChange(info.getKind());
	    		if (type == SyncInfo.DELETION) {
	    			// Nothing needs to be done although subclasses
	    			markAsMerged(file, false, monitor);
	    			return Status.OK_STATUS;
	    		}
				// type == SyncInfo.CHANGE
				IResourceVariant base = info.getBase();
	        	IResourceVariant remote = info.getRemote();
				if (base == null || remote == null || !file.exists()) {
					// Nothing we can do so return a conflict status
					// TODO: Should we handle the case where the local and remote have the same contents for a conflicting addition?
					return new MergeStatus(TeamPlugin.ID, NLS.bind("Conflicting change could not be merged: {0}", new String[] { file.getFullPath().toString() }), new IFile[] { file });
				}
				// We have a conflict, a local, base and remote so we can do 
				// a three-way merge
	            return performThreeWayMerge((IThreeWayDiff)SyncInfoToDeltaConverter.getDeltaFor(info), monitor);
        	} else {
        		return performReplace(SyncInfoToDeltaConverter.getDeltaFor(info), monitor);
        	}
        } catch (CoreException e) {
            return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Merge of {0} failed due to an internal error.", new String[] { file.getFullPath().toString() }), e);
        }
    }

    /*
     * Replace the local contents with the remote contents.
     * The local resource must be a file.
     */
    private IStatus performReplace(IDiffNode delta, IProgressMonitor monitor) throws CoreException {
    	ITwoWayDiff d;
    	if (delta instanceof ITwoWayDiff) {
			d = (ITwoWayDiff) delta;
		} else {
			d = ((IThreeWayDiff)delta).getRemoteChange();
		}
    	if (d == null)
    		return Status.OK_STATUS;
    	IFile file = getLocalFile(d);
    	IResourceVariant remote = (IResourceVariant)d.getAfterState();
    	if (remote == null && file.exists()) {
    		file.delete(false, true, monitor);
    	} else if (remote != null) {
    		InputStream stream = remote.getStorage(monitor).getContents();
    		stream = new BufferedInputStream(stream);
    		try {
	    		if (file.exists()) {
	    			file.setContents(stream, false, true, monitor);
	    		} else {
	    			file.create(stream, false, monitor);
	    		}
    		} finally {
    			try {
					stream.close();
				} catch (IOException e) {
					// Ignore
				}
    		}
    	}
    	// Performing a replace should leave the file in-sync
    	markAsMerged(file, true, monitor);
		return Status.OK_STATUS;
	}
}
