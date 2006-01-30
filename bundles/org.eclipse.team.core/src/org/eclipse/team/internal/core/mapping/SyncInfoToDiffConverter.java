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
package org.eclipse.team.internal.core.mapping;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.diff.provider.*;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.provider.ResourceDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * Covert a SyncInfoSet into a SyncDeltaTree
 */
public class SyncInfoToDiffConverter implements ISyncInfoSetChangeListener {

	public static final class ResourceVariantFileRevision extends FileRevision {
		private final IResourceVariant variant;

		private ResourceVariantFileRevision(IResourceVariant variant) {
			this.variant = variant;
		}

		public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
			return variant.getStorage(monitor);
		}

		public String getName() {
			return variant.getName();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.FileRevision#getContentIndentifier()
		 */
		public String getContentIdentifier() {
			return variant.getContentIdentifier();
		}

		public IResourceVariant getVariant() {
			return variant;
		}
	}

	SyncInfoSet set;
	ResourceDiffTree tree;
	List errors = new ArrayList();
	
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
				IDiff delta = getDeltaFor(info);
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
				IDiff delta = getDeltaFor(info);
				tree.add(delta);
			}
			SyncInfo[] changed = event.getChangedResources();
			for (int i = 0; i < changed.length; i++) {
				SyncInfo info = changed[i];
				IDiff delta = getDeltaFor(info);
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

	public static int asDiffFlags(int syncInfoFlags) {
		if (syncInfoFlags == SyncInfo.IN_SYNC)
			return IDiff.NO_CHANGE;
		int kind = SyncInfo.getChange(syncInfoFlags);
		int diffFlags = 0;
		switch (kind) {
		case SyncInfo.ADDITION:
			diffFlags = IDiff.ADD;
			break;
		case SyncInfo.DELETION:
			diffFlags = IDiff.REMOVE;
			break;
		case SyncInfo.CHANGE:
			diffFlags = IDiff.CHANGE;
			break;
		}
		int direction = SyncInfo.getDirection(syncInfoFlags);
		switch (direction) {
		case SyncInfo.INCOMING:
			diffFlags |= IThreeWayDiff.INCOMING;
			break;
		case SyncInfo.OUTGOING:
			diffFlags |= IThreeWayDiff.OUTGOING;
			break;
		case SyncInfo.CONFLICTING:
			diffFlags |= IThreeWayDiff.CONFLICTING;
			break;
		}
		return diffFlags;
	}
	
	public static IDiff getDeltaFor(SyncInfo info) {
		if (info.getComparator().isThreeWay()) {
			ITwoWayDiff local = getLocalDelta(info);
			ITwoWayDiff remote = getRemoteDelta(info);
			return new ThreeWayDiff(local, remote);
		} else {
			if (info.getKind() != SyncInfo.IN_SYNC) {
				IResourceVariant remote = info.getRemote();
				IResource local = info.getLocal();
				int kind;
				if (remote == null) {
					kind = IDiff.REMOVE;
				} else if (!local.exists()) {
					kind = IDiff.ADD;
				} else {
					kind = IDiff.CHANGE;
				}
				if (local.getType() == IResource.FILE) {
					IFileRevision after = asFileState(remote);
					IFileRevision before = FileRevision.getFileRevisionFor((IFile)local);
					return new ResourceDiff(info.getLocal(), kind, 0, before, after);
				}
				// For folders, we don't need file states
				return new ResourceDiff(info.getLocal(), kind);
			}
			return null;
		}
	}

	private static ITwoWayDiff getRemoteDelta(SyncInfo info) {
		int direction = SyncInfo.getDirection(info.getKind());
		if (direction == SyncInfo.INCOMING || direction == SyncInfo.CONFLICTING) {
			IResourceVariant ancestor = info.getBase();
			IResourceVariant remote = info.getRemote();
			int kind;
			if (ancestor == null) {
				kind = IDiff.ADD;
			} else if (remote == null) {
				kind = IDiff.REMOVE;
			} else {
				kind = IDiff.CHANGE;
			}
			// For folders, we don't need file states
			if (info.getLocal().getType() == IResource.FILE) {
				IFileRevision before = asFileState(ancestor);
				IFileRevision after = asFileState(remote);
				return new ResourceDiff(info.getLocal(), kind, 0, before, after);
			}

			return new ResourceDiff(info.getLocal(), kind);
		}
		return null;
	}

	private static IFileRevision asFileState(final IResourceVariant variant) {
		if (variant == null)
			return null;
		return new ResourceVariantFileRevision(variant);
	}

	private static ITwoWayDiff getLocalDelta(SyncInfo info) {
		int direction = SyncInfo.getDirection(info.getKind());
		if (direction == SyncInfo.OUTGOING || direction == SyncInfo.CONFLICTING) {
			IResourceVariant ancestor = info.getBase();
			IResource local = info.getLocal();
			int kind;
			if (ancestor == null) {
				kind = IDiff.ADD;
			} else if (!local.exists()) {
				kind = IDiff.REMOVE;
			} else {
				kind = IDiff.CHANGE;
			}
			if (local.getType() == IResource.FILE) {
				IFileRevision before = asFileState(ancestor);
				IFileRevision after = FileRevision.getFileRevisionFor((IFile)local);
				return new ResourceDiff(info.getLocal(), kind, 0, before, after);
			}
			// For folders, we don't need file states
			return new ResourceDiff(info.getLocal(), kind);
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		// TODO: How to handle errors (Bug 121121)
		this.errors.addAll(Arrays.asList(errors));
	}

	public IResourceDiffTree getTree() {
		return tree;
	}

	/**
	 * @param twd
	 * @return
	 */
	public static IResourceVariant getRemoteVariant(IThreeWayDiff twd) {
		IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
		if (diff != null)
			return ((ResourceVariantFileRevision)diff.getAfterState()).getVariant();
		diff = (IResourceDiff)twd.getLocalChange();
		if (diff != null)
			return ((ResourceVariantFileRevision)diff.getBeforeState()).getVariant();
		return null;
	}

	/**
	 * @param twd
	 * @return
	 */
	public static IResourceVariant getBaseVariant(IThreeWayDiff twd) {
		IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
		if (diff != null)
			return ((ResourceVariantFileRevision)diff.getBeforeState()).getVariant();
		diff = (IResourceDiff)twd.getLocalChange();
		if (diff != null)
			return ((ResourceVariantFileRevision)diff.getBeforeState()).getVariant();
		return null;
	}
}
