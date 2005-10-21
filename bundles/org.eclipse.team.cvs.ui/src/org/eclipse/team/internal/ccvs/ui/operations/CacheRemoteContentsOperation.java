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
 * Operation that ensures that the contents for remote
 * of each local resource is cached.
 */
public class CacheRemoteContentsOperation extends CacheTreeContentsOperation {

	public CacheRemoteContentsOperation(IWorkbenchPart part, ResourceMapping[] mappers, SyncInfoTree tree) {
		super(part, mappers, tree);
	}

	protected boolean needsContents(SyncInfo info) {
		IResource local = info.getLocal();
		IResourceVariant remote = info.getRemote();
		if (remote != null && local.getType() == IResource.FILE) {
			int direction = SyncInfo.getDirection(info.getKind());
			if (direction == SyncInfo.CONFLICTING || 
					direction == SyncInfo.INCOMING) {
		        if (remote instanceof RemoteFile) {
		            RemoteFile remoteFile = (RemoteFile) remote;
		            if (!remoteFile.isContentsCached()) {
		            	return true;
		            }
		        }
			}
		}
		return false;
	}

	protected ICVSRemoteResource buildTree(CVSTeamProvider provider) throws TeamException {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().buildRemoteTree(provider.getProject(), true, new NullProgressMonitor());
	}

}
