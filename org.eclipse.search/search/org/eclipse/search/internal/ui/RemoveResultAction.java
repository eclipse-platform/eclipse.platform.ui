/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.Iterator;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchResultViewEntry;

class RemoveResultAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveResultAction(ISelectionProvider provider) {
		super(SearchPlugin.getResourceString("SearchResultView.remove.text"));
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.remove.tooltip"));
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
		if (size <= 0)
			return null;
		ArrayList markers= new ArrayList(size * 3);
		int markerCount= 0;
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			markerCount += entry.getMatchCount();
			markers.addAll(entry.getMarkers());
		}
		return (IMarker[])markers.toArray(new IMarker[markerCount]);
	}
}