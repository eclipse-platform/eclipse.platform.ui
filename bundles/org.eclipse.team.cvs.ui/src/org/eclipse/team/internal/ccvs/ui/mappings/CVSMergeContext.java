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
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfoFilter.ContentComparisonSyncInfoFilter;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.PruneFolderVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.core.diff.ResourceDiffTree;
import org.eclipse.team.internal.core.diff.SyncInfoToDiffConverter;
import org.eclipse.team.ui.operations.MergeContext;
import org.eclipse.team.ui.synchronize.ResourceScope;

public class CVSMergeContext extends MergeContext {
	
	private WorkspaceSynchronizeParticipant participant;
	private final SyncInfoToDiffConverter converter;

	public static IMergeContext createContext(IResourceMappingScope scope, IProgressMonitor monitor) {
		WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(new ResourceScope(scope.getRoots()));
		participant.refreshNow(participant.getResources(), NLS.bind("Preparing to merge {0}", new String[] { "TODO: mapping description for CVS merge context initialization" }), monitor);
		ResourceDiffTree tree = new ResourceDiffTree();
		SyncInfoToDiffConverter converter = new SyncInfoToDiffConverter(participant.getSyncInfoSet(), tree);
		converter.connect(monitor);
		participant.getSubscriberSyncInfoCollector().waitForCollector(monitor);
		return new CVSMergeContext(THREE_WAY, participant, scope, converter);
	}
	
	protected CVSMergeContext(String type, WorkspaceSynchronizeParticipant participant, IResourceMappingScope input, SyncInfoToDiffConverter converter) {
		super(input, type, participant.getSyncInfoSet(), converter.getTree());
		this.participant = participant;
		this.converter = converter;
	}

	public void markAsMerged(final IDiffNode node, final boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// Get the latest sync info for the file (i.e. not what is in the set).
				// We do this because the client may have modified the file since the
				// set was populated.
				IResource resource = getDiffTree().getResource(node);
				if (resource.getType() != IResource.FILE)
					return;
				SyncInfo info = getSyncInfo(resource);
				if (info instanceof CVSSyncInfo) {
					CVSSyncInfo cvsInfo = (CVSSyncInfo) info;		
					cvsInfo.makeOutgoing(monitor);
					if (inSyncHint) {
						// Compare the contents of the file with the remote
						// and make the file in-sync if they match
						ContentComparisonSyncInfoFilter comparator = new SyncInfoFilter.ContentComparisonSyncInfoFilter(false);
						if (resource.getType() == IResource.FILE && info.getRemote() != null) {
							if (comparator.compareContents((IFile)resource, info.getRemote(), Policy.subMonitorFor(monitor, 100))) {
								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
								cvsFile.checkedIn(null, false /* not a commit */);
							}
						}
					}
				}
			}
		}, getMergeRule(node), IResource.NONE, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.MergeContext#merge(org.eclipse.team.core.diff.IDiffNode, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus merge(IDiffNode delta, boolean force, IProgressMonitor monitor) throws CoreException {
		IStatus status = super.merge(delta, force, monitor);
		if (status.isOK() && delta.getKind() == IDiffNode.REMOVED) {
			IResource resource = getDiffTree().getResource(delta);
			if (resource.getType() == IResource.FILE && !resource.exists()) {
				// TODO: This behavior is specific to an update from the same branch
				ICVSResource localResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
				localResource.unmanage(monitor);
			}
			pruneEmptyParents(new IDiffNode[] { delta });
		}
		return status;
	}
	
	private void pruneEmptyParents(IDiffNode[] deltas) throws CVSException {
		// TODO: A more explicit tie in to the pruning mechanism would be preferable.
		// i.e. I don't like referencing the option and visitor directly
		if (!CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) return;
		ICVSResource[] cvsResources = new ICVSResource[deltas.length];
		for (int i = 0; i < cvsResources.length; i++) {
			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(getDiffTree().getResource(deltas[i]));
		}
		new PruneFolderVisitor().visit(
			CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()),
			cvsResources);
	}
	
	public void dispose() {
		converter.dispose();
		participant.dispose();
		super.dispose();
	}

	public SyncInfo getSyncInfo(IResource resource) throws CoreException {
		return participant.getSubscriber().getSyncInfo(resource);
	}

	public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
		// TODO: Shouldn't need to use a scope here
		IResource[] resources = getScope().getRoots();
		participant.refreshNow(resources, "TODO: CVS Merge Context Refresh", monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.MergeContext#run(org.eclipse.core.resources.IWorkspaceRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IWorkspaceRunnable runnable, final ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException {
		super.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				EclipseSynchronizer.getInstance().run(rule, new ICVSRunnable(){
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							runnable.run(monitor);
						} catch (CoreException e) {
							throw CVSException.wrapException(e);
						}
					}
				}, monitor);
			}
		
		}, rule, flags, monitor);
		// TODO: wait for the collector so that clients will have an up-to-date diff tree
		participant.getSubscriberSyncInfoCollector().waitForCollector(monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.MergeContext#getMergeRule(org.eclipse.core.resources.IResource)
	 */
	public ISchedulingRule getMergeRule(IDiffNode node) {
		// Return the project since that is what the EclipseSynchronize needs
		return getDiffTree().getResource(node).getProject();
	}

}
