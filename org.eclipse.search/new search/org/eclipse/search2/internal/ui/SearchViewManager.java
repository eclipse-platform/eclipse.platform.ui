/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search2.internal.ui;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * 
 */
public class SearchViewManager {
	
	private IQueryListener fNewQueryListener;
	private int fViewCount= 0;
	
	private LinkedList fLRUSearchViews;
	
	
	public SearchViewManager(QueryManager queryManager) {
		fNewQueryListener= new IQueryListener() {

			public void queryAdded(ISearchQuery query) {
				showNewSearchQuery(query);
			}

			public void queryRemoved(ISearchQuery query) {}
			public void queryStarting(ISearchQuery query) {}
			public void queryFinished(ISearchQuery query) {}
			
		};
		
		queryManager.addQueryListener(fNewQueryListener);
		
		fLRUSearchViews= new LinkedList();
		
	}
	
	public void dispose(QueryManager queryManager) {
		queryManager.removeQueryListener(fNewQueryListener);
	}
	
	
	protected boolean showNewSearchQuery(ISearchQuery query) {
		if (!fLRUSearchViews.isEmpty()) {
			SearchView view= (SearchView) fLRUSearchViews.getFirst();
			view.showSearchResult(query.getSearchResult());
			return true;
		}
		return false;
	}

	public ISearchResultViewPart activateSearchView(boolean useForNewSearch) {
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		
		String defaultPerspectiveId= NewSearchUI.getDefaultPerspectiveId();
		if (defaultPerspectiveId != null) {
			IWorkbenchWindow window= activePage.getWorkbenchWindow();
			if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
				try {
					activePage= PlatformUI.getWorkbench().showPerspective(defaultPerspectiveId, window);
				} catch (WorkbenchException ex) {
					// show view in current perspective
				}
			}
		}
		if (activePage != null) {
			try {
				ISearchResultViewPart viewPart= findLRUSearchResultView(activePage, useForNewSearch);
				String secondaryId= null;
				if (viewPart == null) {
					if (activePage.findViewReference(NewSearchUI.SEARCH_VIEW_ID) != null) {
						secondaryId= String.valueOf(++fViewCount); // avoid a secondary ID because of bug 125315
					}
				} else {
					secondaryId= viewPart.getViewSite().getSecondaryId();
				}
				return (ISearchResultViewPart) activePage.showView(NewSearchUI.SEARCH_VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message); 
			}	
		}
		return null;
	}
	
	public void activateSearchView(ISearchResultViewPart viewPart) {
		try {
			IWorkbenchPage activePage= viewPart.getSite().getPage();
			String secondaryId= viewPart.getViewSite().getSecondaryId();
			activePage.showView(NewSearchUI.SEARCH_VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message); 
		}	
	}

	private ISearchResultViewPart findLRUSearchResultView(IWorkbenchPage page, boolean avoidPinnedViews) {
		boolean viewFoundInPage= false;
		for (Iterator iter= fLRUSearchViews.iterator(); iter.hasNext();) {
			SearchView view= (SearchView) iter.next();
			if (page.equals(view.getSite().getPage())) {
				if (!avoidPinnedViews || !view.isPinned()) {
					return view;
				}
				viewFoundInPage= true;
			}
		}
		if (!viewFoundInPage) {
			// find unresolved views
			IViewReference[] viewReferences= page.getViewReferences();
			for (int i= 0; i < viewReferences.length; i++) {
				IViewReference curr= viewReferences[i];
				if (NewSearchUI.SEARCH_VIEW_ID.equals(curr.getId()) && page.equals(curr.getPage())) {
					SearchView view= (SearchView) curr.getView(true);
					if (view != null && (!avoidPinnedViews || !view.isPinned())) {
						return view;
					}
					
				}
			}
		}
		return null;
	}
	
	
	public void searchViewActivated(SearchView view) {
		fLRUSearchViews.remove(view);
		fLRUSearchViews.addFirst(view);
	}

	public void searchViewClosed(SearchView view) {
		fLRUSearchViews.remove(view);
	}

	public ISearchResultViewPart getActiveSearchView() {
		IWorkbenchPage activePage= SearchPlugin.getActivePage();
		if (activePage != null) {
			return findLRUSearchResultView(activePage, false);
		}
		return null;
	}
	
	
	
}
