/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.part;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Abstract superclass of all multi-page workbench views.
 * <p>
 * Within the workbench there are many views which track the active part.  If a
 * part is activated these views display some properties for the active part.  A
 * simple example is the <code>Outline View</code>, which displays the outline for the
 * active editor.  To avoid loss of context when part activation changes, these 
 * views may implement a multi-page approach.  A separate page is maintained within
 * the view for each source view.  If a part is activated the associated page for the
 * part is brought to top.  If a part is closed the associated page is disposed.
 * <code>PageBookView</code> is a base implementation for multi page views.
 * </p>
 * <p>
 * <code>PageBookView</code>s provide an <code>IPageSite</code> for each of
 * their pages. This site is supplied during the page's initialization. The page
 * may supply a selection provider for this site. <code>PageBookView</code>s deal
 * with these selection providers in a similar way to a workbench page's
 * <code>SelectionService</code>. When a page is made visible, if its site
 * has a selection provider, then changes in the selection are listened for
 * and the current selection is obtained and fired as a selection change event.
 * Selection changes are no longer listened for when a page is made invisible.
 * </p>
 * <p>
 * This class should be subclassed by clients wishing to define new 
 * multi-page views.
 * </p>
 * <p>
 * When a <code>PageBookView</code> is created the following methods are
 * invoked.  Subclasses must implement these.
 * <ul>
 *   <li><code>createDefaultPage</code> - called to create a default page for the
 *		view.  This page is displayed when the active part in the workbench does not
 *		have a page.</li>
 *   <li><code>getBootstrapPart</code> - called to determine the active part in the
 *		workbench.  A page will be created for this part</li>
 * </ul>
 * </p>
 * <p>
 * When a part is activated the base implementation does not know if a page should
 * be created for the part.  Therefore, it delegates creation to the subclass.
 * <ul>
 *   <li><code>isImportant</code> - called when a workbench part is activated.
 *		Subclasses return whether a page should be created for the new part.</li>
 *   <li><code>doCreatePage</code> - called to create a page for a particular part
 *		in the workbench.  This is only invoked when <code>isImportant</code> returns 
 *		</code>true</code>.</li>
 * </ul>
 * </p>
 * <p>
 * When a part is closed the base implementation will destroy the page associated with
 * the particular part.  The page was created by a subclass, so the subclass must also
 * destroy it.  Subclasses must implement these.
 * <ul>
 *   <li><code>doDestroyPage</code> - called to destroy a page for a particular
 *		part in the workbench.</li>
 * </ul>
 * </p>
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
	 * Map from parts to part records (key type: <code>IWorkbenchPart</code>;
	 * value type: <code>PartRec</code>).
	 */
	private Map mapPartToRec = new HashMap();
	
	/**
	 * Map from pages to view sites
	 * Note that view sites were not added to page recs to 
	 * avoid breaking binary compatibility with previous builds
	 */
	private Map mapPageToSite = new HashMap();

	/**
	 * The page rec which provided the current page or
	 * <code>null</code> 
	 */
	private PageRec activeRec;

	/**
	 * The action bar property listener. 
	 */
	private IPropertyChangeListener actionBarPropListener =
		new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS)
					&& activeRec != null
					&& event.getSource() == activeRec.subActionBars) {
					refreshGlobalActionHandlers(); 
				}
			}
		};
		
	/**
	 * Selection change listener to listen for page selection changes
	 */
	private ISelectionChangedListener selectionChangedListener =
		new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				pageSelectionChanged(event);
			}
		};
		
	/** 
	 * Selection provider for this view's site
	 */
	private SelectionProvider selectionProvider = new SelectionProvider();	
		
	/**
	 * A data structure used to store the information about a single page 
	 * within a pagebook view.
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
	
	/**
	 * A selection provider/listener for this view.
	 * It is a selection provider fo this view's site.
	 */
	protected class SelectionProvider implements ISelectionProvider {
		/**
		 * Selection change listeners.
		 */
		private ListenerList selectionChangedListeners = new ListenerList();
				
		/* (non-Javadoc)
		 * Method declared on ISelectionProvider.
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			selectionChangedListeners.add(listener);	
		}
		/* (non-Javadoc)
		 * Method declared on ISelectionProvider.
		 */
		public ISelection getSelection() {
			// get the selection provider from the current page
			IPage currentPage = getCurrentPage();
			// during workbench startup we may be in a state when
			// there is no current page
			if (currentPage == null) 
				return StructuredSelection.EMPTY;
			IPageSite site = getPageSite(currentPage);
			if (site == null)
				return	StructuredSelection.EMPTY;
			ISelectionProvider selProvider = site.getSelectionProvider();
			if (selProvider != null) 
				return selProvider.getSelection();
			else
				return StructuredSelection.EMPTY;
		}
		/* (non-Javadoc)
		 * Method declared on ISelectionProvider.
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			selectionChangedListeners.remove(listener);
		}
		/* (non-Javadoc)
		 * Method declared on ISelectionChangedListener.
		 */
		public void selectionChanged(final SelectionChangedEvent event) {
			// pass on the notification to listeners
			Object[] listeners = selectionChangedListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				final ISelectionChangedListener l = (ISelectionChangedListener)listeners[i];
				Platform.run(new SafeRunnable() {
					public void run() {
						l.selectionChanged(event);
					}
					public void handleException(Throwable e) {
						super.handleException(e);
						//If and unexpected exception happens, remove it
						//to make sure the workbench keeps running.
						removeSelectionChangedListener(l);
					}
				});		
			}
		}
		/* (non-Javadoc)
		 * Method declared on ISelectionProvider.
		 */
		public void setSelection(ISelection selection) {
			// get the selection provider from the current page
			IPage currentPage = getCurrentPage();
			// during workbench startup we may be in a state when
			// there is no current page
			if (currentPage == null) 
				return;
			IPageSite site = getPageSite(currentPage);
			if (site == null)
				return;
			ISelectionProvider selProvider = site.getSelectionProvider();
			// and set its selection
			if (selProvider != null) 
				selProvider.setSelection(selection);
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
 * <code>IPageBookViewPage</code>) before calling createControl 
 * on the page.
 * </p>
 * 
 * @param book the pagebook control
 * @return the default page
 */
