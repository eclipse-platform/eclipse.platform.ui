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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.internal.events.ResourceDeltaFactory;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class SavedState implements ISavedState {
	ElementTree oldTree;
	ElementTree newTree;
	SafeFileTable fileTable;
	String pluginId;
	Workspace workspace;

	SavedState(Workspace workspace, String pluginId, ElementTree oldTree, ElementTree newTree) throws CoreException {
		this.workspace = workspace;
		this.pluginId = pluginId;
		this.newTree = newTree;
		this.oldTree = oldTree;
		this.fileTable = restoreFileTable();
	}
	void forgetTrees() {
		newTree = null;
		oldTree = null;
	}
	public int getSaveNumber() {
		return workspace.getSaveManager().getSaveNumber(pluginId);
	}
	protected SafeFileTable getFileTable() {
		return fileTable;
	}
	protected SafeFileTable restoreFileTable() throws CoreException {
		if (fileTable == null)
			fileTable = new SafeFileTable(pluginId);
		return fileTable;
	}
	public IPath lookup(IPath file) {
		return getFileTable().lookup(file);
	}
	public IPath[] getFiles() {
		return getFileTable().getFiles();
	}
	public void processResourceChangeEvents(IResourceChangeListener listener) {
		try {
			try {
				workspace.prepareOperation(workspace.getRoot());
				if (oldTree == null || newTree == null)
					return;
				workspace.beginOperation(true);
				ResourceDelta delta = ResourceDeltaFactory.computeDelta(workspace, oldTree, newTree, Path.ROOT, -1);
				forgetTrees(); // free trees to prevent memory leak
				workspace.getNotificationManager().broadcastChanges(listener, IResourceChangeEvent.POST_AUTO_BUILD, delta);
			} finally {
				workspace.endOperation(workspace.getRoot(), false, null);
			}
		} catch (CoreException e) {
			// this is unlikelly to happen, so, just log it
			ResourceStatus status = new ResourceStatus(IStatus.WARNING, null, e.getMessage(), e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}
}
