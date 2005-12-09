/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.diff;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * Covert a SyncInfoSet into a SyncDeltaTree
 */
public class SyncInfoToDiffConverter implements ISyncInfoSetChangeListener {

	SyncInfoSet set;
	ResourceDiffTree tree;
	
	public SyncInfoToDiffConverter(SyncInfoTree set, ResourceDiffTree tree) {
		this.set = set;
		this.tree = tree;
	}

	public void connect(IProgressMonitor monitor) {
		set.connect(this, monitor);
	}
	
	public void dispose() {
		set.removeSyncSetChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		try {
			tree.beginInput();
			tree.clear();
			SyncInfo[] infos = set.getSyncInfos();
			for (int i = 0; i < infos.length; i++) {
				SyncInfo info = infos[i];
				IDiffNode delta = getDeltaFor(info);
				tree.add(delta);
			}
		} finally {
			tree.endInput(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoChanged(org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		try {
			tree.beginInput();
			SyncInfo[] added = event.getAddedResources();
			for (int i = 0; i < added.length; i++) {
				SyncInfo info = added[i];
				IDiffNode delta = getDeltaFor(info);
				tree.add(delta);
			}
			SyncInfo[] changed = event.getChangedResources();
			for (int i = 0; i < changed.length; i++) {
				SyncInfo info = changed[i];
				IDiffNode delta = getDeltaFor(info);
				tree.add(delta);
			}
			IResource[] removed = event.getRemovedResources();
			for (int i = 0; i < removed.length; i++) {
				IResource resource = removed[i];
				tree.remove(resource.getFullPath());
			}
		} finally {
			tree.endInput(monitor);
		}
	}

	public static IDiffNode getDeltaFor(SyncInfo info) {
		if (info.getComparator().isThreeWay()) {
			ITwoWayDiff local = getLocalDelta(info);
			ITwoWayDiff remote = getRemoteDelta(info);
			return new ThreeWayDiff(info.getLocal().getFullPath(), local, remote, 0);
		} else {
			return getDelta(info, wrapLocal(info), info.getRemote(), 0);
		}
	}

	private static ITwoWayDiff getRemoteDelta(SyncInfo info) {
		IResourceVariant ancestor = info.getBase();
		IResourceVariant remote = info.getRemote();
		return getDelta(info, ancestor, remote, SyncInfo.INCOMING);
	}

	private static ITwoWayDiff getDelta(SyncInfo info, IResourceVariant before, IResourceVariant after, int direction) {
		int kind = IDiffNode.NO_CHANGE;
		if ((SyncInfo.getDirection(info.getKind()) & direction) == 0) {
			// There is no change so create a NO_CHANGE delta
		} else if (before == null) {
			kind = IDiffNode.ADDED;
		} else if (after == null) {
			kind = IDiffNode.REMOVED;
		} else {
			kind = IDiffNode.CHANGED;
		}
		return new ResourceDiff(info.getLocal().getFullPath(), kind, IDiffNode.NO_CHANGE, before, after);
	}

	private static ITwoWayDiff getLocalDelta(SyncInfo info) {
		IResourceVariant ancestor = info.getBase();
		IResourceVariant local = wrapLocal(info);
		return getDelta(info, ancestor, local, SyncInfo.OUTGOING);
	}

	private static IResourceVariant wrapLocal(final SyncInfo info) {
		if (info.getLocal().exists()) {
			return new IResourceVariant() {
				public byte[] asBytes() {
					return getContentIdentifier().getBytes();
				}
			
				public String getContentIdentifier() {
					return info.getLocalContentIdentifier();
				}
			
				public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
					IResource local = info.getLocal();
					if (local.getType() == IResource.FILE) {
						return (IFile)local;
					}
					return null;
				}
			
				public boolean isContainer() {
					IResource local = info.getLocal();
					return local.getType() != IResource.FILE;
				}
			
				public String getName() {
					return info.getLocal().getName();
				}
			
			};
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		// TODO Need to do something here
		
	}

	public IResourceDiffTree getTree() {
		return tree;
	}
}
