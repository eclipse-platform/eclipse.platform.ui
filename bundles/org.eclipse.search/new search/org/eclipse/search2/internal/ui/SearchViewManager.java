/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search2.internal.ui;

import java.util.LinkedList;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

public class SearchViewManager {

	private IQueryListener fNewQueryListener;
	private int fViewCount= 0;

	private LinkedList<SearchView> fLRUSearchViews;


	public SearchViewManager(QueryManager queryManager) {
		fNewQueryListener= new IQueryListener() {

			@Override
			public void queryAdded(ISearchQuery query) {
				showNewSearchQuery(query);
			}

			@Override
			public void queryRemoved(ISearchQuery query) {}
			@Override
			public void queryStarting(ISearchQuery query) {}
			@Override
			public void queryFinished(ISearchQuery query) {}

		};

		queryManager.addQueryListener(fNewQueryListener);

		fLRUSearchViews= new LinkedList<>();

	}

	public void dispose(QueryManager queryManager) {
		queryManager.removeQueryListener(fNewQueryListener);
	}


	protected boolean showNewSearchQuery(ISearchQuery query) {
		if (!fLRUSearchViews.isEmpty()) {
			SearchView view= fLRUSearchViews.getFirst();
			view.showSearchResult(query.getSearchResult());
			return true;
		}
		return false;
	}

	public ISearchResultViewPart activateSearchView(boolean avoidPinnedViews) {
		return activateSearchView(avoidPinnedViews, false);
	}

	public ISearchResultViewPart activateSearchView(boolean avoidPinnedViews, boolean openInNew) {
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
				ISearchResultViewPart viewPart= null;
				if (!openInNew) {
					viewPart= findLRUSearchResultView(activePage, avoidPinnedViews);
				}
				String secondaryId= null;
				if (viewPart == null) {
					if (activePage.findViewReference(NewSearchUI.SEARCH_VIEW_ID) != null)
						secondaryId= String.valueOf(++fViewCount); // avoid a secondary ID because of bug 125315
				} else if (!SearchPreferencePage.isViewBroughtToFront())
					return viewPart;
				else
					secondaryId= viewPart.getViewSite().getSecondaryId();

				return (ISearchResultViewPart) activePage.showView(NewSearchUI.SEARCH_VIEW_ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message);
			}
		}
		return null;
	}

	public boolean isShown(ISearchQuery query) {
		for (SearchView view : fLRUSearchViews) {
			ISearchResult currentSearchResult= view.getCurrentSearchResult();
			if (currentSearchResult != null && query == currentSearchResult.getQuery()) {
				return true;
			}
		}
		return false;
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
		for (SearchView view : fLRUSearchViews) {
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
			for (IViewReference curr : viewReferences) {
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
