package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000, 2001
 */
import org.eclipse.jface.action.Action;

import org.eclipse.core.resources.IMarker;

class ShowSearchAction extends Action {
	private Search fSearch;
	private IMarker[] fMarkerArrayTemplate= new IMarker[0];
	
	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchAction(Search search) {
		super("");
		fSearch= search;
		String desc= search.getDescription();
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
