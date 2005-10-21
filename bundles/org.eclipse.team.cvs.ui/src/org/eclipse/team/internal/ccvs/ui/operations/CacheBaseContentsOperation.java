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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Operation that ensures that the contents for base
 * of each local resource is cached.
 */
public class CacheBaseContentsOperation extends CacheTreeContentsOperation {

	private final boolean includeOutgoing;

	public CacheBaseContentsOperation(IWorkbenchPart part, ResourceMapping[] mappers, SyncInfoTree tree, boolean includeOutgoing) {
		super(part, mappers, tree);
		this.includeOutgoing = includeOutgoing;
	}
	
	protected boolean needsContents(SyncInfo info) {
		IResource local = info.getLocal();
		IResourceVariant base = info.getBase();
		if (base != null && local.getType() == IResource.FILE) {
			int direction = SyncInfo.getDirection(info.getKind());
			if (isEnabledForDirection(direction)) {
		        if (base instanceof RemoteFile) {
		            RemoteFile remote = (RemoteFile) base;
		            if (!remote.isContentsCached()) {
		            	return true;
		            }
		        }
			}
		}
		return false;
	}

	private boolean isEnabledForDirection(int direction) {
		return direction == SyncInfo.CONFLICTING || 
		(includeOutgoing && direction == SyncInfo.OUTGOING);
	}

	protected ICVSRemoteResource buildTree(CVSTeamProvider provider) throws TeamException {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().buildBaseTree(provider.getProject(), true, new NullProgressMonitor());
	}

}
