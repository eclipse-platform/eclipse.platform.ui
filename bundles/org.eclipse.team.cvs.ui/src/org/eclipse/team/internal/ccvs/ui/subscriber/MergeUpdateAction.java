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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.OrSyncInfoFilter;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This action performs a "cvs update -j start -j end ..." to merge changes
 * into the local workspace.
 */
public class MergeUpdateAction extends SafeUpdateAction {
	
	Subscriber currentSubcriber = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SafeUpdateAction#getOverwriteLocalChanges()
	 */
	protected boolean getOverwriteLocalChanges() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		// Update works for all incoming and conflicting nodes
		return new OrSyncInfoFilter(new FastSyncInfoFilter[] {
			new SyncInfoDirectionFilter(SyncInfo.INCOMING),
			new SyncInfoDirectionFilter(SyncInfo.CONFLICTING)
		});
	}
	
	protected void updated(IResource[] resources) throws TeamException {
		// Mark all succesfully updated resources as merged
		if(resources.length > 0 && currentSubcriber != null) {
			((CVSMergeSubscriber)currentSubcriber).merged(resources);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SafeUpdateAction#runUpdateDeletions(org.eclipse.team.internal.ui.sync.views.SyncInfo[], org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void runUpdateDeletions(SyncInfo[] nodes, IProgressMonitor monitor) throws TeamException {
		// When merging, update deletions become outgoing deletions so just delete
		// the files locally without unmanaging (so the sync info is kept to 
		// indicate an outgoing deletion
		try {
			monitor.beginTask(null, 100 * nodes.length);
			for (int i = 0; i < nodes.length; i++) {
				IResource resource = nodes[i].getLocal();
				if (resource.getType() == IResource.FILE) {
					((IFile)resource).delete(false /* force */, true /* keep local history */, Policy.subMonitorFor(monitor, 100));
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#runUpdateShallow(org.eclipse.team.internal.ui.sync.views.SyncInfo[], org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void runSafeUpdate(SyncInfo[] nodes, IProgressMonitor monitor) throws TeamException {
		if(nodes.length > 0) {
			// Assumption that all nodes are from the same subscriber.
			currentSubcriber = ((CVSSyncInfo)nodes[0]).getSubscriber();
			if (!(currentSubcriber instanceof CVSMergeSubscriber)) {
				throw new CVSException(Policy.bind("MergeUpdateAction.invalidSubscriber", currentSubcriber.toString())); //$NON-NLS-1$
			}
			CVSTag startTag = ((CVSMergeSubscriber)currentSubcriber).getStartTag();
			CVSTag endTag = ((CVSMergeSubscriber)currentSubcriber).getEndTag();

			// Incoming additions require different handling then incoming changes and deletions
			List additions = new ArrayList();
			List changes = new ArrayList();
			for (int i = 0; i < nodes.length; i++) {
				SyncInfo resource = nodes[i];
				int kind = resource.getKind();
				if ((kind & SyncInfo.CHANGE_MASK) == SyncInfo.ADDITION) {
					additions.add(resource);
				} else {
					changes.add(resource);
				}
			}
			
			try {
				monitor.beginTask(null, (additions.size() + changes.size()) * 100);
				if (!additions.isEmpty()) {
					safeUpdate(
						getIResourcesFrom((SyncInfo[]) additions.toArray(new SyncInfo[additions.size()])), 
						new Command.LocalOption[] {
							Command.DO_NOT_RECURSE,
							Update.makeArgumentOption(Update.JOIN, endTag.getName()) 
						},
						Policy.subMonitorFor(monitor, additions.size() * 100));
				}
				if (!changes.isEmpty()) {
					safeUpdate(
						getIResourcesFrom((SyncInfo[]) changes.toArray(new SyncInfo[changes.size()])), 
						new Command.LocalOption[] {
							Command.DO_NOT_RECURSE,
							Update.makeArgumentOption(Update.JOIN, startTag.getName()),
							Update.makeArgumentOption(Update.JOIN, endTag.getName()) 
						},
						Policy.subMonitorFor(monitor, changes.size() * 100));
				}
			} finally {
				monitor.done();
			}
		}
	}
	
	/*
	 * @see UpdateSyncAction#runUpdateDeep(IProgressMonitor, List, RepositoryManager)
	 * incoming-change
	 * incoming-deletion
	 */
	protected void overwriteUpdate(SyncInfoSet set, IProgressMonitor monitor) throws TeamException {
		SyncInfo[] nodes = set.getSyncInfos();
		monitor.beginTask(null, 1000 * nodes.length);
		try {
			for (int i = 0; i < nodes.length; i++) {
				makeRemoteLocal(nodes[i], Policy.subMonitorFor(monitor, 1000));
			}
		} finally {
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#getJobName(org.eclipse.team.ui.sync.SyncInfoSet)
	 */
	protected String getJobName(SyncInfoSet syncSet) {
		return Policy.bind("MergeUpdateAction.jobName", new Integer(syncSet.size()).toString()); //$NON-NLS-1$
	}
}
