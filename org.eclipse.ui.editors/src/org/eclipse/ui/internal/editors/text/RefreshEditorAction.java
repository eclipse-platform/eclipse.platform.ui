/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		final IResource resource= fTextEditor == null ? null : (IResource)fTextEditor.getEditorInput().getAdapter(IResource.class);
		if (resource != null)
			selectionChanged(new StructuredSelection(resource));
		else
			selectionChanged(StructuredSelection.EMPTY);
	}
}
