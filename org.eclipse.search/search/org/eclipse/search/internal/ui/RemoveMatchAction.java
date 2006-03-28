/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * @deprecated old search
 */
class RemoveMatchAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveMatchAction(ISelectionProvider provider) {
		super(SearchMessages.SearchResultView_removeMatch_text); 
		setToolTipText(SearchMessages.SearchResultView_removeMatch_tooltip); 
		fSelectionProvider= provider;
	}
	
	public void run() {
		IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
			}
	}
	
	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;
		
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size != 1)
			return null;
		if (selection.getFirstElement() instanceof ISearchResultViewEntry) {
			IMarker marker= ((ISearchResultViewEntry)selection.getFirstElement()).getSelectedMarker();
			if (marker != null)
				return new IMarker[] {marker};
		}
		return null;
	}
}
