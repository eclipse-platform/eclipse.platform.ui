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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.team.internal.ui.sync.sets.*;

/**
 * This class provides the contents for a AbstractTreeViewer using a SyncSet as the model
 */
public class SyncSetTreeContentProvider extends SyncSetContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		IResource resource = getResource(element);
		if (resource != null) {
			return members(resource);
		} else if (element instanceof SubscriberInput) {
			return members(ResourcesPlugin.getWorkspace().getRoot());
		}
		return new Object[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		IResource resource = getResource(element);
		if (resource == null) return null;
		IContainer parent = resource.getParent();
		return getModelObject(parent);
	}
	
	public AbstractTreeViewer getTreeViewer() {
		if (viewer instanceof AbstractTreeViewer) {
			return (AbstractTreeViewer)viewer;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetContentProvider#handleResourceAdditions(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	protected void handleResourceAdditions(SyncSetChangedEvent event) {
		AbstractTreeViewer tree = getTreeViewer();
		if (tree != null) {
			IResource[] added = event.getAddedRoots();
			// TODO: Should group added roots by their parent
			for (int i = 0; i < added.length; i++) {
				IResource resource = added[i];
				Object parent;
				if (resource.getType() == IResource.PROJECT) {
					parent = getSubscriberInput();
				} else {
					parent = getModelParent(resource);				
				}
				Object element = getModelObject(resource);				
				tree.add(parent, element);		
			}
		} else {
			super.handleResourceAdditions(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.views.SyncSetContentProvider#handleResourceRemovals(org.eclipse.team.ccvs.syncviews.views.SyncSetChangedEvent)
	 */
	protected void handleResourceRemovals(SyncSetChangedEvent event) {
		AbstractTreeViewer tree = getTreeViewer();
		if (tree != null) {
			IResource[] roots = event.getRemovedRoots();
			if (roots.length == 0) return;
			Object[] modelRoots = new Object[roots.length];
			for (int i = 0; i < modelRoots.length; i++) {
				modelRoots[i] = getModelObject(roots[i]);
			}
			tree.remove(modelRoots);
		} else {
			super.handleResourceRemovals(event);
		}
	}
	
	protected Object getModelParent(IResource resource) {
		return getModelObject(resource.getParent());
	}
}
