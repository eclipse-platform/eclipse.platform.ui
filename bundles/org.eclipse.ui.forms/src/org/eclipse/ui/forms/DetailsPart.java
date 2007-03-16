/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
/**
 * This managed form part handles the 'details' portion of the 
 * 'master/details' block. It has a page book that manages pages
 * of details registered for the current selection.
 * <p>By default, details part accepts any number of pages.
 * If dynamic page provider is registered, this number may
 * be excessive. To avoid running out of steam (by creating 
 * a large number of pages with widgets on each), maximum
 * number of pages can be set to some reasonable value (e.g. 10).
 * When this number is reached, old pages (those created first)
 * will be removed and disposed as new ones are added. If
 * the disposed pages are needed again after that, they
 * will be created again.
 * 
 * @since 3.0
 */
public final class DetailsPart implements IFormPart, IPartSelectionListener {
	private IManagedForm managedForm;
	private ScrolledPageBook pageBook;
	private IFormPart masterPart;
	private IStructuredSelection currentSelection;
	private Hashtable pages;
	private IDetailsPageProvider pageProvider;
	private int pageLimit=Integer.MAX_VALUE;
	
	private static class PageBag {
		private static int counter;
		private int ticket;
		private IDetailsPage page;
		private boolean fixed;
		
		public PageBag(IDetailsPage page, boolean fixed) {
			this.page= page;
			this.fixed = fixed;
			this.ticket = ++counter;
		}
		public int getTicket() {
			return ticket;
		}
		public IDetailsPage getPage() {
			return page;
		}
		public void dispose() {
			page.dispose();
			page=null;
		}
		public boolean isDisposed() {
			return page==null;
		}
		public boolean isFixed() {
			return fixed;
		}
		public static int getCurrentTicket() {
			return counter;
		}
	}
/**
 * Creates a details part by wrapping the provided page book.
 * @param mform the parent form
 * @param pageBook the page book to wrap
 */	
	public DetailsPart(IManagedForm mform, ScrolledPageBook pageBook) {
		this.pageBook = pageBook;
		pages = new Hashtable();
		initialize(mform);
	}
/**
 * Creates a new details part in the provided form by creating 
 * the page book.
 * @param mform the parent form
 * @param parent the composite to create the page book in
 * @param style the style for the page book
 */
	public DetailsPart(IManagedForm mform, Composite parent, int style) {
		this(mform, mform.getToolkit().createPageBook(parent, style|SWT.V_SCROLL|SWT.H_SCROLL));
	}
/**
 * Registers the details page to be used for all the objects of
 * the provided object class.
 * @param objectClass an object of type 'java.lang.Class' to be used 
 * as a key for the provided page
 * @param page the page to show for objects of the provided object class
 */
	public void registerPage(Object objectClass, IDetailsPage page) {
		registerPage(objectClass, page, true);
	}
	
	private void registerPage(Object objectClass, IDetailsPage page, boolean fixed) {
		pages.put(objectClass, new PageBag(page, fixed));
		page.initialize(managedForm);
	}
/**
 * Sets the dynamic page provider. The dynamic provider can return
 * different pages for objects of the same class based on their state.
 * @param provider the provider to use
 */
	public void setPageProvider(IDetailsPageProvider provider) {
		this.pageProvider = provider;
	}
/**
 * Commits the part by committing the current page.
 * @param onSave <code>true</code> if commit is requested as a result
 * of the 'save' action, <code>false</code> otherwise.
 */
	public void commit(boolean onSave) {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.commit(onSave);
	}
/**
 * Returns the current page visible in the part.
 * @return the current page
 */
	public IDetailsPage getCurrentPage() {
		Control control = pageBook.getCurrentPage();
		if (control != null) {
			Object data = control.getData();
			if (data instanceof IDetailsPage)
				return (IDetailsPage) data;
		}
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#dispose()
	 */
	public void dispose() {
		for (Enumeration enm = pages.elements(); enm.hasMoreElements();) {
			PageBag pageBag = (PageBag) enm.nextElement();
			pageBag.dispose();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}
/**
 * Tests if the currently visible page is dirty.
 * @return <code>true</code> if the page is dirty, <code>false</code> otherwise.
 */
	public boolean isDirty() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			return page.isDirty();
		return false;
	}
/**
 * Tests if the currently visible page is stale and needs refreshing.
 * @return <code>true</code> if the page is stale, <code>false</code> otherwise.
 */	
	public boolean isStale() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			return page.isStale();
		return false;
	}

/**
 * Refreshes the current page.
 */
	public void refresh() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.refresh();
	}
