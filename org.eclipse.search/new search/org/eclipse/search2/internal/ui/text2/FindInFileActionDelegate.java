/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search2.internal.ui.SearchMessages;


public class FindInFileActionDelegate extends FindInRecentScopeActionDelegate {
	private IEditorPart fEditor= null;
	
	public FindInFileActionDelegate() {
		super(SearchMessages.FindInFileActionDelegate_text);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fEditor= null;
		IWorkbenchPage page= getWorkbenchPage();
		if (page != null) {
			IEditorPart editor= page.getActiveEditor();
			if (editor != null && editor == page.getActivePart()) {
				if (editor.getEditorInput() instanceof IFileEditorInput) {
					fEditor= editor;
				}
			}
		}
		action.setEnabled(fEditor != null);
	}

	public void setActiveEditor(IAction action, IEditorPart editor) {
		if (editor != null && editor.getEditorInput() instanceof IFileEditorInput) {
			fEditor= editor;
		}
		super.setActiveEditor(action, fEditor);
	}

	protected boolean modifyQuery(RetrieverQuery query) {
		if (super.modifyQuery(query)) {
			if (fEditor != null) {
				IEditorInput ei= fEditor.getEditorInput();
				if (ei instanceof IFileEditorInput) {
					query.setSearchScope(new SingleFileScopeDescription(((IFileEditorInput) ei).getFile()));
				}
				return true;
			}
		}
		return false;
	}
}
