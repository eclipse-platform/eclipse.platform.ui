/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;

class RemoveAllResultsAction extends Action {

	public RemoveAllResultsAction() {
		super(SearchMessages.getString("SearchResultView.removeAllResults.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchResultView.removeAllResults.tooltip")); //$NON-NLS-1$
	}
	
	public void run() {
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.deleteMarkers.title"), SearchMessages.getString("Search.Error.deleteMarkers.message")); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}