/**
 * Sets the focus to the currently visible page.
 */
	public void setFocus() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.setFocus();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public boolean setFormInput(Object input) {
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		this.masterPart = part;
		if (currentSelection != null) {
		}
		if (selection instanceof IStructuredSelection)
			currentSelection = (IStructuredSelection) selection;
		else
			currentSelection = null;
		update();
	}
	private void update() {
		Object key = null;
		if (currentSelection != null) {
			for (Iterator iter = currentSelection.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (key == null)
					key = getKey(obj);
				else if (getKey(obj).equals(key) == false) {
					key = null;
					break;
				}
			}
		}
		showPage(key);
	}
	private Object getKey(Object object) {
		if (pageProvider!=null) {
			Object key = pageProvider.getPageKey(object);
			if (key!=null)
				return key;
		}
		return object.getClass();
	}
	private void showPage(final Object key) {
		checkLimit();
		final IDetailsPage oldPage = getCurrentPage();
		if (key != null) {
			PageBag pageBag = (PageBag)pages.get(key);
			IDetailsPage page = pageBag!=null?pageBag.getPage():null;
			if (page==null) {
				// try to get the page dynamically from the provider
				if (pageProvider!=null) {
					page = pageProvider.getPage(key);
					if (page!=null) {
						registerPage(key, page, false);
					}
				}
			}
			if (page != null) {
				final IDetailsPage fpage = page;
				BusyIndicator.showWhile(pageBook.getDisplay(), new Runnable() {
					public void run() {
						if (!pageBook.hasPage(key)) {
							Composite parent = pageBook.createPage(key);
							fpage.createContents(parent);
							parent.setData(fpage);
						}
						//commit the current page
						if (oldPage!=null && oldPage.isDirty())
							oldPage.commit(false);
						//refresh the new page
						if (fpage.isStale())
							fpage.refresh();
						fpage.selectionChanged(masterPart, currentSelection);
						pageBook.showPage(key);
					}
				});
				return;
			}
		}
		// If we are switching from an old page to nothing,
		// don't loose data 
		if (oldPage!=null && oldPage.isDirty())
			oldPage.commit(false);
		pageBook.showEmptyPage();
	}
	private void checkLimit() {
		if (pages.size() <= getPageLimit()) return;
		// overflow
		int currentTicket = PageBag.getCurrentTicket();
		int cutoffTicket = currentTicket - getPageLimit();
		for (Enumeration enm=pages.keys(); enm.hasMoreElements();) {
			Object key = enm.nextElement();
			PageBag pageBag = (PageBag)pages.get(key);
			if (pageBag.getTicket()<=cutoffTicket) {
				// candidate - see if it is active and not fixed
				if (!pageBag.isFixed() && !pageBag.getPage().equals(getCurrentPage())) {
					// drop it
					pageBag.dispose();
					pages.remove(key);
					pageBook.removePage(key, false);				
				}
			}
		}
	}
	/**
	 * Returns the maximum number of pages that should be
	 * maintained in this part. When an attempt is made to
	 * add more pages, old pages are removed and disposed
	 * based on the order of creation (the oldest pages
	 * are removed). The exception is made for the 
	 * page that should otherwise be disposed but is
	 * currently active.
	 * @return maximum number of pages for this part
	 */
	public int getPageLimit() {
		return pageLimit;
	}
	/**
	 * Sets the page limit for this part. 
	 * @see #getPageLimit()
	 * @param pageLimit the maximum number of pages that
	 * should be maintained in this part.
	 */
	public void setPageLimit(int pageLimit) {
		this.pageLimit = pageLimit;
		checkLimit();
	}
}
