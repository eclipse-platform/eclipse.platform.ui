package org.eclipse.search2.internal.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultPageContainer;
import org.eclipse.search.ui.ISearchResultPresentation;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchJobEvent;
import org.eclipse.search.ui.SearchResultEvent;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.basic.views.IContextMenuConstants;

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * @author Thomas Mäder
 *
 */
public class SearchView extends PageBookView implements ISearchResultPageContainer, ISearchResultListener, ISearchResultChangedListener {
	private HashMap fPartsToPages;
	private HashMap fPagesToParts;
	private HashMap fSearchViewStates;
	private ExtensionService fSearchViewPageService;
	private Action fSearchesDropDownAction;
	private ISearchResult fCurrentSearch;
	private DummyPart fDefaultPart;
	private HashMap fSearchCategories;
	private long fLastUpdateTime= 0;
	private SearchAgainAction fSearchAgainAction;
	
	class DummyPart implements IWorkbenchPart {
		public void addPropertyListener(IPropertyListener listener) {/*dummy*/}
		public void createPartControl(Composite parent) {/*dummy*/}
		public void dispose() {/*dummy*/}
		public IWorkbenchPartSite getSite() { return null; }
		public String getTitle() { return null; }
		public Image getTitleImage() { return null; }
		public String getTitleToolTip() { return null; }
		public void removePropertyListener(IPropertyListener listener) {/*dummy*/}
		public void setFocus() {/*dummy*/}
		public Object getAdapter(Class adapter) { return null; }
	}
	
	class EmptySearchView extends Page implements ISearchResultPage {
		Control fControl;

		public void createControl(Composite parent) {
			fControl= new Tree(parent, SWT.NONE);
			//fControl.setText(SearchMessages.getString("SearchView.empty.message")); //$NON-NLS-1$
		}

		public Control getControl() {
			return fControl;
		}

		public void setFocus() {
			if (fControl != null)
				fControl.setFocus();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search2.ui.ISearchResultsPage#setInput(org.eclipse.search2.ui.ISearchResult, java.lang.Object)
		 */
		public void setInput(ISearchResult search, Object viewState) {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search2.ui.ISearchResultsPage#setViewPart(org.eclipse.search2.ui.ISearchResultView)
		 */
		public void setViewPart(ISearchResultPageContainer part) {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search2.ui.ISearchResultsPage#getUIState()
		 */
		public Object getUIState() {
			// TODO Auto-generated method stub
			return null;
		}

	
	}

	public SearchView() {
		super();
		fPartsToPages= new HashMap();
		fPagesToParts= new HashMap();
		setTitleImage(SearchPluginImages.get(SearchPluginImages.T_VIEW));
		fSearchViewPageService= new ExtensionService("org.eclipse.search.searchResultViewPages", "targetClass"); //$NON-NLS-1$ //$NON-NLS-2$
		fSearchCategories= new HashMap();
		fSearchViewStates= new HashMap();
	}

	protected IPage createDefaultPage(PageBook book) {
		IPageBookViewPage page= new EmptySearchView();
		page.createControl(book);
		initPage(page);
		DummyPart part= new DummyPart();
		fPartsToPages.put(part, page);
		fPagesToParts.put(page, part);
		fDefaultPart= part;
		return page;
	}

	protected PageRec doCreatePage(IWorkbenchPart part) {
		IPageBookViewPage page = (IPageBookViewPage) fPartsToPages.get(part);
		initPage(page);
		page.createControl(getPageBook());
		PageRec rec = new PageRec(part, page);
		return rec;
	}

	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();
						
		// empty cross-reference cache
		fPartsToPages.remove(part);
	}

	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}

	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof DummyPart;
	}
	
