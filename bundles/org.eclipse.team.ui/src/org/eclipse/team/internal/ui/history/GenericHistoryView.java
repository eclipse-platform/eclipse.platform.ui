/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ui.history;

import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.history.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.*;

public class GenericHistoryView extends ViewPart implements IHistoryView, IPropertyChangeListener, IShowInTarget {

	private static final String HISTORY_VIEW_GROUP = "org.eclipse.team.ui.historyView"; //$NON-NLS-1$
	private static final String NAVIGATION_GROUP = "org.eclipse.team.ui.navigation"; //$NON-NLS-1$
	private static final int MAX_NAVIGATION_HISTORY_ENTRIES = 15;

	static boolean sameSource(IHistoryPageSource source1, IHistoryPageSource source2) {
		return source1 == source2 || (source1 != null && source2 != null && source1.equals(source2));
	}
	
	class PageContainer {
		private Page page;
		private SubActionBars subBars;
		private final IHistoryPageSource source;

		public PageContainer(Page page, IHistoryPageSource source) {
			this.page = page;
			this.source = source;
		}

		public Page getPage() {
			return page;
		}

		public void setPage(Page page) {
			this.page = page;
		}

		public SubActionBars getSubBars() {
			return subBars;
		}

		public void setSubBars(SubActionBars subBars) {
			this.subBars = subBars;
		}

		public IHistoryPageSource getSource() {
			return source;
		}
		
		public boolean matches(Object object, IHistoryPageSource pageSource) {
			if (page instanceof IHistoryPage) {
				Object input = ((IHistoryPage)page).getInput();
				if (input != null)
					return input.equals(object) && sameSource(getPageSourceFor(object, pageSource), getPageSourceFor(input, source));
			}
			return false;
		}

		public boolean canShow(Object object, IHistoryPageSource pageSource) {
			if (page instanceof IHistoryPage && sameSource(getPageSourceFor(object, pageSource), getPageSourceFor(((IHistoryPage)page).getInput(), source))) {
				return ((IHistoryPage)page).isValidInput(object);
			}
			return false;
		}
	}
	
	/*
	 * The navigation history for this view.
	 * The history adds the MRU to the end so basic navigation goes backwards.
	 */
	class NavigationHistory {
		List history = new ArrayList();
		int position;
		private boolean navigating;
		public int size() {
			return history.size();
		}
		public void gotoPreviousEntry() {
			if (position > 0) {
				position--;
				gotoEntry();
			} else {
				position = history.size() - 1;
				gotoEntry();
			}
		}
		private void gotoEntry() {
			try {
				navigating = true;
				NavigationHistoryEntry currentEntry = getCurrentEntry();
				showHistoryPageFor(currentEntry.object, true, true, currentEntry.source);
			} finally {
				navigating = false;
			}
		}
		private NavigationHistoryEntry getCurrentEntry() {
			return (NavigationHistoryEntry)history.get(position);
		}
		public void addEntry(Object object, String name, IHistoryPageSource source) {
			if (!navigating) {
				NavigationHistoryEntry navigationHistoryEntry = new NavigationHistoryEntry(object, name, source);
				if (history.contains(navigationHistoryEntry)) {
					history.remove(navigationHistoryEntry);
				}
				history.add(navigationHistoryEntry);
				if (history.size() > MAX_NAVIGATION_HISTORY_ENTRIES) {
					history.remove(0);
				}
				position = history.size() - 1;
			}
			navigateAction.update();
		}
		public NavigationHistoryEntry[] getEntries() {
			return (NavigationHistoryEntry[]) history.toArray(new NavigationHistoryEntry[history.size()]);
		}
		private NavigationHistoryEntry getEntry(int i) {
			return (NavigationHistoryEntry)history.get(i);
		}
		public void gotoEntry(NavigationHistoryEntry navigationHistoryEntry) {
			position = history.indexOf(navigationHistoryEntry);
			gotoEntry();
		}
		public NavigationHistoryEntry getPreviousEntry() {
			int next = position - 1;
			if (next < 0)
				next = size() - 1;
			return getEntry(next);
		}
		
