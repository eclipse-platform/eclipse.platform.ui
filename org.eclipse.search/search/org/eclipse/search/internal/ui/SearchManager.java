/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.IGroupByKeyComputer;
import org.eclipse.search.ui.SearchUI;


/**
 * Manage search results
 * 	@deprecated old search
 */
public class SearchManager implements IResourceChangeListener {

	static SearchManager fgDefault;

	Search fCurrentSearch= null;

	private SearchManager() {
		SearchPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private HashSet fListeners= new HashSet();
	private LinkedList fPreviousSearches= new LinkedList();
	private boolean fIsRemoveAll= false;

	public static synchronized SearchManager getDefault() {
		if (fgDefault == null)
			fgDefault= new SearchManager();
		return fgDefault;
	}

	public void dispose() {
		SearchPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Returns the list with previous searches (ISearch).
	 * @return previous searches
	 */
	LinkedList getPreviousSearches() {
		return fPreviousSearches;
	}
	/**
	 * Returns the list with current (last) results
	 * @return the current results
	 */
	ArrayList getCurrentResults() {
		if (fCurrentSearch == null)
			return new ArrayList(0);
		return (ArrayList)fCurrentSearch.getResults();
	}

	public Search getCurrentSearch() {
		return fCurrentSearch;
	}

	void removeAllSearches() {
		SearchPlugin.getWorkspace().removeResourceChangeListener(this);
		WorkspaceModifyOperation op= new WorkspaceModifyOperation(null) {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(SearchMessages.SearchManager_updating, 100);
				SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
				monitor.worked(100);
				monitor.done();
			}
		};
		boolean isAutoBuilding= SearchPlugin.setAutoBuilding(false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		} finally {
			SearchPlugin.getWorkspace().addResourceChangeListener(this);
			SearchPlugin.setAutoBuilding(isAutoBuilding);
		}

		// clear searches
		fPreviousSearches= new LinkedList();
		fCurrentSearch= null;

		// update viewers
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			handleAllSearchesRemoved(viewer);
		}
	}

	private void handleAllSearchesRemoved(SearchResultViewer viewer) {
		viewer.handleAllSearchesRemoved();
	}

