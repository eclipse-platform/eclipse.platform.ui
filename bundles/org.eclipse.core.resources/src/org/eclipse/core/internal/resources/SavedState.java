/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Standard implementation of the ISavedState interface.
 */
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
			final ISchedulingRule rule = workspace.getRoot();
			try {
				workspace.prepareOperation(rule, null);
				if (oldTree == null || newTree == null)
					return;
				workspace.beginOperation(true);
				ResourceDelta delta = ResourceDeltaFactory.computeDelta(workspace, oldTree, newTree, Path.ROOT, -1);
				forgetTrees(); // free trees to prevent memory leak
				workspace.getNotificationManager().broadcastChanges(listener, IResourceChangeEvent.POST_BUILD, delta);
			} finally {
				workspace.endOperation(rule, false, null);
			}
		} catch (CoreException e) {
			// this is unlikelly to happen, so, just log it
			ResourceStatus status = new ResourceStatus(IStatus.WARNING, null, e.getMessage(), e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}
}
