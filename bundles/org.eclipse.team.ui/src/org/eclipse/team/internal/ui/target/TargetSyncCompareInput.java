/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.target.ITargetRunnable;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.sync.CatchupReleaseViewer;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;

public class TargetSyncCompareInput extends SyncCompareInput {

	private IResource[] resources;

	protected TargetSyncCompareInput(IResource[] resources, int granularity) {
		super(granularity);
		this.resources = resources;
	}

	/**
	 * @see SyncCompareInput#createSyncElements(IProgressMonitor)
	 */
	protected IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, resources.length);
			RemoteTargetSyncElement[] elements = new RemoteTargetSyncElement[resources.length];
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				TargetProvider provider = TargetManager.getProvider(resource.getProject());
				elements[i] = new RemoteTargetSyncElement(resources[i], provider.getRemoteResourceFor(resource));				
				monitor.worked(1);
			}
			return elements;
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see CompareEditorInput#createDiffViewer(Composite)
	 */
	public Viewer createDiffViewer(Composite parent) {
		CatchupReleaseViewer viewer = new TargetCatchupReleaseViewer(parent, this);
		setViewer(viewer);
		return viewer;
	}
	
	protected void updateView() {
		// Update the view
		if (getDiffRoot().hasChildren()) {
			getViewer().refresh();
		} else {
			getViewer().setInput(null);
		}
		
		// Update the status line
		updateStatusLine();
	}
	
	/**
	 * @see SyncCompareInput#collectResourceChanges(IDiffContainer, IRemoteSyncElement, IProgressMonitor)
	 */
	protected IDiffElement collectResourceChanges(
		final IDiffContainer parent,
		final IRemoteSyncElement tree,
		IProgressMonitor pm) {
		
		final IDiffElement[] result = new IDiffElement[] {null};
		try {
			TargetProvider provider = TargetManager.getProvider(tree.getLocal().getProject());
			provider.run(new ITargetRunnable() {
				public void run(IProgressMonitor monitor) throws TeamException {
					result[0] = TargetSyncCompareInput.super.collectResourceChanges(parent, tree, monitor);
				}
			}, pm);
		} catch (TeamException e) {
			TeamUIPlugin.log(e.getStatus());
		}
		return result[0];
	}
}
