/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.internal.ui.SearchMessages;

public class TextSearchResultCollector implements ITextSearchResultCollector {
	
	private static final String SPACE_MATCHES= " " + SearchMessages.getString("SearchResultCollector.matches"); //$NON-NLS-2$ //$NON-NLS-1$

	private IProgressMonitor fMonitor;
	private ISearchResultView fView;
	private TextSearchOperation fOperation;
	private int fMatchCount= 0;
		
	/**
	 * Returns the progress monitor used to setup and report progress.
	 */
	public IProgressMonitor getProgressMonitor() {
		return fMonitor;
	}
	
	void setProgressMonitor(IProgressMonitor pm) {
		fMonitor= pm;
	}
	
	/**
	 * Called before the actual search starts.
	 */
	public void aboutToStart() throws CoreException {
		fView= SearchUI.getSearchResultView();
		fMatchCount= 0;
		if (fView != null) {
			fView.searchStarted(
				TextSearchPage.EXTENSION_POINT_ID,
				fOperation.getDescription(),
				fOperation.getImageDescriptor(),
				null,
				null,
				new GotoMarkerAction(),
				new GroupByKeyComputer(),
				fOperation);
		}
		SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
	}
	 
	/**
	 * Accepts the given search result.
	 */
	public void accept(final IResource resource, String line, int start, int length, final int lineNumber) throws CoreException {
		IMarker marker= resource.createMarker(SearchUI.SEARCH_MARKER);
		HashMap attributes= new HashMap(4);
		attributes.put(SearchUI.LINE, line);
		attributes.put(IMarker.CHAR_START, new Integer(start));
		attributes.put(IMarker.CHAR_END, new Integer(start + length));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		marker.setAttributes(attributes);
		
		fView.addMatch(resource.getFullPath().lastSegment().toString(), resource, resource, marker);
		if (!getProgressMonitor().isCanceled())
			getProgressMonitor().subTask(++fMatchCount + SPACE_MATCHES);
	}
	
	/**
	 * Called when the search has ended.
	 */
	public void done() {
		if (!getProgressMonitor().isCanceled())
			getProgressMonitor().setTaskName(SearchMessages.getString("SearchResultCollector.done") + ": " + fMatchCount + SPACE_MATCHES + "   "); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		if (fView != null)
			fView.searchFinished();
			
		// Cut no longer unused references because the collector might be re-used
		fView= null;
		fMonitor= null;
	}

	void setOperation(TextSearchOperation operation) {
		fOperation= operation;
	}
}