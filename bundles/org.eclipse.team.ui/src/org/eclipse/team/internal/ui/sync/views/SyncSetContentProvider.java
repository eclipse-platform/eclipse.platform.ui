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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.actions.TeamAction;

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
			((StructuredViewer) viewer).refresh(getModelObject(infos[i].getLocal()), true);
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
			((StructuredViewer) viewer).refresh(getModelObject(resource));
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
			((StructuredViewer) viewer).refresh(getModelObject(resource.getParent()));
		}
	}

	public StructuredViewer getViewer() {
		return (StructuredViewer)viewer;
	}
	
	/**
	 * Return the IResource for the given model object that was returned by 
	 * SyncSet#members(IResource). Return <code>null</code> if the given
	 * object does not have a corresponding IResource.
	 * 
	 * @param element
	 * @return
	 */
	public IResource getResource(Object obj) {
		return (IResource)TeamAction.getAdapter(obj, IResource.class);
	}
	
	/**
	 * Return the sync kind for the given model object that was returned by 
	 * SyncSet#members(IResource). If syncSet is null, then the 
	 * sync kind for SyncContainers will always be 0.
	 * 
	 * @param element
	 * @return
	 */
	public int getSyncKind(SyncSet syncSet, Object element) {
		SyncInfo info = getSyncInfo(element);
		if (info != null) {
			return info.getKind();
		}
		return 0;
	}
	
	/**
	 * Return the children of the given container who are either out-of-sync or contain
	 * out-of-sync resources.
	 */
	public Object[] members(IResource resource) {
		IResource[] resources = getSyncSet().members(resource);
		Object[] result = new Object[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource child = resources[i];
			result[i] = getModelObject(child);
		}
		return result;
	}
	
	/**
	 * Return the SyncInfo for the given model object that was returned by 
	 * SyncSet#members(IResource). If syncSet is null, then the 
	 * sync info will also be null.
	 * 
	 * @param element
	 * @return
	 */
	public SyncInfo getSyncInfo(Object element) {
		if (element instanceof SyncInfo) {
			return ((SyncInfo) element);
		}  else if (element instanceof SyncResource) {
			SyncResource syncResource = (SyncResource)element;
			return syncResource.getSyncInfo();
		}
		return null;
	}
	
	/**
	 * Get the model object (SyncSet, SyncInfo or SyncContainer) that is the
	 * parent of the given model object.
	 * 
	 * @param syncSet
	 * @param object
	 * @return
	 */
	public Object getParent(Object object) {
		IResource resource = getResource(object);
		if (resource == null) return null;
		IContainer parent = resource.getParent();
		return getModelObject(parent);
	}

	/**
	 * Return the model object for the given IResource.
	 * @param resource
	 */
	public Object getModelObject(IResource resource) {
		if (resource.getType() == IResource.ROOT) {
			return getSyncSet();
		} else {
			return new SyncResource(getSyncSet(), resource);
		}
	}
	
	protected Object[] getModelObjects(IResource[] resources) {
		Object[] result = new Object[resources.length];
		for (int i = 0; i < resources.length; i++) {
			result[i] = getModelObject(resources[i]);
		}
		return result;
	}
}
