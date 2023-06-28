/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.actions.RefreshAction;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Refresh text editor action.
 *
 * @since 3.3
 */
public class RefreshEditorAction extends RefreshAction implements IUpdate {

	private ITextEditor fTextEditor;

	public RefreshEditorAction(ITextEditor textEditor) {
		super(textEditor.getSite());
		fTextEditor= textEditor;
		update();
	}

	@Override
	public void update() {
		final IResource resource= fTextEditor == null ? null : fTextEditor.getEditorInput().getAdapter(IResource.class);
		if (resource != null)
			selectionChanged(new StructuredSelection(resource));
		else
			selectionChanged(StructuredSelection.EMPTY);
	}
}
