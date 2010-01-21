/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;

import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;

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
 * they will share the common key binding and selection service. Since 3.1,
 * FormEditor is a page change provider. It allows listeners to attach to it and
 * get notified when pages are changed. This new API in JFace allows dynamic
 * help to update on page changes.
 * 
 * @since 3.0
 */
public abstract class FormEditor extends MultiPageEditorPart  {

	/**
	 * An array of pages currently in the editor. Page objects are not limited
	 * to those that implement <code>IFormPage</code>, hence the size of this
	 * array matches the number of pages as viewed by the user.
	 * <p>
	 * Subclasses can access this field but should not modify it.
	 */
	protected Vector pages = new Vector();

	private FormToolkit toolkit;

	private int currentPage = -1;

	private static class FormEditorSelectionProvider extends
			MultiPageSelectionProvider {
		private ISelection globalSelection;

		/**
		 * @param formEditor the editor
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
			if (globalSelection != null) {
			    return globalSelection;
			}
			return StructuredSelection.EMPTY;
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
		addPages();
	}

	/*
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */
	protected Composite createPageContainer(Composite parent) {
		parent = super.createPageContainer(parent);
		toolkit = createToolkit(parent.getDisplay());
		return parent;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#getSelectedPage()
	 */
	public Object getSelectedPage() {
		return getActivePageInstance();
	}

	/**
	 * Adds the form page to this editor. Form page will be loaded lazily. Its
	 * part control will not be created until it is activated for the first
	 * time.
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
	 * Adds the form page to this editor at the specified index (0-based). Form
	 * page will be loaded lazily. Its part control will not be created until it
	 * is activated for the first time.
	 * 
	 * @param index
	 *            the position to add the page at (0-based)
	 * @param page
	 *            the form page to add
	 * @since 3.1
	 */
	public void addPage(int index, IFormPage page) throws PartInitException {
		super.addPage(index, page.getPartControl());
		configurePage(index, page);
		updatePageIndices(index+1);
	}

	/**
	 * Adds a simple SWT control as a page. Overrides superclass implementation
	 * to keep track of pages.
	 * 
	 * @param control
	 *            the page control to add
	 * @return the 0-based index of the newly added page
	 */
	public int addPage(Control control) {
		int i = super.addPage(control);
		try {
			registerPage(-1, control);
		} catch (PartInitException e) {
			// cannot happen for controls
		}
		return i;
	}

	/**
	 * Adds a simple SWT control as a page. Overrides superclass implementation
	 * to keep track of pages.
	 * 
	 * @param control
	 *            the page control to add
	 * @param index
	 *            the index at which to add the page (0-based)
	 * @since 3.1
	 */
	public void addPage(int index, Control control) {
		super.addPage(index, control);
		try {
			registerPage(index, control);
		} catch (PartInitException e) {
			// cannot happen for controls
		}
		updatePageIndices(index+1);
	}

	/**
	 * Tests whether the editor is dirty by checking all the pages that
	 * implement <code>IFormPage</code>. If none of them is dirty, the method
	 * delegates further processing to <code>super.isDirty()</code>.
	 * 
	 * @return <code>true</code> if any of the pages in the editor are dirty,
	 *         <code>false</code> otherwise.
	 * @since 3.1
	 */

	public boolean isDirty() {
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++) {
				Object page = pages.get(i);
				if (page instanceof IFormPage) {
					IFormPage fpage = (IFormPage) page;
					if (fpage.isDirty())
						return true;

				} else if (page instanceof IEditorPart) {
					IEditorPart editor = (IEditorPart) page;
					if (editor.isDirty()) {
						return true;
					}
				}
			}
		}
		return super.isDirty();
	}
	
	/**
	 * Commits all dirty pages in the editor. This method should
	 * be called as a first step of a 'save' operation.
	 * @param onSave <code>true</code> if commit is performed as part
	 * of the 'save' operation, <code>false</code> otherwise.
	 * @since 3.3
	 */

	protected void commitPages(boolean onSave) {
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++) {
				Object page = pages.get(i);
				if (page instanceof IFormPage) {
					IFormPage fpage = (IFormPage)page;
					IManagedForm mform = fpage.getManagedForm();
					if (mform != null && mform.isDirty())
						mform.commit(onSave);
				}
			}
		}	
	}

	/**
	 * Adds a complete editor part to the multi-page editor.
	 * 
	 * @see MultiPageEditorPart#addPage(IEditorPart, IEditorInput)
	 */
	public int addPage(IEditorPart editor, IEditorInput input)
			throws PartInitException {
		int index = super.addPage(editor, input);
		if (editor instanceof IFormPage)
			configurePage(index, (IFormPage) editor);
		else
			registerPage(-1, editor);
		return index;
	}

	/**
	 * Adds a complete editor part to the multi-page editor at the specified
	 * position.
	 * 
	 * @see MultiPageEditorPart#addPage(int, IEditorPart, IEditorInput)
	 * @since 3.1
	 */
	public void addPage(int index, IEditorPart editor, IEditorInput input)
			throws PartInitException {
		super.addPage(index, editor, input);
		if (editor instanceof IFormPage)
			configurePage(index, (IFormPage) editor);
		else
			registerPage(index, editor);
		updatePageIndices(index+1);
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
		// setPageImage(index, page.getTitleImage());
		page.setIndex(index);
		registerPage(index, page);
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
			}
			updatePageIndices(pageIndex);
		}
		super.removePage(pageIndex);
	}

	// fix the page indices after the removal/insertion
	private void updatePageIndices(int start) {
		for (int i = start; i < pages.size(); i++) {
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
		if (toolkit != null) {
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
		int oldPageIndex = getCurrentPage();
		if (oldPageIndex != -1 && pages.size() > oldPageIndex
				&& pages.get(oldPageIndex) instanceof IFormPage
				&& oldPageIndex != newPageIndex) {
			// Check the old page
			IFormPage oldFormPage = (IFormPage) pages.get(oldPageIndex);
			if (oldFormPage.canLeaveThePage() == false) {
				setActivePage(oldPageIndex);
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
		if (oldPageIndex != -1 && pages.size() > oldPageIndex
				&& pages.get(oldPageIndex) instanceof IFormPage) {
			// Commit old page before activating the new one
			IFormPage oldFormPage = (IFormPage) pages.get(oldPageIndex);
			IManagedForm mform = oldFormPage.getManagedForm();
			if (mform != null)
				mform.commit(false);
		}
		if (pages.size() > newPageIndex
				&& pages.get(newPageIndex) instanceof IFormPage)
			((IFormPage) pages.get(newPageIndex)).setActive(true);
		if (oldPageIndex != -1 && pages.size() > oldPageIndex
				&& newPageIndex != oldPageIndex && 
				pages.get(oldPageIndex) instanceof IFormPage)
			((IFormPage) pages.get(oldPageIndex)).setActive(false);
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
	 * @see #setActivePage(String, Object)
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
			super.setActivePage(pageIndex);
			IFormPage activePage = (IFormPage) pages.get(pageIndex);
			activePage.setActive(true);
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

	private void registerPage(int index, Object page) throws PartInitException {
		if (!pages.contains(page)) {
			if (index == -1)
				pages.add(page);
			else
				pages.add(index, page);
		}
		if (page instanceof IFormPage) {
			IFormPage fpage = (IFormPage) page;
			if (fpage.isEditor() == false)
				fpage.init(getEditorSite(), getEditorInput());
		}
	}
}
