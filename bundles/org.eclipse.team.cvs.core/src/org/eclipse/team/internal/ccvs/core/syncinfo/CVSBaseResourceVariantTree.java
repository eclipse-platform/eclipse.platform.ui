/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.ResourceVariantByteStore;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;


public class CVSBaseResourceVariantTree extends ResourceVariantByteStore {
	public void dispose() {
		// Do nothing
	}
	public byte[] getBytes(IResource resource) throws TeamException {
		if (resource.getType() == IResource.FILE) {
			// For a file, return the entry line
			byte[] bytes =  EclipseSynchronizer.getInstance().getSyncBytes(resource);
			if (bytes != null) {
				// Use the base sync info (i.e. no deletion or addition)
				if (ResourceSyncInfo.isDeletion(bytes)) {
					bytes = ResourceSyncInfo.convertFromDeletion(bytes);
				} else if (ResourceSyncInfo.isAddition(bytes)) {
					bytes = null;
				}
			}
			return bytes;
		} else {
			// For a folder, return the folder sync info bytes if the folder is managed or is a project
			if (EclipseSynchronizer.getInstance().isManagedCVSFolder((IContainer)resource)) {
				FolderSyncInfo info = EclipseSynchronizer.getInstance().getFolderSync((IContainer)resource);
				if (info != null) {
					return info.getBytes();
				}
			}
			return null;
		}
	}
	public boolean isVariantKnown(IResource resource) throws TeamException {
		return getBytes(resource) != null;
	}
	public boolean flushBytes(IResource resource, int depth) throws TeamException {
		throw new UnsupportedOperationException();
	}
	public boolean setBytes(IResource resource, byte[] bytes) throws TeamException {
		throw new UnsupportedOperationException();
	}
	public boolean deleteBytes(IResource resource) throws TeamException {
		throw new UnsupportedOperationException();
	}
	public IResource[] members(IResource resource) throws TeamException {
		if(resource.getType() == IResource.FILE) {
			return new IResource[0];
		}	
		CVSWorkspaceSubscriber workspaceSubscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
		if(resource.getType() == IResource.ROOT) {
			return workspaceSubscriber.roots();
		}
		IResource[] members = EclipseSynchronizer.getInstance().members((IContainer)resource);
		List result = new ArrayList();
		for (int i = 0; i < members.length; i++) {
			IResource member = members[i];
			if (workspaceSubscriber.isSupervised(member)) {
				result.add(member);
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}
}