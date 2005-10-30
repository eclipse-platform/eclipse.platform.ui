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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.CVSSyncInfo;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.ui.mapping.IMergeContext;
import org.eclipse.team.ui.mapping.IMergeStatus;
import org.eclipse.team.ui.mapping.IResourceMappingScope;
import org.eclipse.team.ui.operations.MergeContext;

public class CVSMergeContext extends MergeContext {
	
	private WorkspaceSynchronizeParticipant participant;

	public static IMergeContext createContext(IResourceMappingScope scope, IProgressMonitor monitor) {
		WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(scope);
		participant.refreshNow(participant.getResources(), NLS.bind("Preparing to merge {0}", new String[] { "TODO: mapping description for CVS merge context initialization" }), monitor);
		return new CVSMergeContext(THREE_WAY, participant, scope);
	}
	
	protected CVSMergeContext(String type, WorkspaceSynchronizeParticipant participant, IResourceMappingScope input) {
		super(input, type, participant.getSyncInfoSet());
		this.participant = participant;
	}

	public IStatus markAsMerged(IFile file, IProgressMonitor monitor) {
		try {
			SyncInfo info = getSyncInfoTree().getSyncInfo(file);
			if (info instanceof CVSSyncInfo) {
				CVSSyncInfo cvsInfo = (CVSSyncInfo) info;
				IResourceVariant remoteVar = cvsInfo.getRemote();
				if (!file.exists()) {
					if (remoteVar==null){
						//file has been deleted remotely and locally
						return Status.OK_STATUS;
					}
					
					//Don't make outgoing incoming changes as the lack
					//of a local will throw a NPE
					if ((cvsInfo.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.INCOMING){
						return Status.OK_STATUS;
					}
				}
				
				cvsInfo.makeOutgoing(monitor);
			}
			return Status.OK_STATUS;
		} catch (TeamException e) {
			return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, NLS.bind("Merge of {0} failed due to an internal error.", new String[] { file.getFullPath().toString() }), e); //$NON-NLS-1$
		}
	}
	
	public IStatus merge(SyncInfo info, IProgressMonitor monitor) {

		int direction = SyncInfo.getDirection(info.getKind());
		boolean isIncoming = (direction == SyncInfo.INCOMING);

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
					}
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return new Status(IStatus.ERROR, TeamPlugin.ID, IMergeStatus.INTERNAL_ERROR, "CVS Merge Context error", e); //$NON-NLS-1$
			}
		}

		return statusReturn;
	}
	
	public void dispose() {
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
