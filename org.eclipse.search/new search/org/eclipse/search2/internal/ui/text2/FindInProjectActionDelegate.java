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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class FindInProjectActionDelegate extends FindInRecentScopeActionDelegate {

	public FindInProjectActionDelegate() {
		super(SearchMessages.FindInProjectActionDelegate_text);
	}

	protected boolean modifyQuery(RetrieverQuery query) {
		if (super.modifyQuery(query)) {
			IWorkbenchPage page= getWorkbenchPage();
			IEditorPart editor= page.getActiveEditor();
			if (editor != null) {
				IEditorInput ei= editor.getEditorInput();
				if (ei instanceof IFileEditorInput) {
					IFileEditorInput fi= (IFileEditorInput) ei;
					IProject proj= fi.getFile().getProject();
					query.setSearchScope(new SelectedResourcesScopeDescription(new IResource[] {proj}, false));
					return true;
				}
			}
			return true;
		}
		return false;
	}
}
