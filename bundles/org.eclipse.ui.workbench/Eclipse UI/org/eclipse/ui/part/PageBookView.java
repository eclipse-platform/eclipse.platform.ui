/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.part;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Abstract superclass of all multi-page workbench views.
 * <p>
 * Within the workbench there are many views which track the active part. If a
 * part is activated these views display some properties for the active part. A
 * simple example is the <code>Outline View</code>, which displays the outline
 * for the active editor. To avoid loss of context when part activation changes,
 * these views may implement a multi-page approach. A separate page is
 * maintained within the view for each source view. If a part is activated the
 * associated page for the part is brought to top. If a part is closed the
 * associated page is disposed. <code>PageBookView</code> is a base
 * implementation for multi page views.
 * </p>
 * <p>
 * <code>PageBookView</code>s provide an <code>IPageSite</code> for each of
 * their pages. This site is supplied during the page's initialization. The page
 * may supply a selection provider for this site. <code>PageBookView</code>s
 * deal with these selection providers in a similar way to a workbench page's
 * <code>SelectionService</code>. When a page is made visible, if its site has a
 * selection provider, then changes in the selection are listened for and the
 * current selection is obtained and fired as a selection change event.
 * Selection changes are no longer listened for when a page is made invisible.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to define new multi-page
 * views.
 * </p>
 * <p>
 * When a <code>PageBookView</code> is created the following methods are
 * invoked. Subclasses must implement these.
 * </p>
 * <ul>
 * <li><code>createDefaultPage</code> - called to create a default page for the
 * view. This page is displayed when the active part in the workbench does not
 * have a page.</li>
 * <li><code>getBootstrapPart</code> - called to determine the active part in
 * the workbench. A page will be created for this part</li>
 * </ul>
 * <p>
 * When a part is activated the base implementation does not know if a page
 * should be created for the part. Therefore, it delegates creation to the
 * subclass.
 * </p>
 * <ul>
 * <li><code>isImportant</code> - called when a workbench part is activated.
 * Subclasses return whether a page should be created for the new part.</li>
 * <li><code>doCreatePage</code> - called to create a page for a particular part
 * in the workbench. This is only invoked when <code>isImportant</code> returns
 * <code>true</code>.</li>
 * </ul>
 * <p>
 * When a part is closed the base implementation will destroy the page
 * associated with the particular part. The page was created by a subclass, so
 * the subclass must also destroy it. Subclasses must implement these.
 * </p>
 * <ul>
 * <li><code>doDestroyPage</code> - called to destroy a page for a particular
 * part in the workbench.</li>
 * </ul>
 */
public abstract class PageBookView extends ViewPart implements IPartListener {
	/**
	 * The pagebook control, or <code>null</code> if not initialized.
	 */
	private PageBook book;

	/**
	 * The page record for the default page.
	 */
	private PageRec defaultPageRec;

	/**
	 * Map from parts to part records (key type: <code>IWorkbenchPart</code>; value
	 * type: <code>PartRec</code>).
	 */
	private final Map<IWorkbenchPart, PageRec> mapPartToRec = new HashMap<>();

	/**
	 * Map from pages to view sites Note that view sites were not added to page recs
	 * to avoid breaking binary compatibility with previous builds
	 */
	private final Map<IPage, IPageSite> mapPageToSite = new HashMap<>();

	/**
	 * Map from pages to the number of pageRecs actively associated with a page.
	 */
	private final Map<IPage, Integer> mapPageToNumRecs = new HashMap<>();

	/**
	 * The page rec which provided the current page or <code>null</code>
	 */
	private PageRec activeRec;

	/**
	 * If the part is hidden (usually an editor) then store it so we can continue to
	 * track it when it becomes visible.
	 */
	private IWorkbenchPart hiddenPart;