	public void showSearchResult(ISearchResult search) {
		ISearchResultPage page= null;
		if (search != null) {
			page= (ISearchResultPage) fSearchViewPageService.getExtensionObject(search, ISearchResultPage.class);
			if (page == null)
				return;
		}

		// detach the previous page.
		ISearchResultPage currentPage= (ISearchResultPage) getCurrentPage();
		Object uiState= currentPage.getUIState();
		if (fCurrentSearch != null) {
			fSearchViewStates.put(fCurrentSearch, uiState);
			fCurrentSearch.removeListener(this);
		}
		currentPage.setInput(null, null);
		
		// switch to a new page
		if (page != null && page != currentPage) {
			IWorkbenchPart part= (IWorkbenchPart) fPagesToParts.get(page);
			if (part == null) {
				part= new DummyPart();
				fPagesToParts.put(page, part);
				fPartsToPages.put(part, page);
				page.setViewPart(this);
			}
			partActivated(part);
		}
		
		// connect to the new pages
		fCurrentSearch= search;
		if (fCurrentSearch != null)
			fCurrentSearch.addListener(this);
		if (page != null)
			page.setInput(search, fSearchViewStates.get(search));
		updateTitle(search);
	}
	
	private void updateTitle(ISearchResult search) {
		String label= SearchMessages.getString("SearchView.title.search"); //$NON-NLS-1$
		if (search != null) {
			ISearchResultPresentation category= getSearchCategory(search);
			if (search.isRunning()) {
				label= label+SearchMessages.getString("SearchView.title.running"); //$NON-NLS-1$
			}
			if (category != null) 
				label= label+" ("+category.getText(search)+")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setTitle(label);
	}
	
	public void updateTitle() {
		if (getPageBook() != null && !getPageBook().isDisposed()) {
			getPageBook().getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateTitle(fCurrentSearch);
				}
			});
		}
	}

	public ISearchResult getCurrentSearchResult() {
		return fCurrentSearch;
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActions();
		initializeToolBar();
		NewSearchUI.getSearchManager().addSearchResultListener(this);
	}

	private void initializeToolBar() {
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		tbm.add(new Separator("ViewSpecificGroup")); //$NON-NLS-1$
		tbm.add(new Separator("SearchesGroup")); //$NON-NLS-1$
		tbm.appendToGroup("SearchesGroup", fSearchesDropDownAction); //$NON-NLS-1$
		getViewSite().getActionBars().updateActionBars();
	}
		
	private void createActions() {
		fSearchesDropDownAction= new SearchDropDownAction(this);
		fSearchAgainAction= new SearchAgainAction(this);
	}

	public void dispose() {
		NewSearchUI.getSearchManager().removeSearchResultListener(this);
		Iterator categories= fSearchCategories.values().iterator();
		while (categories.hasNext()) {
			ISearchResultPresentation element= (ISearchResultPresentation) categories.next();
			element.dispose();
		}
		if (fCurrentSearch != null)
			fCurrentSearch.removeListener(this);
		super.dispose();
	}

	public void searchResultAdded(ISearchResult search) {
		// should post to ui thread
		fSearchCategories.put(search, search.createPresentation(this));
		showSearchResult(search);
	}

	public void searchResultRemoved(ISearchResult search) {
		NewSearchUI.cancelSearch(search);
		if (search.equals(fCurrentSearch)) {
			showSearchResult(null);
			partActivated(fDefaultPart);
		}
		ISearchResultPresentation category= (ISearchResultPresentation) fSearchCategories.remove(search);
		if (category != null)
			category.dispose();
	}

	public ISearchResultPresentation getSearchCategory(ISearchResult result) {
		return (ISearchResultPresentation) fSearchCategories.get(result);
	}


	public void searchResultsChanged(SearchResultEvent e) {
		if (e instanceof SearchJobEvent) {
			updateTitle();
			fLastUpdateTime= 0;
		} else {
			long now= System.currentTimeMillis();
			if (now-fLastUpdateTime > 500) {
				fLastUpdateTime= now;
				updateTitle();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menuManager) {
		menuManager.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchAgainAction);
	}
}
