/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;
import java.util.Vector;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.*;
/**
 * This class forms a base of multi-page form editors that typically use one or
 * more pages with forms and one page for raw source of the editor input.
 * <p>
 * Pages are added 'lazily' i.e. adding a page reserves a tab for it but does
 * not cause the page control to be created. Page control is created when an
 * attempt is made to select the page in question. This allows editors with
 * several tabs and complex pages to open quickly.
 * <p>
 * Subclasses should extend this class and implement <code>addPages</code>
 * method. One of the two <code>addPage</code> methods should be called to
 * contribute pages to the editor. One adds complete (standalone) editors as
 * nested tabs. These editors will be created right away and will be hooked so
 * that key bindings, selection service etc. is compatible with the one for the
 * standalone case. The other method adds classes that implement
 * <code>IFormPage</code> interface. These pages will be created lazily and
 * they will share the common key binding and selection service.
 * 
 * @since 3.0
 */
public abstract class FormEditor extends MultiPageEditorPart {
	private FormToolkit toolkit;
	protected Vector pages;
	private IEditorPart sourcePage;
	private int currentPage = -1;
	private static class FormEditorSelectionProvider
			extends
				MultiPageSelectionProvider {
		private ISelection globalSelection;
		/**
		 * @param multiPageEditor
		 */
		public FormEditorSelectionProvider(FormEditor formEditor) {
			super(formEditor);
		}
		public ISelection getSelection() {
			IEditorPart activeEditor = ((FormEditor) getMultiPageEditor())
					.getActiveEditor();
			if (activeEditor != null) {
				ISelectionProvider selectionProvider = activeEditor.getSite()
						.getSelectionProvider();
				if (selectionProvider != null)
					return selectionProvider.getSelection();
			}
			return globalSelection;
		}
		/*
		 * (non-Javadoc) Method declared on <code> ISelectionProvider </code> .
		 */
		public void setSelection(ISelection selection) {
			IEditorPart activeEditor = ((FormEditor) getMultiPageEditor())
					.getActiveEditor();
			if (activeEditor != null) {
				ISelectionProvider selectionProvider = activeEditor.getSite()
						.getSelectionProvider();
				if (selectionProvider != null)
					selectionProvider.setSelection(selection);
			} else {
				this.globalSelection = selection;
				fireSelectionChanged(new SelectionChangedEvent(this,
						globalSelection));
			}
		}
	}
	/**
	 * The constructor.
	 */
	public FormEditor() {
		pages = new Vector();
	}
	/**
	 * Overrides super to plug in a different selection provider.
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		site.setSelectionProvider(new FormEditorSelectionProvider(this));
	}
	/**
	 * Creates the common toolkit for this editor and adds pages to the editor.
	 * 
	 * @see #addPages
	 */
	protected void createPages() {
		toolkit = createToolkit(getContainer().getDisplay());
		addPages();
	}
	/**
	 * Creates the form toolkit. The method can be implemented to substitute a
	 * subclass of the toolkit that should be used for this editor. A typical
	 * use of this method would be to create the form toolkit using one shared
	 * <code>FormColors</code> object to share resources across the multiple
	 * editor instances.
	 * 
	 * @param display
	 *            the display to use when creating the toolkit
	 * @return the newly created toolkit instance
	 */
	protected FormToolkit createToolkit(Display display) {
		return new FormToolkit(display);
	}
	/**
	 * Subclass should implement this method to add pages to the editor using
	 * 'addPage(IFormPage)' method.
	 */
	protected abstract void addPages();
	/**
	 * Adds the form page to this editor. Form page will be
	 * loaded lazily. Its part control will not be created
	 * until it is activated for the first time.
	 * 
	 * @param page
	 *            the form page to add
	 */
	public int addPage(IFormPage page) throws PartInitException {
		int i = super.addPage(page.getPartControl());
		configurePage(i, page);
		return i;
	}
/**
 * Adds a simple SWT control as a page. Overrides superclass
 * implementation to keep track of pages.
 *@param control the page control to add
 *@return the 0-based index of the newly added page 
 */
	public int addPage(Control control) {
		int i = super.addPage(control);
		try {
			registerPage(control);
		} catch (PartInitException e) {
			// cannot happen for controls
		}
		return i;
	}
	/**
	 * Adds the complete editor part to the multi-page editor.
	 */
	public int addPage(IEditorPart editor, IEditorInput input)
			throws PartInitException {
		int index = super.addPage(editor, input);
		if (editor instanceof IFormPage)
			configurePage(index, (IFormPage) editor);
		else
			registerPage(editor);
		return index;
	}
	/**
	 * Configures the form page.
	 * 
	 * @param index
	 *            the page index
	 * @param page
	 *            the page to configure
	 * @throws PartInitException
	 *             if there are problems in configuring the page
	 */
	protected void configurePage(int index, IFormPage page)
			throws PartInitException {
		setPageText(index, page.getTitle());
		//setPageImage(index, page.getTitleImage());
		page.setIndex(index);
		registerPage(page);
	}
	/**
	 * Overrides the superclass to remove the page from the page table.
	 * 
	 * @param pageIndex
	 *            the 0-based index of the page in the editor
	 */
	public void removePage(int pageIndex) {
		if (pageIndex >= 0 && pageIndex < pages.size()) {
			Object page = pages.get(pageIndex);
			pages.remove(page);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) page;
				if (!fpage.isEditor())
					fpage.dispose();
				updatePageIndices();
			}
		}
		super.removePage(pageIndex);
	}
	// fix the page indices after the removal
	private void updatePageIndices() {
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) page;
				fpage.setIndex(i);
			}
		}
	}
	/**
	 * Called to indicate that the editor has been made dirty or the changes
	 * have been saved.
	 */
	public void editorDirtyStateChanged() {
		firePropertyChange(PROP_DIRTY);
	}
	/**
	 * Disposes the pages and the toolkit after disposing the editor itself.
	 * Subclasses must call 'super' when reimplementing the method.
	 */
	public void dispose() {
		super.dispose();
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) page;
				// don't dispose source pages because they will
				// be disposed as nested editors by the superclass
				if (!fpage.isEditor())
					fpage.dispose();
			}
		}
		pages = null;
		// toolkit may be null if editor has been instantiated
		// but never created - see defect #62190  
		if (toolkit!=null) {
			toolkit.dispose();
			toolkit = null;
		}
	}
	/**
	 * Returns the toolkit owned by this editor.
	 * 
	 * @return the toolkit object
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}
	/**
	 * Widens the visibility of the method in the superclass.
	 * 
	 * @return the active nested editor
	 */
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}
	/**
	 * Returns the current page index. The value is identical to the value of
	 * 'getActivePage()' except during the page switch, when this method still
	 * has the old active page index.
	 * <p>
	 * Another important difference is during the editor closing. When the tab
	 * folder is disposed, 'getActivePage()' will return -1, while this method
	 * will still return the last active page.
	 * 
	 * @see #getActivePage
	 * 
	 * @return the currently selected page or -1 if no page is currently
	 *         selected
	 */
	protected int getCurrentPage() {
		return currentPage;
	}
	/**
	 * @see MultiPageEditorPart#pageChange(int)
	 */
	protected void pageChange(int newPageIndex) {
		// fix for windows handles
		int oldPage = getCurrentPage();
		if (oldPage != -1 && pages.size() > oldPage
				&& pages.get(oldPage) instanceof IFormPage
				&& oldPage != newPageIndex) {
			// Check the old page
			IFormPage oldFormPage = (IFormPage) pages.get(oldPage);
			if (oldFormPage.canLeaveThePage() == false) {
				setActivePage(oldPage);
				return;
			}
		}
		// Now is the absolute last moment to create the page control.
		Object page = pages.get(newPageIndex);
		if (page instanceof IFormPage) {
			IFormPage fpage = (IFormPage) page;
			if (fpage.getPartControl() == null) {
				fpage.createPartControl(getContainer());
				setControl(newPageIndex, fpage.getPartControl());
				fpage.getPartControl().setMenu(getContainer().getMenu());
			}
		}
		if (oldPage != -1 && pages.size() > oldPage
				&& pages.get(oldPage) instanceof IFormPage) {
			// Commit old page before activating the new one
			IFormPage oldFormPage = (IFormPage) pages.get(oldPage);
			IManagedForm mform = oldFormPage.getManagedForm();
			if (mform != null)
				mform.commit(false);
		}
		if (pages.size() > newPageIndex
				&& pages.get(newPageIndex) instanceof IFormPage)
			((IFormPage) pages.get(newPageIndex)).setActive(true);
		if (oldPage != -1 && pages.size() > oldPage
				&& pages.get(oldPage) instanceof IFormPage)
			((IFormPage) pages.get(oldPage)).setActive(false);
		// Call super - this will cause pages to switch
		super.pageChange(newPageIndex);
		this.currentPage = newPageIndex;
	}
	/**
	 * Sets the active page using the unique page identifier.
	 * 
	 * @param pageId
	 *            the id of the page to switch to
	 * @return page that was set active or <samp>null </samp> if not found.
	 */
	public IFormPage setActivePage(String pageId) {
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) page;
				if (fpage.getId().equals(pageId)) {
					setActivePage(i);
					return fpage;
				}
			}
		}
		return null;
	}
	/**
	 * Finds the page instance that has the provided id.
	 * 
	 * @param pageId
	 *            the id of the page to find
	 * @return page with the matching id or <code>null</code> if not found.
	 */
	public IFormPage findPage(String pageId) {
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) pages.get(i);
				if (fpage.getId().equals(pageId))
					return fpage;
			}
		}
		return null;
	}
	/**
	 * Sets the active page using the unique page identifier and sets its input
	 * to the provided object.
	 * 
	 * @param pageId
	 *            the id of the page to switch to
	 * @param pageInput
	 *            the page input
	 * @return page that was set active or <samp>null </samp> if not found.
	 */
	public IFormPage setActivePage(String pageId, Object pageInput) {
		IFormPage page = setActivePage(pageId);
		if (page != null) {
			IManagedForm mform = page.getManagedForm();
			if (mform != null)
				mform.setInput(pageInput);
		}
		return page;
	}
	/**
	 * Iterates through the pages calling similar method until a page is found
	 * that contains the desired page input.
	 * 
	 * @param pageInput
	 *            the object to select and reveal
	 * @return the page that accepted the request or <code>null</code> if no
	 *         page has the desired object.
	 * 
	 * @see #setActivePage
	 */
	public IFormPage selectReveal(Object pageInput) {
		for (int i = 0; i < pages.size(); i++) {
			Object page = pages.get(i);
			if (page instanceof IFormPage) {
				IFormPage fpage = (IFormPage) page;
				if (fpage.selectReveal(pageInput))
					return fpage;
			}
		}
		return null;
	}
	/**
	 * Returns active page instance if the currently selected page index is not
	 * -1, or <code>null</code> if it is.
	 * 
	 * @return active page instance if selected, or <code>null</code> if no
	 *         page is currently active.
	 */
	public IFormPage getActivePageInstance() {
		int index = getActivePage();
		if (index != -1) {
			Object page = pages.get(index);
			if (page instanceof IFormPage)
				return (IFormPage) page;
		}
		return null;
	}
	/**
	 * @see MultiPageEditorPart#setActivePage(int)
	 */
	protected void setActivePage(int pageIndex) {
		// fix for window handles problem
		// this should be called only when the editor is first opened
		if (pages.size() > pageIndex
				&& pages.get(pageIndex) instanceof IFormPage) {
			pageChange(pageIndex);
			IFormPage activePage = (IFormPage) pages.get(pageIndex);
			activePage.setActive(true);
			super.setActivePage(pageIndex);
		} else
			super.setActivePage(pageIndex);
		updateActionBarContributor(pageIndex);
	}
	/**
	 * Notifies action bar contributor about page change.
	 * 
	 * @param pageIndex
	 *            the index of the new page
	 */
	protected void updateActionBarContributor(int pageIndex) {
		// this is to enable the undo/redo actions before a page change has
		// occurred
		IEditorActionBarContributor contributor = getEditorSite()
				.getActionBarContributor();
		if (contributor != null
				&& contributor instanceof MultiPageEditorActionBarContributor) {
			((MultiPageEditorActionBarContributor) contributor)
					.setActivePage(getEditor(pageIndex));
		}
	}
	/**
	 * Closes the editor programmatically.
	 * 
	 * @param save
	 *            if <code>true</code>, the content should be saved before
	 *            closing.
	 */
	public void close(final boolean save) {
		Display display = getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (toolkit != null) {
					getSite().getPage().closeEditor(FormEditor.this, save);
				}
			}
		});
	}
	private void registerPage(Object page) throws PartInitException {
		if (!pages.contains(page))
			pages.add(page);
		if (page instanceof IFormPage) {
			IFormPage fpage = (IFormPage) page;
			if (fpage.isEditor() == false)
				fpage.init(getEditorSite(), getEditorInput());
		}
	}
}
