package org.eclipse.team.core.subscribers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;

/**
 * Describes the relative synchronization of a <b>remote</b>
 * resource and a <b>local</b> resource using a <b>base</b>
 * resource for comparison.
 * <p>
 * Differences between the base and local resources
 * are classified as <b>outgoing changes</b>; if there is
 * a difference, the local resource is considered the
 * <b>outgoing resource</b>.
 * </p>
 * <p>
 * Differences between the base and remote resources
 * are classified as <b>incoming changes</b>; if there is
 * a difference, the remote resource is considered the
 * <b>incoming resource</b>.
 * </p>
 * <p>
 * Differences between the local and remote resources
 * determine the <b>sync status</b>. The sync status does
 * not take into account the common resource.
 * </p>
 * <p>
 * Note that under this parse of the world, a resource
 * can have both incoming and outgoing changes at the
 * same time, but may nevertheless be in sync!
 * <p>
 * [Issue: "Gender changes" are also an interesting aspect...
 * ]
 * </p>
 */
public class SyncInfo implements IAdaptable {
	
	/*====================================================================
	 * Constants defining synchronization types:  
	 *====================================================================*/

	/**
	 * Sync constant (value 0) indicating element is in sync.
	 */
	public static final int IN_SYNC = 0;
	
	/**
	 * Sync constant (value 1) indicating that one side was added.
	 */
	public static final int ADDITION = 1;
	
	/**
	 * Sync constant (value 2) indicating that one side was deleted.
	 */
	public static final int DELETION = 2;
	
	/**
	 * Sync constant (value 3) indicating that one side was changed.
	 */
	public static final int CHANGE = 3;

	/**
	 * Bit mask for extracting the change type.
	 */
	public static final int CHANGE_MASK = CHANGE;
	
	/*====================================================================
	 * Constants defining synchronization direction: 
	 *====================================================================*/
	
	/**
	 * Sync constant (value 4) indicating a change to the local resource.
	 */
	public static final int OUTGOING = 4;
	
	/**
	 * Sync constant (value 8) indicating a change to the remote resource.
	 */
	public static final int INCOMING = 8;
	
	/**
	 * Sync constant (value 12) indicating a change to both the remote and local resources.
	 */
	public static final int CONFLICTING = 12;
	
	/**
	 * Bit mask for extracting the synchronization direction. 
	 */
	public static final int DIRECTION_MASK = CONFLICTING;
	
	/*====================================================================
	 * Constants defining synchronization conflict types:
	 *====================================================================*/
	
	/**
	 * Sync constant (value 16) indication that both the local and remote resources have changed 
	 * relative to the base but their contents are the same. 
	 */
	public static final int PSEUDO_CONFLICT = 16;
	
	/**
	 * Sync constant (value 32) indicating that both the local and remote resources have changed 
	 * relative to the base but their content changes do not conflict (e.g. source file changes on different 
	 * lines). These conflicts could be merged automatically.
	 */
	public static final int AUTOMERGE_CONFLICT = 32;
	
	/**
	 * Sync constant (value 64) indicating that both the local and remote resources have changed relative 
	 * to the base and their content changes conflict (e.g. local and remote resource have changes on 
	 * same lines). These conflicts can only be correctly resolved by the user.
	 */
	public static final int MANUAL_CONFLICT = 64;
	
	/*====================================================================
	 * Members:
	 *====================================================================*/
	 private IResource local;
	 private IRemoteResource base;
	 private IRemoteResource remote;
	 private TeamSubscriber subscriber;
	 
	 private int syncKind;
	
	/**
	 * Construct a sync info object.
	 */
	public SyncInfo(IResource local, IRemoteResource base, IRemoteResource remote, TeamSubscriber subscriber, IProgressMonitor monitor) throws TeamException {
		this.local = local;
		this.base = base;
		this.remote = remote;
		this.subscriber = subscriber;
		this.syncKind = calculateKind(monitor);
	}
	
	/**
	 * Returns the state of the local resource. Note that the
	 * resource may or may not exist.
	 *
	 * @return a resource
	 */
	public IResource getLocal() {
		return local;
	}
		
	/**
	 * Returns the remote resource handle  for the base resource,
	 * or <code>null</code> if the base resource does not exist.
	 * <p>
	 * [Note: The type of the common resource may be different from the types
	 * of the local and remote resources.
	 * ]
	 * </p>
	 *
	 * @return a remote resource handle, or <code>null</code>
	 */
	public IRemoteResource getBase() {
		return base;
	}
	
	/**
	 * Returns the handle for the remote resource,
	 * or <code>null</code> if the remote resource does not exist.
	 * <p>
	 * [Note: The type of the remote resource may be different from the types
	 * of the local and common resources.
	 * ]
	 * </p>
	 *
	 * @return a remote resource handle, or <code>null</code>
	 */
	public IRemoteResource getRemote() {
		return remote;
	}
	
