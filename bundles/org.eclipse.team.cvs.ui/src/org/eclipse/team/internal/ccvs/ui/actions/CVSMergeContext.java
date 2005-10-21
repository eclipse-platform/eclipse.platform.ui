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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSSyncInfo;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.mapping.ResourceMappingScope;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

public class CVSMergeContext extends MergeContext {
	
	private WorkspaceSynchronizeParticipant participant;

	public static IMergeContext createContext(IResourceMappingOperationInput input, IProgressMonitor monitor) {
		WorkspaceSynchronizeParticipant participant = new WorkspaceSynchronizeParticipant(input.asSynchronizationScope());
		participant.refreshNow(participant.getResources(), NLS.bind("Preparing to merge {0}", new String[] { "TODO: mapping description for CVS merge context initialization" }), monitor);
		return new CVSMergeContext(THREE_WAY, participant, input);
	}
	
	protected CVSMergeContext(String type, WorkspaceSynchronizeParticipant participant, IResourceMappingOperationInput input) {
		super(type, participant.getSyncInfoSet(), input);
		this.participant = participant;
	}

	public IStatus markAsMerged(IFile file, IProgressMonitor monitor) {
		try {
			SyncInfo info = getSyncInfoTree().getSyncInfo(file);
			if (info instanceof CVSSyncInfo) {
				CVSSyncInfo cvsInfo = (CVSSyncInfo) info;
				cvsInfo.makeOutgoing(monitor);
			}
			return Status.OK_STATUS;
		} catch (TeamException e) {
			return e.getStatus();
		}
	}
	
	public void dispose() {
		participant.dispose();
		super.dispose();
	}

	public SyncInfo getSyncInfo(IResource resource) throws CoreException {
		return participant.getSubscriber().getSyncInfo(resource);
	}

	public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
		IResource[] resources = new ResourceMappingScope("", getResourceMappings(ALL_MAPPINGS), traversals).getRoots();
		participant.refreshNow(resources, "TODO: CVS Merge Context Refresh", monitor);
	}

	public ISynchronizeScope getScope() {
		return (ResourceMappingScope)participant.getScope();
	}

}
