/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import org.eclipse.core.commands.operations.IUndoContext;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSwitcher;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.OpenSearchDialogAction;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

public class SearchView extends PageBookView implements ISearchResultViewPart, IQueryListener {

	private static final String MEMENTO_TYPE= "view"; //$NON-NLS-1$
	private static final String MEMENTO_KEY_IS_PINNED= "isPinned"; //$NON-NLS-1$
	private static final String MEMENTO_KEY_LAST_ACTIVATION= "org.eclipse.search.lastActivation"; //$NON-NLS-1$
	private static final String MEMENTO_KEY_RESTORE= "org.eclipse.search.restore"; //$NON-NLS-1$
	private HashMap fPartsToPages;
	private HashMap fPagesToParts;
	private HashMap fSearchViewStates;
	private SearchPageRegistry fSearchViewPageService;
	private SearchHistoryDropDownAction fSearchesDropDownAction;
	private ISearchResult fCurrentSearch;
	private DummyPart fDefaultPart;
	private SearchAgainAction fSearchAgainAction;
	private CancelSearchAction fCancelAction;
	private PinSearchViewAction fPinSearchViewAction;
	private UndoRedoActionGroup fUndoRedoActionGroup;

	private IMemento fPageState;
	private boolean fIsPinned;
	private int fActivationCount= 0;
	private String fDefaultPartName;