	/**
	 * The action bar property listener.
	 */
	private IPropertyChangeListener actionBarPropListener = event -> {
		if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS) && activeRec != null
				&& event.getSource() == activeRec.subActionBars) {
			refreshGlobalActionHandlers();
		}
	};

	/**
	 * Selection change listener to listen for page selection changes
	 */
	private ISelectionChangedListener selectionChangedListener = this::pageSelectionChanged;

	/**
	 * Selection change listener to listen for page selection changes
	 */
	private ISelectionChangedListener postSelectionListener = this::postSelectionChanged;

	/**
	 * Selection provider for this view's site
	 */
	private SelectionProvider selectionProvider = new SelectionProvider();

	/**
	 * A data structure used to store the information about a single page within a
	 * pagebook view.
	 */
	protected static class PageRec {

		/**
		 * The part.
		 */
		public IWorkbenchPart part;

		/**
		 * The page.
		 */
		public IPage page;

		/**
		 * The page's action bars
		 */
		public SubActionBars subActionBars;

		/**
		 * Creates a new page record initialized to the given part and page.
		 *
		 * @param part the part
		 * @param page the page
		 */
		public PageRec(IWorkbenchPart part, IPage page) {
			this.part = part;
			this.page = page;
		}

		/**
		 * Disposes of this page record by <code>null</code>ing its fields.
		 */
		public void dispose() {
			part = null;
			page = null;
		}
	}

	private static class SelectionManager extends EventManager {
		/**
		 *
		 * @param listener listen
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			addListenerObject(listener);
		}

		/**
		 *
		 * @param listener listen
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			removeListenerObject(listener);
		}

		/**
		 *
		 * @param event the event
		 */
		public void selectionChanged(final SelectionChangedEvent event) {
			// pass on the notification to listeners
			for (Object listener : getListeners()) {
				final ISelectionChangedListener selectionChangedListener = (ISelectionChangedListener) listener;
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() {
						selectionChangedListener.selectionChanged(event);
					}
				});
			}
		}

	}

	/**
	 * A selection provider/listener for this view. It is a selection provider for
	 * this view's site.
	 */
	protected class SelectionProvider implements IPostSelectionProvider {

		private SelectionManager fSelectionListener = new SelectionManager();

		private SelectionManager fPostSelectionListeners = new SelectionManager();

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			fSelectionListener.addSelectionChangedListener(listener);
		}

		@Override
		public ISelection getSelection() {
			// get the selection provider from the current page
			IPage currentPage = getCurrentPage();
			// during workbench startup we may be in a state when
			// there is no current page
			if (currentPage == null) {
				return StructuredSelection.EMPTY;
			}
			IPageSite site = getPageSite(currentPage);
			if (site == null) {
				return StructuredSelection.EMPTY;
			}
			ISelectionProvider selProvider = site.getSelectionProvider();
			if (selProvider != null) {
				return selProvider.getSelection();
			}
			return StructuredSelection.EMPTY;
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			fSelectionListener.removeSelectionChangedListener(listener);
		}

		/**
		 * The selection has changed. Process the event, notifying selection listeners
		 * and post selection listeners.
		 *
		 * @param event the change
		 */
		public void selectionChanged(final SelectionChangedEvent event) {
			fSelectionListener.selectionChanged(event);
		}

		/**
		 * The selection has changed, so notify any post-selection listeners.
		 *
		 * @param event the change
		 */
		public void postSelectionChanged(final SelectionChangedEvent event) {
			fPostSelectionListeners.selectionChanged(event);
		}

		@Override
		public void setSelection(ISelection selection) {
			// get the selection provider from the current page
			IPage currentPage = getCurrentPage();
			// during workbench startup we may be in a state when
			// there is no current page
			if (currentPage == null) {
				return;
			}
			IPageSite site = getPageSite(currentPage);
			if (site == null) {
				return;
			}
			ISelectionProvider selProvider = site.getSelectionProvider();
			// and set its selection
			if (selProvider != null) {
				selProvider.setSelection(selection);
			}
		}

		@Override
		public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
			fPostSelectionListeners.addSelectionChangedListener(listener);
		}

		@Override
		public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
			fPostSelectionListeners.removeSelectionChangedListener(listener);
		}
	}

	/**
	 * Creates a new pagebook view.
	 */
	protected PageBookView() {
		super();
	}

	/**
	 * Creates and returns the default page for this view.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * <p>
	 * Subclasses must call initPage with the new page (if it is an
	 * <code>IPageBookViewPage</code>) before calling createControl on the page.
	 * </p>
	 *
	 * @param book the pagebook control
	 * @return the default page
	 */
	protected abstract IPage createDefaultPage(PageBook book);

	/**
	 * Creates a page for a given part. Adds it to the pagebook but does not show
	 * it.
	 *
	 * @param part The part we are making a page for.
	 * @return IWorkbenchPart
	 */
	private PageRec createPage(IWorkbenchPart part) {
		PageRec rec = doCreatePage(part);
		if (rec != null) {
			mapPartToRec.put(part, rec);
			preparePage(rec);
		}
		return rec;
	}

	/**
	 * Prepares the page in the given page rec for use in this view.
	 */
	private void preparePage(PageRec rec) {
		IPageSite site = null;
		Integer count;

		if (!doesPageExist(rec.page)) {
			if (rec.page instanceof IPageBookViewPage) {
				site = ((IPageBookViewPage) rec.page).getSite();
			}
			if (site == null) {
				// We will create a site for our use
				site = new PageSite(getViewSite());
			}
			mapPageToSite.put(rec.page, site);

			rec.subActionBars = (SubActionBars) site.getActionBars();
			rec.subActionBars.addPropertyChangeListener(actionBarPropListener);
			// for backward compability with IPage
			rec.page.setActionBars(rec.subActionBars);

			count = Integer.valueOf(0);
		} else {
			site = mapPageToSite.get(rec.page);
			rec.subActionBars = (SubActionBars) site.getActionBars();
			count = mapPageToNumRecs.get(rec.page);
		}

		mapPageToNumRecs.put(rec.page, Integer.valueOf(count.intValue() + 1));
	}

	/**
	 * Initializes the given page with a page site.
	 * <p>
	 * Subclasses should call this method after the page is created but before
	 * creating its controls.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 *
	 * @param page The page to initialize
	 */
	protected void initPage(IPageBookViewPage page) {
		try {
			page.init(new PageSite(getViewSite()));
		} catch (PartInitException e) {
			WorkbenchPlugin.log(getClass(), "initPage", e); //$NON-NLS-1$
		}
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IWorkbenchPart</code> method creates a <code>PageBook</code> control
	 * with its default page showing. Subclasses may extend.
	 */
	@Override
	public void createPartControl(Composite parent) {

		// Create the page book.
		book = new PageBook(parent, SWT.NONE);

		// Create the default page rec.
		IPage defaultPage = createDefaultPage(book);
		defaultPageRec = new PageRec(null, defaultPage);
		preparePage(defaultPageRec);

		// Show the default page
		showPageRec(defaultPageRec);

		// Listen to part activation events.
		getSite().getPage().addPartListener(partListener);
		showBootstrapPart();
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IWorkbenchPart</code> method cleans up all the pages. Subclasses may
	 * extend.
	 */
	@Override
	public void dispose() {
		// stop listening to part activation
		getSite().getPage().removePartListener(partListener);

		// Deref all of the pages.
		activeRec = null;
		if (defaultPageRec != null) {
			// check for null since the default page may not have
			// been created (ex. perspective never visible)
			removePage(defaultPageRec, false);
			defaultPageRec = null;
		}
		Map<IWorkbenchPart, PageRec> clone = (Map<IWorkbenchPart, PageRec>) ((HashMap<IWorkbenchPart, PageRec>) mapPartToRec)
				.clone();
		clone.values().forEach(rec -> removePage(rec, true));

		// Run super.
		super.dispose();
	}

	/**
	 * Creates a new page in the pagebook for a particular part. This page will be
	 * made visible whenever the part is active, and will be destroyed with a call
	 * to <code>doDestroyPage</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * <p>
	 * Subclasses must call initPage with the new page (if it is an
	 * <code>IPageBookViewPage</code>) before calling createControl on the page.
	 * </p>
	 *
	 * @param part the input part
	 * @return the record describing a new page for this view
	 * @see #doDestroyPage
	 */
	protected abstract PageRec doCreatePage(IWorkbenchPart part);

	/**
	 * Destroys a page in the pagebook for a particular part. This page was returned
	 * as a result from <code>doCreatePage</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param part       the input part
	 * @param pageRecord a page record for the part
	 * @see #doCreatePage
	 */
	protected abstract void doDestroyPage(IWorkbenchPart part, PageRec pageRecord);

	/**
	 * Returns true if the page has already been created.
	 *
	 * @param page the page to test
	 * @return true if this page has already been created.
	 */
	protected boolean doesPageExist(IPage page) {
		return mapPageToNumRecs.containsKey(page);
	}

	/**
	 * The <code>PageBookView</code> implementation of this <code>IAdaptable</code>
	 * method delegates to the current page, if it implements
	 * <code>IAdaptable</code>.
	 */
	@Override
	public <T> T getAdapter(Class<T> key) {
		// delegate to the current page, if supported
		IPage page = getCurrentPage();
		T adapter = Adapters.adapt(page, key);
		if (adapter != null) {
			return adapter;
		}
		// if the page did not find the adapter, look for one provided by
		// this view before delegating to super.
		adapter = getViewAdapter(key);
		if (adapter != null) {
			return adapter;
		}
		// delegate to super
		return super.getAdapter(key);
	}

	/**
	 * Returns an adapter of the specified type, as provided by this view (not the
	 * current page), or <code>null</code> if this view does not provide an adapter
	 * of the specified adapter.
	 * <p>
	 * The default implementation returns <code>null</code>. Subclasses may
	 * override.
	 * </p>
	 *
	 * @param adapter the adapter class to look up
	 * @return a object castable to the given class, or <code>null</code> if this
	 *         object does not have an adapter for the given class
	 * @since 3.2
	 */
	protected <T> T getViewAdapter(Class<T> adapter) {
		return null;
	}

	/**
	 * Returns the active, important workbench part for this view.
	 * <p>
	 * When the page book view is created it has no idea which part within the
	 * workbook should be used to generate the first page. Therefore, it delegates
	 * the choice to subclasses of <code>PageBookView</code>.
	 * </p>
	 * <p>
	 * Implementors of this method should return an active, important part in the
	 * workbench or <code>null</code> if none found.
	 * </p>
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @return the active important part, or <code>null</code> if none
	 */
	protected abstract IWorkbenchPart getBootstrapPart();

	/**
	 * Returns the part which contributed the current page to this view.
	 *
	 * @return the part which contributed the current page or <code>null</code> if
	 *         no part contributed the current page
	 */
	protected IWorkbenchPart getCurrentContributingPart() {
		if (activeRec == null) {
			return null;
		}
		return activeRec.part;
	}

	/**
	 * Returns the currently visible page for this view or <code>null</code> if no
	 * page is currently visible.
	 *
	 * @return the currently visible page
	 */
	public IPage getCurrentPage() {
		if (activeRec == null) {
			return null;
		}
		return activeRec.page;
	}

	/**
	 * Returns the view site for the given page of this view.
	 *
	 * @param page the page
	 * @return the corresponding site, or <code>null</code> if not found
	 */
	protected PageSite getPageSite(IPage page) {
		return (PageSite) mapPageToSite.get(page);
	}

	/**
	 * Returns the default page for this view.
	 *
	 * @return the default page
	 */
	public IPage getDefaultPage() {
		return defaultPageRec.page;
	}

	/**
	 * Returns the pagebook control for this view.
	 *
	 * @return the pagebook control, or <code>null</code> if not initialized
	 */
	protected PageBook getPageBook() {
		return book;
	}

	/**
	 * Returns the page record for the given part.
	 *
	 * @param part the part
	 * @return the corresponding page record, or <code>null</code> if not found
	 */
	protected PageRec getPageRec(IWorkbenchPart part) {
		return mapPartToRec.get(part);
	}

	/**
	 * Returns the page record for the given page of this view.
	 *
	 * @param page the page
	 * @return the corresponding page record, or <code>null</code> if not found
	 */
	protected PageRec getPageRec(IPage page) {
		Iterator<PageRec> itr = mapPartToRec.values().iterator();
		while (itr.hasNext()) {
			PageRec rec = itr.next();
			if (rec.page == page) {
				return rec;
			}
		}
		return null;
	}

	/**
	 * Returns whether the given part should be added to this view.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param part the input part
	 * @return <code>true</code> if the part is relevant, and <code>false</code>
	 *         otherwise
	 */
	protected abstract boolean isImportant(IWorkbenchPart part);

	@Override
	public void init(IViewSite site) throws PartInitException {
		site.setSelectionProvider(selectionProvider);
		super.init(site);
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IPartListener</code> method shows the page when the given part is
	 * activated. Subclasses may extend.
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		// Is this an important part? If not just return.
		if (isImportant(part)) {
			hiddenPart = null;

			// Create a page for the part.
			PageRec rec = getPageRec(part);
			if (rec == null) {
				rec = createPage(part);
			}

			// Show the page.
			if (rec != null) {
				showPageRec(rec);
			} else {
				showPageRec(defaultPageRec);
			}
		}

		// If *we* are activating then activate the context
		if (part == this) {
			PageSite pageSite = getPageSite(getCurrentPage());
			if (pageSite != null) {
				IEclipseContext pageContext = pageSite.getSiteContext();
				if (pageContext != null) {
					pageContext.activate();
				}
			}
		}
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IPartListener</code> method does nothing. Subclasses may extend.
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// Do nothing by default
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IPartListener</code> method deal with the closing of the active part.
	 * Subclasses may extend.
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		// Update the active part.
		if (activeRec != null && activeRec.part == part) {
			showPageRec(defaultPageRec);
		}

		// Find and remove the part page.
		PageRec rec = getPageRec(part);
		if (rec != null) {
			removePage(rec, true);
		}
		if (part == hiddenPart) {
			hiddenPart = null;
		}
	}

	/**
	 * The <code>PageBookView</code> implementation of this
	 * <code>IPartListener</code> method does nothing. Subclasses may extend.
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// Do nothing.
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// Do nothing by default.
	}

	/**
	 * Refreshes the global actions for the active page.
	 */
	private void refreshGlobalActionHandlers() {
		// Clear old actions.
		IActionBars bars = getViewSite().getActionBars();
		bars.clearGlobalActionHandlers();

		// Set new actions.
		Map<?, ?> newActionHandlers = activeRec.subActionBars.getGlobalActionHandlers();
		if (newActionHandlers != null) {
			newActionHandlers.entrySet()
					.forEach(e -> bars.setGlobalActionHandler((String) e.getKey(), (IAction) e.getValue()));
		}
	}

	/**
	 * Removes a page record. If it is the last reference to the page dispose of it
	 * - otherwise just decrement the reference count.
	 *
	 * @param doDestroy if <code>true</code>, also call
	 *                  {@link #doDestroyPage(IWorkbenchPart, PageRec)}
	 */
	private void removePage(PageRec rec, boolean doDestroy) {
		mapPartToRec.remove(rec.part);

		int newCount = mapPageToNumRecs.get(rec.page).intValue() - 1;

		if (newCount == 0) {
			Object site = mapPageToSite.remove(rec.page);
			mapPageToNumRecs.remove(rec.page);

			Control control = rec.page.getControl();
			if (control != null && !control.isDisposed()) {
				// Dispose the page's control so pages don't have to do this in
				// their
				// dispose method.
				// The page's control is a child of this view's control so if
				// this view
				// is closed, the page's control will already be disposed.
				control.dispose();
			}

			if (doDestroy) {
				// free the page
				doDestroyPage(rec.part, rec);
			}

			if (rec.subActionBars != null) {
				rec.subActionBars.dispose();
			}

			if (site instanceof PageSite) {
				((PageSite) site).dispose();
			}
		} else {
			mapPageToNumRecs.put(rec.page, Integer.valueOf(newCount));
		}
	}

	@Override
	public void setFocus() {
		// first set focus on the page book, in case the page
		// doesn't properly handle setFocus
		if (book != null && !book.isDisposed()) {
			book.setFocus();
		}
		// then set focus on the page, if any
		if (activeRec != null && !activeRec.page.getControl().isDisposed()) {
			activeRec.page.setFocus();
		}
	}

	/**
	 * Handle page selection changes.
	 */
	private void pageSelectionChanged(SelectionChangedEvent event) {
		// forward this change from a page to our site's selection provider
		SelectionProvider provider = (SelectionProvider) getSite().getSelectionProvider();
		if (provider != null) {
			provider.selectionChanged(event);
		}
	}

	/**
	 * Handle page selection changes.
	 */
	private void postSelectionChanged(SelectionChangedEvent event) {
		// forward this change from a page to our site's selection provider
		SelectionProvider provider = (SelectionProvider) getSite().getSelectionProvider();
		if (provider != null) {
			provider.postSelectionChanged(event);
		}
	}

	/**
	 * Shows a page for the active workbench part.
	 */
	private void showBootstrapPart() {
		IWorkbenchPart part = getBootstrapPart();
		if (part != null) {
			partActivated(part);
		}
	}

	/**
	 * Shows page contained in the given page record in this view. The page record
	 * must be one from this pagebook view.
	 * <p>
	 * The <code>PageBookView</code> implementation of this method asks the pagebook
	 * control to show the given page's control, and records that the given page is
	 * now current. Subclasses may extend.
	 * </p>
	 *
	 * @param pageRec the page record containing the page to show
	 */
	protected void showPageRec(PageRec pageRec) {
		// If already showing do nothing
		if (activeRec == pageRec) {
			return;
		}
		// If the page is the same, just set activeRec to pageRec
		if (activeRec != null && pageRec != null && activeRec.page == pageRec.page) {
			activeRec = pageRec;
			return;
		}

		// Hide old page.
		if (activeRec != null) {
			PageSite pageSite = (PageSite) mapPageToSite.get(activeRec.page);

			activeRec.subActionBars.deactivate();

			deactivate(pageSite);
		}

		// Show new page.
		activeRec = pageRec;
		Control pageControl = activeRec.page.getControl();
		if (pageControl != null && !pageControl.isDisposed()) {
			PageSite pageSite = (PageSite) mapPageToSite.get(activeRec.page);

			// Verify that the page control is not disposed
			// If we are closing, it may have already been disposed
			book.showPage(pageControl);
			activeRec.subActionBars.activate();
			refreshGlobalActionHandlers();

			// activate the nested services
			pageSite.activate();

			// add our selection listener
			ISelectionProvider provider = pageSite.getSelectionProvider();
			if (provider != null) {
				ISelection selection = provider.getSelection();
				if (selection == null) {
					selection = StructuredSelection.EMPTY;
				}
				forwardSelection(new SelectionChangedEvent(provider, selection));

				provider.addSelectionChangedListener(selectionChangedListener);
				if (provider instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) provider).addPostSelectionChangedListener(postSelectionListener);
				} else {
					provider.addSelectionChangedListener(postSelectionListener);
				}
			}
			// Update action bars.
			getViewSite().getActionBars().updateActionBars();
		}
	}

	private void deactivate(PageSite pageSite) {
		if (pageSite == null) {
			reportNullPageSiteOnDeactivate(activeRec);
			return;
		}

		// deactivate the nested services
		pageSite.deactivate();

		// remove our selection listener
		ISelectionProvider provider = pageSite.getSelectionProvider();
		if (provider != null) {
			provider.removeSelectionChangedListener(selectionChangedListener);
			if (provider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) provider).removePostSelectionChangedListener(postSelectionListener);
			} else {
				provider.removeSelectionChangedListener(postSelectionListener);
			}
			forwardSelection(new SelectionChangedEvent(provider, StructuredSelection.EMPTY));
		}
	}

	private void forwardSelection(SelectionChangedEvent event) {
		getSelectionProvider().selectionChanged(event);
		getSelectionProvider().postSelectionChanged(event);
	}

	/**
	 * Extra diagnostic report for bug 453151
	 *
	 * @param pr the record for which we don't know the pageSite anymore
	 */
	private void reportNullPageSiteOnDeactivate(PageRec pr) {
		IPage page = pr.page;
		if (page == null) {
			WorkbenchPlugin.log(new IllegalStateException("Bug 453151: page is null in PageBookView.deactivate")); //$NON-NLS-1$
		} else {
			boolean hasKey = mapPageToSite.containsKey(page);
			Integer count = mapPageToNumRecs.get(page);
			Control control = page.getControl();
			boolean disposed = control != null && control.isDisposed();
			String s = "Bug 453151: pageSite is null for page: " //$NON-NLS-1$
					+ page.getClass().getName() + ", page count: " + count //$NON-NLS-1$
					+ ", key exists: " + hasKey + ", disposed: " + disposed; //$NON-NLS-1$ //$NON-NLS-2$
			WorkbenchPlugin.log(new IllegalStateException(s));
		}
	}

	/**
	 * Returns the selectionProvider for this page book view.
	 *
	 * @return a SelectionProvider
	 */
	protected SelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	private IPartListener2 partListener = new IPartListener2() {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if (partRef == null) {
				WorkbenchPlugin.log("partRef is null in PageBookView partActivated"); //$NON-NLS-1$
				return;
			}
			IWorkbenchPart part = partRef.getPart(false);
			PageBookView.this.partActivated(part);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			PageBookView.this.partBroughtToTop(partRef.getPart(false));
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			PageBookView.this.partClosed(partRef.getPart(false));
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			PageBookView.this.partDeactivated(partRef.getPart(false));
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			PageBookView.this.partHidden(partRef.getPart(false));
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			PageBookView.this.partOpened(partRef.getPart(false));
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			PageBookView.this.partVisible(partRef.getPart(false));
		}
	};

	/**
	 * Make sure that the part is not considered if it is hidden.
	 *
	 * @param part the part which got hidden
	 * @since 3.5
	 */
	protected void partHidden(IWorkbenchPart part) {
		if (part == null || part != getCurrentContributingPart()) {
			return;
		}
		// if we've minimized the editor stack, that's no reason to
		// drop our content
		IWorkbenchPage page = getSite().getPage();
		if (page.getPartState(page.getReference(part)) == IWorkbenchPage.STATE_MINIMIZED) {
			return;
		}
		// if we're switching from a part source in our own stack,
		// we also don't want to clear our content.
		if (part instanceof IViewPart) {
			final IViewPart[] viewStack = page.getViewStack(this);
			if (containsPart(viewStack, part)) {
				return;
			}
		}
		hiddenPart = part;
		showPageRec(defaultPageRec);
	}

	/**
	 * @param viewStack view parts to check; might be <code>null</code>
	 * @param part      part to search in viewStack; might be <code>null</code>
	 * @return <code>true</code> if the part is in the viewStack
	 */
	private boolean containsPart(IViewPart[] viewStack, IWorkbenchPart part) {
		if (viewStack == null) {
			return false;
		}
		for (IViewPart viewPart : viewStack) {
			if (viewPart == part) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Make sure that the part is not considered if it is hidden.
	 *
	 * @param part the part which got visible
	 * @since 3.5
	 */
	protected void partVisible(IWorkbenchPart part) {
		if (part == null || part != hiddenPart) {
			return;
		}
		partActivated(part);
	}
}
