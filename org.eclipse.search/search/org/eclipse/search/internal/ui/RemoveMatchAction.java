/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search.internal.ui;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * @deprecated old search
 */
@Deprecated
class RemoveMatchAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveMatchAction(ISelectionProvider provider) {
		super(SearchMessages.SearchResultView_removeMatch_text);
		setToolTipText(SearchMessages.SearchResultView_removeMatch_tooltip);
		fSelectionProvider= provider;
	}

	@Override
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
