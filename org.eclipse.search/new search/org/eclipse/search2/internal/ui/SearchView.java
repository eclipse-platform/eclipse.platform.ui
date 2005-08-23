/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o New search view sets incorrect title
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=60966)
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;

import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search2.internal.ui.text.AnnotationManagers;

public class SearchView extends PageBookView implements ISearchResultViewPart, IQueryListener {
	private static final String MEMENTO_TYPE= "view"; //$NON-NLS-1$
	private HashMap fPartsToPages;
	private HashMap fPagesToParts;
	private HashMap fSearchViewStates;
	private SearchPageRegistry fSearchViewPageService;
	private SearchDropDownAction fSearchesDropDownAction;
	private ISearchResult fCurrentSearch;
	private DummyPart fDefaultPart;
	private SearchAgainAction fSearchAgainAction;
	private CancelSearchAction fCancelAction;
	
	private IMemento fPageState;
	
	/**
	 * Creates the groups and separators for the search view's context menu
	 * 
	 * @param menu the context menu
	 */
	public static void createContextMenuGroups(IMenuManager menu) {
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	/**
	 * Creates the groups and separators for the search view's 
	 * tool bar
	 * 
	 * @param toolbar the toolbar
	 */
	public static void createToolBarGroups(IToolBarManager toolbar) {
		toolbar.add(new Separator(IContextMenuConstants.GROUP_NEW));
		toolbar.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		toolbar.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		toolbar.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
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

		/* (non-Javadoc)
		 * @see org.eclipse.search.ui.ISearchResultPage#getLabel()
		 */
		public String getLabel() {
			return ""; //$NON-NLS-1$
		}
	}

	public SearchView() {
		super();
		fPartsToPages= new HashMap();
		fPagesToParts= new HashMap();
		fSearchViewPageService= new SearchPageRegistry("org.eclipse.search.searchResultViewPages", "searchResultClass", "id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		if (search != null) {
			InternalSearchUI.getInstance().getSearchManager().touch(search.getQuery());
		}
		ISearchResultPage page= null;
		if (search != null) {
			page= fSearchViewPageService.getExtensionObject(search, ISearchResultPage.class);
			if (page == null) {
				String format= SearchMessages.SearchView_error_noResultPage; 
				String message= MessageFormat.format(format, new Object[] { search.getClass().getName() });
				SearchPlugin.log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, message, null));
				return;
			}
		}

		// detach the previous page.
		ISearchResultPage currentPage= (ISearchResultPage) getCurrentPage();
		Object uiState= currentPage.getUIState();
		if (fCurrentSearch != null) {
			if (uiState != null)
				fSearchViewStates.put(fCurrentSearch, uiState);
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
		if (page != null)
			page.setInput(search, fSearchViewStates.get(search));
		updateLabel();
		updateCancelAction();
	}
	
	public void updateLabel() {
		ISearchResultPage page= getActivePage();
		String label= ""; //$NON-NLS-1$
		if (page != null) {
			label= page.getLabel();
		}
		setContentDescription(label);
	}
	
	public ISearchResult getCurrentSearchResult() {
		return fCurrentSearch;
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	
		createActions();
		initializeToolBar();
		InternalSearchUI.getInstance().getSearchManager().addQueryListener(this);
		
		/*
		 * Register help.
		 * 
		 * XXX: This is not dynamic, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=99120
		 */ 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ISearchHelpContextIds.New_SEARCH_VIEW);
	}

	private void initializeToolBar() {
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		createToolBarGroups(tbm);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fCancelAction); //$NON-NLS-1$
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchesDropDownAction); //$NON-NLS-1$
		getViewSite().getActionBars().updateActionBars();
	}
		
	private void createActions() {
		fSearchesDropDownAction= new SearchDropDownAction(this);
		fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
		fSearchAgainAction= new SearchAgainAction(this);
		// hackery to get the shortcut to show up
		fSearchAgainAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
		fCancelAction= new CancelSearchAction(this);
		fCancelAction.setEnabled(false);
	}

	public void dispose() {
		InternalSearchUI.getInstance().getSearchManager().removeQueryListener(this);
		AnnotationManagers.searchResultActivated(getSite().getWorkbenchWindow(), null);
		super.dispose();
	}

	public void queryStarting(ISearchQuery query) {
		updateCancelAction();
	}

	public void queryFinished(ISearchQuery query) {
		updateCancelAction();
	}

	private void updateCancelAction() {
		ISearchResult result= getCurrentSearchResult();
		boolean queryRunning= false;
		if (result != null) {
			queryRunning= InternalSearchUI.getInstance().isQueryRunning(result.getQuery());
		}
		fCancelAction.setEnabled(queryRunning);
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

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menuManager) {
		ISearchResult result= getCurrentSearchResult();
		if (result != null) {
			fSearchAgainAction.setEnabled(result.getQuery().canRerun());
			menuManager.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchAgainAction);
			MenuManager showInSubMenu = new MenuManager(SearchMessages.SearchView_showIn_menu);  
			showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getViewSite().getWorkbenchWindow()));
			menuManager.appendToGroup(IContextMenuConstants.GROUP_OPEN, showInSubMenu);
		}
	}

	
	// Methods related to saving page state. -------------------------------------------

	public void saveState(IMemento memento) {
		for (Iterator pages = fPagesToParts.keySet().iterator(); pages.hasNext(); ) {
			ISearchResultPage page = (ISearchResultPage) pages.next();
			IMemento child= memento.createChild(MEMENTO_TYPE, page.getID()); //$NON-NLS-1$
			page.saveState(child);
		}
	}
	

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		createContextMenuGroups(site.getActionBars().getMenuManager());
		fPageState= memento;
		IWorkbenchSiteProgressService progressService= getProgressService();
		if (progressService != null)
			progressService.showBusyForFamily(InternalSearchUI.FAMILY_SEARCH);
	}
	

	protected void initPage(IPageBookViewPage page) {
		super.initPage(page);
		page.getSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), fSearchAgainAction);
		page.getSite().getActionBars().updateActionBars();

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
	
	public IWorkbenchSiteProgressService getProgressService() {
		IWorkbenchSiteProgressService service = null;
		Object siteService =
			getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if(siteService != null)
			service = (IWorkbenchSiteProgressService) siteService;
		return service;
	}

    public void showBusy(boolean busy) {
        super.showBusy(busy);
        getProgressService().warnOfContentChange();
    }
    
	public Object getAdapter(Class adapter) {
		Object superAdapter= super.getAdapter(adapter);
		if (superAdapter != null)
			return superAdapter;
		if (adapter == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					return new ShowInContext(null, getSelectionProvider().getSelection());
				}
			};
		}
		return null;
	}
}
