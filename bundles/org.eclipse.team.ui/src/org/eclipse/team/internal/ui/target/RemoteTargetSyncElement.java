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

public class RemoteTargetSyncElement extends RemoteSyncElement {

	private IRemoteTargetResource remote;
	private LocalTargetSyncElement localSyncElement;
	private TargetProvider provider;

	public RemoteTargetSyncElement(IResource local, IRemoteTargetResource remote) {
		this.localSyncElement = new LocalTargetSyncElement(local);
		this.remote = remote;
		try {
			this.provider = TargetManager.getProvider(local.getProject());
		} catch (TeamException e) {			
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
		return localSyncElement.create(local, base, data);
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
		return localSyncElement.getLocal();
	}

	/**
	 * @see ILocalSyncElement#getBase()
	 */
	public IRemoteResource getBase() {
		return null;
	}

	/**
	 * @see ILocalSyncElement#isCheckedOut()
	 */
	public boolean isCheckedOut() {
		return false;
	}

	/**
	 * @see ILocalSyncElement#hasRemote()
	 */
	public boolean hasRemote() {
		return false;
	}
	
	/**
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
		int description = IN_SYNC;
		IResource local = getLocal();
		boolean isDirty = provider.isDirty(local);
		boolean isOutOfDate = provider.isOutOfDate(local);
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
					if (description != IN_SYNC && compare(granularity, false, local, remote, progress))
						description |= PSEUDO_CONFLICT;
				}
			}
			return description;
	}
}
