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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.part.Page;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Minimap page which displays scaled content of the given text editor.
 *
 */
public class MinimapPage extends Page {

	/**
	 * Try to create a MinimapPage for a text editor. Only succeeds if the
	 * {@link ITextViewer} for the editor can be determined.
	 *
	 * @param textEditor
	 *            the {@link ITextEditor} for which the page should be created.
	 * @return the created MinimapPage or <code>null</code>
	 */
	public static MinimapPage createMinimapPage(ITextEditor textEditor) {
		ITextViewer textViewer = textEditor.getAdapter(ITextViewer.class);
		if (textViewer == null) {
			// try fallback that sometimes works (TextViewer implements
			// ITextOperationTarget)
			ITextOperationTarget textOperationTarget = textEditor.getAdapter(ITextOperationTarget.class);
			if (textOperationTarget instanceof ITextViewer) {
				textViewer = (ITextViewer) textOperationTarget;
			}
		}
		if (textViewer == null) {
			return null;
		}
		return new MinimapPage(textViewer);
	}

	private final ITextViewer fEditorViewer;
	private MinimapWidget fMinimapWidget;

	private MinimapPage(ITextViewer textViewer) {
		fEditorViewer = textViewer;
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
