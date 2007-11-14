/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


/**
 * Refresh text editor action.
 * 
 * @since 3.3
 */
public class RefreshEditorAction extends Action implements IUpdate {
	
	private ITextEditor fTextEditor;
	private RefreshAction fImpl;

	public RefreshEditorAction(ITextEditor textEditor) {
		Assert.isLegal(textEditor != null);
		fTextEditor= textEditor;
		update();
	}
	
	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		final IResource resource= fTextEditor == null ? null : (IResource)fTextEditor.getEditorInput().getAdapter(IResource.class);
		if (resource == null)
			return;

		if (fImpl == null)
			fImpl= new RefreshAction(fTextEditor.getSite().getShell());

		fImpl.selectionChanged(new StructuredSelection(resource));
		fImpl.run();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		final IResource resource= fTextEditor == null ? null : (IResource)fTextEditor.getEditorInput().getAdapter(IResource.class);
		setEnabled(resource != null);
	}

}
