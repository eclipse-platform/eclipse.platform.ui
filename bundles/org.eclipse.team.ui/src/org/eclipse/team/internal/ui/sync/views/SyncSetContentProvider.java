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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * This class provides the contents for a StructuredViewer using a SyncSet as the model
 */
public abstract class SyncSetContentProvider implements IStructuredContentProvider, ISyncSetChangedListener {

	protected Viewer viewer;
	
	protected SyncSet getSyncSet() {
		Object input = viewer.getInput();
		if (input == null) return null;
		return (SyncSet)input;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		this.viewer = v;
		SyncSet oldSyncSet = null;
		SyncSet newSyncSet = null;
		if (oldInput instanceof SyncSet) {
			oldSyncSet = (SyncSet) oldInput;
		}
		if (newInput instanceof SyncSet) {
			newSyncSet = (SyncSet) newInput;
		}
		if (oldSyncSet != newSyncSet) {
			if (oldSyncSet != null) {
				oldSyncSet.removeSyncSetChangedListener(this);
			}
			if (newSyncSet != null) {
				newSyncSet.addSyncSetChangedListener(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public abstract Object[] getElements(Object inputElement);
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		SyncSet syncSet = getSyncSet();
		if (syncSet != null) {
			syncSet.removeSyncSetChangedListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	public void syncSetChanged(final SyncSetChangedEvent event) {
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {
					handleSyncSetChanges(event);
				}
			});
		}
	}

	/**
	 * Update the viewer with the sync-set changes, aditions and removals
	 * in the given event. This method is invoked from within the UI thread.
	 * @param event
	 */
	protected void handleSyncSetChanges(SyncSetChangedEvent event) {
		viewer.getControl().setRedraw(false);
		if (event.isReset()) {
			// On a reset, refresh the entire view
			((StructuredViewer) viewer).refresh();
		} else {
			handleResourceChanges(event);
			handleResourceRemovals(event);
			handleResourceAdditions(event);
		}
		viewer.getControl().setRedraw(true);
	}

	/**
	 * Update the viewer for the sync set changes in the provided event.
	 * This method is invoked by <code>handleSyncSetChanges</code>.
	 * Subclasses may override.
	 * @param event
	 * @see #handleSyncSetChanges(SyncSetChangedEvent)
	 */
	protected void handleResourceChanges(SyncSetChangedEvent event) {
		// Refresh the viewer for each changed resource
		SyncInfo[] infos = event.getChangedResources();
		for (int i = 0; i < infos.length; i++) {			
			((StructuredViewer) viewer).refresh(SyncSet.getModelObject(getSyncSet(), infos[i].getLocal()), true);
		}
	}

	/**
	 * Update the viewer for the sync set removals in the provided event.
	 * This method is invoked by <code>handleSyncSetChanges</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected void handleResourceRemovals(SyncSetChangedEvent event) {
		// Update the viewer for each removed resource
		IResource[] removed = event.getRemovedRoots();
		for (int i = 0; i < removed.length; i++) {
			IResource resource = removed[i];
			((StructuredViewer) viewer).refresh(SyncSet.getModelObject(getSyncSet(), resource));
		}
	}

	/**
	 * Update the viewer for the sync set additions in the provided event.
	 * This method is invoked by <code>handleSyncSetChanges</code>.
	 * Subclasses may override.
	 * @param event
	 */
	protected void handleResourceAdditions(SyncSetChangedEvent event) {
		// Update the viewer for each of the added resource's parents
		IResource[] added = event.getAddedRoots();
		for (int i = 0; i < added.length; i++) {
			IResource resource = added[i];
			((StructuredViewer) viewer).refresh(SyncSet.getModelObject(getSyncSet(), resource.getParent()));
		}
	}

	public StructuredViewer getViewer() {
		return (StructuredViewer)viewer;
	}
	
	protected Object getModelObject(IResource resource) {
		return SyncSet.getModelObject(getSyncSet(), resource);
	}
	
	protected Object getModelObject(SyncInfo info) {
		return getModelObject(info.getLocal());
	}
	
	protected Object[] getModelObjects(SyncInfo[] infos) {
		Object[] resources = new Object[infos.length];
		for (int i = 0; i < resources.length; i++) {
			resources[i] = getModelObject(infos[i]);
		}
		return resources;
	}
	
	protected Object[] getModelObjects(IResource[] resources) {
		Object[] result = new Object[resources.length];
		for (int i = 0; i < resources.length; i++) {
			result[i] = getModelObject(resources[i]);
		}
		return result;
	}
}
