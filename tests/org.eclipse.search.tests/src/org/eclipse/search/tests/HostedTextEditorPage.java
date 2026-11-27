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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * Test editor page that is not a multi-page editor and not an {@link ITextEditor} itself, but hosts
 * one and adapts to it.
 */
public class HostedTextEditorPage extends EditorPart {

	private final TextEditor fTextEditor= new TextEditor();

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		fTextEditor.init(site, input);
		fTextEditor.addPropertyListener((_, propId) -> firePropertyChange(propId));
	}

	@Override
	public void createPartControl(Composite parent) {
		fTextEditor.createPartControl(parent);
	}

	@Override
	public void setFocus() {
		fTextEditor.setFocus();
	}

	@Override
	public void dispose() {
		fTextEditor.dispose();
		super.dispose();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == ITextEditor.class) {
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
	public boolean isDirty() {
		return fTextEditor.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
