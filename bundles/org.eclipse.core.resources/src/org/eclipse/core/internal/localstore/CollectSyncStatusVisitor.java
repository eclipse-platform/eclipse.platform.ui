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
package org.eclipse.core.internal.localstore;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

//
/**
 * Visits a unified tree, and collects local sync information in 
 * a multistatus.  At the end of the visit, the resource tree will NOT
 * be synchronized with the filesystem, but all discrepancies between
 * the two will be recorded in the returned status.
 */
public class CollectSyncStatusVisitor extends RefreshLocalVisitor {
	protected List affectedResources;
	protected MultiStatus status;

	/**
	 * Creates a new visitor, whose sync status will have the given title.
	 */
	public CollectSyncStatusVisitor(String multiStatusTitle, IProgressMonitor monitor) {
		super(monitor);
		status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IStatus.INFO, multiStatusTitle, null);
		affectedResources = new ArrayList(20);
	}

	protected void changed(Resource target) {
		String message = NLS.bind(Messages.localstore_resourceIsOutOfSync, target.getFullPath());
		status.add(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message));
		affectedResources.add(target);
		resourceChanged = true;
	}

	protected void createResource(UnifiedTreeNode node, Resource target) {
		changed(target);
	}

	protected void deleteResource(UnifiedTreeNode node, Resource target) {
		changed(target);
	}

	protected void fileToFolder(UnifiedTreeNode node, Resource target) {
		changed(target);
	}

	protected void folderToFile(UnifiedTreeNode node, Resource target) {
		changed(target);
	}

	/**
	 * Returns the list of resources that were not synchronized with
	 * the local filesystem.
	 */
	public List getAffectedResources() {
		return affectedResources;
	}

	/**
	 * Returns the sync status that has been collected as a result of this visit.
	 */
	public MultiStatus getSyncStatus() {
		return status;
	}

	protected void makeLocal(UnifiedTreeNode node, Resource target) {
		changed(target);
	}

	protected void refresh(Container parent) {
		changed(parent);
	}

	protected void resourceChanged(UnifiedTreeNode node, Resource target) {
		changed(target);
	}
}
