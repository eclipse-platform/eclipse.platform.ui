package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.*;
import org.eclipse.update.ui.forms.internal.*;
import java.util.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelection;
public abstract class MultiPageView extends ViewPart implements ISelectionListener {
	protected IFormWorkbook formWorkbook;
	private Vector pages;
	protected String firstPageId;
	private Hashtable table = new Hashtable();
	private Menu contextMenu;
	
	public MultiPageView () {
		formWorkbook = new NoTabsWorkbook();
		formWorkbook.setFirstPageSelected(false);
		pages = new Vector();
		createPages();
	}

	protected void addPage(String id, IUpdateFormPage page) {
		table.put(id, page);
		pages.addElement(page);
	}
	
	protected void removePage(IUpdateFormPage page) {
		table.remove(page);
		pages.removeElement(page);
	}
	
	public abstract void createPages();

	/**
	 * @see WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * @see WorkbenchPart#createPartControl(Composite)
	 */
public void createPartControl(Composite parent) {
	formWorkbook.createControl(parent);
	formWorkbook.addFormSelectionListener(new IFormSelectionListener() {
		public void formSelected(IFormPage page) {
			//updateSynchronizedViews((IPDEEditorPage) page);
			//getContributor().setActivePage((IPDEEditorPage) page);
		}
	});
	MenuManager manager = new MenuManager();
	IMenuListener listener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			contextMenuAboutToShow(manager);
		}
	};
	manager.setRemoveAllWhenShown(true);
	manager.addMenuListener(listener);
	contextMenu = manager.createContextMenu(formWorkbook.getControl());

	for (Iterator iter = pages.iterator(); iter.hasNext();) {
		IFormPage page = (IFormPage) iter.next();
		formWorkbook.addPage(page);
	}
    if (firstPageId != null)
	    showPage(firstPageId);
}

public void contextMenuAboutToShow(IMenuManager menu) {
/*
	PDEEditorContributor contributor = getContributor();
	getCurrentPage().contextMenuAboutToShow(menu);
	if (contributor!=null) contributor.contextMenuAboutToShow(menu);
*/
}
public IUpdateFormPage getPage(String pageId) {
	return (IUpdateFormPage)table.get(pageId);
}

public IUpdateFormPage getCurrentPage() {
	return (IUpdateFormPage)formWorkbook.getCurrentPage();
}

public Menu getContextMenu() {
	return contextMenu;
}

public IAction getAction(String id) {
	//return getContributor().getGlobalAction(id);
	return null;
}

public IFormPage showPage(String id) {
	return showPage(getPage(id));
}

public void showPage(String id, Object openToObject) {
	IUpdateFormPage page = showPage(getPage(id));
	if (page != null)
		page.openTo(openToObject);
}

public IUpdateFormPage showPage(final IUpdateFormPage page) {
	IUpdateFormPage oldPage = getCurrentPage();
	formWorkbook.selectPage(page);
	return page;
}

public void init(IViewSite site) throws PartInitException {
	setSite(site);
	site.getPage().addSelectionListener(this);
	for (Iterator iter = pages.iterator(); iter.hasNext();) {
		IUpdateFormPage page = (IUpdateFormPage) iter.next();
		page.init(null);
	}
}
	
public void dispose() {
	// remove ourselves as a selection listener
	getSite().getPage().removeSelectionListener(this);
	for (int i = 0; i < pages.size(); i++) {
		IUpdateFormPage page = (IUpdateFormPage) pages.elementAt(i);
		page.dispose();
	}
	// run super.
	super.dispose();
}

public void selectionChanged(IWorkbenchPart part, ISelection sel) {
}

}