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
package org.eclipse.ui.forms.editor;

import java.util.Vector;

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
 * 
 * @since 3.0
 */

public abstract class FormEditor extends MultiPageEditorPart {
	private FormToolkit toolkit;
	private Vector pages;
	private IEditorPart sourcePage;
	private IEditorPart fLastActiveEditor = null;
	private int currentPage = -1;

	/**
	 * The constructor.
	 */

	public FormEditor() {
		pages = new Vector();
	}

	/**
	 * Creates the common toolkit for this editor and adds pages to the editor.
	 * 
	 * @see #addPages
	 */
	protected final void createPages() {
		toolkit = new FormToolkit(getContainer().getDisplay());
		addPages();
	}

	/**
	 * Subclass should implement this method to add pages to the editor using
	 * 'addPage(IFormPage)' method. @addPage
	 */
	protected abstract void addPages();

	/**
	 * Overrides 'super' to create a multi-page key binding editor site for key
	 * binding delegation down to the editor nested sites.
	 */
	protected IEditorSite createSite(IEditorPart editor) {
		return new MultiPageKeyBindingEditorSite(this, editor);
	}
	/**
	 * Adds the form page to this editor.
	 * 
	 * @param page
	 *            the page to add
	 */
	protected void addPage(IFormPage page) {
		int i = addPage(page.getPartControl());
		setPageText(i, page.getTitle());
		setPageImage(i, page.getTitleImage());
		page.setIndex(i);
		registerPage(page);
	}

	/**
	 * Disposes the pages and the toolkit after disposing the editor itself.
	 * Subclasses must call 'super' when reimplementing the method.
	 */
	public void dispose() {
		super.dispose();
		for (int i = 0; i < pages.size(); i++) {
			IFormPage page = (IFormPage) pages.get(i);
			page.dispose();
		}
		pages.clear();
		toolkit.dispose();
	}

	/**
	 * Returns the toolkit owned by this editor.
	 * 
	 * @return the toolkit object
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}

	/*
	 * Widens visibility for access by MultiPageKeyBindingEditorSite
	 */
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}

	/**
	 * Returns the current page index. The value is identical to the value of
	 * 'getActivePage()' except during the page switch, when this method still
	 * has the old active page index.
	 * 
	 * @return
	 */
	protected int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @see MultiPageEditorPart#pageChange(int)
	 */
	protected void pageChange(int newPageIndex) {
		// deactivate the old editor's site
		if (fLastActiveEditor != null) {
			((MultiPageKeyBindingEditorSite) fLastActiveEditor.getSite())
				.deactivate();
			fLastActiveEditor = null;
		}
		// Now is the absolute last moment to create the page control.
		IFormPage page = (IFormPage) pages.get(newPageIndex);
		if (page.getPartControl() == null) {
			page.createPartControl(getContainer());
		}
		// fix for windows handles
		int oldPage = getCurrentPage();
		if (pages.size() > newPageIndex
			&& pages.get(newPageIndex) instanceof IFormPage)
			 ((IFormPage) pages.get(newPageIndex)).setActive(true);
		if (pages.size() > oldPage && pages.get(oldPage) instanceof IFormPage)
			 ((IFormPage) pages.get(oldPage)).setActive(false);

		// Call super - this will cause pages to switch
		super.pageChange(newPageIndex);

		// activate the new editor's site
		IEditorPart activeEditor = getActiveEditor();
		if (activeEditor != null) {
			if (activeEditor.getSite()
				instanceof MultiPageKeyBindingEditorSite) {
				((MultiPageKeyBindingEditorSite) activeEditor.getSite())
					.activate();
				fLastActiveEditor = activeEditor;
			}
		}
		this.currentPage = newPageIndex;
	}

	/**
	 * Sets the active page using the unique page identifier.
	 * 
	 * @param pageId
	 *            the id of the page to switch to
	 * @return page that was set active or <samp>null</samp> if not found.
	 */
	public IFormPage setActivePage(String pageId) {
		for (int i = 0; i < pages.size(); i++) {
			IFormPage page = (IFormPage) pages.get(i);
			if (page.getId().equals(pageId)) {
				setActivePage(i);
				return page;
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
	 * @return page that was set active or <samp>null</samp> if not found.
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
	 * @see MultiPageEditorPart#setActivePage(int)
	 */
	protected void setActivePage(int pageIndex) {
		// fix for window handles problem
		// this should be called only when the editor is first opened
		if (pages.size() > pageIndex
			&& pages.get(pageIndex) instanceof IFormPage) {
			IFormPage activePage = (IFormPage) pages.get(pageIndex);
			activePage.setActive(true);
			super.setActivePage(pageIndex);
		} else
			super.setActivePage(pageIndex);

		// this is to enable the undo/redo actions before a page change has
		// occurred
		IEditorActionBarContributor contributor =
			getEditorSite().getActionBarContributor();
		if (contributor != null
			&& contributor instanceof MultiPageEditorActionBarContributor) {
			((MultiPageEditorActionBarContributor) contributor).setActivePage(
				getEditor(pageIndex));
		}
	}

	private void registerPage(IFormPage page) {
		if (!pages.contains(page))
			pages.add(page);
	}
}