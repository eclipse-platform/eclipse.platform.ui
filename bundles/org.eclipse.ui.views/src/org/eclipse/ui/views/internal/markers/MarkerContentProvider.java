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

package org.eclipse.ui.views.internal.markers;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;


class MarkerContentProvider implements IStructuredContentProvider, IMarkerChangedListener {
	
	private TableViewer viewer;
	private IWorkbenchPartSite site;
	private MarkerRegistry registry;
	
	public MarkerContentProvider(IWorkbenchPartSite site, MarkerRegistry registry) {
		this.site = site;
		this.registry = registry;
		registry.addMarkerChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return registry.getElements().toArray();
	}

	/**
	 * The visual part that is using this content provider is about
	 * to be disposed. Deallocate all allocated SWT resources.
	 */
	public void dispose() {
		registry.removeMarkerChangedListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput == null) {
			IResource resource = (IResource) newInput;
			registry.setInput((IResource) newInput);
		}
		this.viewer = (TableViewer) viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.internal.markerregistry.IMarkerChangedListener#markerChanged(java.util.List, java.util.List, java.util.List)
	 */
	public void markerChanged(final List additions, final List removals, final List changes) {
		if (viewer != null) {
			site.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateViewer(additions, removals, changes);
				}
			});
		}
	}

	/**
	 * Updates the viewer given the lists of added, removed, and changes 
	 * markers. This is called inside an syncExec.
	 */
	private void updateViewer(List additions, List removals, List changes) {
			
		// The widget may have been destroyed by the time this is run.  
		// Check for this and do nothing if so.
		Control ctrl = viewer.getControl();

		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}
		
		ctrl.setRedraw(false);
	
		//update the viewer based on the marker changes.
		//process removals before additions, to avoid multiple equal elements in 
		//the viewer
		if (removals.size() > 0) {

			// Cancel any open cell editor.  We assume that the one being edited 
			// is the one being removed.
			viewer.cancelEditing();
			viewer.remove(removals.toArray());
		}

		if (additions.size() > 0) {
			viewer.add(additions.toArray());
		}

		if (changes.size() > 0) {
			viewer.update(changes.toArray(), null);
		}
		
		ctrl.setRedraw(true);
	}
	
}
