/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchResultViewEntry;

class RemoveMatchAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveMatchAction(ISelectionProvider provider) {
		super(SearchPlugin.getResourceString("SearchResultView.removeMatch.text"), SearchPluginImages.DESC_CLCL_SEARCH_REM);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.removeMatch.tooltip"));
		fSelectionProvider= provider;
	}
	
	public void run() {
		IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.deleteMarkers.");
			}
	}
	
	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;
		
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size != 1)
			return null;
		IMarker[] result= new IMarker[size];
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			ISearchResultViewEntry entry= (ISearchResultViewEntry)iter.next();
			result[i]= entry.getSelectedMarker();
		}
		return result;
	}
}