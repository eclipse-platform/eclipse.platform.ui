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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * Test editor with one text page for its own file and one for the sibling file
 * {@link #SIBLING_NAME}, like the PDE manifest editor. It opens on the sibling page.
 */
public class MultiFileEditor extends MultiPageEditorPart {

	public static final String SIBLING_NAME= "sibling.txt"; //$NON-NLS-1$

	private static final int OWN_PAGE= 0;

	private static final int SIBLING_PAGE= 1;

	private TextEditor fOwnEditor;

	private TextEditor fSiblingEditor;

	public ITextEditor getOwnEditor() {
		return fOwnEditor;
	}

	public ITextEditor getSiblingEditor() {
		return fSiblingEditor;
	}

	public boolean isOwnPageActive() {
		return getActivePage() == OWN_PAGE;
	}

	@Override
	protected void createPages() {
		IFile file= ((IFileEditorInput) getEditorInput()).getFile();
		IFile sibling= file.getParent().getFile(IPath.fromPortableString(SIBLING_NAME));
		fOwnEditor= new TextEditor();
		fSiblingEditor= new TextEditor();
		try {
			addPage(OWN_PAGE, fOwnEditor, getEditorInput());
			addPage(SIBLING_PAGE, fSiblingEditor, new FileEditorInput(sibling));
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
		setActivePage(SIBLING_PAGE);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		fOwnEditor.doSave(monitor);
		fSiblingEditor.doSave(monitor);
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