	void setCurrentSearch(final Search search) {
		if (fCurrentSearch == search)
			return;

		SearchPlugin.getWorkspace().removeResourceChangeListener(this);
		WorkspaceModifyOperation op= new WorkspaceModifyOperation(null) {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				internalSetCurrentSearch(search, monitor);
			}
		};
		boolean isAutoBuilding= SearchPlugin.setAutoBuilding(false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_switchSearch_title, SearchMessages.Search_Error_switchSearch_message);
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		} finally {
			SearchPlugin.setAutoBuilding(isAutoBuilding);
		}

		getPreviousSearches().remove(search);
		getPreviousSearches().addFirst(search);
	}

	void internalSetCurrentSearch(final Search search, IProgressMonitor monitor) {
		if (fCurrentSearch != null)
			fCurrentSearch.backupMarkers();

		final Search previousSearch= fCurrentSearch;
		fCurrentSearch= search;
		monitor.beginTask(SearchMessages.SearchManager_updating, getCurrentResults().size() + 20);

		// remove current search markers
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
		}
		monitor.worked(10);

		// add search markers
		Iterator iter= getCurrentResults().iterator();
		ArrayList emptyEntries= new ArrayList(10);
		boolean filesChanged= false;
		boolean filesDeleted= false;
		IGroupByKeyComputer groupByKeyComputer= getCurrentSearch().getGroupByKeyComputer();
		while (iter.hasNext()) {
			monitor.worked(1);
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator attrPerMarkerIter= entry.getAttributesPerMarker().iterator();
			entry.clearMarkerList();
			if (entry.getResource() == null || !entry.getResource().exists()) {
				emptyEntries.add(entry);
				filesDeleted= true;
				continue;
			}
			while (attrPerMarkerIter.hasNext()) {
				IMarker newMarker= null;
				try {
					newMarker= entry.getResource().createMarker(entry.getMarkerType());
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.Search_Error_createMarker_title, SearchMessages.Search_Error_createMarker_message);
					continue;
				}
				try {
					newMarker.setAttributes((Map)attrPerMarkerIter.next());
					if (groupByKeyComputer !=null && groupByKeyComputer.computeGroupByKey(newMarker) == null) {
						filesDeleted= true;
						newMarker.delete();
						continue;
					}
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.Search_Error_markerAttributeAccess_title, SearchMessages.Search_Error_markerAttributeAccess_message);
				}
				entry.add(newMarker);
			}
			if (entry.getMatchCount() == 0)
				emptyEntries.add(entry);
			else if (!filesChanged && entry.getResource().getModificationStamp() != entry.getModificationStamp())
				filesChanged= true;
		}
		getCurrentResults().removeAll(emptyEntries);
		monitor.worked(10);

		String warningMessage= null;
		Display display= getDisplay();

		if (filesChanged)
			warningMessage= SearchMessages.SearchManager_resourceChanged;
		if (filesDeleted) {
			if (warningMessage == null)
				warningMessage= ""; //$NON-NLS-1$
			else
				warningMessage += "\n";			 //$NON-NLS-1$
			warningMessage += SearchMessages.SearchManager_resourceDeleted;
		}
		if (warningMessage != null) {
			if (display != null && !display.isDisposed()) {
				final String warningTitle= SearchMessages.SearchManager_resourceChangedWarning;
				final String warningMsg= warningMessage;
				display.syncExec(new Runnable() {
					public void run() {
						MessageDialog.openWarning(getShell(), warningTitle, warningMsg);
					}
				});
			}
		}

		// update viewers
		iter= fListeners.iterator();
		if (display != null && !display.isDisposed()) {
			final Viewer visibleViewer= ((SearchResultView)SearchUI.getSearchResultView()).getViewer();
			while (iter.hasNext()) {
				final SearchResultViewer viewer= (SearchResultViewer)iter.next();
				display.syncExec(new Runnable() {
					public void run() {
						if (previousSearch != null && viewer == visibleViewer)
							previousSearch.setSelection(viewer.getSelection());
						viewer.setInput(null);
						viewer.setPageId(search.getPageId());
						viewer.setGotoMarkerAction(search.getGotoMarkerAction());
						viewer.setContextMenuTarget(search.getContextMenuContributor());
						viewer.setActionGroupFactory(null);
						viewer.setInput(getCurrentResults());
						viewer.setActionGroupFactory(search.getActionGroupFactory());
						viewer.setSelection(fCurrentSearch.getSelection(), true);
					}
				});
			}
		}
		monitor.done();
	}

	/**
	 * Returns the number of matches
	 * @return the number of matches
	 */
	int getCurrentItemCount() {
		if (fCurrentSearch != null)
			return fCurrentSearch.getItemCount();
		return 0;
	}

	void removeAllResults() {
		fIsRemoveAll= true;
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
			fIsRemoveAll= false;
		}
	}

	void addNewSearch(final Search newSearch) {

		SearchPlugin.getWorkspace().removeResourceChangeListener(this);

		// Clear the viewers
		Iterator iter= fListeners.iterator();
		Display display= getDisplay();
		if (display != null && !display.isDisposed()) {
			final Viewer visibleViewer= ((SearchResultView)SearchUI.getSearchResultView()).getViewer();
			while (iter.hasNext()) {
				final SearchResultViewer viewer= (SearchResultViewer)iter.next();
				display.syncExec(new Runnable() {
					public void run() {
						if (fCurrentSearch != null && viewer == visibleViewer)
							fCurrentSearch.setSelection(viewer.getSelection());
						setNewSearch(viewer, newSearch);
					}
				});
			}
		}

		if (fCurrentSearch != null) {
			if (fCurrentSearch.isSameSearch(newSearch))
				getPreviousSearches().remove(fCurrentSearch);
			else
				fCurrentSearch.backupMarkers();
		}
		fCurrentSearch= newSearch;
		getPreviousSearches().addFirst(fCurrentSearch);

		// Remove the markers
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
		}
	}

	void searchFinished(ArrayList results) {
		Assert.isNotNull(results);
		getCurrentSearch().setResults(results);

		Display display= getDisplay();
		if (display == null || display.isDisposed())
			return;

		if (Thread.currentThread() == display.getThread())
			handleNewSearchResult();
		else {
			display.syncExec(new Runnable() {
				public void run() {
					handleNewSearchResult();
				}
			});
		}
		SearchPlugin.getWorkspace().addResourceChangeListener(this);
	}

	//--- Change event handling -------------------------------------------------

	void addSearchChangeListener(SearchResultViewer viewer) {
		fListeners.add(viewer);
	}

	void removeSearchChangeListener(SearchResultViewer viewer) {
		Assert.isNotNull(viewer);
		fListeners.remove(viewer);
	}

	private final void handleSearchMarkersChanged(IMarkerDelta[] markerDeltas) {
		if (fIsRemoveAll) {
			handleRemoveAll();
			fIsRemoveAll= false;
			return;
		}

		Iterator iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).getControl().setRedraw(false);

		for (int i=0; i < markerDeltas.length; i++) {
			handleSearchMarkerChanged(markerDeltas[i]);
		}

		iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).getControl().setRedraw(true);

	}

	private void handleSearchMarkerChanged(IMarkerDelta markerDelta) {
		int kind= markerDelta.getKind();
		// don't listen for adds will be done by ISearchResultView.addMatch(...)
		if (((kind & IResourceDelta.REMOVED) != 0))
			handleRemoveMatch(markerDelta.getMarker());
		else if ((kind & IResourceDelta.CHANGED) != 0)
			handleUpdateMatch(markerDelta.getMarker());
	}

	private void handleRemoveAll() {
		if (fCurrentSearch != null)
			fCurrentSearch.removeResults();
		Iterator iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).handleRemoveAll();
	}

	private void handleNewSearchResult() {
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			viewer.setInput(getCurrentResults());
		}
	}

	private void setNewSearch(SearchResultViewer viewer, Search search) {
		viewer.setInput(null);
		viewer.clearTitle();
		viewer.setPageId(search.getPageId());
		viewer.setGotoMarkerAction(search.getGotoMarkerAction());
		viewer.setContextMenuTarget(search.getContextMenuContributor());
		viewer.setActionGroupFactory(search.getActionGroupFactory());
	}

	private void handleRemoveMatch(IMarker marker) {
		SearchResultViewEntry entry= findEntry(marker);
		if (entry != null) {
			entry.remove(marker);
			if (entry.getMatchCount() == 0) {
				getCurrentResults().remove(entry);
				Iterator iter= fListeners.iterator();
				while (iter.hasNext())
					((SearchResultViewer)iter.next()).handleRemoveMatch(entry);
			}
			else {
				Iterator iter= fListeners.iterator();
				while (iter.hasNext())
					((SearchResultViewer)iter.next()).handleUpdateMatch(entry, true);
			}
		}
	}

	private void handleUpdateMatch(IMarker marker) {
		SearchResultViewEntry entry= findEntry(marker);
		if (entry != null) {
			Iterator iter= fListeners.iterator();
			while (iter.hasNext())
				((SearchResultViewer)iter.next()).handleUpdateMatch(entry, false);
		}
	}

	private SearchResultViewEntry findEntry(IMarker marker) {
		Iterator entries= getCurrentResults().iterator();
		while (entries.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)entries.next();
			if (entry.contains(marker))
				return entry;
		}
		return null;
	}

	/**
	 * Received a resource event. Since the delta could be created in a
	 * separate thread this methods post the event into the viewer's
	 * display thread.
	 * @param event the event
	 */
	public final void resourceChanged(final IResourceChangeEvent event) {
		if (event == null)
			return;

		final IMarkerDelta[] markerDeltas= event.findMarkerDeltas(SearchUI.SEARCH_MARKER, true);
		if (markerDeltas == null || markerDeltas.length < 1)
			return;

		Display display= getDisplay();
		if (display == null || display.isDisposed())
			return;

		Runnable runnable= new Runnable() {
			public void run() {
				if (getCurrentSearch() != null) {
					handleSearchMarkersChanged(markerDeltas);
					// update title and actions
					Iterator iter= fListeners.iterator();
					while (iter.hasNext()) {
						SearchResultViewer viewer= (SearchResultViewer)iter.next();
						viewer.enableActions();
						viewer.updateTitle();
					}
				}
			}
		};
		display.syncExec(runnable);
	}
	/**
	 * Find and return a valid display
	 * @return the display
	 */
	private Display getDisplay() {
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			Control control= ((Viewer)iter.next()).getControl();
			if (control != null && !control.isDisposed()) {
				Display display= control.getDisplay();
				if (display != null && !display.isDisposed())
					return display;
			}
		}
		return null;
	}
	/**
	 * Find and return a valid shell
	 * @return the shell
	 */
	private Shell getShell() {
		return SearchPlugin.getActiveWorkbenchShell();
	}
}

