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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.ui.sync.SyncInfoSet;

/**
 * This action performs an update for any CVSSyncTreeSubscriber.
 * Warning: This action will operate on any out-of-sync resource.
 * For non-automergable conflicts and outgoing changes, this action
 * will repace the local resource with the remote resources (deleting
 * the local if there is no remote). It is up to the subclass to 
 * ensure that only suitable nodes are in the sync set.
 */
public abstract class SubscriberUpdateAction extends CVSSubscriberAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#getFilteredSyncInfoSet(org.eclipse.team.internal.ui.sync.views.SyncInfo[])
	 */
	protected SyncInfoSet getFilteredSyncInfoSet(SyncInfo[] selectedResources) {
		SyncInfoSet syncSet = super.getFilteredSyncInfoSet(selectedResources);
		if (!performPrompting(syncSet)) return null;
		return syncSet;
	}
	
	/**
	 * Perform appropriate prompting given the elements in the sync set. 
	 * Elements can be removed from the sync set based on user input.
	 * Returning false will cancel the operation. By default, no
	 * prompting is performed and true is returned.
	 *  
	 * @param syncSet
	 * @return
	 */
	protected boolean performPrompting(SyncInfoSet syncSet) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#run(org.eclipse.team.ui.sync.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException {
	
		SyncInfo[] changed = syncSet.getSyncInfos();
		if (changed.length == 0) return;
		
		// The list of sync resources to be updated using "cvs update"
		List updateShallow = new ArrayList();
		// A list of sync resource folders which need to be created locally 
		// (incoming addition or previously pruned)
		Set parentCreationElements = new HashSet();
		// A list of sync resources that are incoming deletions.
		// We do these first to avoid case conflicts
		List updateDeletions = new ArrayList();
		// A list of sync resources that need to be unmanaged and locally deleted
		// Note: This list is also used to unmanaged outgoing deletions
		// and to remove conflict local changes when override conflicts is chosen
		List deletions = new ArrayList();
	
		for (int i = 0; i < changed.length; i++) {
			SyncInfo changedNode = changed[i];
			
			// Make sure that parent folders exist
			SyncInfo parent = getParent(changedNode);
			if (parent != null && isOutOfSync(parent)) {
				// We need to ensure that parents that are either incoming folder additions
				// or previously pruned folders are recreated.
				parentCreationElements.add(parent);
			}
			
			IResource resource = changedNode.getLocal();
			int kind = changedNode.getKind();
			if (resource.getType() == IResource.FILE) {
				// add the file to the list of files to be updated
				updateShallow.add(changedNode);
				
				// Not all change types will require a "cvs update"
				// Some can be deleted locally without performing an update
				switch (kind & SyncInfo.DIRECTION_MASK) {
					case SyncInfo.INCOMING:
						switch (kind & SyncInfo.CHANGE_MASK) {
							case SyncInfo.DELETION:
								// Incoming deletions can just be deleted instead of updated
								updateDeletions.add(changedNode);
								updateShallow.remove(changedNode);
								break;
						}
						break;
					case SyncInfo.OUTGOING:
						// outgoing changes can be deleted before the update
						deletions.add(changedNode);
						switch (kind & SyncInfo.CHANGE_MASK) {
							case SyncInfo.ADDITION:
								// an outgoing addition does not need an update
								updateShallow.remove(changedNode);
								break;
						}
						break;
					case SyncInfo.CONFLICTING:
						//	conflicts can be deleted before the update
						deletions.add(changedNode);	
						switch (kind & SyncInfo.CHANGE_MASK) {
							case SyncInfo.DELETION:
								// conflicting deletions do not need an update
								updateShallow.remove(changedNode);
								break;
							case SyncInfo.CHANGE:
								// some conflicting changes can be handled by an update
								// (e.g. automergable)
								if (supportsShallowUpdateFor(changedNode)) {
									// Don't delete the local resource since the
									// action can accomodate the shallow update
									deletions.remove(changedNode);
								}
								break;
						}
						break;
				}
			} else {
				// Special handling for folders to support shallow operations on files
				// (i.e. folder operations are performed using the sync info already
				// contained in the sync info.
				if (isOutOfSync(changedNode)) {
					parentCreationElements.add(changedNode);
				} else if (((kind & SyncInfo.DIRECTION_MASK) == SyncInfo.OUTGOING)
						&& ((kind & SyncInfo.CHANGE_MASK) == SyncInfo.ADDITION)) {
					// The folder is an outgoing addition which is being overridden
					// Add it to the list of resources to be deleted
					deletions.add(changedNode);
				}
			}

		}
		try {
			// Calculate the total amount of work needed
			int work = (deletions.size() + updateDeletions.size() + updateShallow.size()) * 100;
			monitor.beginTask(null, work);

			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();

			// TODO: non of the work should be done until after the connection to
			// the repository is made
			// TODO: deleted files that are also being updated should be written to 
			// a backup file in case the update fails. The backups could be purged after
			// the update succeeds.
			if (parentCreationElements.size() > 0) {
				makeInSync((SyncInfo[]) parentCreationElements.toArray(new SyncInfo[parentCreationElements.size()]));				
			}
			if (deletions.size() > 0) {
				runLocalDeletions((SyncInfo[])deletions.toArray(new SyncInfo[deletions.size()]), manager, Policy.subMonitorFor(monitor, deletions.size() * 100));
			}
			if (updateDeletions.size() > 0) {
				runUpdateDeletions((SyncInfo[])updateDeletions.toArray(new SyncInfo[updateDeletions.size()]), manager, Policy.subMonitorFor(monitor, updateDeletions.size() * 100));
			}			
			if (updateShallow.size() > 0) {
				runUpdateShallow((SyncInfo[])updateShallow.toArray(new SyncInfo[updateShallow.size()]), manager, Policy.subMonitorFor(monitor, updateShallow.size() * 100));
			}
		} catch (final TeamException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
		return;
	}

	/**
	 * Method which indicates whether a shallow update will work for the given
	 * node which is a conflicting change. The default is to return false. 
	 * 
	 * @param changedNode
	 * @return
	 */
	protected boolean supportsShallowUpdateFor(SyncInfo changedNode) {
		return false;
	}

	/**
	 * @param element
	 */
	protected void unmanage(SyncInfo element, IProgressMonitor monitor) throws CVSException {
		CVSWorkspaceRoot.getCVSResourceFor(element.getLocal()).unmanage(monitor);
		
	}

	/**
	 * Method deleteAndKeepHistory.
	 * @param iResource
	 * @param iProgressMonitor
	 */
	protected void deleteAndKeepHistory(IResource resource, IProgressMonitor monitor) throws CVSException {
		try {
			if (!resource.exists()) return;
			if (resource.getType() == IResource.FILE)
				((IFile)resource).delete(false /* force */, true /* keep history */, monitor);
			else if (resource.getType() == IResource.FOLDER)
				((IFolder)resource).delete(false /* force */, true /* keep history */, monitor);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	protected void runLocalDeletions(SyncInfo[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		monitor.beginTask(null, nodes.length * 100);
		for (int i = 0; i < nodes.length; i++) {
			SyncInfo node = nodes[i];
			unmanage(node, Policy.subMonitorFor(monitor, 50));
			deleteAndKeepHistory(node.getLocal(), Policy.subMonitorFor(monitor, 50));
		}
		pruneEmptyParents(nodes);
		monitor.done();
	}

	protected void runUpdateDeletions(SyncInfo[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// As an optimization, perform the deletions locally
		runLocalDeletions(nodes, manager, monitor);
	}

	protected void runUpdateShallow(SyncInfo[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// TODO: Should use custom update which skips non-automergable conflicts
		manager.update(getIResourcesFrom(nodes), new Command.LocalOption[] { Command.DO_NOT_RECURSE }, false, monitor);
	}
	
	protected IResource[] getIResourcesFrom(SyncInfo[] nodes) {
		List resources = new ArrayList(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			resources.add(nodes[i].getLocal());
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	/**
	 * Prompt for non-automergeable conflicts.
	 * Note: This method is designed to be overridden by test cases.
	 * @return false to cancel, true to overwrite local changes
	 */
	protected boolean promptForConflicts() {
		final boolean[] result = new boolean[] { false };
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = MessageDialog.openQuestion(shell, Policy.bind("UpdateSyncAction.Overwrite_local_changes__5"), Policy.bind("UpdateSyncAction.You_have_local_changes_you_are_about_to_overwrite._Do_you_wish_to_continue__6")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		return result[0];
	}

	protected String getErrorTitle() {
		return Policy.bind("UpdateAction.update"); //$NON-NLS-1$
	}
}
