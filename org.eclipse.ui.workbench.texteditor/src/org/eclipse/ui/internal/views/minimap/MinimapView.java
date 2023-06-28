/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 *******************************************************************************/
package org.eclipse.ui.internal.views.minimap;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSite;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Minimap view used to display content of current text editor with scale.
 *
 */
public class MinimapView extends PageBookView {

	private String defaultText;

	public MinimapView() {
		this.defaultText = MinimapMessages.MinimapViewNoMinimap;
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		this.initPage(page);
		page.createControl(book);
		page.setMessage(this.defaultText);
		return page;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		Page page = createMinimapPage(part);
		if (page == null) {
			return null;
		}
		PageSite site = new PageSite(this.getViewSite());
		page.init(site);
		page.createControl(this.getPageBook());
		return new PageBookView.PageRec(part, page);
	}

	private Page createMinimapPage(IWorkbenchPart part) {
		if (part instanceof MultiPageEditorPart) {
			return new MultiPageMinimapPage((MultiPageEditorPart) part);
		}
		return MinimapPage.createMinimapPage((ITextEditor) part);
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		IPage page = rec.page;
		page.dispose();
		rec.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = this.getSite().getPage();
		return page != null ? page.getActiveEditor() : null;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof ITextEditor || part instanceof MultiPageEditorPart;
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}
}
