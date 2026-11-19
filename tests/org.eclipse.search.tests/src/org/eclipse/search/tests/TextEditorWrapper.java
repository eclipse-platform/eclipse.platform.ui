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
 * Test editor that is not an {@link ITextEditor} itself, but wraps one and adapts to it.
 */
public class TextEditorWrapper extends MultiPageEditorPart {

	public static final String ID= "org.eclipse.search.tests.TextEditorWrapper"; //$NON-NLS-1$

	private TextEditor fTextEditor;

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	@Override
	protected void createPages() {
		fTextEditor= new TextEditor();
		try {
			addPage(fTextEditor, getEditorInput());
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == ITextEditor.class && fTextEditor != null) {
			return adapter.cast(fTextEditor);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		fTextEditor.doSave(monitor);
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
