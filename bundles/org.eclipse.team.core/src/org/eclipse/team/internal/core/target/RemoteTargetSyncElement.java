/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Is a synchronization element that can calculate three-way sync
 * states based on timestamps. This is useful for synchronizing between
 * repositories without revision history and thus the base contents is
 * not available (e.g. non-versioning DAV, FTP...)
 * 
 * @see IRemoteSyncElement
 */
public class RemoteTargetSyncElement extends RemoteSyncElement {

	private IRemoteTargetResource remote;
	private IResource local;
	private TargetProvider provider;

	public RemoteTargetSyncElement(TargetProvider provider, IResource local, IRemoteTargetResource remote) {
		this.local = local;
		this.remote = remote;
		this.provider = provider;	
	}
	
	/**
	 * @see RemoteSyncElement#create(boolean, IResource, IRemoteResource, IRemoteResource, Object)
	 */
	public IRemoteSyncElement create(boolean isThreeWay, IResource local, IRemoteResource base, IRemoteResource remote, Object data) {
		return new RemoteTargetSyncElement(provider, local, (IRemoteTargetResource)remote);
	}

	/**
	 * @see RemoteSyncElement#timestampEquals(IResource, IRemoteResource)
	 */
	protected boolean timestampEquals(IResource e1, IRemoteResource e2) {
		return false;
	}

	/**
	 * @see RemoteSyncElement#timestampEquals(IRemoteResource, IRemoteResource)
	 */
	protected boolean timestampEquals(IRemoteResource e1, IRemoteResource e2) {
		return false;
	}

	/**
	 * @see LocalSyncElement#create(IResource, IRemoteResource, Object)
	 */
	public ILocalSyncElement create(IResource local, IRemoteResource base, Object data) {
		return new RemoteTargetSyncElement(provider, local, (IRemoteTargetResource)base);
	}

	/**
	 * @see LocalSyncElement#getData()
	 */
	protected Object getData() {
		return null;
	}

	/**
	 * @see LocalSyncElement#isIgnored(IResource)
	 */
	protected boolean isIgnored(IResource resource) {
		return false;
	}

	/**
	 * @see IRemoteSyncElement#getRemote()
	 */
	public IRemoteResource getRemote() {
		return remote;
	}

	/**
	 * @see IRemoteSyncElement#isThreeWay()
	 */
	public boolean isThreeWay() {
		return true;
	}

	/**
	 * @see ILocalSyncElement#getLocal()
	 */
	public IResource getLocal() {
		return local;
	}

	/**
	 * @see ILocalSyncElement#getBase()
	 */
	public IRemoteResource getBase() {
		return null;
	}

	/**
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
		progress.beginTask(null, 100);
		int description = IN_SYNC;
		IResource local = getLocal();
		boolean localExists = local.exists();
		boolean hasBase = provider.hasBase(local);
		boolean isOutgoing;
		boolean isIncoming;
		if (hasBase) {
			isOutgoing = provider.isDirty(local);
			isIncoming = isOutOfDate(Policy.subMonitorFor(progress, 10));
		} else {
			// if there's no base, use existance to determine direction
			isOutgoing = localExists;
			isIncoming = remote != null;
		}
		
		if (remote == null) {
			if (!localExists) {
				// this should never happen
				// Assert.isTrue(false);
			} else {
				// no remote but a local
				if (!isOutgoing && isIncoming) {
					description = INCOMING | DELETION;
				} else if (isOutgoing && isIncoming) {
					description = CONFLICTING | CHANGE;
				} else if (!isOutgoing && !isIncoming) {
					description = OUTGOING | ADDITION;
				} else if (isOutgoing && !isIncoming) {
					description = OUTGOING | ADDITION;
				}
			}
		} else {
			if (!localExists) {
				// a remote but no local
				if (!isOutgoing /* and both out of date and not out of date */) {
					description = INCOMING | ADDITION;
				} else if (isOutgoing && !isIncoming) {
					description = OUTGOING | DELETION;
				} else if (isOutgoing && isIncoming) {
					description = CONFLICTING | CHANGE;
				}
			} else {
				// have a local and a remote			
				if (!isOutgoing && !isIncoming) {
					// ignore, there is no change;
				} else if (!isOutgoing && isIncoming) {
					description = INCOMING | CHANGE;
				} else if (isOutgoing && !isIncoming) {
					description = OUTGOING | CHANGE;
				} else {
					description = CONFLICTING | CHANGE;
				}
				// if contents are the same, then mark as pseudo change
				if (description != IN_SYNC && compare(granularity, false, local, remote, Policy.subMonitorFor(progress, 90)))
					description |= PSEUDO_CONFLICT;
			}
		}
		return description;
	}
	/**
	 * Returns the provider.
	 * @return TargetProvider
	 */
	protected TargetProvider getProvider() {
		return provider;
	}

	/**
	 * Return true if the resource associated with the receiver is out-of-date
	 */
	protected boolean isOutOfDate(IProgressMonitor monitor) {
		IResource local = getLocal();
		if (provider.hasBase(local)) {
			try{
				return provider.isOutOfDate(local, monitor);
			} catch(TeamException e) {
				TeamPlugin.log(e.getStatus());
				return true;
			}
		}
		return false;
	}

}
