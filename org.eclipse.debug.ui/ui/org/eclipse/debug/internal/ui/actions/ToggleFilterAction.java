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
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * A generic Toggle filter action, meant to be subclassed to provide
 * a specific filter.
 */
public abstract class ToggleFilterAction extends Action {

	/**
	 * The viewer that this action works for
	 */
	private StructuredViewer fViewer;
	
	/**
	 * The filter this action applies to the viewer
	 */
	private ViewerFilter fViewerFilter;

	public void run() {
		valueChanged(isChecked());
	}
	/**
	 * Adds or removes the viewer filter depending
	 * on the value of the parameter.
	 */
	protected void valueChanged(final boolean on) {
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
			public void run() {
				if (on) {
					ViewerFilter filter= getViewerFilter();
					ViewerFilter[] filters= getViewer().getFilters();
					boolean alreadyAdded= false;
					for (int i= 0; i < filters.length; i++) {
						ViewerFilter addedFilter= filters[i];
						if (addedFilter.equals(filter)) {
							alreadyAdded= true;
							break;
						}
					}
					if (!alreadyAdded) {
						getViewer().addFilter(filter);
					}
					
				} else {
					getViewer().removeFilter(getViewerFilter());
				}
			}
		});
	}

	/**
	 * Returns the <code>ViewerFilter</code> that this action
	 * will add/remove from the viewer, or <code>null</code>
	 * if no filter is involved.
	 */
	protected ViewerFilter getViewerFilter() {
		return fViewerFilter;
	}

	protected void setViewerFilter(ViewerFilter filter) {
		fViewerFilter= filter;
	}
	
	protected StructuredViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
}
