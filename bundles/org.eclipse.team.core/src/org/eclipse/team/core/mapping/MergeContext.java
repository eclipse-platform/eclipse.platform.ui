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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Provides the context for an <code>IResourceMappingMerger</code>.
 * It provides access to the ancestor and remote resource mapping contexts
 * so that resource mapping mergers can attempt head-less auto-merges.
 * The ancestor context is only required for merges while the remote
 * is required for both merge and replace.
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
    protected MergeContext(IResourceMappingScope input, String type, SyncInfoTree tree, IResourceDiffTree deltaTree) {
    	super(input, type, tree, deltaTree);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiffNode[], boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void markAsMerged(final IDiffNode[] nodes, final boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < nodes.length; i++) {
					IDiffNode node = nodes[i];
					markAsMerged(node, inSyncHint, monitor);
				}
			}
		}, getMergeRule(nodes), IResource.NONE, monitor);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.mapping.IMergeContext#merge(org.eclipse.team.core.delta.ISyncDelta[], boolean, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus merge(final IDiffNode[] deltas, final boolean force, IProgressMonitor monitor) throws CoreException {
		final List failedFiles = new ArrayList();
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < deltas.length; i++) {
					IDiffNode delta = deltas[i];
					IStatus s = merge(delta, force, monitor);
					if (!s.isOK()) {
						if (s.getCode() == IMergeStatus.CONFLICTS) {
							failedFiles.addAll(Arrays.asList(((IMergeStatus)s).getConflictingFiles()));
						} else {
							throw new CoreException(s);
						}
					}
				}
			}
		}, getMergeRule(deltas), IResource.NONE, monitor);
		if (failedFiles.isEmpty()) {
			return Status.OK_STATUS;
		} else {
			return new MergeStatus(TeamPlugin.ID, "Could not merge all files", (IFile[]) failedFiles.toArray(new IFile[failedFiles.size()]));
		}
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#merge(org.eclipse.team.core.diff.IDiffNode, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus merge(IDiffNode delta, boolean force, IProgressMonitor monitor) throws CoreException {
		if (getDiffTree().getResource(delta).getType() != IResource.FILE)
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
	    			markAsMerged(delta, false, monitor);
	    			return Status.OK_STATUS;
	    		}
				// type == SyncInfo.CHANGE
				IResourceDiff remoteChange = (IResourceDiff)twDelta.getRemoteChange();
				IResourceVariant base = null;
	        	IResourceVariant remote = null;
	        	if (remoteChange != null) {
					base = remoteChange.getBeforeState();
		        	remote = remoteChange.getAfterState();
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

    /*
     * Replace the local contents with the remote contents.
     * The local resource must be a file.
     */
    private IStatus performReplace(IDiffNode delta, IProgressMonitor monitor) throws CoreException {
    	IResourceDiff d;
    	if (delta instanceof IResourceDiff) {
			d = (IResourceDiff) delta;
		} else {
			d = (IResourceDiff)((IThreeWayDiff)delta).getRemoteChange();
		}
    	if (d == null)
    		return Status.OK_STATUS;
    	IFile file = getLocalFile(d);
    	IResourceVariant remote = d.getAfterState();
    	if (remote == null && file.exists()) {
    		file.delete(false, true, monitor);
    	} else if (remote != null) {
    		ensureParentsExist(file);
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
    	markAsMerged(delta, true, monitor);
		return Status.OK_STATUS;
	}

	/**
	 * Ensure that the parent folders of the given resource exist 
	 * @param resource a resource
	 * @throws CoreException 
	 */
	protected void ensureParentsExist(IResource resource) throws CoreException {
		IContainer parent = resource.getParent();
		if (parent.getType() != IResource.FOLDER) {
			// this method will only create folders
			return;
		}
		if (!parent.exists()) {
			ensureParentsExist(parent);
			((IFolder)parent).create(false, true, null);
		}
	}
	
	/**
	 * Default implementation of <code>run</code> that invokes the
	 * corresponding <code>run</code> on {@link IWorkspace}.
	 * @see org.eclipse.team.core.mapping.IMergeContext#run(org.eclipse.core.resources.IWorkspaceRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IWorkspaceRunnable runnable, ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException {
		ResourcesPlugin.getWorkspace().run(runnable, rule, flags, monitor);
	}
	
	/**
	 * Default implementation that returns the resource itself.
	 * Subclass should override to provide the appropriate rule.
	 * @see org.eclipse.team.core.mapping.IMergeContext#getMergeRule(org.eclipse.core.resources.IResource)
	 */
	public ISchedulingRule getMergeRule(IDiffNode node) {
		return getDiffTree().getResource(node);
	}
	
	public ISchedulingRule getMergeRule(IDiffNode[] deltas) {
		ISchedulingRule result = null;
		for (int i = 0; i < deltas.length; i++) {
			IDiffNode node = deltas[i];
			ISchedulingRule rule = getMergeRule(node);
			if (result == null) {
				result = rule;
			} else {
				result = MultiRule.combine(result, rule);
			}
		}
		return result;
	}
}