	private Composite fPageContent;
	private Link fDescription;
	private Composite fDescriptionComposite;

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
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new Separator(IContextMenuConstants.GROUP_EDIT));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	/**
	 * Creates the groups and separators for the search view's context menu
	 *
	 * @param menu the context menu
	 */
	private static void createViewMenuGroups(IMenuManager menu) {
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_EDIT));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_FILTERING));
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
		toolbar.add(new Separator(IContextMenuConstants.GROUP_EDIT));
		toolbar.add(new GroupMarker(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		toolbar.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
		toolbar.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
	}

	static class DummyPart implements IWorkbenchPart {
		private int fLastActivation= 0;
		public void setLastActivation(int lastActivation) {
			fLastActivation= lastActivation;
		}
		public int getLastActivation() {
			return fLastActivation;
		}
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

	static class EmptySearchView extends Page implements ISearchResultPage {
		private Composite fControl;
		private String fId;

		public void createControl(Composite parent) {
			Color background= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));

			composite.setBackground(background);

			Link link= new Link(composite, SWT.NONE);
			link.setText(SearchMessages.SearchView_empty_search_label);
			link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
			link.setBackground(background);
			link.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new OpenSearchDialogAction().run();
				}
			});

			fControl= composite;
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
			// add something to avoid the empty menu
			IMenuManager menuManager= pageSite.getActionBars().getMenuManager();
			menuManager.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, new OpenSearchPreferencesAction());
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
		fSearchViewPageService= new SearchPageRegistry();
		fSearchViewStates= new HashMap();
		fIsPinned= false;
	}

	/**
	 * @return the search result page registry
	 */
	public SearchPageRegistry getSearchPageRegistry() {
		return fSearchViewPageService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		if (part == this) {
			InternalSearchUI.getInstance().getSearchViewManager().searchViewActivated(this);
		}
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
		ISearchResultPage newPage= null;
		if (search != null) {
			newPage= fSearchViewPageService.findPageForSearchResult(search, true);
			if (newPage == null) {
				String format= SearchMessages.SearchView_error_noResultPage;
				String message= MessageFormat.format(format, new Object[] { search.getClass().getName() });
				SearchPlugin.log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, message, null));
				return;
			}
		}
		internalShowSearchPage(newPage, search);
	}

	public void showEmptySearchPage(String pageId) {
		ISearchResultPage newPage= fSearchViewPageService.findPageForPageId(pageId, true);
		internalShowSearchPage(newPage, null);
	}


	private void internalShowSearchPage(ISearchResultPage page, ISearchResult search) {
		// detach the previous page.
		ISearchResultPage currentPage= (ISearchResultPage) getCurrentPage();
		if (fCurrentSearch != null && currentPage != null) {
			fSearchViewStates.put(fCurrentSearch, currentPage.getUIState());
			currentPage.setInput(null, null);
		}

		fCurrentSearch= search;

		if (page != null) {
			if (page != currentPage) {
				DummyPart part= (DummyPart) fPagesToParts.get(page);
				if (part == null) {
					part= new DummyPart();
					fPagesToParts.put(page, part);
					fPartsToPages.put(part, page);
					page.setViewPart(this);
				}
				part.setLastActivation(++fActivationCount);
				partActivated(part);
			}

			// connect to the new pages
			Object uiState= search != null ? fSearchViewStates.get(search) : null;
			page.setInput(search, uiState);
		}
		updatePartName();
		updateLabel();
		updateCancelAction();

		updateHelpContextID(page);

	}

	private void updateHelpContextID(ISearchResultPage page) {
		String helpContextId= null;
		String pageId= null;

		if (page != null)
			pageId= page.getID();

		if (pageId != null)
			helpContextId= fSearchViewPageService.getHelpContextId(pageId);

		if (helpContextId == null)
			helpContextId= ISearchHelpContextIds.New_SEARCH_VIEW;

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fPageContent.getParent(), helpContextId);
	}

	public void updateLabel() {
		ISearchResultPage page= getActivePage();
		String label= ""; //$NON-NLS-1$
		if (page != null) {
			label= LegacyActionTools.escapeMnemonics(page.getLabel());
		}
		if (!fPageContent.isDisposed()) {
			if (label.length() == 0) {
				if (fDescriptionComposite != null) {
					fDescriptionComposite.dispose();
					fDescriptionComposite= null;
					fPageContent.layout();
				}
			} else {
				if (fDescriptionComposite == null) {
					fDescriptionComposite= new Composite(fPageContent, SWT.NONE);
					fDescriptionComposite.moveAbove(null);

					GridLayout layout= new GridLayout();
					layout.marginHeight= 0;
					layout.marginWidth= 0;
					layout.verticalSpacing= 0;
					fDescriptionComposite.setLayout(layout);
					fDescriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

					fDescription= new Link(fDescriptionComposite, SWT.NONE);
					GridData gridData= new GridData(SWT.FILL, SWT.CENTER, true, false);
					gridData.horizontalIndent= 5;
					fDescription.setLayoutData(gridData);
					fDescription.setText(label);

					Label separator= new Label(fDescriptionComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
					separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					fPageContent.layout();
				} else {
					fDescription.setText(label);
				}
			}
		}
	}

	public ISearchResult getCurrentSearchResult() {
		return fCurrentSearch;
	}

	public void createPartControl(Composite parent) {
		createActions();

		fPageContent= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		fPageContent.setLayout(layout);

		fDescriptionComposite= null;

		super.createPartControl(fPageContent);
		getPageBook().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fDefaultPartName= getPartName();
		initializeToolBar();
		InternalSearchUI.getInstance().getSearchManager().addQueryListener(this);
		initializePageSwitcher();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ISearchHelpContextIds.New_SEARCH_VIEW);
		restorePageFromMemento();

		showLatestSearch();
	}

	private void initializePageSwitcher() {
		new PageSwitcher(this) {
			public void activatePage(Object page) {
				ISearchResult searchResult= ((ISearchQuery) page).getSearchResult();
				InternalSearchUI.getInstance().showSearchResult(SearchView.this, searchResult, false);
			}

			public ImageDescriptor getImageDescriptor(Object page) {
				ISearchResult searchResult= ((ISearchQuery) page).getSearchResult();
				return searchResult.getImageDescriptor();
			}

			public String getName(Object page) {
				ISearchResult searchResult= ((ISearchQuery) page).getSearchResult();
				return searchResult.getLabel();
			}

			public Object[] getPages() {
				return NewSearchUI.getQueries();
			}
		};
	}

	private void restorePageFromMemento() {
		if (fPageState != null) {
			int bestActivation= -1;
			IMemento restorePageMemento= null;
			IMemento[] children= fPageState.getChildren(MEMENTO_TYPE);
			for (int i= 0; i < children.length; i++) {
				IMemento pageMemento= children[i];
				if (pageMemento.getString(MEMENTO_KEY_RESTORE) != null) {
					Integer lastActivation= pageMemento.getInteger(MEMENTO_KEY_LAST_ACTIVATION);
					if (lastActivation != null && lastActivation.intValue() > bestActivation) {
						bestActivation= lastActivation.intValue();
						restorePageMemento= pageMemento;
					}
				}
			}
			if (restorePageMemento != null) {
				showEmptySearchPage(restorePageMemento.getID());
				String pinned= fPageState.getString(MEMENTO_KEY_IS_PINNED);
				if (String.valueOf(true).equals(pinned)) {
					setPinned(true);
					fPinSearchViewAction.update();
				}
			}
		}
	}

	private void initializeToolBar() {
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		createToolBarGroups(tbm);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchAgainAction);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fCancelAction);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchesDropDownAction);
		tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fPinSearchViewAction);
		getViewSite().getActionBars().updateActionBars();
	}

	private void createActions() {
		fSearchesDropDownAction= new SearchHistoryDropDownAction(this);
		fSearchesDropDownAction.updateEnablement();
		fSearchAgainAction= new SearchAgainAction(this);
		fSearchAgainAction.setEnabled(false);
		fSearchAgainAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);
		fCancelAction= new CancelSearchAction(this);
		fCancelAction.setEnabled(false);
		fPinSearchViewAction= new PinSearchViewAction(this);

		IUndoContext workspaceContext= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		fUndoRedoActionGroup= new UndoRedoActionGroup(getViewSite(), workspaceContext, true);
	}

	public void dispose() {
		if (fUndoRedoActionGroup != null) {
			fUndoRedoActionGroup.dispose();
		}
		InternalSearchUI.getInstance().getSearchViewManager().searchViewClosed(this);
		InternalSearchUI.getInstance().getSearchManager().removeQueryListener(this);
		super.dispose();
	}

	public void queryStarting(ISearchQuery query) {
		if (fCurrentSearch != null && fCurrentSearch.equals(query.getSearchResult())) {
			updateCancelAction();
		}
	}

	public void queryFinished(ISearchQuery query) {
		if (fCurrentSearch != null && fCurrentSearch.equals(query.getSearchResult())) {
			updateCancelAction();
		}
	}

	private void updateCancelAction() {
		ISearchResult result= getCurrentSearchResult();
		boolean queryRunning= false;
		if (result != null) {
			queryRunning= InternalSearchUI.getInstance().isQueryRunning(result.getQuery());
		}
		fCancelAction.setEnabled(queryRunning);
		fSearchAgainAction.setEnabled(!queryRunning && result != null && result.getQuery().canRerun());
	}

	public void queryAdded(ISearchQuery query) {
		fSearchesDropDownAction.updateEnablement();
	}

	public void queryRemoved(ISearchQuery query) {
		InternalSearchUI.getInstance().cancelSearch(query);
		if (query.getSearchResult().equals(fCurrentSearch)) {
			showSearchResult(null);
			partActivated(fDefaultPart);
		}
		fSearchViewStates.remove(query.getSearchResult());
		fSearchesDropDownAction.disposeMenu();
		fSearchesDropDownAction.updateEnablement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menuManager) {
		ISearchResult result= getCurrentSearchResult();
		if (result != null) {
			menuManager.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchAgainAction);
			// first check if we have a selection for the show in mechanism, bugzilla 127718
			IShowInSource showInSource= (IShowInSource) getAdapter(IShowInSource.class);
			if (showInSource != null) {
				ShowInContext context= showInSource.getShowInContext();
				if (context != null) {
					ISelection sel= context.getSelection();
					if (sel != null && !sel.isEmpty()) {
						MenuManager showInSubMenu= new MenuManager(getShowInMenuLabel());
						showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN.create(getViewSite().getWorkbenchWindow()));
						menuManager.appendToGroup(IContextMenuConstants.GROUP_OPEN, showInSubMenu);
					}
				}
			}
		}
	}

	private String getShowInMenuLabel() {
		String keyBinding= null;

		IBindingService bindingService= (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		if (bindingService != null)
			keyBinding= bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_QUICK_MENU);

		if (keyBinding == null)
			keyBinding= ""; //$NON-NLS-1$

		return NLS.bind(SearchMessages.SearchView_showIn_menu, keyBinding);
	}


	// Methods related to saving page state. -------------------------------------------

	public void saveState(IMemento memento) {
		for (Iterator iter= fPagesToParts.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry= (Map.Entry) iter.next();
			ISearchResultPage page= (ISearchResultPage) entry.getKey();
			DummyPart part= (DummyPart) entry.getValue();

			IMemento child= memento.createChild(MEMENTO_TYPE, page.getID());
			page.saveState(child);
			child.putInteger(MEMENTO_KEY_LAST_ACTIVATION, part.getLastActivation());
		}
		memento.putString(MEMENTO_KEY_IS_PINNED, String.valueOf(isPinned()));
	}


	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		IMenuManager menuManager= site.getActionBars().getMenuManager();
		createViewMenuGroups(menuManager);
		fPageState= memento;
	}


	protected void initPage(IPageBookViewPage page) {
		super.initPage(page);
		IActionBars actionBars= page.getSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fSearchAgainAction);
		actionBars.updateActionBars();

		fUndoRedoActionGroup.fillActionBars(actionBars);

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

	private void showLatestSearch() {
		ISearchQuery[] queries= InternalSearchUI.getInstance().getSearchManager().getQueries();
		if (queries.length > 0)
			showSearchResult(queries[0].getSearchResult());
	}

	/*
	 *  TODO workaround for focus problem. Clarify focus behavior.
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
		if (!busy)
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

	/**
	 * Marks the view as pinned.
	 *
	 * @param pinned if <code>true</code> the view is marked as pinned
	 */
	public void setPinned(boolean pinned) {
		fIsPinned= pinned;
	}

	/**
	 * @return returns <code>true</code> the view is marked as pinned
	 */
	public boolean isPinned() {
		return fIsPinned;
	}

	public void updatePartName() {
		if (fDefaultPartName != null) {
			// mstodo not yet enabled.
//			String partName= null;
//			ISearchResultPage page= getActivePage();
//			if (page != null && isPinned()) {
//				partName= getSearchPageRegistry().findLabelForPageId(page.getID());
//			}
//			setPartName(partName != null ? partName : fDefaultPartName);
		}
	}
}
