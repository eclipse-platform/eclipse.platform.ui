package org.eclipse.update.ui.internal.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.update.ui.forms.*;
import java.util.*;
import org.eclipse.jface.action.*;


public abstract class MultiPageEditor extends EditorPart {
	protected IFormWorkbook formWorkbook;
	private Vector pages;
	protected String firstPageId;
	private Hashtable table = new Hashtable();
	private Menu contextMenu;
	
	public MultiPageEditor () {
		formWorkbook = new CustomWorkbook();
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
			editorContextMenuAboutToShow(manager);
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

public void editorContextMenuAboutToShow(IMenuManager menu) {
/*
	PDEEditorContributor contributor = getContributor();
	getCurrentPage().contextMenuAboutToShow(menu);
	if (contributor!=null) contributor.contextMenuAboutToShow(menu);
*/
}

public IFormPage getPage(String pageId) {
	return (IFormPage)table.get(pageId);
}

public IFormPage getCurrentPage() {
	return formWorkbook.getCurrentPage();
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
	IFormPage page = showPage(getPage(id));
	//if (page != null)
	//	page.openTo(openToObject);
}

public IFormPage showPage(final IFormPage page) {
	IFormPage oldPage = getCurrentPage();
	formWorkbook.selectPage(page);
	return page;
}

	/**
	 * @see EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see EditorPart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * @see EditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		setSite(site);
		setInput(input);
		for (Iterator iter = pages.iterator(); iter.hasNext();) {
			IUpdateFormPage page = (IUpdateFormPage) iter.next();
			page.init(null);
		}
	}


	/**
	 * @see EditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
	}

	/**
	 * @see EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/**
	 * @see EditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}

}

