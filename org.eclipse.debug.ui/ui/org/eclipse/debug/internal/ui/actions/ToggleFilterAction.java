package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
	 * Returns the appropriate tool tip text depending on
	 * the state of the action.
	 */
	protected String getToolTipText(boolean on) {
		return on ? getShowText() : getHideText();
	}

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
				setToolTipText(getToolTipText(on));									
			}
		});

	}

	/**
	 * Returns the <code>ViewerFilter</code> that this action
	 * will add/remove from the viewer, or <code>null</code>
	 * if no filter is involved.
	 */
	protected abstract ViewerFilter getViewerFilter();

	
	protected abstract String getShowText();

	protected abstract String getHideText();
	
	protected StructuredViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
}