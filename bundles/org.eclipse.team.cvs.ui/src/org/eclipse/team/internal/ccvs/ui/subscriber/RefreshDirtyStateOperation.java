/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.synchronize.SyncInfoFilter.ContentComparisonSyncInfoFilter;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.CacheBaseContentsOperation;
import org.eclipse.team.internal.core.mapping.SyncInfoToDiffConverter;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Resets the dirty state of files whose contents match their base.
 */
public class RefreshDirtyStateOperation extends CVSSubscriberOperation {
	
	protected RefreshDirtyStateOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		super(configuration, elements);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberOperation#run(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void run(SyncInfoSet set, IProgressMonitor monitor) throws TeamException {
		final SyncInfo[] infos = set.getSyncInfos();
		if (infos.length == 0) return;
        monitor.beginTask(null, 200);
		IProject project = infos[0].getLocal().getProject();
        ensureBaseContentsCached(project, infos, Policy.subMonitorFor(monitor, 100));
        performCleanTimestamps(project, infos, monitor);
        monitor.done();
	}

	private void performCleanTimestamps(IProject project, final SyncInfo[] infos, IProgressMonitor monitor) throws CVSException {
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
        final ContentComparisonSyncInfoFilter comparator = new SyncInfoFilter.ContentComparisonSyncInfoFilter(false);
		folder.run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				monitor.beginTask(null, infos.length * 100);
				for (int i = 0; i < infos.length; i++) {
					SyncInfo info = infos[i];
					IResource resource = info.getLocal();
					if (resource.getType() == IResource.FILE) {
						if (comparator.compareContents((IFile)resource, info.getBase(), Policy.subMonitorFor(monitor, 100))) {
							ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
							cvsFile.checkedIn(null, false /* not a commit */);
						}
					}
				}
				monitor.done();
			}
		}, Policy.subMonitorFor(monitor, 100));
	}
	
	private void ensureBaseContentsCached(IProject project, SyncInfo[] infos, IProgressMonitor monitor) throws CVSException {
		List diffs = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			IDiffNode node = SyncInfoToDiffConverter.getDeltaFor(info);
			diffs.add(node);
		}
		ensureBaseContentsCached(project, (IDiffNode[]) diffs.toArray(new IDiffNode[diffs.size()]), monitor);
	}

	private void ensureBaseContentsCached(IProject project, IDiffNode[] nodes, IProgressMonitor monitor) throws CVSException {
		try {
			ResourceDiffTree tree = new ResourceDiffTree();
			for (int i = 0; i < nodes.length; i++) {
				IDiffNode node = nodes[i];
				tree.add(node);
			}
			new CacheBaseContentsOperation(getPart(), new ResourceMapping[] { (ResourceMapping)project.getAdapter(ResourceMapping.class) },
					tree, true).run(monitor);
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
    }
    
    protected String getErrorTitle() {
		return CVSUIMessages.RefreshDirtyStateOperation_0; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#getJobName(org.eclipse.team.ui.sync.SyncInfoSet)
	 */
	protected String getJobName() {
		return CVSUIMessages.RefreshDirtyStateOperation_1; 
	}
}
