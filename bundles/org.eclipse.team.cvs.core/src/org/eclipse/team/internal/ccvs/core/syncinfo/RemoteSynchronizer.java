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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * A remote resource sychronizer caches the remote sync bytes that can be 
 * used to create remote handles
 */
public class RemoteSynchronizer extends ResourceSynchronizer {
	
	public static final String SYNC_KEY_QUALIFIER = "org.eclipse.team.cvs"; //$NON-NLS-1$
	protected QualifiedName syncName;
	protected Set changedResources = new HashSet();
	
	public RemoteSynchronizer(String id) {
		syncName = new QualifiedName(SYNC_KEY_QUALIFIER, id);
		getSynchronizer().add(syncName);
	}
	
	/**
	 * Dispose of any cached remote sync info.
	 */
	public void dispose() {
		getSynchronizer().remove(getSyncName());
	}

	protected ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}

	protected QualifiedName getSyncName() {
		return syncName;
	}

	public byte[] getSyncBytes(IResource resource) throws CVSException {
		try {
			return getSynchronizer().getSyncInfo(getSyncName(), resource);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	public void setSyncBytes(IResource resource, byte[] bytes) throws CVSException {
		byte[] oldBytes = getSyncBytes(resource);
		if (oldBytes != null && Util.equals(oldBytes, bytes)) return;
		try {
			getSynchronizer().setSyncInfo(getSyncName(), resource, bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		changedResources.add(resource);
	}

	public void removeSyncBytes(IResource resource, int depth, boolean silent) throws CVSException {
		if (resource.exists() || resource.isPhantom()) {
			try {
				getSynchronizer().flushSyncInfo(getSyncName(), resource, depth);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			if(silent == false) {
				changedResources.add(resource);
			}
		}
	}
}