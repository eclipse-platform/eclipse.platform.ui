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
package org.eclipse.team.internal.ui.target;

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
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;

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

	public RemoteTargetSyncElement(IResource local, IRemoteTargetResource remote) {
		this.local = local;
		this.remote = remote;
		try {
			this.provider = TargetManager.getProvider(local.getProject());
		} catch (TeamException e) {
			TeamUIPlugin.log(e.getStatus());
			this.remote = null;		
		}
	}
	
	/**
	 * @see RemoteSyncElement#create(boolean, IResource, IRemoteResource, IRemoteResource, Object)
	 */
	public IRemoteSyncElement create(boolean isThreeWay, IResource local, IRemoteResource base, IRemoteResource remote, Object data) {
		return new RemoteTargetSyncElement(local, (IRemoteTargetResource)remote);
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
		return new RemoteTargetSyncElement(local, remote);
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
		boolean isDirty = provider.isDirty(local);
		boolean isOutOfDate;
		try{
			isOutOfDate = provider.isOutOfDate(local, Policy.subMonitorFor(progress, 10));
		} catch(TeamException e) {
			isOutOfDate = true; // who knows?
		}
		
		boolean localExists = local.exists();
		
		if (remote == null) {
			if (!localExists) {
				// this should never happen
				// Assert.isTrue(false);
			} else {
				// no remote but a local
				if (!isDirty && isOutOfDate) {
					description = INCOMING | DELETION;
				} else if (isDirty && isOutOfDate) {
					description = CONFLICTING | CHANGE;
				} else if (!isDirty && !isOutOfDate) {
					description = OUTGOING | ADDITION;
				}
			}
		} else {
			if (!localExists) {
				// a remote but no local
				if (!isDirty && !isOutOfDate) {
					description = INCOMING | ADDITION;
				} else if (isDirty && !isOutOfDate) {
					description = OUTGOING | DELETION;
				} else if (isDirty && isOutOfDate) {
					description = CONFLICTING | CHANGE;
				}
			} else {
				// have a local and a remote			
				if (!isDirty && !isOutOfDate) {
					// ignore, there is no change;
				} else if (!isDirty && isOutOfDate) {
					description = INCOMING | CHANGE;
				} else if (isDirty && !isOutOfDate) {
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
}
