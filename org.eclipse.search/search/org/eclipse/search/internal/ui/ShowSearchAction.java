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
		String text= desc;
		int i= desc.lastIndexOf("{0}");
		if (i != -1) {
			// replace "{0}" with the match count
			int count= search.getItemCount();
			// minimize length infront of " - " to 20 and add ...
			if (i > 20 + 3) {
				if (desc.indexOf('"') == 0 && desc.indexOf('"', 1) == i - 4)
					text= desc.substring(0, 21) + "\"... - ";
				else
					text= desc.substring(0, 20) + "... - ";
			}
			else
				text= desc.substring(0, i);
			text += count;
			// cut away last 's' if count is 1
			if (count == 1 && desc.lastIndexOf('s') == (desc.length() - 1))
				text += desc.substring(i + 3, desc.length() - 1);
			else
			 	text += desc.substring(i + 3);
		}
		else {
			// minimize length to 30 and add ...
			if (desc.length() > 30)
				text= desc.substring(0, 30) + "... ";
		}
		setText(text);
		setToolTipText(search.getDescription());
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
