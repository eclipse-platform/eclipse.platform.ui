package org.eclipse.search.internal.ui.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;

public class TextSearchResultCollector implements ITextSearchResultCollector {
	
	private IProgressMonitor fMonitor;
	private ISearchResultView fView;
	private TextSearchOperation fOperation;
	private int fMatchCount= 0;
	private String SPACE_MATCHES= " " + SearchPlugin.getResourceString("SearchResultCollector.matches");
		
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
			getProgressMonitor().setTaskName(SearchPlugin.getResourceString("SearchResultCollector.done") + ": " + fMatchCount + SPACE_MATCHES + "   ");
		if (fView != null)
			fView.searchFinished();
	}

	void setOperation(TextSearchOperation operation) {
		fOperation= operation;
	}
}