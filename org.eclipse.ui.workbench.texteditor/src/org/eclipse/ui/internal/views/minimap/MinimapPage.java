/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 *******************************************************************************/
package org.eclipse.ui.internal.views.minimap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.part.Page;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Minimap page which displays scaled content of the given text editor.
 *
 */
public class MinimapPage extends Page {

	private final ISourceViewer fEditorViewer;
	private MinimapWidget fMinimapWidget;

	public MinimapPage(ITextEditor textEditor) {
		fEditorViewer = (ISourceViewer) textEditor.getAdapter(ITextOperationTarget.class);
	}

	@Override
	public void createControl(Composite parent) {
		// Create minimap styled text
		fMinimapWidget = new MinimapWidget(parent, fEditorViewer);
		fMinimapWidget.install();
	}

	@Override
	public Control getControl() {
		return fMinimapWidget.getControl();
	}

	@Override
	public void setFocus() {
		fMinimapWidget.getControl().setFocus();
	}

	@Override
	public void dispose() {
		fMinimapWidget.uninstall();
		super.dispose();
	}
}
