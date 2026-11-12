/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * Test editor with two pages for its own file: a text editor page and a
 * {@link HostedTextEditorPage}. It opens on the hosted page and uses the default adapter, so it
 * adapts to the text editor hosted in the active page.
 */
public class SameFilePagesEditor extends MultiPageEditorPart {

	private static final int TEXT_PAGE= 0;

	private static final int HOSTED_PAGE= 1;

	private TextEditor fTextEditor;

	private HostedTextEditorPage fHostedPage;

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	public ITextEditor getHostedEditor() {
		return fHostedPage.getTextEditor();
	}

	public boolean isHostedPageActive() {
		return getActivePage() == HOSTED_PAGE;
	}

	@Override
	protected void createPages() {
		fTextEditor= new TextEditor();
		fHostedPage= new HostedTextEditorPage();
		try {
			addPage(TEXT_PAGE, fTextEditor, getEditorInput());
			addPage(HOSTED_PAGE, fHostedPage, getEditorInput());
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
		setActivePage(HOSTED_PAGE);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		fTextEditor.doSave(monitor);
		fHostedPage.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		// not allowed
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
