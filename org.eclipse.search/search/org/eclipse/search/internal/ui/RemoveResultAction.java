/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.custom.BusyIndicator;

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
class RemoveResultAction extends Action {

	private ISelectionProvider fSelectionProvider;

	public RemoveResultAction(ISelectionProvider provider, boolean stringsDependOnMatchCount) {
		fSelectionProvider= provider;
		if (!stringsDependOnMatchCount || usePluralLabel()) {
			setText(SearchMessages.SearchResultView_removeEntries_text);
			setToolTipText(SearchMessages.SearchResultView_removeEntries_tooltip);
		}
		else {
			setText(SearchMessages.SearchResultView_removeEntry_text);
			setToolTipText(SearchMessages.SearchResultView_removeEntry_tooltip);
		}
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}

	@Override
	public void run() {
		final IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null) {
			BusyIndicator.showWhile(SearchPlugin.getActiveWorkbenchShell().getDisplay(), () -> {
				try {
					SearchPlugin.getWorkspace().deleteMarkers(markers);
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
				}
			});
		}
	}

	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;

		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size <= 0)
			return null;
		ArrayList<IMarker> markers= new ArrayList<>(size * 3);
		int markerCount= 0;
		Iterator<?> iter= selection.iterator();
		while (iter.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			markerCount += entry.getMatchCount();
			markers.addAll(entry.getMarkers());
		}
		return markers.toArray(new IMarker[markerCount]);
	}

	private boolean usePluralLabel() {
		ISelection s= fSelectionProvider.getSelection();
		if (s == null || s.isEmpty() || !(s instanceof IStructuredSelection))
			return false;
		IStructuredSelection selection= (IStructuredSelection)s;

		if (selection.size() != 1)
			return true;

		Object firstElement= selection.getFirstElement();
		if (firstElement instanceof ISearchResultViewEntry)
			return ((ISearchResultViewEntry)firstElement).getMatchCount() > 1;
		return false;
	}
}
