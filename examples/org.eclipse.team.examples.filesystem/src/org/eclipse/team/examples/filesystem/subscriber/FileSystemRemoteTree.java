/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayRemoteTree;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;
import org.eclipse.team.examples.filesystem.FileSystemProvider;

/**
 * The file sytem three-way remote resource varant tree taht provides
 * the ability to traverse the file system for the creation of resource variants.
 */
public class FileSystemRemoteTree extends ThreeWayRemoteTree {
	
	/**
	 * Create the file syetm remote resource variant tree
	 * @param subscriber the file system subscriber
	 */
	public FileSystemRemoteTree(FileSystemSubscriber subscriber) {
		super(subscriber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.AbstractResourceVariantTree#fetchMembers(org.eclipse.team.core.variants.IResourceVariant, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResourceVariant[] fetchMembers(IResourceVariant variant, IProgressMonitor progress) throws TeamException {
		return ((FileSystemResourceVariant)variant).members();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.AbstractResourceVariantTree#fetchVariant(org.eclipse.core.resources.IResource, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), FileSystemPlugin.PROVIDER_ID);
		if (provider != null) {
			return ((FileSystemProvider)provider).getResourceVariant(resource);
		}
		return null;
	}
}