protected abstract IPage createDefaultPage(PageBook book);
/**
 * Creates a page for a given part.  Adds it to the pagebook but does
 * not show it.
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
 * Prepares the page in the given page rec for use
 * in this view.
 */
private void preparePage(PageRec rec) {
	IPageSite site = null;
	if (rec.page instanceof IPageBookViewPage) {
		site = ((IPageBookViewPage)rec.page).getSite();
	}
	if (site == null) {
		// We will create a site for our use
		site = new PageSite(getViewSite());
	}
	mapPageToSite.put(rec.page, site);
	rec.subActionBars = (SubActionBars)site.getActionBars();
	rec.subActionBars.addPropertyChangeListener(actionBarPropListener);
	// for backward compability with IPage
	rec.page.setActionBars(rec.subActionBars);
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
 * @param the page to initialize
 */
protected void initPage(IPageBookViewPage page) {
	try {
		page.init(new PageSite(getViewSite()));
	} catch (PartInitException e) {
		WorkbenchPlugin.log(e.getMessage());
	}
}
/**
 * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
 * method creates a <code>PageBook</code> control with its default page showing.
 * Subclasses may extend.
 */
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
	getSite().getPage().addPartListener(this);
	showBootstrapPart();
}
/**
 * The <code>PageBookView</code> implementation of this 
 * <code>IWorkbenchPart</code> method cleans up all the pages.
 * Subclasses may extend.
 */
