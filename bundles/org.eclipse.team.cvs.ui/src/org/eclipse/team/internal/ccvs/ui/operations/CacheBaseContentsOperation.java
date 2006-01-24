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
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileState;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfoFilter.ContentComparisonSyncInfoFilter;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation that ensures that the contents for base
 * of each local resource is cached.
 */
public class CacheBaseContentsOperation extends CacheTreeContentsOperation {

	private final boolean includeOutgoing;

	public CacheBaseContentsOperation(IWorkbenchPart part, ResourceMapping[] mappings, IResourceDiffTree tree, boolean includeOutgoing) {
		super(part, mappings, tree);
		this.includeOutgoing = includeOutgoing;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#getRemoteFileState(org.eclipse.team.core.diff.IThreeWayDiff)
	 */
	protected IFileState getRemoteFileState(IThreeWayDiff twd) {
		IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
		if (diff == null)
			diff = (IResourceDiff)twd.getLocalChange();
		IFileState base = diff.getBeforeState();
		return base;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#isEnabledForDirection(int)
	 */
	protected boolean isEnabledForDirection(int direction) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CacheTreeContentsOperation#buildTree(org.eclipse.team.internal.ccvs.core.CVSTeamProvider)
	 */
	protected ICVSRemoteResource buildTree(CVSTeamProvider provider) throws TeamException {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().buildBaseTree(provider.getProject(), true, new NullProgressMonitor());
	}
	
	protected void execute(CVSTeamProvider provider, IResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		IResource[] localChanges = getFilesWithLocalChanges(resources, recurse);
		super.execute(provider, resources, recurse, monitor);
		// Now that the contents are cached, reset the timestamps for any false local changes
		if (localChanges.length > 0) {
			performCleanTimestamps(localChanges[0].getProject(), localChanges, monitor);
		}
	}
	
	private IResource[] getFilesWithLocalChanges(IResource[] resources, boolean recurse) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			IDiffNode[] nodes = getTree().getDiffs(resource, recurse ? IResource.DEPTH_INFINITE: IResource.DEPTH_ONE);
			for (int j = 0; j < nodes.length; j++) {
				IDiffNode node = nodes[j];
				if (isFileWithLocalChange(node)) {
					result.add(getTree().getResource(node));
				}
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	private boolean isFileWithLocalChange(IDiffNode node) {
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			return (twd.getDirection() == IThreeWayDiff.OUTGOING || twd.getDirection() == IThreeWayDiff.CONFLICTING)
				&& getTree().getResource(node).getType() == IResource.FILE;
		}
		return false;
	}
	
	private void performCleanTimestamps(IProject project, final IResource[] resources, IProgressMonitor monitor) throws CVSException {
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(project);
        final ContentComparisonSyncInfoFilter comparator = new SyncInfoFilter.ContentComparisonSyncInfoFilter(false);
		folder.run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				monitor.beginTask(null, resources.length * 100);
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					if (resource.getType() == IResource.FILE) {
						if (comparator.compareContents((IFile)resource, (IResourceVariant)CVSWorkspaceRoot.getRemoteResourceFor(resource), Policy.subMonitorFor(monitor, 100))) {
							ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
							cvsFile.checkedIn(null, false /* not a commit */);
						}
					}
				}
				monitor.done();
			}
		}, Policy.subMonitorFor(monitor, 100));
	}

}
