/*
 * Created on Jun 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * @author JLemieux
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CVSMergeSyncInfo extends CVSSyncInfo {

	protected int calculateKind(IProgressMonitor progress) throws TeamException {
		// Report merged resources as in-sync
		if (((CVSMergeSubscriber)getSubscriber()).isMerged(getLocal())) {
			return IN_SYNC;
		}
		
		int kind = super.calculateKind(progress);
		
		// Report outgoing resources as in-sync
		if((kind & DIRECTION_MASK) == OUTGOING) {
			return IN_SYNC;
		}
		
		return kind;
	}

	public CVSMergeSyncInfo(IResource local, IRemoteResource base, IRemoteResource remote, TeamSubscriber subscriber, IProgressMonitor monitor) throws TeamException {
		super(local, base, remote, subscriber, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.CVSSyncInfo#makeOutgoing(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void makeOutgoing(IProgressMonitor monitor) throws TeamException {
		// Make the resource outgoing by marking it as merged with the subscriber
		CVSMergeSubscriber subscriber = (CVSMergeSubscriber)getSubscriber();
		subscriber.merged(new IResource[] {getLocal() });
	}
}
