/*
 * Created on Jun 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.*;

/**
 * @author JLemieux
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CVSMergeSyncInfo extends CVSSyncInfo {

	public CVSMergeSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote, Subscriber subscriber) {
		super(local, base, remote, subscriber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncInfo#handleDeletionConflicts(int)
	 */
	protected int handleDeletionConflicts(int kind) {
		// (see bug 40053).
		if(kind == (SyncInfo.CONFLICTING | SyncInfo.DELETION | SyncInfo.PSEUDO_CONFLICT)) {
			return SyncInfo.IN_SYNC;
		}
		return kind;
	}

	protected int calculateKind() throws TeamException {
		// Report merged resources as in-sync
		if (((CVSMergeSubscriber)getSubscriber()).isMerged(getLocal())) {
			return IN_SYNC;
		}
		
		int kind = super.calculateKind();
		
		// Report outgoing resources as in-sync
		if((kind & DIRECTION_MASK) == OUTGOING) {
			return IN_SYNC;
		}
		
		return kind;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncInfo#makeOutgoing(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus makeOutgoing(IProgressMonitor monitor) throws TeamException {
		// Make the resource outgoing by marking it as merged with the subscriber
		CVSMergeSubscriber subscriber = (CVSMergeSubscriber)getSubscriber();
		subscriber.merged(new IResource[] {getLocal() });
		return Status.OK_STATUS;
	}
}
