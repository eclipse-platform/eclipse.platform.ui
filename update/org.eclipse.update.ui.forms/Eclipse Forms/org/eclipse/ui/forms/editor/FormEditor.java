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

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * This class forms a base of multi-page form editors that typically use one or
 * more pages with forms and one page for raw source of the editor input.
 * <p>Pages are added 'lazily' i.e. adding a page reserves a tab for it
 * but does not cause the page control to be created. Page control
 * is created when an attempt is made to select the page in question.
 * This allows editors with several tabs and complex pages to open
 * quickly.
 * 
 * @since 3.0
 */

public abstract class FormEditor extends MultiPageEditorPart {
	private FormToolkit toolkit;
	private Vector pages;

	/**
	 * The constructor.
	 */

	public FormEditor() {
	}

	/**
	 * Creates the common toolkit for this editor. Subclasses should override
	 * this method to create pages but must call 'super' before attempting to
	 * use the toolkit.
	 */
	protected void createPages() {
		toolkit = new FormToolkit(getContainer().getDisplay());
	}

	protected void addPage(IFormPage page) {
		int i = addPage(page.getPartControl());
		setPageText(i, page.getTitle());
		setPageImage(i, page.getTitleImage());
		page.setIndex(i);
		registerPage(page);
	}

	/**
	 * Disposes the toolkit after disposing the editor itself.
	 */
	public void dispose() {
		super.dispose();
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

	/**
	 * @see MultiPageEditorPart#pageChange(int)
	 */
/*
	protected void pageChange(int newPageIndex) {
		// fix for windows handles
		int oldPage = getCurrentPage();
		if (fPages.size() > newPageIndex && fPages.get(newPageIndex) instanceof IFlatPage)
			((IFlatPage) fPages.get(newPageIndex)).setActivated(true);
		if (fPages.size() > oldPage && fPages.get(oldPage) instanceof IFlatPage)
			((IFlatPage) fPages.get(oldPage)).setActivated(false);
		super.pageChange(newPageIndex);
	}
*/	
	
	private void registerPage(IFormPage page) {
		if (pages == null)
			pages = new Vector();
		if (!pages.contains(page))
			pages.add(page);
	}	
}