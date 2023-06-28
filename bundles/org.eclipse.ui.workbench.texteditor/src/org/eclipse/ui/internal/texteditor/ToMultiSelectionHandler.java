/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.Adapters;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension5;

public class ToMultiSelectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		ITextEditor textEditor = Adapters.adapt(editor, ITextEditor.class);
		if (textEditor == null) {
			return null;
		}
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		if (!(selection instanceof IBlockTextSelection)) {
			return null;
		}
		IBlockTextSelection blockSelection = (IBlockTextSelection) selection;
		IRegion[] initialRegions = ((IMultiTextSelection) blockSelection).getRegions();
		IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
		if (document == null) {
			return null;
		}
		IMultiTextSelection newSelection = new MultiTextSelection(document, initialRegions);
		if (!(editor instanceof ITextEditorExtension5)) {
			return null;
		}
		ITextEditorExtension5 ext = (ITextEditorExtension5) editor;
		ext.setBlockSelectionMode(false);
		textEditor.getSelectionProvider().setSelection(newSelection);
		return newSelection;
	}

}
