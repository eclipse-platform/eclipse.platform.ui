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

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search2.internal.ui.SearchMessages;


/**
 * @author markus.schorn@windriver.com
 */
public class FindInRecentScopeActionDelegate extends RetrieverAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {
	private IWorkbenchWindow fWindow;

	public FindInRecentScopeActionDelegate() {
		this(SearchMessages.FindInRecentScopeActionDelegate_text);
	}

	public FindInRecentScopeActionDelegate(String text) {
		setText(text);
	}

	// IWorkbenchWindowActionDelegate
	public void dispose() {
		fWindow= null;
	}

	// IWorkbenchWindowActionDelegate
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	// IEditorActionDelegate
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ITextEditor) {
			fWindow= targetEditor.getSite().getWorkbenchWindow();
		} else {
			fWindow= null;
		}
	}

	// IActionDelegate
	public void selectionChanged(IAction action, ISelection selection) {
	}

	// IActionDelegate
	final public void run(IAction action) {
		run();
	}

	// RetrieverAction
	protected IWorkbenchPage getWorkbenchPage() {
		if (fWindow != null) {
			return fWindow.getActivePage();
		}
		return null;
	}

	protected boolean modifyQuery(RetrieverQuery query) {
		IWorkbenchPage page= getWorkbenchPage();
		String searchFor= null;
		if (page != null) {
			searchFor= extractSearchTextFromSelection(page.getSelection());
		}
		if (searchFor == null || searchFor.length() == 0) {
			searchFor= extractSearchTextFromEditor(page.getActiveEditor());
		}
		query.setSearchString(searchFor);
		return true;
	}
}
