package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;

/**
 * Manage search results
 */
class SearchManager implements IResourceChangeListener {

	/**
	 * Check if the resource delta describes a remove all operation.
	 * This is the case if
	 * - all marker deltas are removes and there are no search markers in the marker manager
	 * - there is at least one remove and one non-remove
	 */
	private static class RemoveAllChecker implements IResourceDeltaVisitor {
		public boolean result= false;
		public int count= 0;
		public int removeCount= 0;
		public boolean visit(IResourceDelta delta) throws CoreException {
			if ( (delta.getFlags() & IResourceDelta.MARKERS) == 0)
				return true;
			IMarkerDelta[] markers= delta.getMarkerDeltas();
			if (markers == null)
				return true;
			for (int i= 0; i < markers.length; i++) {
				IMarkerDelta markerDelta= markers[i];
				if (!markerDelta.isSubtypeOf(SearchUI.SEARCH_MARKER))
					continue;
				count++;
				int kind= markerDelta.getKind();
				if ((kind & IResourceDelta.REMOVED) != 0)
					removeCount++;
			}
			result= (removeCount > 0 && count > removeCount);
			return true;
		}
	};

	static final SearchManager fgDefault= new SearchManager();
	
	Search fCurrentSearch= null;
	
	private SearchManager() {
		SearchPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	private HashSet fListeners= new HashSet();
	private LinkedList fPreviousSearches= new LinkedList();
	private boolean fIsNewSearch= false;
	
	public static SearchManager getDefault() {
		return fgDefault;
	}
	/**
	 * Returns the list with previous searches (ISearch).
	 */
	LinkedList getPreviousSearches() {
		return fPreviousSearches;
	}
	/**
	 * Returns the list with current (last) results
	 */
	ArrayList getCurrentResults() {
		if (fCurrentSearch == null)
			return new ArrayList(0);
		else
			return (ArrayList)fCurrentSearch.getResults();
	}

	Search getCurrentSearch() {
		return fCurrentSearch;
	}

	void removeAllSearches() {
		SearchPlugin.getWorkspace().removeResourceChangeListener(this);
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(SearchPlugin.getResourceString("SearchManager.updating"), 100);
				SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
				monitor.worked(100);
				monitor.done();
			}
		};
		boolean isAutoBuilding= SearchPlugin.getWorkspace().isAutoBuilding();
		if (isAutoBuilding)
			// disable auto-build during search operation
			SearchPlugin.setAutoBuilding(false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.deleteMarkers.");
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		} finally {
			SearchPlugin.getWorkspace().addResourceChangeListener(this);
			if (isAutoBuilding)
				// enable auto-building again
				SearchPlugin.setAutoBuilding(true);				
		}

		// clear searches
		fPreviousSearches= new LinkedList();
		fCurrentSearch= null;

