package org.eclipse.team.core.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.internal.Assert;

public abstract class RemoteSyncElement implements IRemoteSyncElement {
	
	/*
	 * @see ILocalSyncElement#getName()
	 */
	public String getName() {
		if (getLocal() != null) {
			return getLocal().getName();
		}
		if (getRemote() != null) {
			return getRemote().getName();
		}
		if (getBase() != null) {
			return getBase().getName();
		}
		// A sync tree should never of been created without at least one
		// contributor.
		Assert.isTrue(false);
		return null;
	}

	/*
	 * @see ILocalSyncElement#isContainer()
	 */
	public boolean isContainer() {
		if (getLocal() != null) {
			return getLocal().getType() != IResource.FILE;
		}
		if (getRemote() != null) {
			return getRemote().isContainer();
		}
		if (getBase() != null) {
			return getBase().isContainer();
		}
		// A sync tree should never of been created without at least one
		// contributor.
		Assert.isTrue(false);
		return false;
	}

	/*
	 * Helper method to create a remote sync element.
	 */
	protected abstract IRemoteSyncElement create(IResource local, IRemoteResource base, IRemoteResource remote);
		
	/*
	 * @see ILocalSyncElement#members()
	 */
	public ILocalSyncElement[] members(IProgressMonitor progress) throws TeamException {
		// create union of the local, base, and remote trees
		IRemoteResource remote = getRemote();
		IRemoteResource base = getBase();
		IResource local = getLocal();
		
		IRemoteResource[] remoteChildren =
			remote != null ? remote.members(progress) : new IRemoteResource[0];
			
		IRemoteResource[] baseChildren =
			base != null ? base.members(progress) : new IRemoteResource[0];
			
		IResource[] localChildren;			
		try {	
			if( local.getType() != IResource.FILE ) {
				localChildren = ((IContainer)local).members();
			} else {
				localChildren = new IResource[0];
			}
		} catch(CoreException e) {
			throw new TeamException(e.getStatus());
		}
			
		if (remoteChildren.length > 0 || localChildren.length > 0) {
			List syncChildren = new ArrayList(10);
			Set allSet = new HashSet(20);
			Map localSet = null;
			Map remoteSet = null;
			Map baseSet = null;

			if (localChildren.length > 0) {
				localSet = new HashMap(10);
				for (int i = 0; i < localChildren.length; i++) {
					IResource localChild = localChildren[i];
					String name = localChild.getName();
					localSet.put(name, localChild);
					allSet.add(name);
				}
			}

			if (remoteChildren.length > 0) {
				remoteSet = new HashMap(10);
				for (int i = 0; i < remoteChildren.length; i++) {
					IRemoteResource remoteChild = remoteChildren[i];
					String name = remoteChild.getName();
					remoteSet.put(name, remoteChild);
					allSet.add(name);
				}
			}
			
			if (baseChildren.length > 0) {
				baseSet = new HashMap(10);
				for (int i = 0; i < baseChildren.length; i++) {
					IRemoteResource baseChild = baseChildren[i];
					String name = baseChild.getName();
					baseSet.put(name, baseChild);
					allSet.add(name);
				}
			}
			
			Iterator e = allSet.iterator();
			while (e.hasNext()) {
				String keyChildName = (String) e.next();

				if (progress != null) {
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}
					// XXX show some progress?
				}

				IResource localChild =
					localSet != null ? (IResource) localSet.get(keyChildName) : null;

				IRemoteResource remoteChild =
					remoteSet != null ? (IRemoteResource) remoteSet.get(keyChildName) : null;
					
				IRemoteResource baseChild =
					baseSet != null ? (IRemoteResource) baseSet.get(keyChildName) : null;


				if (localChild == null) {
					// there has to be a remote resource available if we got this far
					Assert.isTrue(remoteChild != null || baseChild != null);
					boolean isContainer = remoteChild != null ? remoteChild.isContainer() : baseChild.isContainer();
					
					localChild =	getResourceChild(local /* parent */, keyChildName, isContainer);
				}

				syncChildren.add(create(localChild, baseChild, remoteChild));
			}
			return (IRemoteSyncElement[]) syncChildren.toArray(new IRemoteSyncElement[syncChildren.size()]);
		}
		else {
			return new IRemoteSyncElement[0];
		}
	}

	/*
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
		return IN_SYNC;
	}
	
	/*
	 * Returns a handle to a non-existing resource.
	 */
	private IResource getResourceChild(IResource parent, String childName, boolean isContainer) {
		if (parent.getType() == IResource.FILE) {
			return null;
		}
		if (isContainer) {
			return ((IContainer) parent).getFolder(new Path(childName));
		} else {
			return ((IContainer) parent).getFile(new Path(childName));
		}
	}
}

