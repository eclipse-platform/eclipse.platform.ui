package org.eclipse.ui.views.markers.internal;
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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
/**
 * The MarkerTreeContentProvider is the content provider for the marker
 * trees.
 * @since 3.2
 *
 */
public class MarkerTreeContentProvider implements ITreeContentProvider {
	
	DeferredTreeContentManager manager;
	TreeViewer viewer;
	IDeferredWorkbenchAdapter input;
	IWorkbenchPartSite partSite;
	
	/**
	 * Create a new content provider for the view.
	 * @param site
	 */
	MarkerTreeContentProvider(IWorkbenchPartSite site){
		partSite = site;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return manager.getChildren(parentElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if(element instanceof ConcreteMarker){
    		return input;		
    	}
    	return null;
	}

	public boolean hasChildren(Object element) {
		return input.equals(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return manager.getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		//Nothing to do here.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		manager = new DeferredTreeContentManager(this, this.viewer,partSite);
		input = (IDeferredWorkbenchAdapter) newInput;	

	}

	/**
	 * Refresh the children without changing the widget
	 *  yet.
	 */
	public void refresh() {
		getChildren(input);		
	}

}
