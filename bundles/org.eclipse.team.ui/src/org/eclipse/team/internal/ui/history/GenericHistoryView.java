/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ui.history;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.history.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.*;

public class GenericHistoryView extends ViewPart implements IHistoryView {

	class PageContainer {
		private Page page;
		private SubActionBars subBars;

		public PageContainer(Page page) {
			this.page = page;
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
					Object resource = Utils.getAdapter(lastSelectedElement, IResource.class);
					if (resource != null)
						itemDropped((IResource) resource, false);
					else
						itemDropped(lastSelectedElement, false);
					//reset lastSelectedElement
					lastSelectedElement = null;
				}
			}
		}

	};

	private boolean linkingEnabled;

	private boolean viewPinned;

	public final static String VIEW_ID = "org.eclipse.team.ui.GenericHistoryView"; //$NON-NLS-1$

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
		getSite().getPage().addSelectionListener(selectionListener);
	}

	private void configureToolbars(IActionBars actionBars) {

		pinAction = new Action(TeamUIMessages.GenericHistoryView_PinCurrentHistory, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_PINNED)) {
			public void run() {
				if (isChecked()) {
					//uncheck editor linking and disable
					linkWithEditorAction.setChecked(false);
					linkWithEditorAction.setEnabled(false);
					setLinkingEnabled(false);
				} else {
					//renable the linking button
					linkWithEditorAction.setEnabled(true);
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
				setLinkingEnabled(isViewPinned() ? false : isChecked());
			}
		};
		linkWithEditorAction.setChecked(isLinkingEnabled());
		linkWithEditorAction.setToolTipText(TeamUIMessages.GenericHistoryView_LinkWithTooltip);
		
		//Create the local tool bar
		IToolBarManager tbm = actionBars.getToolBarManager();
		tbm.add(new Separator("historyView")); //$NON-NLS-1$
		tbm.appendToGroup("historyView", refreshAction);  //$NON-NLS-1$
		tbm.appendToGroup("historyView", linkWithEditorAction);  //$NON-NLS-1$
		tbm.appendToGroup("historyView", pinAction);  //$NON-NLS-1$
		tbm.update(false);
	}

	boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	/**
	 * Enabled linking to the active editor
	 * @param enabled	flag indiciating whether linking is enabled
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
						itemDropped((IResource) resource, false);
					else
						itemDropped(lastSelectedElement, false);
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
	 * @param pageRec the page record containing the page to show
	 */
	protected void showPageRec(PageContainer pageContainer) {
		// If already showing do nothing
		if (currentPageContainer == pageContainer)
			return;
		// If the page is the same, just set activeRec to pageRec
		if (currentPageContainer != null && pageContainer != null && currentPageContainer == pageContainer) {
			currentPageContainer = pageContainer;
			return;
		}

		// Hide old page.
		if (currentPageContainer != null) {
			currentPageContainer.getSubBars().deactivate();
			//give the current page a chance to dispose
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
			currentPageContainer.getSubBars().activate();
			refreshGlobalActionHandlers();
			// Update action bars.
			getViewSite().getActionBars().updateActionBars();
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
	
	public IHistoryPage itemDropped(Object object, boolean refresh) {
		
		//check to see if history view is visible - if it's not, don't bother
		//going to the trouble of fetching the history
		if (!this.getSite().getPage().isPartVisible(this))
			return null;
		
		
		IResource resource = Utils.getResource(object);
		if (resource != null) {
			
			//check to see if this resource is alreadu being displayed in another page
			IHistoryPage existingPage = checkForExistingPage(object, resource.getName(), refresh);
			if (existingPage != null){
				return existingPage;
			}
			
			//now check to see if this view is pinned
			IHistoryPage pinnedPage = checkForPinnedView(object, resource.getName(), refresh);
			if (pinnedPage != null)
				return pinnedPage;
		
			//check to see if resource is managed
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(resource.getProject());
			//couldn't find a repo provider; try showing it in a local page
			Object tempPageSource = null;
			if (teamProvider == null){
				tempPageSource = new LocalHistoryPageSource();
			} else {
				IFileHistoryProvider fileHistory = teamProvider.getFileHistoryProvider();
				
				if (fileHistory != null) {
					tempPageSource = Utils.getAdapter(fileHistory, IHistoryPageSource.class,true);
				}
				if (tempPageSource == null) {
					tempPageSource = Utils.getAdapter(teamProvider, IHistoryPageSource.class,true);
				}
			}
			
			if (tempPageSource instanceof IHistoryPageSource) {
				IHistoryPageSource pageSource = (IHistoryPageSource) tempPageSource;

				//If a current page exists, see if it can handle the dropped item
				if (currentPageContainer.getPage() instanceof IHistoryPage) {
					PageContainer tempPageContainer = currentPageContainer;
					if (!((IHistoryPage) tempPageContainer.getPage()).isValidInput(resource)) {
						tempPageContainer = createPage(pageSource, resource);
					}
					if (tempPageContainer != null) {
						if (((IHistoryPage) tempPageContainer.getPage()).setInput(resource)){
							setContentDescription(resource.getName());
							showPageRec(tempPageContainer);
							return (IHistoryPage) tempPageContainer.getPage();
						}
					} else {
						showPageRec(defaultPageContainer);
					}
				}
			}
		}
		else if (object != null){
			IHistoryPageSource historyPageSource = (IHistoryPageSource) Utils.getAdapter(object, IHistoryPageSource.class);
			//Check to see that this object can be adapted to an IHistoryPageSource, else
			//we don't know how to display it
			if (historyPageSource == null)
				return null;
			
			//If a current page exists, see if it can handle the dropped item
			if (currentPageContainer.getPage() instanceof IHistoryPage) {
				PageContainer tempPageContainer = currentPageContainer;
				if (!((IHistoryPage) tempPageContainer.getPage()).isValidInput(object)) {
					tempPageContainer = createPage(historyPageSource, object);
				}
				if (tempPageContainer != null) {
					
					//check to see if this resource is alreadu being displayed in another page
					IHistoryPage existingPage = checkForExistingPage(object, ((IHistoryPage) tempPageContainer.getPage()).getName(), refresh);
					if (existingPage != null){
						return existingPage;
					}
					
					IHistoryPage pinnedPage = checkForPinnedView(object, ((IHistoryPage) tempPageContainer.getPage()).getName(), refresh);
					if (pinnedPage != null)
						return pinnedPage;
					
					if (((IHistoryPage) tempPageContainer.getPage()).setInput(object)){
						setContentDescription(((IHistoryPage) tempPageContainer.getPage()).getName());
						showPageRec(tempPageContainer);
						return (IHistoryPage) tempPageContainer.getPage();
					}
				} else {
					showPageRec(defaultPageContainer);
				}
			}
		}

		return null;
	}

	private IHistoryPage checkForPinnedView(Object object, String objectName, boolean refresh) {
		if (isViewPinned()) {
			try {
				IViewPart view = null;
				//check to see if a view already contains this object
				String id = VIEW_ID + System.currentTimeMillis();
				IHistoryPage page = searchHistoryViewsForObject(object, refresh);
				if (page != null)
					return page;
				
				//check to see if an unpinned version of the history view exists
				GenericHistoryView historyView = findUnpinnedHistoryView();
				if (historyView != null){
					getSite().getPage().activate(historyView);
					return historyView.itemDropped(object, refresh);
				}
				
				view = getSite().getPage().showView(VIEW_ID, id, IWorkbenchPage.VIEW_CREATE);
				getSite().getPage().activate(view);
				if (view instanceof GenericHistoryView)
					return ((GenericHistoryView) view).itemDropped(object, refresh);
		
			} catch (PartInitException e) {
			}
		}
		return null;
	}

	private IHistoryPage checkForExistingPage(Object object, String objectName, boolean refresh) {
		//first check to see if the main history view contains the current resource
		if (currentPageContainer != null && 
			((IHistoryPage)currentPageContainer.getPage()).getInput() != null){
			if (((IHistoryPage)currentPageContainer.getPage()).getInput().equals(object)){ 
				//current page contains object, so just refresh it
				IHistoryPage tempPage =((IHistoryPage)currentPageContainer.getPage());
				if (refresh)
					tempPage.refresh();
				
				return tempPage;
			}
		}
		
		return searchHistoryViewsForObject(object, refresh);
	}

	private IHistoryPage searchHistoryViewsForObject(Object object, boolean  refresh) {
		IWorkbenchPage page = getSite().getPage();
		IViewReference[] historyViews = page.getViewReferences();
		for (int i = 0; i < historyViews.length; i++) {
			if (historyViews[i].getId().equals(VIEW_ID)){
				IViewPart historyView = historyViews[i].getView(true);
				if (historyView != null){
					IHistoryPage historyPage = ((IHistoryView)historyView).getHistoryPage();
					if (historyPage != null){
						Object input = historyPage.getInput();
						if (input != null && input.equals(object)){
							//this view already contains the file, so just reuse it
							getSite().getPage().bringToTop(historyView);
							return ((GenericHistoryView) historyView).itemDropped(object, refresh);
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private GenericHistoryView findUnpinnedHistoryView(){
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

	private PageContainer createPage(IHistoryPageSource participant, Object object) {
		Page page = participant.createPage(object);
		PageSite site = initPage(page);
		((IHistoryPage) page).setSite(new WorkbenchHistoryPageSite(this, page.getSite()));
		page.createControl(book);
		PageContainer container = new PageContainer(page);
		container.setSubBars((SubActionBars) site.getActionBars());
		return container;
	}

	protected PageContainer createDefaultPage(PageBook book) {
		GenericHistoryViewDefaultPage page = new GenericHistoryViewDefaultPage();
		PageSite site = initPage(page);
		page.createControl(book);
		PageContainer container = new PageContainer(page);
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

		if (input instanceof FileRevisionEditorInput) {
			//See if the input adapts to a file revision
			Object fileRev =((FileRevisionEditorInput) input).getAdapter(IFileRevision.class);
			if (fileRev != null){
				itemDropped(fileRev, false);
			}
		} // Handle regular file editors
		else {
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				itemDropped(file, false); /* don't fetch if already cached */
			}
			
			//see if it adapts to an IHistoryPageSource
			Object pageSource = Utils.getAdapter(input, IHistoryPageSource.class);
			if (pageSource != null)
				itemDropped(input, false);
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
		currentPageContainer.getPage().dispose();
		defaultPageContainer.getPage().dispose();
		currentPageContainer = null;
		defaultPageContainer = null;
		//Remove the part listeners
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
		//Remove the selection listener
		getSite().getPage().removeSelectionListener(selectionListener);
	}

	public IHistoryPage showHistoryFor(Object object) {
		 return itemDropped(object, true);
	}

	public IHistoryPage getHistoryPage() {
		if (currentPageContainer != null &&
			currentPageContainer.getPage() != null)
			return (IHistoryPage) currentPageContainer.getPage();
		
		return (IHistoryPage) defaultPageContainer.getPage();
	}
	
	/**
	 * Updates the content description of the view with the passed
	 * in string.
	 * @param description
	 */
	public void updateContentDescription(String description){
		setContentDescription(description);
	}
}
