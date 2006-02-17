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

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;

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
		fWindow= null;
		if (targetEditor != null) {
			fWindow= targetEditor.getSite().getWorkbenchWindow();
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
		if (page == null) {
			return false;
		}
		String searchFor= getSearchForString(page);
		query.setSearchString(searchFor);
		return true;
	}

	protected FileNamePatternSearchScope getOldSearchScope(boolean includeDerived) {
		return null;
	}
	

}
