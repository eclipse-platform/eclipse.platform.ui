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
package org.eclipse.ui.forms;
import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
/**
 * This managed form part handles the 'details' portion of the 
 * 'master/details' block. It has a page book that manages pages
 * of details registered for the current selection.
 * 
 * TODO (dejan) - spell out subclass contract
 * TODO (dejan) - mark non-overrideable methods as final
 * @since 3.0
 */
public class DetailsPart implements IFormPart, IPartSelectionListener {
	private IManagedForm managedForm;
	private ScrolledPageBook pageBook;
	private IFormPart masterPart;
	private IStructuredSelection currentSelection;
	private Hashtable pages;
	private IDetailsPageProvider pageProvider;
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
 * @param objectClass
 * @param page
 */
	public void registerPage(Object objectClass, IDetailsPage page) {
		pages.put(objectClass, page);
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
 * @boolean onSave <code>true</code> if commit is requested as a result
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
		for (Enumeration enum = pages.elements(); enum.hasMoreElements();) {
			IDetailsPage page = (IDetailsPage) enum.nextElement();
			page.dispose();
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
 */
	public boolean isDirty() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			return page.isDirty();
		return false;
	}
/**
 * Tests if the currently visible page is stale and needs refreshing.
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
		final IDetailsPage oldPage = getCurrentPage();
		if (key != null) {
			IDetailsPage page = (IDetailsPage) pages.get(key);
			if (page==null) {
				// try to get the page dynamically from the provider
				if (pageProvider!=null) {
					page = pageProvider.getPage(key);
					if (page!=null) {
						page.initialize(managedForm);
						pages.put(key, page);
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
		pageBook.showEmptyPage();
	}
}