		public void updateName(IHistoryPage historyPage,
				IHistoryPageSource pageSource) {
			NavigationHistoryEntry[] historyEntries = getEntries();
			for (int i = 0; i < historyEntries.length; i++) {
				NavigationHistoryEntry historyEntry = historyEntries[i];
				if (historyEntry.matches(historyPage, pageSource))
					historyEntry.name = historyPage.getName();
			}
			navigateAction.update();
		}
	}
	
	static class NavigationHistoryEntry {
		Object object;
		String name;
		IHistoryPageSource source;
		public NavigationHistoryEntry(Object object, String name, IHistoryPageSource source) {
			this.object = object;
			this.name = name;
			this.source = source;
		}
		public boolean equals(Object obj) {
			if (obj instanceof NavigationHistoryEntry) {
				NavigationHistoryEntry other = (NavigationHistoryEntry) obj;
				return other.object.equals(this.object) && sameSource(source, other.source);
			}
			return false;
		}
		public boolean matches(IHistoryPage historyPage,
				IHistoryPageSource pageSource) {
			return object.equals(historyPage.getInput())
					&& sameSource(source, pageSource);
		}
		public int hashCode() {
			return object.hashCode();
		}
	}
	
	abstract class MenuCreator implements IMenuCreator {
		private MenuManager menuManager;
		public void dispose() {
			if(menuManager != null) {
				menuManager.dispose();
				menuManager = null;
			}
		}
		public Menu getMenu(Control parent) {
			Menu fMenu = null;
			if (menuManager == null) {
				menuManager = new MenuManager();
				fMenu = menuManager.createContextMenu(parent);
				IAction[] actions = getDropDownActions();
				for (int i = actions.length - 1; i >= 0 ; i--) {
					IAction action = actions[i];
					menuManager.add(action);
				}
				updateMenuState();
			} else {
				fMenu = menuManager.getMenu();
			}
			return fMenu;
		}
		protected void updateMenuState() {
			if (menuManager != null)
				menuManager.update(true);
		}

		protected abstract IAction[] getDropDownActions();
		
		public Menu getMenu(Menu parent) {
			return null;
		}
		
		public void rebuildMenu() {
			if(menuManager != null) {
				menuManager.dispose();
				menuManager = null;
			}
		}
	}
	
	class NavigationHistoryAction extends Action {
		private MenuCreator menuCreator;
		private IAction[] actions;

		public NavigationHistoryAction() {
			menuCreator = new MenuCreator() {
				protected IAction[] getDropDownActions() {
					return getActions();
				}
			};
			setMenuCreator(menuCreator);
			update();
		}
		private IAction[] createActions() {
			NavigationHistoryEntry[] entries = getDropDownEntries();
			List actions = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				NavigationHistoryEntry navigationHistoryEntry = entries[i];
				actions.add(new NavigationHistoryEntryAction(navigationHistoryEntry));
			}
			return (IAction[]) actions.toArray(new IAction[actions.size()]);
		}
		protected NavigationHistoryEntry[] getDropDownEntries() {
			return navigationHistory.getEntries();
		}
		public void run() {
			navigationHistory.gotoPreviousEntry();
			updateCheckState();
		}
		public void update() {
			setEnabled(navigationHistory.size() > 1);
			if (isEnabled()) {
				setToolTipText(NLS.bind(TeamUIMessages.GenericHistoryView_1, navigationHistory.getPreviousEntry().name));
			} else {
				setToolTipText(TeamUIMessages.GenericHistoryView_2);
			}
			actions = null;
			menuCreator.rebuildMenu();
			updateCheckState();
		}
		
