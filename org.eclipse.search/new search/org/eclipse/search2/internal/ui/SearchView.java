package org.eclipse.search2.internal.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;

import org.eclipse.search.internal.ui.SearchPluginImages;

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
public class SearchView extends PageBookView implements ISearchResultViewPart, IQueryListener, ISearchResultListener {
	private static final String MEMENTO_TYPE= "view"; //$NON-NLS-1$
	private HashMap fPartsToPages;
	private HashMap fPagesToParts;
	private HashMap fSearchViewStates;
	private SearchPageRegistry fSearchViewPageService;
	private SearchDropDownAction fSearchesDropDownAction;
	private ISearchResult fCurrentSearch;
	private DummyPart fDefaultPart;
	private long fLastUpdateTime= 0;
	private SearchAgainAction fSearchAgainAction;
	private CancelSearchAction fCancelAction;
	
	private IMemento fPageState;
	
	private static void createStandardGroups(IContributionManager menu) {
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
	}

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
		private String fId;

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
		public void setViewPart(ISearchResultViewPart part) {
			// do nothing
		}

		/** (non-Javadoc)
		 * @see org.eclipse.search2.ui.ISearchResultsPage#getUIState()
		 */
		public Object getUIState() {
			// empty implementation
			return null;
		}

	
		public void init(IPageSite pageSite) {
			super.init(pageSite);
			getSite().setSelectionProvider(null);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search.ui.ISearchResultPage#saveState(org.eclipse.ui.IMemento)
		 */
		public void saveState(IMemento memento) {
			// do nothing
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search.ui.ISearchResultPage#restoreState(org.eclipse.ui.IMemento)
		 */
		public void restoreState(IMemento memento) {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search.ui.ISearchResultPage#setID(java.lang.String)
		 */
		public void setID(String id) {
			fId= id;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.search.ui.ISearchResultPage#getID()
		 */
		public String getID() {
			return fId;
		}
	}

	public SearchView() {
		super();
		fPartsToPages= new HashMap();
		fPagesToParts= new HashMap();
		setTitleImage(SearchPluginImages.get(SearchPluginImages.T_VIEW));
		fSearchViewPageService= new SearchPageRegistry("org.eclipse.search.searchResultViewPages", "targetClass", "id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			page= fSearchViewPageService.getExtensionObject(search, ISearchResultPage.class);
			if (page == null)
				return;
		}

		// detach the previous page.
		ISearchResultPage currentPage= (ISearchResultPage) getCurrentPage();
		Object uiState= currentPage.getUIState();
		if (fCurrentSearch != null) {
			if (uiState != null)
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
		
		// TODO workaround for bug 53391
		label+= "("; //$NON-NLS-1$
		if (search != null) {
			boolean queryRunning= InternalSearchUI.getInstance().isQueryRunning(search.getQuery());
			fCancelAction.setEnabled(queryRunning);
			if (queryRunning) {
				label= label+SearchMessages.getString("SearchView.title.running"); //$NON-NLS-1$
			}
			label= label+" "+search.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// TODO workaround for bug 53391
		label+= ")"; //$NON-NLS-1$
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
		InternalSearchUI.getInstance().getSearchManager().addQueryListener(this);
	}

	private void initializeToolBar() {
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		createStandardGroups(tbm);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fCancelAction); //$NON-NLS-1$
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchesDropDownAction); //$NON-NLS-1$
		getViewSite().getActionBars().updateActionBars();
	}
		
	private void createActions() {
		fSearchesDropDownAction= new SearchDropDownAction(this);
		fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
		fSearchAgainAction= new SearchAgainAction(this);
		fCancelAction= new CancelSearchAction(this);
		fCancelAction.setEnabled(false);
	}

	public void dispose() {
		InternalSearchUI.getInstance().getSearchManager().removeQueryListener(this);
		if (fCurrentSearch != null)
			fCurrentSearch.removeListener(this);
		super.dispose();
	}

	public void queryAdded(ISearchQuery query) {
		showSearchResult(query.getSearchResult());
		fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
	}

	public void queryRemoved(ISearchQuery query) {
		InternalSearchUI.getInstance().cancelSearch(query);
		if (query.getSearchResult().equals(fCurrentSearch)) {
			showSearchResult(null);
			partActivated(fDefaultPart);
		}
		fSearchViewStates.remove(query.getSearchResult());
		fSearchesDropDownAction.disposeMenu();
		fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
	}

	public void searchResultChanged(SearchResultEvent e) {
		long now= System.currentTimeMillis();
		if (now-fLastUpdateTime > 500) {
			fLastUpdateTime= now;
			updateTitle();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menuManager) {
		menuManager.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchAgainAction);
	}

	public void queryStarting(ISearchQuery query) {
		updateTitle();
	}

	public void queryFinished(ISearchQuery query) {
		updateTitle();
	}
	
	// Methods related to saving page state. -------------------------------------------
	/**
	 * { @inheritDoc }
	 */
	public void saveState(IMemento memento) {
		for (Iterator pages = fPagesToParts.keySet().iterator(); pages.hasNext(); ) {
			ISearchResultPage page = (ISearchResultPage) pages.next();
			IMemento child= memento.createChild(MEMENTO_TYPE, page.getID()); //$NON-NLS-1$
			page.saveState(child);
		}
	}
	
	/**
	 * { @inheritDoc }
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fPageState= memento;
	}
	
	/**
	 * { @inheritDoc }
	 */
	protected void initPage(IPageBookViewPage page) {
		super.initPage(page);
		ISearchResultPage srPage= (ISearchResultPage) page;
		IMemento memento= null;
		if (fPageState != null) {
			IMemento[] mementos= fPageState.getChildren(MEMENTO_TYPE);
			for (int i= 0; i < mementos.length; i++) {
				if (mementos[i].getID().equals(srPage.getID())) {
					memento= mementos[i];
					break;
				}
			}
		}
		srPage.restoreState(memento);
	}
	
	/*
	 *  TODO workaround for focus problem. Clarify focus behaviour.
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		IPage currentPage= getCurrentPage();
		if (currentPage != null)
			currentPage.setFocus();
		else 
			super.setFocus();
	}
	
	public ISearchResultPage getActivePage() {
		IPage page= getCurrentPage();
		if (page instanceof ISearchResultPage)
			return (ISearchResultPage) page;
		return null;
	}
}
