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

import java.util.Date;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.PruneFolderVisitor;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.delta.DeltaTree;
import org.eclipse.team.internal.core.delta.SyncInfoToDeltaConverter;
import org.eclipse.team.ui.operations.MergeContext;
import org.eclipse.team.ui.synchronize.ResourceScope;

public class CVSMergeContext extends MergeContext {
	
	private WorkspaceSynchronizeParticipant participant;
	private final SyncInfoToDeltaConverter converter;

	public static IMergeContext createContext(IResourceMappingScope scope, IProgressMonitor monitor) {
		WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(new ResourceScope(scope.getRoots()));
		participant.refreshNow(participant.getResources(), NLS.bind("Preparing to merge {0}", new String[] { "TODO: mapping description for CVS merge context initialization" }), monitor);
		DeltaTree tree = new DeltaTree();
		SyncInfoToDeltaConverter converter = new SyncInfoToDeltaConverter(participant.getSyncInfoSet(), tree);
		converter.connect(monitor);
		participant.getSubscriberSyncInfoCollector().waitForCollector(monitor);
		return new CVSMergeContext(THREE_WAY, participant, scope, converter);
	}
	
	protected CVSMergeContext(String type, WorkspaceSynchronizeParticipant participant, IResourceMappingScope input, SyncInfoToDeltaConverter converter) {
		super(input, type, participant.getSyncInfoSet(), converter.getTree());
		this.participant = participant;
		this.converter = converter;
	}

	public IStatus markAsMerged(IFile file, boolean inSyncHint, IProgressMonitor monitor) {
		try {
			// Get the latest sync info for the file (i.e. not what is in the set).
			// We do this because the client may have modified the file since the
			// set was populated.
			SyncInfo info = getSyncInfo(file);
			if (info instanceof CVSSyncInfo) {
				CVSSyncInfo cvsInfo = (CVSSyncInfo) info;		
				cvsInfo.makeOutgoing(monitor);
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Merge of {0} failed due to an internal error.", new String[] { file.getFullPath().toString() }), e); //$NON-NLS-1$
		}
	}
	
	public IStatus merge(SyncInfo info, IProgressMonitor monitor) {

		int direction = SyncInfo.getDirection(info.getKind());
		boolean isIncoming = (direction == SyncInfo.INCOMING);
		
		// For folders, just make them in-sync
		if (info.getLocal().getType() != IResource.FILE) {
			if (isIncoming && info instanceof CVSSyncInfo) {
				CVSSyncInfo syncInfo = (CVSSyncInfo) info;
				try {
					syncInfo.makeInSync();
				} catch (CVSException e) {
					return new CVSStatus(IStatus.ERROR, e.getMessage(), e);
				}
			}
			return Status.OK_STATUS;
		}
		
		// For incoming file changes, make sure the parents exist
		if (isIncoming && info.getRemote() != null && !info.getLocal().exists()) {
			IContainer parent = info.getLocal().getParent();
			try {
				ensureExists(parent);
			} catch (CoreException e) {
				return new CVSStatus(IStatus.ERROR, e.getMessage(), e);
			}
		}

		IStatus statusReturn = super.merge(info, monitor);

		if (statusReturn.isOK() && isIncoming && info.getLocal().getType() == IResource.FILE) {
			try {
				if (info instanceof CVSSyncInfo) {
					CVSSyncInfo syncInfo = (CVSSyncInfo) info;

					int change = SyncInfo.getChange(info.getKind());

					ICVSResource localResource = CVSWorkspaceRoot.getCVSResourceFor(syncInfo.getLocal());

					if (change == SyncInfo.ADDITION || change == SyncInfo.CHANGE) {
						ICVSFile cvsLocalFile = (ICVSFile) localResource;
						ResourceSyncInfo resinfo = new ResourceSyncInfo(syncInfo.getRemote().asBytes());
						Date modTime = resinfo.getTimeStamp();
						cvsLocalFile.setTimeStamp(modTime);
						modTime = cvsLocalFile.getTimeStamp();

						MutableResourceSyncInfo newInfoWithTimestamp = resinfo.cloneMutable();
						newInfoWithTimestamp.setTimeStamp(modTime);
						newInfoWithTimestamp.setTag(cvsLocalFile.getParent().getFolderSyncInfo().getTag());
						cvsLocalFile.setSyncInfo(newInfoWithTimestamp, ICVSFile.CLEAN);

					} else if (change == SyncInfo.DELETION) {
						//clear out sync bytes
						if (!localResource.exists()) {
							localResource.unmanage(monitor);
						}
						pruneEmptyParents(new SyncInfo[] {info} );
					}
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, "CVS Merge Context error", e); //$NON-NLS-1$
			}
		}

		return statusReturn;
	}

	private void pruneEmptyParents(SyncInfo[] nodes) throws CVSException {
		// TODO: A more explicit tie in to the pruning mechanism would be prefereable.
		// i.e. I don't like referencing the option and visitor directly
		if (!CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) return;
		ICVSResource[] cvsResources = new ICVSResource[nodes.length];
		for (int i = 0; i < cvsResources.length; i++) {
			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(nodes[i].getLocal());
		}
		new PruneFolderVisitor().visit(
			CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()),
			cvsResources);
	}
	
	private void ensureExists(IContainer parent) throws CoreException {
		if (!parent.exists()) {
			ensureExists(parent.getParent());
			SyncInfo parentInfo = getSyncInfo(parent);
			if (parentInfo instanceof CVSSyncInfo) {
				CVSSyncInfo syncInfo = (CVSSyncInfo) parentInfo;
				syncInfo.makeInSync();
			}
		}
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

}
