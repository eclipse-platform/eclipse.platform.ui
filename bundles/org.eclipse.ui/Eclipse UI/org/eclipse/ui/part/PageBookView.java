package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.SubActionBars;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.FocusEvent;
import java.util.*;

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
 * This class should be subclassed by clients wishing to define new 
 * multi-page views.
 * </p>
 * <p>
 * When a <code>PageBookView</code> the following methods are
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
public abstract class PageBookView extends ViewPart 
	implements IPartListener
{
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
				if (event.getProperty() == SubActionBars.P_ACTION_HANDLERS
					&& event.getSource() == activeRec.subActionBars) {
					refreshGlobalActionHandlers();
				}
			}
		};
		
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
		SubActionBars bars = new SubActionBars(getViewSite().getActionBars());
		rec.subActionBars = bars;
		rec.page.setActionBars(bars);
		bars.addPropertyChangeListener(actionBarPropListener);
	}
	return rec;
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
	SubActionBars bars = new SubActionBars(getViewSite().getActionBars());
	defaultPageRec.subActionBars = bars;
	defaultPageRec.page.setActionBars(bars);
	bars.addPropertyChangeListener(actionBarPropListener);

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
	defaultPageRec = null;
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
 *
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
	if (activeRec.part == part) {
		activeRec.subActionBars.dispose();
		activeRec = null;
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
	mapPartToRec.remove(rec.part);

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
	// Hide old page.
	if (activeRec != null)
		activeRec.subActionBars.deactivate();

	// Show new page.
	activeRec = pageRec;
	Control pageControl = activeRec.page.getControl();
	if (pageControl != null && !pageControl.isDisposed()) {
		// Verify that the page control is not disposed
		// If we are closing, it may have already been disposed
		book.showPage(pageControl);
		activeRec.subActionBars.activate();
		refreshGlobalActionHandlers();

		// Update action bars.
		getViewSite().getActionBars().updateActionBars();
	}
}
}
