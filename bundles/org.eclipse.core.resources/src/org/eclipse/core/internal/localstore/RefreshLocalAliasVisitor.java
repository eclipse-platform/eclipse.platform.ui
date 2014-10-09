/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Container;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Performs a local refresh, and additionally updates all aliases of the
 * refreshed resource.
 */
public class RefreshLocalAliasVisitor extends RefreshLocalVisitor {
	public RefreshLocalAliasVisitor(IProgressMonitor monitor) {
		super(monitor);
	}

	@Override
	protected void createResource(UnifiedTreeNode node, Resource target) throws CoreException {
		super.createResource(node, target);
		IFileStore store = node.getStore();
		if (store == null)
			return;
		IResource[] aliases = workspace.getAliasManager().computeAliases(target, store);
		if (aliases != null)
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].getProject().isOpen() && !((Resource) aliases[i]).isFiltered())
					super.createResource(node, (Resource) aliases[i]);
			}
	}

	@Override
	protected void deleteResource(UnifiedTreeNode node, Resource target) throws CoreException {
		super.deleteResource(node, target);
		IFileStore store = node.getStore();
		if (store == null)
			return;
		IResource[] aliases = workspace.getAliasManager().computeAliases(target, store);
		if (aliases != null) {
			boolean wasFilteredOut = false;
			if (store.fetchInfo() != null && store.fetchInfo().exists())
				wasFilteredOut = target.isFiltered();
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].getProject().isOpen()) {
					if (wasFilteredOut) {
						if (((Resource) aliases[i]).isFiltered())
							super.deleteResource(node, (Resource) aliases[i]);
					}
					else
						super.deleteResource(node, (Resource) aliases[i]);
				}
			}
		}
	}

	@Override
	protected void resourceChanged(UnifiedTreeNode node, Resource target) {
		super.resourceChanged(node, target);
		IFileStore store = node.getStore();
		if (store == null)
			return;
		IResource[] aliases = workspace.getAliasManager().computeAliases(target, store);
		if (aliases != null)
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].getProject().isOpen())
					super.resourceChanged(node, (Resource) aliases[i]);
			}
	}

	@Override
	protected void fileToFolder(UnifiedTreeNode node, Resource target) throws CoreException {
		super.fileToFolder(node, target);
		IFileStore store = node.getStore();
		if (store == null)
			return;
		IResource[] aliases = workspace.getAliasManager().computeAliases(target, store);
		if (aliases != null)
			for (int i = 0; i < aliases.length; i++)
				super.fileToFolder(node, (Resource) aliases[i]);
	}

	@Override
	protected void folderToFile(UnifiedTreeNode node, Resource target) throws CoreException {
		super.folderToFile(node, target);
		IFileStore store = node.getStore();
		if (store == null)
			return;
		IResource[] aliases = workspace.getAliasManager().computeAliases(target, store);
		if (aliases != null)
			for (int i = 0; i < aliases.length; i++)
				super.folderToFile(node, (Resource) aliases[i]);
	}

	@Override
	protected void refresh(Container parent) throws CoreException {
		parent.getLocalManager().refresh(parent, IResource.DEPTH_ZERO, true, null);
	}

}
