/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DetailsPart implements IFormPart, IPartSelectionListener {
	private IManagedForm managedForm;
	private ScrolledPageBook pageBook;
	private IStructuredSelection currentSelection;
	private Hashtable pages;

	public DetailsPart(ManagedForm mform, ScrolledPageBook pageBook) {
		this.pageBook = pageBook;
		pages = new Hashtable();
		initialize(mform);
	}
	
	public DetailsPart(ManagedForm mform, Composite parent, int style) {
		this(mform, mform.getToolkit().createPageBook(parent));
	}
	
	public void registerPage(Object objectClass, IDetailsPage page) {
		pages.put(objectClass, page);
		page.initialize(managedForm);
	}
	
	public void commit(boolean onSave) {
		IDetailsPage page = getCurrentPage();
		if (page!=null)
			page.commit();
	}
	public IDetailsPage getCurrentPage() {
		Control control = pageBook.getCurrentPage();
		if (control!=null) {
			Object data = control.getData();
			if (data instanceof IDetailsPage)
				return (IDetailsPage)data;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#dispose()
	 */
	public void dispose() {
		for (Enumeration enum=pages.elements(); enum.hasMoreElements();) {
			IDetailsPage page = (IDetailsPage)enum.nextElement();
			page.dispose();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#isDirty()
	 */
	public boolean isDirty() {
		IDetailsPage page = getCurrentPage();
		if (page!=null) return page.isDirty();
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#refresh()
	 */
	public void refresh() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		IDetailsPage page = getCurrentPage();
		if (page!=null)
			page.setFocus();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public void setFormInput(Object input) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		if (currentSelection!=null) {
			// see if the page is dirty
		}
		if (selection instanceof IStructuredSelection)
			currentSelection = (IStructuredSelection)selection;
		else currentSelection = null;
		update();
	}
	private void update() {
		Class key=null;
		if (currentSelection != null) {
			for (Iterator iter=currentSelection.iterator();iter.hasNext();) {
				Object obj = iter.next();
				if (key==null)
					key = obj.getClass();
				else if (obj.getClass().equals(key)==false) {
					key=null;
					break;
				}
			}
		}
		showPage(key);
	}
	private void showPage(Object key) {
		if (key!=null) {
			IDetailsPage page = (IDetailsPage)pages.get(key);
			if (page!=null) {
				if (!pageBook.hasPage(key)) {
					Composite parent = pageBook.createPage(key);
					page.createContents(parent);
				}
				page.inputChanged(currentSelection);
				pageBook.showPage(key);
				return;
			}
		}
		pageBook.showEmptyPage();
	}
}