/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.core.resources.IMarker;

class ShowSearchAction extends Action {
	private Search fSearch;
	private IMarker[] fMarkerArrayTemplate= new IMarker[0];
	
	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchAction(Search search) {
		fSearch= search;
		String desc= search.getShortDescription();
		setText(desc);
		setToolTipText(desc);
		setImageDescriptor(search.getImageDescriptor());
	}
	/**
	 *	Invoke the resource wizard selection wizard
	 *
	 *	@param browser org.eclipse.jface.parts.Window
	 */
	public void run() {
		if (fSearch != SearchManager.getDefault().getCurrentSearch())
			SearchManager.getDefault().setCurrentSearch(fSearch);
	}
}
