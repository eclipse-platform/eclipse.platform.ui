/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.custom.BusyIndicator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

class RemoveResultAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveResultAction(ISelectionProvider provider, boolean stringsDependOnMatchCount) {
		fSelectionProvider= provider;
		if (!stringsDependOnMatchCount || getSelectedEntriesCount() > 1) {
			setText(SearchMessages.getString("SearchResultView.removeEntries.text")); //$NON-NLS-1$
			setToolTipText(SearchMessages.getString("SearchResultView.removeEntries.tooltip")); //$NON-NLS-1$
		}
		else {
			setText(SearchMessages.getString("SearchResultView.removeEntry.text")); //$NON-NLS-1$
			setToolTipText(SearchMessages.getString("SearchResultView.removeEntry.tooltip")); //$NON-NLS-1$
		}
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}
	
	public void run() {
		final IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null) {
			BusyIndicator.showWhile(SearchPlugin.getActiveWorkbenchShell().getDisplay(), new Runnable() {
				public void run() {
					try {					
						SearchPlugin.getWorkspace().deleteMarkers(markers);
					} catch (CoreException ex) {
						ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.deleteMarkers.title"), SearchMessages.getString("Search.Error.deleteMarkers.message")); //$NON-NLS-2$ //$NON-NLS-1$
					}
				}
			});
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

	protected int getSelectedEntriesCount() {
		ISelection s= fSelectionProvider.getSelection();
		if (s == null || s.isEmpty() || !(s instanceof IStructuredSelection))
			return 0;
		IStructuredSelection selection= (IStructuredSelection)s;
		return selection.size();
	}
}