public void dispose() {
	// stop listening to part activation
	getSite().getPage().removePartListener(this);

	// Deref all of the pages.
	activeRec = null;
	if (defaultPageRec != null) {
		// check for null since the default page may not have
		// been created (ex. perspective never visible)
		defaultPageRec.page.dispose();
		defaultPageRec = null;
	}
	Map clone = (Map)((HashMap)mapPartToRec).clone();
	Iterator enum = clone.values().iterator();
	while (enum.hasNext()) {
		PageRec rec = (PageRec) enum.next();
		removePage(rec);
	}

	// Run super.
	super.dispose();
}
/**
 * Creates a new page in the pagebook for a particular part.  This
 * page will be made visible whenever the part is active, and will be
 * destroyed with a call to <code>doDestroyPage</code>.
 * <p>
 * Subclasses must implement this method.
 * </p>
 * <p> 
 * Subclasses must call initPage with the new page (if it is an
 * <code>IPageBookViewPage</code>) before calling createControl 
 * on the page.
 * </p>
 * @param part the input part
 * @return the record describing a new page for this view
 * @see #doDestroyPage
 */
protected abstract PageRec doCreatePage(IWorkbenchPart part);
/**
 * Destroys a page in the pagebook for a particular part.  This page
 * was returned as a result from <code>doCreatePage</code>.
 * <p>
 * Subclasses must implement this method.
 * </p>
 *
 * @param part the input part
 * @param pageRecord a page record for the part
 * @see #doCreatePage
 */
protected abstract void doDestroyPage(IWorkbenchPart part, PageRec pageRecord);

/**
 * The <code>PageBookView</code> implementation of this <code>IAdaptable</code> 
 * method delegates to the current page, if it implements <code>IAdaptable</code>.
 */
public Object getAdapter(Class key) {
	// delegate to the current page, if supported
	IPage page = getCurrentPage();
	if (page instanceof IAdaptable) {
		return ((IAdaptable) page).getAdapter(key);
	}
	return super.getAdapter(key);
}

/**
 * Returns the active, important workbench part for this view.  
 * <p>
 * When the page book view is created it has no idea which part within
 * the workbook should be used to generate the first page.  Therefore, it
 * delegates the choice to subclasses of <code>PageBookView</code>.
 * </p><p>
 * Implementors of this method should return an active, important part
 * in the workbench or <code>null</code> if none found.
 * </p><p>
 * Subclasses must implement this method.
 * </p>
 *
 * @return the active important part, or <code>null</code> if none
 */
protected abstract IWorkbenchPart getBootstrapPart();
/**
 * Returns the part which contributed the current 
 * page to this view.
 *
 * @return the part which contributed the current page
 * or <code>null</code> if no part contributed the current page
 */
protected IWorkbenchPart getCurrentContributingPart() {
	if (activeRec == null)
		return null;
	return activeRec.part;
}
/**
 * Returns the currently visible page for this view or
 * <code>null</code> if no page is currently visible.
 *
 * @return the currently visible page
 */
public IPage getCurrentPage() {
	if (activeRec == null)
		return null;
	return activeRec.page;
}
/**
 * Returns the view site for the given page of this view.
 *
 * @param page the page
 * @return the corresponding site, or <code>null</code> if not found
 */
protected PageSite getPageSite(IPage page) {
	return (PageSite)mapPageToSite.get(page);
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
	return (PageRec) mapPartToRec.get(part);
}
/**
 * Returns the page record for the given page of this view.
 *
 * @param page the page
 * @return the corresponding page record, or <code>null</code> if not found
 */
