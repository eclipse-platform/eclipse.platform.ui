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

import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;

import org.eclipse.search2.internal.ui.SearchMessages;

public class FindInWorkingSetActionDelegate extends FindInRecentScopeActionDelegate {

	public FindInWorkingSetActionDelegate() {
		super(SearchMessages.FindInWorkingSetActionDelegate_text);
	}

	protected boolean modifyQuery(RetrieverQuery query) {
		if (super.modifyQuery(query)) {
			IWorkbenchPage page= getWorkbenchPage();
			if (page != null) {
				IScopeDescription scope= WorkingSetScopeDescription.createWithDialog(page, query.getScopeDescription());
				if (scope != null) {
					query.setSearchScope(scope);
					return true;
				}
			}
		}
		return false;
	}
	
	protected FileNamePatternSearchScope getOldSearchScope(boolean includeDerived) {
		IWorkbenchPage page= getWorkbenchPage();
		if (page != null) {
			IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
			IWorkingSetSelectionDialog dialog= manager.createWorkingSetSelectionDialog(page.getWorkbenchWindow().getShell(), true);
			if (dialog.open() == Window.OK) {
				IWorkingSet[] workingSets= dialog.getSelection();
				if (workingSets != null) {
					return FileNamePatternSearchScope.newSearchScope(workingSets, includeDerived);
				}
			} else {
				throw new OperationCanceledException();
			}
		}
		return FileNamePatternSearchScope.newWorkspaceScope(includeDerived);
	}
}