		private void updateCheckState() {
			IAction[] actions = getActions();
			for (int i = 0; i < actions.length; i++) {
				IAction action = actions[i];
				if (action instanceof NavigationHistoryEntryAction) {
					NavigationHistoryEntryAction a = (NavigationHistoryEntryAction) action;
					a.update();
				}
			}
			menuCreator.updateMenuState();
		}
		public void dispose() {
			menuCreator.dispose();
		}
		private IAction[] getActions() {
			if (actions == null)
				actions = createActions();
			return actions;
		}
	}
	
	class NavigationHistoryEntryAction extends Action {

		private final NavigationHistoryEntry navigationHistoryEntry;

		public NavigationHistoryEntryAction(NavigationHistoryEntry navigationHistoryEntry) {
			super(navigationHistoryEntry.name);
			this.navigationHistoryEntry = navigationHistoryEntry;
		}
		
		public void run() {
			navigationHistory.gotoEntry(navigationHistoryEntry);
			navigateAction.updateCheckState();
		}
		
		public void update() {
			setChecked(navigationHistory.getCurrentEntry() == navigationHistoryEntry);
		}
		
	}

	/**
	 * The pagebook control, or <code>null</code> if not initialized.
	 */
	private PageBook book;

	/**
	 * View actions
	 */
	private Action refreshAction;
	private Action linkWithEditorAction;
	private Action pinAction;
	private NavigationHistoryAction navigateAction;

	/**
	 * The page container for the default page.
	 */
	private PageContainer defaultPageContainer;

	/**
	 * The current page container
	 */
	PageContainer currentPageContainer;
	
	/**
	 * The drop target + drop target listener
	 */
	DropTarget dropTarget;
	GenericHistoryDropAdapter dropAdapter;
	
	NavigationHistory navigationHistory = new NavigationHistory();
	
	/**
	 * Keeps track of the last selected element (either by selecting or opening an editor)
	 */
	private Object lastSelectedElement;

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			if (part == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}

		public void partOpened(IWorkbenchPart part) {
			if (part == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}
	};

	private IPartListener2 partListener2 = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
		}

		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		public void partClosed(IWorkbenchPartReference ref) {
		}

		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref) {
		}

		public void partHidden(IWorkbenchPartReference ref) {
		}

		public void partVisible(IWorkbenchPartReference ref) {
			if (ref.getPart(true) == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}

		public void partInputChanged(IWorkbenchPartReference ref) {
		}
	};

	private ISelectionListener selectionListener = new ISelectionListener() {

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {

			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structSelection = (IStructuredSelection) selection;
				//Always take the first element - this is not intended to work with multiple selection
				//Also, hang on to this selection for future use in case the history view is not visible
				lastSelectedElement = structSelection.getFirstElement();
				
				if (!isLinkingEnabled() || !checkIfPageIsVisible()) {
					return;
				}
				
				if (lastSelectedElement != null){
					Object resource;
					if (lastSelectedElement instanceof SyncInfoModelElement) {
						SyncInfoModelElement syncInfoModelElement = (SyncInfoModelElement) lastSelectedElement;
						resource = syncInfoModelElement.getSyncInfo().getLocal();
					} else {
						resource = Utils.getAdapter(lastSelectedElement, IResource.class);
					}
					if (resource != null)
						showHistory((IResource) resource);
					else
						showHistory(lastSelectedElement);
					//reset lastSelectedElement
					lastSelectedElement = null;
				}
			}
		}

	};

	private boolean linkingEnabled;

	private boolean viewPinned;

	/**
	 * Refreshes the global actions for the active page.
	 */
	void refreshGlobalActionHandlers() {
		// Clear old actions.
		IActionBars bars = getViewSite().getActionBars();
		bars.clearGlobalActionHandlers();

		// Set new actions.
		Map newActionHandlers = currentPageContainer.getSubBars().getGlobalActionHandlers();
		if (newActionHandlers != null) {
			Set keys = newActionHandlers.entrySet();
			Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				bars.setGlobalActionHandler((String) entry.getKey(), (IAction) entry.getValue());
			}
		}
		
		//add refresh action handler from history view
		bars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		
	}

	public void createPartControl(Composite parent) {
		// Create the page book.
		book = new PageBook(parent, SWT.NONE);

		this.linkingEnabled = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_EDITOR_LINKING);

		// Create the default page rec.
		defaultPageContainer = createDefaultPage(book);
		
		//Contribute toolbars
		configureToolbars(getViewSite().getActionBars());

		//add global action handler
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		
		//initialize the drag and drop
		initDragAndDrop();

		// Show the default page	
		showPageRec(defaultPageContainer);

		// add listener for editor page activation - this is to support editor
		// linking
		getSite().getPage().addPartListener(partListener);
		getSite().getPage().addPartListener(partListener2);

		// add listener for selections
		getSite().getPage().addPostSelectionListener(selectionListener);
	}

	private void configureToolbars(IActionBars actionBars) {

		pinAction = new Action(TeamUIMessages.GenericHistoryView_PinCurrentHistory, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_PINNED)) {
			public void run() {
				if (isChecked()) {
					//uncheck editor linking
					linkWithEditorAction.setChecked(false);
					setLinkingEnabled(false);
				}
				setViewPinned(isChecked());
			}
		};
		pinAction.setChecked(isViewPinned());
		pinAction.setToolTipText(TeamUIMessages.GenericHistoryView_0);

		refreshAction = new Action(TeamUIMessages.GenericHistoryView_Refresh, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_REFRESH)) {
			public void run() {
				((IHistoryPage) currentPageContainer.getPage()).refresh();
			}
		};
		refreshAction.setToolTipText(TeamUIMessages.GenericHistoryView_RefreshTooltip);
		refreshAction.setEnabled(true);
		
		
		linkWithEditorAction = new Action(TeamUIMessages.GenericHistoryView_LinkWithEditor, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_LINK_WITH)) {
			public void run() {
				if (isChecked()) {
					// uncheck pinned
					pinAction.setChecked(false);
					setViewPinned(false);
				}
				setLinkingEnabled(isViewPinned() ? false : isChecked());
			}
		};
		linkWithEditorAction.setChecked(isLinkingEnabled());
		linkWithEditorAction.setToolTipText(TeamUIMessages.GenericHistoryView_LinkWithTooltip);
		
		navigateAction = new NavigationHistoryAction();
		Utils.initAction(navigateAction, "action.previousHistory."); //$NON-NLS-1$
		
		//Create the local tool bar
		IToolBarManager tbm = actionBars.getToolBarManager();
		tbm.add(new Separator(HISTORY_VIEW_GROUP));
		tbm.appendToGroup(HISTORY_VIEW_GROUP, refreshAction);
		tbm.appendToGroup(HISTORY_VIEW_GROUP, linkWithEditorAction);
		tbm.appendToGroup(HISTORY_VIEW_GROUP, pinAction);
		tbm.add(new Separator(NAVIGATION_GROUP));
		tbm.appendToGroup(NAVIGATION_GROUP, navigateAction);
		tbm.update(false);
	}

	boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	/**
	 * Enabled linking to the active editor
	 * @param enabled flag indicating whether linking is enabled
	 */
	public void setLinkingEnabled(boolean enabled) {
		this.linkingEnabled = enabled;

		// remember the last setting in the dialog settings		
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_EDITOR_LINKING, enabled);

		// if turning linking on, update the selection to correspond to the active editor
		if (enabled) {
			editorActivated(getSite().getPage().getActiveEditor());
		}
	}

	/**
	 * Sets the current view pinned
	 * @param b
	 */
	void setViewPinned(boolean pinned) {
		this.viewPinned = pinned;
	}

	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance(), PluginTransfer.getInstance()};

		dropTarget = new DropTarget(book, ops);
		dropTarget.setTransfer(transfers);
		dropAdapter = new GenericHistoryDropAdapter(this);
		dropTarget.addDropListener(dropAdapter);
	}
	
	public void setFocus() {
		if (isLinkingEnabled()){
			if (lastSelectedElement != null){
				if (lastSelectedElement instanceof IEditorPart){
					editorActivated((IEditorPart) lastSelectedElement);
				} else {
					Object resource = Utils.getAdapter(lastSelectedElement, IResource.class);
					if (resource != null)
						showHistoryPageFor((IResource) resource, false, false, null);
					else
						showHistoryPageFor(lastSelectedElement, false, false, null);
				}
				//reset lastSelectedElement to null to prevent updating history view if it just gets focus
				lastSelectedElement  = null;
			}
		}
		
		if (currentPageContainer.page instanceof IPage){
			((IPage) currentPageContainer.page).setFocus();
		}
	}

	/**
	 * Shows page contained in the given page record in this view. The page record must 
	 * be one from this pagebook view.
	 * <p>
	 * The <code>PageBookView</code> implementation of this method asks the
	 * pagebook control to show the given page's control, and records that the
	 * given page is now current. Subclasses may extend.
	 * </p>
	 *
	 * @param pageContainer the page record containing the page to show
	 */
	protected void showPageRec(PageContainer pageContainer) {
		// If already showing do nothing
		if (currentPageContainer == pageContainer) {
			addNavigationHistoryEntry();
			return;
		}

		// Hide old page.
		if (currentPageContainer != null) {
			currentPageContainer.getSubBars().deactivate();
			//give the current page a chance to dispose
			((IHistoryPage)currentPageContainer.getPage()).removePropertyChangeListener(this);
			currentPageContainer.getPage().dispose();
			currentPageContainer.getSubBars().dispose();
		}
		// Show new page.
		currentPageContainer = pageContainer;
	
		Control pageControl = currentPageContainer.getPage().getControl();
		if (pageControl != null && !pageControl.isDisposed()) {
			// Verify that the page control is not disposed
			// If we are closing, it may have already been disposed
			book.showPage(pageControl);
			((IHistoryPage)currentPageContainer.getPage()).addPropertyChangeListener(this);
			currentPageContainer.getSubBars().activate();
			refreshGlobalActionHandlers();
			// Update action bars.
			getViewSite().getActionBars().updateActionBars();
			addNavigationHistoryEntry();
		}
	}

	private void addNavigationHistoryEntry() {
		if (currentPageContainer != null) {
			Object input = ((IHistoryPage)currentPageContainer.getPage()).getInput();
			if (input != null)
				navigationHistory.addEntry(input, ((IHistoryPage)currentPageContainer.getPage()).getName(), currentPageContainer.getSource());
		}
	}

	/**
	 * Initializes the given page with a page site.
	 * <p>
	 * Subclasses should call this method after
	 * the page is created but before creating its
	 * controls.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 * @param page The page to initialize
	 */
	protected PageSite initPage(IPageBookViewPage page) {
		try {
			PageSite site = new PageSite(getViewSite());
			page.init(site);
			return site;
		} catch (PartInitException e) {
			TeamUIPlugin.log(e);
		}
		return null;
	}
	
	public IHistoryPage showHistoryFor(Object object, boolean force) {
		return showHistoryPageFor(object, true, force, null);
	}
	
	public IHistoryPage showHistoryPageFor(Object object, boolean refresh, boolean force, IHistoryPageSource pageSource) {
		
		// Check to see if history view is visible - if it's not, don't bother
		// going to the trouble of fetching the history
		if (!this.getSite().getPage().isPartVisible(this))
			return null;
		
		// Ensure that there is a page source available
		pageSource = getPageSourceFor(object, pageSource);
		if (pageSource == null || !pageSource.canShowHistoryFor(object))
			return null;
		
		// Check to see if the object is already being displayed in another page
		IHistoryPage existingPage = checkForExistingPage(object, refresh, force, pageSource);
		if (existingPage != null){
			return existingPage;
		}
		
		// Now check to see if this view is pinned
		if (isViewPinned() && !force) {
			return handlePinnedView(object, refresh, pageSource);
		}
		
		// If a current page exists, see if it can handle the dropped item.
		// Otherwise, create a new page
		PageContainer tempPageContainer = null;
		if (currentPageContainer!= null && currentPageContainer.canShow(object, pageSource)) {
			tempPageContainer = currentPageContainer;
		} else {
			tempPageContainer = createPage(pageSource, object);
		}
		
		// Set the new page to the current page for the view
		IHistoryPage historyPage = ((IHistoryPage)tempPageContainer.getPage());
		historyPage.setInput(object);
		((HistoryPage)historyPage).setHistoryView(this);
		setContentDescription(historyPage.getName());
		showPageRec(tempPageContainer);
		return historyPage;
	}

	private IHistoryPageSource getPageSourceFor(Object object, IHistoryPageSource pageSource) {
		if (object == null || pageSource != null)
			return pageSource;
		IResource resource = Utils.getResource(object);
		if (resource == null) {
			return (IHistoryPageSource) Utils.getAdapter(object, IHistoryPageSource.class);
		} else {
			if (resource.getProject() == null)
				return null;
			//check to see if resource is managed
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(resource.getProject());
			if (teamProvider == null){
				// couldn't find a repository provider; try showing it in a local page
				return LocalHistoryPageSource.getInstance();
			} else {
				IFileHistoryProvider fileHistory = teamProvider.getFileHistoryProvider();
				
				if (fileHistory != null) {
					IHistoryPageSource source = (IHistoryPageSource)Utils.getAdapter(fileHistory, IHistoryPageSource.class,true);
					if (source != null)
						return source;
				}
				return (IHistoryPageSource)Utils.getAdapter(teamProvider, IHistoryPageSource.class,true);
			}
		}
	}

	private IHistoryPage handlePinnedView(Object object, boolean refresh, IHistoryPageSource source) {
		try {
			// Check to see if an unpinned version of the history view exists
			GenericHistoryView historyView = findUnpinnedHistoryView();
			if (historyView != null){
				getSite().getPage().activate(historyView);
				return historyView.showHistoryPageFor(object, refresh, true, source);
			}
			// Otherwise, open another instance of the view
			String id = VIEW_ID + System.currentTimeMillis();
			IViewPart view = getSite().getPage().showView(VIEW_ID, id, IWorkbenchPage.VIEW_CREATE);
			getSite().getPage().activate(view);
			if (view instanceof GenericHistoryView)
				return ((GenericHistoryView) view).showHistoryPageFor(object, refresh, true, source);
	
		} catch (PartInitException e) {
		}
		return null;
	}

	private IHistoryPage checkForExistingPage(Object object, boolean refresh, boolean thisViewOnly, IHistoryPageSource pageSource) {
		//first check to see if the main history view contains the current resource
		IHistoryPage tempPage = checkForExistingPage(object, refresh, pageSource);
		if (tempPage != null || thisViewOnly)
			return tempPage;
		tempPage = searchHistoryViewsForObject(object, refresh, pageSource);
		if (tempPage != null)
			getSite().getPage().bringToTop((IWorkbenchPart)tempPage.getHistoryView());
		return tempPage;
	}
	
	private IHistoryPage checkForExistingPage(Object object, boolean refresh, IHistoryPageSource pageSource) {
		//first check to see if the main history view contains the current resource
		if (currentPageContainer != null) {
			if (currentPageContainer.matches(object, pageSource)){ 
				//current page contains object, so just refresh it
				IHistoryPage tempPage =((IHistoryPage)currentPageContainer.getPage());
				if (refresh)
					tempPage.refresh();
				
				return tempPage;
			}
		}
		return null;
	}

	private IHistoryPage searchHistoryViewsForObject(Object object, boolean  refresh, IHistoryPageSource pageSource) {
		IWorkbenchPage page = getSite().getPage();
		IViewReference[] historyViews = page.getViewReferences();
		for (int i = 0; i < historyViews.length; i++) {
			if (historyViews[i].getId().equals(VIEW_ID)){
				IViewPart historyView = historyViews[i].getView(true);
				if (historyView instanceof GenericHistoryView) {
					GenericHistoryView ghv = (GenericHistoryView)historyView;
					IHistoryPage historyPage = ghv.checkForExistingPage(object, refresh, pageSource);
					if (historyPage != null) {
						return historyPage;
					}
				}
			}
		}
		return null;
	}
	
	public GenericHistoryView findUnpinnedHistoryView(){
		IWorkbenchPage page = getSite().getPage();
		IViewReference[] historyViews = page.getViewReferences();
		for (int i = 0; i < historyViews.length; i++) {
			if (historyViews[i].getId().equals(VIEW_ID)){
				IViewPart historyView = historyViews[i].getView(false);
				if (!((GenericHistoryView)historyView).isViewPinned())
					return (GenericHistoryView) historyView;
			}
		}
		return null;
	}
	
	boolean isViewPinned() {
		return viewPinned;
	}

	private PageContainer createPage(IHistoryPageSource source, Object object) {
		Page page = source.createPage(object);
		PageSite site = initPage(page);
		((IHistoryPage) page).setSite(new WorkbenchHistoryPageSite(this, page.getSite()));
		page.createControl(book);
		PageContainer container = new PageContainer(page, source);
		container.setSubBars((SubActionBars) site.getActionBars());
		return container;
	}

	protected PageContainer createDefaultPage(PageBook book) {
		GenericHistoryViewDefaultPage page = new GenericHistoryViewDefaultPage();
		PageSite site = initPage(page);
		page.createControl(book);
		PageContainer container = new PageContainer(page, null);
		container.setSubBars((SubActionBars) site.getActionBars());
		return container;
	}

	/**
	 * An editor has been activated.  Fetch the history if the file is shared and the history view
	 * is visible in the current page.
	 * 
	 * @param editor the active editor
	 */
	protected void editorActivated(IEditorPart editor) {
		//If this history view is not visible, keep track of this editor
		//for future use
		if (editor != null && !checkIfPageIsVisible())
			lastSelectedElement = editor;
		
		//Only fetch contents if the view is shown in the current page.
		if (editor == null || !isLinkingEnabled() || !checkIfPageIsVisible() || isViewPinned()) {
			return;
		}
		IEditorInput input = editor.getEditorInput();

		IFile file = ResourceUtil.getFile(input);
		if (file != null) {
			showHistory(file); /* don't fetch if already cached */
		} else {
			//see if it adapts to an IHistoryPageSource
			Object pageSource = Utils.getAdapter(input, IHistoryPageSource.class);
			if (pageSource != null)
				showHistory(input);
		}
	}

	private boolean checkIfPageIsVisible() {
		return getViewSite().getPage().isPartVisible(this);
	}

	public void dispose() {
		super.dispose();
		//Remove the drop listener
		if (dropTarget != null && !dropTarget.isDisposed())
			dropTarget.removeDropListener(dropAdapter);
		//Call dispose on current and default pages
		((IHistoryPage)currentPageContainer.getPage()).removePropertyChangeListener(this);
		currentPageContainer.getPage().dispose();
		defaultPageContainer.getPage().dispose();
		currentPageContainer = null;
		defaultPageContainer = null;
		//Remove the part listeners
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
		//Remove the selection listener
		getSite().getPage().removePostSelectionListener(selectionListener);
		navigateAction.dispose();
	}

	public IHistoryPage showHistoryFor(Object object) {
		 return showHistoryFor(object, false);
	}

	public IHistoryPage getHistoryPage() {
		if (currentPageContainer != null &&
			currentPageContainer.getPage() != null)
			return (IHistoryPage) currentPageContainer.getPage();
		
		return (IHistoryPage) defaultPageContainer.getPage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == currentPageContainer.getPage()) {
			if (event.getProperty().equals(IHistoryPage.P_NAME)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IHistoryPage historyPage = (IHistoryPage) currentPageContainer.getPage();
						setContentDescription(historyPage.getName());
						navigationHistory.updateName(historyPage, currentPageContainer.getSource());
					}
				});
			} else if (event.getProperty().equals(IHistoryPage.P_DESCRIPTION)) {
				// We don't show the description
			}
		}
	}

	public IHistoryView findAppropriateHistoryViewFor(Object input,
			IHistoryPageSource pageSource) {
		// First, check to see if the input and pageSource of this view match the input
		IHistoryPage page = searchHistoryViewsForObject(input, false, pageSource);
		if (page != null) {
			return page.getHistoryView();
		}
		return findUnpinnedHistoryView();
	}
	
	private void showHistory(Object object) {
		// Only show the history if the input differs
		// (i.e. don't do the change if the input is the same but the page source differs; bug 167648)
		if (getHistoryPage().getInput() != object)
			showHistoryPageFor(object, false, false, null);
	}

	public boolean show(ShowInContext context) {
		ISelection selection = context.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				// If we can show the selection, return.
				// Otherwise, fall through and attempt to show the input
				if ((showHistoryFor(ss.getFirstElement()) != null))
					return true;
			}
		}
		if (context.getInput() != null) {
			return (showHistoryFor(context.getInput()) != null);
		}
		return false;
	}
}
