/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core.sync;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * A standard abstract class that provides implementations for interesting 
 * <code>IRemoteSyncElement</code> methods. The <code>members</code> method
 * provided will create a unified tree based on the local, base, and remote
 * children. The <code>getSyncKind</code> method will calculate the relative
 * sync kind of the remote node.
 * 
 * @since 2.0
 */
public abstract class RemoteSyncElement extends LocalSyncElement implements IRemoteSyncElement {

	/**
	 * Creates a client specific sync element from a <b>local</b>, <b>base</b>, and 
	 * <b>remote</b> resources. The <b>base</b> and <b>remote</b> resource may be 
	 * <code>null</code>.
	 * 
	 * @param local the local resource in the workbench. Will never be <code>null</code>.
	 * @param base the base resource, may me <code>null</code>.
	 * @param remote the remote resource, may be <code>null</code>.
	 * @param data client specific data.
	 * 
	 * @return a client specific sync element.
	 */
	public abstract IRemoteSyncElement create(boolean isThreeWay, IResource local, IRemoteResource base, IRemoteResource remote, Object data);
		
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
			if( local.getType() != IResource.FILE && local.exists() ) {
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

				if(!localChild.exists() || !isIgnored(localChild)) {
					syncChildren.add(create(isThreeWay(), localChild, baseChild, remoteChild, getData()));
				}
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
		int description = IN_SYNC;
	
		IResource local = getLocal();
		IRemoteResource remote = getRemote();
		IRemoteResource base = getBase();
		
		boolean localExists = getLocal().exists();
	
		if (isThreeWay()) {
			if (base == null) {
				if (remote == null) {
					if (!localExists) {
						Assert.isTrue(false);
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
							if (isGranularityContents(granularity) && 
							    compare(granularity, true, local, remote, Policy.subMonitorFor(progress, 30))) {
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
						if (compare(granularity, false, base, remote, progress))
							description = OUTGOING | DELETION;
						else
							description = CONFLICTING | CHANGE;
					}
				} else {
					if (remote == null) {
						if (compare(granularity, false, local, base, progress))
							description = INCOMING | DELETION;
						else
							description = CONFLICTING | CHANGE;
					} else {
						progress.beginTask(null, 90);
						boolean ay = compare(granularity, false, local, base, Policy.subMonitorFor(progress, 30));
						boolean am = compare(granularity, false, base, remote, Policy.subMonitorFor(progress, 30));
						if (ay && am) {
							;
						} else if (ay && !am) {
							description = INCOMING | CHANGE;
						} else if (!ay && am) {
							description = OUTGOING | CHANGE;
						} else {
							description = CONFLICTING | CHANGE;
							if (isGranularityContents(granularity) && 
							    compare(granularity, true, local, remote, Policy.subMonitorFor(progress, 30))) {
								description |= PSEUDO_CONFLICT;
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
					if (! compare(granularity, false, local, remote, Policy.subMonitorFor(progress, 30)))
						description= CHANGE;
				}
			}
		}
		return description;
	}
	
	/**
	 * Helper methods for comparisons that returns true if the resource contents are the same.
	 * 
	 * If timestampDiff is true then the timestamps don't differ and there's no point checking the
	 * contents.
	 */
	protected boolean compare(int granularity, boolean force, IResource e1, IRemoteResource e2, IProgressMonitor monitor) {
		boolean timestampEquals;
		if (force) {
			timestampEquals = false;
		} else {
			timestampEquals = timestampEquals(e1, e2);
		}
		if (!timestampEquals && isGranularityContents(granularity)) {
			try {
				monitor.beginTask(null, 100);
				return contentsEqual(
					getContents(e1, Policy.subMonitorFor(monitor, 50)), 
					getContents(e2, Policy.subMonitorFor(monitor, 50)),
					granularity == GRANULARITY_CONTENTS_IGNORE_WHITESPACE);
			} finally {
				monitor.done();
			}
		} else {
			return timestampEquals;
		}
	}
	
	protected boolean compare(int granularity, boolean force, IRemoteResource e1, IRemoteResource e2, IProgressMonitor monitor) {
		boolean timestampEquals;
		if (force) {
			timestampEquals = false;
		} else {
			timestampEquals = timestampEquals(e1, e2);
		}
		if (!timestampEquals && isGranularityContents(granularity)) {
			try {
				monitor.beginTask(null, 100);
				return contentsEqual(
					getContents(e1, Policy.subMonitorFor(monitor, 50)), 
					getContents(e2, Policy.subMonitorFor(monitor, 50)),
					granularity == GRANULARITY_CONTENTS_IGNORE_WHITESPACE);
			} finally {
				monitor.done();
			}
		} else {
			return timestampEquals;
		}
	}
	
	protected abstract boolean timestampEquals(IResource e1, IRemoteResource e2);
	protected abstract boolean timestampEquals(IRemoteResource e1, IRemoteResource e2);
	
	protected boolean isGranularityContents(int granularity) {
		return granularity != GRANULARITY_TIMESTAMP;
	}
	
	private InputStream getContents(IResource resource, IProgressMonitor monitor) {
		try {
			if (resource instanceof IStorage)
				return new BufferedInputStream(((IStorage) resource).getContents());
			return null;
		} catch (CoreException e) {
			return null;
		}
	}
	
	private InputStream getContents(IRemoteResource remote, IProgressMonitor monitor) {
		try {
			if (!remote.isContainer())
				return new BufferedInputStream(remote.getContents(monitor));
			return null;
		} catch (TeamException exception) {
			// The remote node has gone away .
			return null;	
		}
	}
	
	/**
	 * Returns <code>true</code> if both input streams byte contents is identical. 
	 *
	 * @param input1 first input to contents compare
	 * @param input2 second input to contents compare
	 * @return <code>true</code> if content is equal
	 */
	private boolean contentsEqual(InputStream is1, InputStream is2, boolean ignoreWhitespace) {
		if (is1 == is2)
			return true;

		if (is1 == null && is2 == null) // no byte contents
			return true;

		try {
			if (is1 == null || is2 == null) // only one has contents
				return false;

			while (true) {
				int c1 = is1.read();
				while (ignoreWhitespace && isWhitespace(c1)) c1 = is1.read();
				int c2 = is2.read();
				while (ignoreWhitespace && isWhitespace(c2)) c2 = is2.read();
				if (c1 == -1 && c2 == -1)
					return true;
				if (c1 != c2)
					break;

			}
		} catch (IOException ex) {
		} finally {
			if (is1 != null) {
				try {
					is1.close();
				} catch (IOException ex) {
				}
			}
			if (is2 != null) {
				try {
					is2.close();
				} catch (IOException ex) {
				}
			}
		}
		return false;
	}

	/*
	 * Skip all whitspace.
	 */
	private boolean isWhitespace(int c) {
		if (c == -1) return false;
		return Character.isWhitespace((char)c);
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
		
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return getName() + kindToString(getSyncKind(GRANULARITY_TIMESTAMP, null));
	}
	
	static public String kindToString(int kind) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("["); //$NON-NLS-1$
		if(kind==IN_SYNC) {
			buffer.append("in-sync"); //$NON-NLS-1$
		} else {
			switch(kind & DIRECTION_MASK) {
				case CONFLICTING: buffer.append("conflicting"); break; //$NON-NLS-1$
				case OUTGOING: buffer.append("outgoing"); break; //$NON-NLS-1$
				case INCOMING: buffer.append("incoming"); break; //$NON-NLS-1$
			}		
			switch(kind & CHANGE_MASK) {
				case CHANGE: buffer.append("change"); break; //$NON-NLS-1$
				case ADDITION: buffer.append("addition"); break; //$NON-NLS-1$
				case DELETION: buffer.append("deletion"); break; //$NON-NLS-1$
			}
			if((kind & MANUAL_CONFLICT) != 0) buffer.append("{manual}"); //$NON-NLS-1$
			if((kind & AUTOMERGE_CONFLICT) != 0) buffer.append("{auto}"); //$NON-NLS-1$
		}
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}