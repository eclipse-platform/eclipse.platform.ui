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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.IPageChangedListener;

import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Minimap with multi page editor which shows a minimap for the current page
 * which is an {@link ITextEditor}.
 */
public class MultiPageMinimapPage extends Page {

	private final MultiPageEditorPart fMultiPageEditor;
	private final Map<Object, MinimapPage> fMinimapPageMap;
	private final Set<Object> fUnsupportedEditorPages;
	private final IPageChangedListener fPageChangedListener;
	private PageBook fPageBook;
	private Label fErrorLabel;

	public MultiPageMinimapPage(MultiPageEditorPart multiPageEditor) {
		this.fMultiPageEditor = multiPageEditor;
		this.fMinimapPageMap = new HashMap<>();
		this.fUnsupportedEditorPages = new HashSet<>();
		this.fPageChangedListener = e -> {
			Object selectedPage = multiPageEditor.getSelectedPage();
			// Find from cache the minimap for the selected page
			MinimapPage minimapPage = fMinimapPageMap.get(selectedPage);
			if (minimapPage != null) {
				fPageBook.showPage(minimapPage.getControl());
				return;
			}
			if (fUnsupportedEditorPages.contains(selectedPage)) {
				fPageBook.showPage(fErrorLabel);
				return;
			}

			if (selectedPage instanceof ITextEditor) {
				// Create and show a minimap page for the given text editor page
				ITextEditor textEditor = (ITextEditor) selectedPage;
				minimapPage = MinimapPage.createMinimapPage(textEditor);
			}
			if (minimapPage != null) {
				minimapPage.createControl(fPageBook);
				fMinimapPageMap.put(selectedPage, minimapPage);
				fPageBook.showPage(minimapPage.getControl());
			} else {
				fUnsupportedEditorPages.add(selectedPage);
				fPageBook.showPage(fErrorLabel);
			}
		};
		multiPageEditor.addPageChangedListener(fPageChangedListener);
	}

	@Override
	public void createControl(Composite parent) {
		fPageBook = new PageBook(parent, SWT.NORMAL);
		fErrorLabel = new Label(fPageBook, SWT.NORMAL);
		fErrorLabel.setText(MinimapMessages.MinimapViewNoMinimap);
		fPageChangedListener.pageChanged(null);
	}

	@Override
	public Control getControl() {
		return fPageBook;
	}

	@Override
	public void setFocus() {
		fPageBook.setFocus();
	}

	@Override
	public void dispose() {
		fMultiPageEditor.removePageChangedListener(fPageChangedListener);
		fMinimapPageMap.values().forEach(MinimapPage::dispose);
		fMinimapPageMap.clear();
		fUnsupportedEditorPages.clear();
		super.dispose();
	}
}
