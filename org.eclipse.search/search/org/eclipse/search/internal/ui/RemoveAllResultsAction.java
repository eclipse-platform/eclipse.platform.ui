package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.action.Action;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;

class RemoveAllResultsAction extends Action {

	public RemoveAllResultsAction() {
		super(SearchPlugin.getResourceString("SearchResultView.removeAllResults.text"), SearchPluginImages.DESC_CLCL_SEARCH_REM_ALL);
		setToolTipText(SearchPlugin.getResourceString("SearchResultView.removeAllResults.tooltip"));
	}
	
	public void run() {
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.deleteMarkers.");
		}
	}
}