		// update viewers
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			viewer.setInput(null);
		}
	}

	void setCurrentSearch(final Search search) {
		if (fCurrentSearch == search)
			return;
		SearchPlugin.getWorkspace().removeResourceChangeListener(this);
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				internalSetCurrentSearch(search, monitor);
			}
		};
		boolean isAutoBuilding= SearchPlugin.getWorkspace().isAutoBuilding();
		if (isAutoBuilding)
			// disable auto-build during search operation
			SearchPlugin.setAutoBuilding(false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.switchSearch.");
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		} finally {
			SearchPlugin.getWorkspace().addResourceChangeListener(this);
			if (isAutoBuilding)
				// enable auto-building again
				SearchPlugin.setAutoBuilding(true);				
		}
	}

	void internalSetCurrentSearch(final Search search, IProgressMonitor monitor) {
		if (fCurrentSearch != null)
			fCurrentSearch.backupMarkers();
				
		fCurrentSearch= search;
		monitor.beginTask(SearchPlugin.getResourceString("SearchManager.updating"), getCurrentResults().size() + 20);
		
		// remove current search markers
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.deleteMarkers.");
		}
		monitor.worked(10);

		// add search markers
		Iterator iter= getCurrentResults().iterator();
		ArrayList emptyEntries= new ArrayList(10);
		boolean filesChanged= false;
		boolean filesDeleted= false;
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
			if (!filesChanged && entry.getResource().getModificationStamp() != entry.getModificationStamp())
				filesChanged= true;
			while (attrPerMarkerIter.hasNext()) {
				IMarker newMarker= null;
				try {
					newMarker= entry.getResource().createMarker(SearchUI.SEARCH_MARKER);
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createMarker.");
					continue;
				}
				try {
					newMarker.setAttributes((Map)attrPerMarkerIter.next());
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.markerAttributeAccess.");
				}
				entry.add(newMarker);
			}
		}
		getCurrentResults().removeAll(emptyEntries);
		monitor.worked(10);
		
		String warningMessage= null;
		Display display= getDisplay();
		
		if (filesChanged)
			warningMessage= SearchPlugin.getResourceString("SearchManager.resourceChanged");
		if (filesDeleted) {
			if (warningMessage == null)
				warningMessage= "";
			else
				warningMessage += "\n";			
			warningMessage += SearchPlugin.getResourceString("SearchManager.resourceDeleted");
		}
		if (warningMessage != null) {
			if (display != null && !display.isDisposed()) {
				final String warningTitle= SearchPlugin.getResourceString("SearchManager.resourceChangedWarning");
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
			while (iter.hasNext()) {
				final SearchResultViewer viewer= (SearchResultViewer)iter.next();
				viewer.setPageId(search.getPageId());
				viewer.setGotoMarkerAction(search.getGotoMarkerAction());
				viewer.setContextMenuTarget(search.getContextMenuContributor());
				display.syncExec(new Runnable() {
					public void run() {
						viewer.internalSetLabelProvider(search.getLabelProvider());
						viewer.setInput(getCurrentResults());
					}
				});
			}
		}
		monitor.done();
	}
	/**
	 * Returns the list with current (last) results
	 */
	int getCurrentItemCount() {
		if (fCurrentSearch != null)
			return fCurrentSearch.getItemCount();
		else
			return 0;
	}

	void addNewSearch(Search newSearch) {
		if (fCurrentSearch != null) {
			if (fCurrentSearch.isSameSearch(newSearch))
				getPreviousSearches().remove(fCurrentSearch);
			else
				fCurrentSearch.backupMarkers();
		}
		fCurrentSearch= newSearch;
		getPreviousSearches().addFirst(fCurrentSearch);
	}

	void setCurrentResults(ArrayList results) {
		Assert.isNotNull(results);
		((Search)getCurrentSearch()).setResults(results);
		if (results.isEmpty()) {
			// directly update because there will be no delta
				Display display= getDisplay();
				if (display == null || display.isDisposed())
					return;
				display.syncExec(new Runnable() {
					public void run() {
						handleNewSearchResult();
					}
				});
		}
		else
			fIsNewSearch= true;
	}
	
	//--- Change event handling -------------------------------------------------

	void addSearchChangeListener(SearchResultViewer viewer) {
		fListeners.add(viewer);
	}

	void removeSearchChangeListener(SearchResultViewer viewer) {
		fListeners.remove(viewer);
	}
	/**
	 * Received a resource event. Since the delta could be created in a 
	 * separate thread this methods post the event into the viewer's 
	 * display thread.
	 */
	public final void handleResourceChanged(final IResourceChangeEvent event) {
		RemoveAllChecker checker= new RemoveAllChecker();
		IResourceDelta delta= event.getDelta();
		if (delta == null)
			return;
		final boolean removeAll= checkRemoveAll(checker, delta, null);

		if (fIsNewSearch && (checker.count > 0 || checker.removeCount > 0)) {
			fIsNewSearch= false;
			handleNewSearchResult();
			return;
		}
		
		if (removeAll) {
			if (!fIsNewSearch)
				handleRemoveAll();
			else
				fIsNewSearch= false;
			return;
		}

		IResourceDeltaVisitor visitor= new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				if ( (delta.getFlags() & IResourceDelta.MARKERS) == 0)
					return true;
				IMarkerDelta[] markers= delta.getMarkerDeltas();
				if (markers == null)
					return true;
				for (int i= 0; i < markers.length; i++) {
					IMarkerDelta markerDelta= markers[i];
					int kind= markerDelta.getKind();
					IMarker marker= markerDelta.getMarker();
					if (markerDelta.isSubtypeOf(SearchUI.SEARCH_MARKER)) {
						if ((kind & IResourceDelta.ADDED) != 0)
							handleAddMatch(marker);
						else if (!removeAll && ((kind & IResourceDelta.REMOVED) != 0))
							handleRemoveMatch(marker);
						else if ((kind & IResourceDelta.CHANGED) != 0)
							handleUpdateMatch(marker);
					}
				}
				return true;
			}
		};
		try {
			delta.accept(visitor);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.resourceChanged.");
		}
	}

	private void handleRemoveAll() {
		if (fCurrentSearch != null)
			((Search)fCurrentSearch).removeResults();
		Iterator iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).handleRemoveAll();
	}

	private void handleAddMatch(IMarker marker) {
		Object groupByKey= getCurrentSearch().getGroupByKeyComputer().computeGroupByKey(marker);
		SearchResultViewEntry entry= findEntry(groupByKey);
		if (entry == null) {
			entry= new SearchResultViewEntry(groupByKey, marker.getResource());
			getCurrentResults().add(entry);
			entry.add(marker);
			Iterator iter= fListeners.iterator();
			while (iter.hasNext())
				((SearchResultViewer)iter.next()).handleAddMatch(entry);
		}
		else {
			entry.add(marker);
			Iterator iter= fListeners.iterator();
			while (iter.hasNext())
				((SearchResultViewer)iter.next()).handleUpdateMatch(entry);
		}
	}
	
	private void handleNewSearchResult() {
		Iterator iter= fListeners.iterator();
		final Search search= getCurrentSearch();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			viewer.setPageId(search.getPageId());
			viewer.setContextMenuTarget(search.getContextMenuContributor());
			viewer.internalSetLabelProvider(search.getLabelProvider());
			viewer.setGotoMarkerAction(search.getGotoMarkerAction());
			viewer.setInput(getCurrentResults());
		}
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
					((SearchResultViewer)iter.next()).handleUpdateMatch(entry);
			}
		}
	}

	private void handleUpdateMatch(IMarker marker) {
		SearchResultViewEntry entry= findEntry(marker);
		if (entry != null) {
			Iterator iter= fListeners.iterator();
			while (iter.hasNext())
				((SearchResultViewer)iter.next()).handleUpdateMatch(entry);
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

	private SearchResultViewEntry findEntry(Object key) {
		if (key == null)
			return null;
		Iterator entries= getCurrentResults().iterator();
		while (entries.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)entries.next();
			if (key.equals(entry.getGroupByKey()))
				return entry;
		}
		return null;
	}
	/**
	 * Received a resource event. Since the delta could be created in a 
	 * separate thread this methods post the event into the viewer's 
	 * display thread.
	 */
	public final void resourceChanged(final IResourceChangeEvent event) {
		Display display= getDisplay();
		if (display == null || display.isDisposed())
			return;
		Runnable runnable= new Runnable() {
			public void run() {
				handleResourceChanged(event);
				// update title and actions
				Iterator iter= fListeners.iterator();
				while (iter.hasNext()) {
					SearchResultViewer viewer= (SearchResultViewer)iter.next();
					viewer.enableActions();
					viewer.updateTitle();
				}
			}
		};
		display.syncExec(runnable);	
	}
	/**
	 * Find and return a valid display
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
	 */
	private Shell getShell() {
		return SearchPlugin.getActiveWorkbenchShell();
	}
	/**
	 * Check if the resource delta describes a remove all operation.
	 * This is the case if
	 * - all marker deltas are removes and there are no search markers in the marker manager
	 * - there is at least one remove and one non-remove
	 */
	private boolean checkRemoveAll(RemoveAllChecker checker, IResourceDelta delta, String id) {
		try {
			delta.accept(checker);
			int markerCount= SearchPlugin.getWorkspace().getRoot().findMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE).length;
			return (markerCount == 0 && checker.count == checker.removeCount);
		} catch(CoreException e) {
			// handle each delta individually
			return false;
		}
	}
}