	/**
	 * Returns the subscriber that created and maintains this sync info
	 * object. 
	 */
	public TeamSubscriber getSubscriber() {
		return subscriber;
	}
	
	/**
	 * Returns the kind of synchronization for this node. 
	 * @return
	 */
	public int getKind() {
		return syncKind;
	}
	
	static public boolean isInSync(int kind) {
		return kind == IN_SYNC;
	}
	
	static public int getDirection(int kind) {
		return kind & DIRECTION_MASK;
	}
		
	static public int getChange(int kind) {
		return kind & CHANGE_MASK;
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if(other instanceof SyncInfo) {
			return getLocal().equals(((SyncInfo)other).getLocal());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class) {
			return getLocal();
		}
		return null;
	}
	
	public String toString() {
		return getLocal().getName() + " " + kindToString(getKind()); //$NON-NLS-1$
	}
	
	public static String kindToString(int kind) {
		String label = ""; //$NON-NLS-1$
		if(kind==IN_SYNC) {
			label = Policy.bind("RemoteSyncElement.insync"); //$NON-NLS-1$
		} else {
			switch(kind & DIRECTION_MASK) {
				case CONFLICTING: label = Policy.bind("RemoteSyncElement.conflicting"); break; //$NON-NLS-1$
				case OUTGOING: label = Policy.bind("RemoteSyncElement.outgoing"); break; //$NON-NLS-1$
				case INCOMING: label = Policy.bind("RemoteSyncElement.incoming"); break; //$NON-NLS-1$
			}	
			switch(kind & CHANGE_MASK) {
				case CHANGE: label = Policy.bind("concatStrings", label, Policy.bind("RemoteSyncElement.change")); break; //$NON-NLS-1$ //$NON-NLS-2$
				case ADDITION: label = Policy.bind("concatStrings", label, Policy.bind("RemoteSyncElement.addition")); break; //$NON-NLS-1$ //$NON-NLS-2$
				case DELETION: label = Policy.bind("concatStrings", label, Policy.bind("RemoteSyncElement.deletion")); break; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if((kind & MANUAL_CONFLICT) != 0) {			
				label = Policy.bind("concatStrings", label, Policy.bind("RemoteSyncElement.manual")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if((kind & AUTOMERGE_CONFLICT) != 0) {				
				label = Policy.bind("concatStrings", label, Policy.bind("RemoteSyncElement.auto")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return Policy.bind("RemoteSyncElement.delimit", label); //$NON-NLS-1$
	}
	
	protected int calculateKind(IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		int description = IN_SYNC;
		
		ComparisonCriteria criteria = subscriber.getCurrentComparisonCriteria();
		
		boolean localExists = local.exists();
		
		if (subscriber.isThreeWay()) {
			if (base == null) {
				if (remote == null) {
					if (!localExists) {						
						description = IN_SYNC;
					} else {
						description = OUTGOING | ADDITION;
					}
				} else {
					if (!localExists) {
						description = INCOMING | ADDITION;
					} else {
						description = CONFLICTING | ADDITION;
						try {
							progress.beginTask(null, 60);
							if (criteria.compare(local, remote, Policy.subMonitorFor(progress, 30))) {
								description |= PSEUDO_CONFLICT;
							}
						} finally {
							progress.done();
						}
					}
				}
			} else {
				if (!localExists) {
					if (remote == null) {
						description = CONFLICTING | DELETION | PSEUDO_CONFLICT;
					} else {
						if (criteria.compare(base, remote, progress))
							description = OUTGOING | DELETION;
						else						
							description = CONFLICTING | CHANGE;
					}
				} else {
					if (remote == null) {
						if (criteria.compare(local, base, progress))
							description = INCOMING | DELETION;
						else
							description = CONFLICTING | CHANGE;
					} else {
						progress.beginTask(null, 90);
						boolean ay = criteria.compare(local, base, Policy.subMonitorFor(progress, 30));
						boolean am = criteria.compare(base, remote, Policy.subMonitorFor(progress, 30));
						if (ay && am) {
							;
						} else if (ay && !am) {
							description = INCOMING | CHANGE;
						} else if (!ay && am) {
							description = OUTGOING | CHANGE;
						} else {
							if(! criteria.compare(local, remote, Policy.subMonitorFor(progress, 30))) {
								description = CONFLICTING | CHANGE;
							}
						}
						progress.done();
					}
				}
			}
		} else { // two compare without access to base contents
			if (remote == null) {
				if (!localExists) {
					Assert.isTrue(false);
					// shouldn't happen
				} else {
					description= DELETION;
				}
			} else {
				if (!localExists) {
					description= ADDITION;
				} else {
					if (! criteria.compare(local, remote, Policy.subMonitorFor(progress, 30)))
						description= CHANGE;
				}
			}
		}
		return description;
	}
}
