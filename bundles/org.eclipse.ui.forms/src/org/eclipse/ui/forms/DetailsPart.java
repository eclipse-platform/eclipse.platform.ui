/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;
import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class DetailsPart implements IFormPart, IPartSelectionListener {
	private IManagedForm managedForm;
	private ScrolledPageBook pageBook;
	private IStructuredSelection currentSelection;
	private Hashtable pages;
	private IDetailsPageProvider pageProvider;
	
	public DetailsPart(ManagedForm mform, ScrolledPageBook pageBook) {
		this.pageBook = pageBook;
		pages = new Hashtable();
		initialize(mform);
	}
	public DetailsPart(ManagedForm mform, Composite parent, int style) {
		this(mform, mform.getToolkit().createPageBook(parent, SWT.V_SCROLL|SWT.H_SCROLL));
	}
	public void registerPage(Object objectClass, IDetailsPage page) {
		pages.put(objectClass, page);
		page.initialize(managedForm);
	}
	public void setPageProvider(IDetailsPageProvider provider) {
		this.pageProvider = provider;
	}
	public void commit(boolean onSave) {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.commit();
	}
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#isDirty()
	 */
	public boolean isDirty() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			return page.isDirty();
		return false;
	}
	
	public boolean isStale() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			return page.isStale();
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#refresh()
	 */
	public void refresh() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.refresh();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		IDetailsPage page = getCurrentPage();
		if (page != null)
			page.setFocus();
	}
	
	public void markStale() {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public void setFormInput(Object input) {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		if (currentSelection != null) {
			// see if the page is dirty
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
						}
						if (fpage.isStale())
							fpage.refresh();
						fpage.inputChanged(currentSelection);
						pageBook.showPage(key);
					}
				});
				return;
			}
		}
		pageBook.showEmptyPage();
	}
}