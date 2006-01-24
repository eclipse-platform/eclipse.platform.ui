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

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.history.*;
import org.eclipse.ui.*;
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
			if (!isLinkingEnabled()) {
				return;
			}

			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structSelection = (IStructuredSelection) selection;
				//Always take the first element - this is not intended to work with multiple selection
				Object firstElement = structSelection.getFirstElement();
				itemDropped(firstElement);
			}
		}

	};

	/**
	 * The action bar property listener. 
	 */
	private IPropertyChangeListener actionBarPropListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS) && currentPageContainer.getPage() != null && event.getSource() == currentPageContainer.getSubBars()) {
				refreshGlobalActionHandlers();
			}
		}
	};

	private boolean linkingEnabled;

	private boolean viewPinned;

	public final static String viewId = "org.eclipse.team.ui.GenericHistoryView"; //$NON-NLS-1$

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
	}

	public void createPartControl(Composite parent) {
		// Create the page book.
		book = new PageBook(parent, SWT.NONE);

		this.linkingEnabled = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_EDITOR_LINKING);

		// Create the default page rec.
		defaultPageContainer = createDefaultPage(book);
		
		//Contribute toolbars
		configureToolbars(getViewSite().getActionBars());

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

		linkWithEditorAction = new Action(TeamUIMessages.GenericHistoryView_LinkWithEditor, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_LINK_WITH)) {
			public void run() {
				setLinkingEnabled(isViewPinned() ? false : isChecked());
			}
		};
		linkWithEditorAction.setChecked(isLinkingEnabled());

		//previousHistory = new GenericHistoryDropDownAction();

		//Create the local tool bar
		IToolBarManager tbm = actionBars.getToolBarManager();
		//Take out history support for now
		//tbm.add(previousHistory);
		tbm.add(refreshAction);
		tbm.add(linkWithEditorAction);
		tbm.add(pinAction);
		tbm.update(false);
	}

	boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	/**
	 * Enabled linking to the active editor
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
		// TODO Auto-generated method stub

	}

	/**
	 * Prepares the page in the given page rec for use
	 * in this view.
	 * @param rec
	 */
	private void preparePage(PageContainer pageContainer) {
		pageContainer.getSubBars().addPropertyChangeListener(actionBarPropListener);
		//for backward compability with IPage
		pageContainer.getPage().setActionBars(pageContainer.getSubBars());
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
			// remove our selection listener
			/*            ISelectionProvider provider = ((PageSite) mapPageToSite.get(activeRec.page)).getSelectionProvider();
			 if (provider != null)
			 provider
			 .removeSelectionChangedListener(selectionChangedListener);*/
		}
		// Show new page.
		currentPageContainer = pageContainer;
		//setContentDescription(((IHistoryPage)currentPageContainer.getPage()).getName());
		Control pageControl = currentPageContainer.getPage().getControl();
		if (pageControl != null && !pageControl.isDisposed()) {
			// Verify that the page control is not disposed
			// If we are closing, it may have already been disposed
			book.showPage(pageControl);
			currentPageContainer.getSubBars().activate();
			//refreshGlobalActionHandlers();
			// add our selection listener
			/*ISelectionProvider provider = ((PageSite) mapPageToSite
			 .get(activeRec.page)).getSelectionProvider();
			 if (provider != null)
			 provider.addSelectionChangedListener(selectionChangedListener);*/
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
	
	public void itemDropped(Object object) {

		IResource resource = Utils.getResource(object);
		if (resource != null) {
			//check first to see if this view is pinned
			if (checkForPinnedView(object, resource.getName()))
				return;
		
			//check to see if resource is managed
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(resource.getProject());
			//couldn't find a repo provider; try showing it in a local page
			if (teamProvider == null){
				localItemDropped(resource);
				return;
			}
			IFileHistoryProvider fileHistory = teamProvider.getFileHistoryProvider();
			Object tempPageSource = Platform.getAdapterManager().getAdapter(fileHistory, IHistoryPageSource.class);
			if (tempPageSource instanceof IHistoryPageSource) {
				IHistoryPageSource pageSource = (IHistoryPageSource) tempPageSource;

				//If a current page exists, see if it can handle the dropped item
				if (currentPageContainer.getPage() instanceof IHistoryPage) {
					PageContainer tempPageContainer = currentPageContainer;
					if (!((IHistoryPage) tempPageContainer.getPage()).canShowHistoryFor(resource)) {
						tempPageContainer = createPage(pageSource, resource);
					}
					if (tempPageContainer != null) {
						if (((IHistoryPage) tempPageContainer.getPage()).showHistory(resource, true)){
							setContentDescription(resource.getName());
							showPageRec(tempPageContainer);
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
				return;
			
			//If a current page exists, see if it can handle the dropped item
			if (currentPageContainer.getPage() instanceof IHistoryPage) {
				PageContainer tempPageContainer = currentPageContainer;
				if (!((IHistoryPage) tempPageContainer.getPage()).canShowHistoryFor(object)) {
					tempPageContainer = createPage(historyPageSource, object);
				}
				if (tempPageContainer != null) {
					
					if (checkForPinnedView(object, ((IHistoryPage) tempPageContainer.getPage()).getName()))
						return;
					
					if (((IHistoryPage) tempPageContainer.getPage()).showHistory(object, true)){
						setContentDescription(((IHistoryPage) tempPageContainer.getPage()).getName());
						showPageRec(tempPageContainer);
					}
				} else {
					showPageRec(defaultPageContainer);
				}
			}
		}

	}

	private boolean checkForPinnedView(Object object, String objectName) {
		if (isViewPinned()) {
			try {
				//get the file name
				IViewPart view = getSite().getPage().showView(viewId, viewId + objectName + System.currentTimeMillis(), IWorkbenchPage.VIEW_CREATE);
				if (view instanceof GenericHistoryView) {
					GenericHistoryView view2 = (GenericHistoryView) view;
					view2.itemDropped(object);
				}
				return true;
			} catch (PartInitException e) {
			}
		}
		return false;
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
	
	protected PageContainer createLocalPage(PageBook book){
		LocalHistoryPage page = new LocalHistoryPage();
		PageSite site = initPage(page);
		((IHistoryPage) page).setSite(new WorkbenchHistoryPageSite(this, page.getSite()));
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
		// Only fetch contents if the view is shown in the current page.
		if (editor == null || !isLinkingEnabled() || !checkIfPageIsVisible() || isViewPinned()) {
			return;
		}
		IEditorInput input = editor.getEditorInput();

		if (input instanceof FileRevisionEditorInput) {
			IFile file;
			try {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(((FileRevisionEditorInput) input).getStorage().getFullPath());
				if (file != null) {
					itemDropped(file);
				}
			} catch (CoreException e) {
			}
		} // Handle regular file editors
		else {
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				itemDropped(file);//, false /* don't fetch if already cached */);
			}
		}
	}

	private boolean checkIfPageIsVisible() {
		return getViewSite().getPage().isPartVisible(this);
	}

	public void dispose() {
		super.dispose();
		//Remove the drop listener
		dropTarget.removeDropListener(dropAdapter);
		//Call dispose on current and default pages
		currentPageContainer.getPage().dispose();
		defaultPageContainer.getPage().dispose();
		//Remove the part listeners
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
		//Remove the selection listener
		getSite().getPage().removeSelectionListener(selectionListener);
	}

	public void localItemDropped(IResource resource) {
		PageContainer container = createLocalPage(this.book);
		if (((IHistoryPage) container.getPage()).showHistory(resource, true)){
			setContentDescription(resource.getName());
			showPageRec(container);
		}
	}

	public void showHistoryFor(Object object) {
		itemDropped(object);
	}
}