protected PageRec getPageRec(IPage page) {
	Iterator enum = mapPartToRec.values().iterator();
	while (enum.hasNext()) {
		PageRec rec = (PageRec)enum.next();
		if (rec.page == page)
			return rec;
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
 *   otherwise
 */
protected abstract boolean isImportant(IWorkbenchPart part);
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void init(IViewSite site) throws PartInitException {
	site.setSelectionProvider(selectionProvider);
	super.init(site);
}
/**
 * The <code>PageBookView</code> implementation of this <code>IPartListener</code>
 * method shows the page when the given part is activated. Subclasses may extend.
 */
public void partActivated(IWorkbenchPart part) {
	// Is this an important part?  If not just return.
	if (!isImportant(part))
		return;
		
	// Create a page for the part.
	PageRec rec = getPageRec(part);
	if (rec == null)
		rec = createPage(part);

	// Show the page.
	if (rec != null) {
		showPageRec(rec);
	} else {
		showPageRec(defaultPageRec);
	}
}
/**
 * The <code>PageBookView</code> implementation of this <code>IPartListener</code>
 * method does nothing. Subclasses may extend.
 */
public void partBroughtToTop(IWorkbenchPart part) {
}
/**
 * The <code>PageBookView</code> implementation of this <code>IPartListener</code>
 * method deal with the closing of the active part. Subclasses may extend.
 */
public void partClosed(IWorkbenchPart part) {
	// Update the active part.
	if (activeRec != null && activeRec.part == part) {
		showPageRec(defaultPageRec);
	}
	
	// Find and remove the part page.
	PageRec rec = getPageRec(part);
	if (rec != null)
		removePage(rec);
}
/**
 * The <code>PageBookView</code> implementation of this <code>IPartListener</code>
 * method does nothing. Subclasses may extend.
 */
public void partDeactivated(IWorkbenchPart part) {
	// Do nothing.
}
/**
 * The <code>PageBookView</code> implementation of this <code>IPartListener</code>
 * method does nothing. Subclasses may extend.
 */
public void partOpened(IWorkbenchPart part) {
}
/* (non-Javadoc)
 * Refreshes the global actions for the active page.
 */
private void refreshGlobalActionHandlers() {
	// Clear old actions.
	IActionBars bars = getViewSite().getActionBars();
	bars.clearGlobalActionHandlers();

	// Set new actions.
	Map newActionHandlers = activeRec.subActionBars.getGlobalActionHandlers();
	if (newActionHandlers != null) {
		Set keys = newActionHandlers.entrySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			bars.setGlobalActionHandler((String)entry.getKey(),
				(IAction)entry.getValue());
		}
	}
}
/**
 * Removes a page.
 */
private void removePage(PageRec rec) {
	mapPageToSite.remove(rec.page);
	mapPartToRec.remove(rec.part);

	if (rec.subActionBars != null) {
		rec.subActionBars.dispose();
	}

	Control control = rec.page.getControl();
	if (control != null && !control.isDisposed()) {
		// Dispose the page's control so pages don't have to do this in their 
		// dispose method. 
		// The page's control is a child of this view's control so if this view 
		// is closed, the page's control will already be disposed.
		control.dispose();
	}

	// free the page 
	doDestroyPage(rec.part, rec);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void setFocus() {
	if (activeRec == null)
		book.setFocus();
	else
		activeRec.page.getControl().setFocus();
}

/**
 * Handle page selection changes.
 */
private void pageSelectionChanged(SelectionChangedEvent event) {
	// forward this change from a page to our site's selection provider
	SelectionProvider provider = (SelectionProvider)getSite().getSelectionProvider();
	if (provider != null)
		provider.selectionChanged(event);
}
/**
 * Shows a page for the active workbench part.
 */
private void showBootstrapPart() {
	IWorkbenchPart part = getBootstrapPart();
	if (part != null)
		partActivated(part);
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
protected void showPageRec(PageRec pageRec) {
	// If already showing do nothing
	if (activeRec == pageRec)
		return;
	
	// Hide old page.
	if (activeRec != null) {
		activeRec.subActionBars.deactivate();
		// remove our selection listener
		ISelectionProvider provider = ((PageSite)mapPageToSite.get(activeRec.page)).getSelectionProvider();
		if (provider != null) 
			provider.removeSelectionChangedListener(selectionChangedListener);
	}			
	// Show new page.
	activeRec = pageRec;
	Control pageControl = activeRec.page.getControl();
	if (pageControl != null && !pageControl.isDisposed()) {
		// Verify that the page control is not disposed
		// If we are closing, it may have already been disposed
		book.showPage(pageControl);
		activeRec.subActionBars.activate();
		refreshGlobalActionHandlers();
		// add our selection listener
		ISelectionProvider provider = 
			((PageSite)mapPageToSite.get(activeRec.page)).getSelectionProvider();
		if (provider != null) 
			provider.addSelectionChangedListener(selectionChangedListener);		
		// Update action bars.
		getViewSite().getActionBars().updateActionBars();
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
}
