/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

class RemoveMatchAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveMatchAction(ISelectionProvider provider) {
		super(SearchMessages.getString("SearchResultView.removeMatch.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("SearchResultView.removeMatch.tooltip")); //$NON-NLS-1$
		fSelectionProvider= provider;
	}
	
	public void run() {
		IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.deleteMarkers.title"), SearchMessages.getString("Search.Error.deleteMarkers.message")); //$NON-NLS-2$ //$NON-NLS-1$
			}
	}
	
	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;
		
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size != 1)
			return null;
		if (selection.getFirstElement() instanceof ISearchResultViewEntry) {
			IMarker marker= ((ISearchResultViewEntry)selection.getFirstElement()).getSelectedMarker();
			if (marker != null)
				return new IMarker[] {marker};
		}
		return null;
	}
}