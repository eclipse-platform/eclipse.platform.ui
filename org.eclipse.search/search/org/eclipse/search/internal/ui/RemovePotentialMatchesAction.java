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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;

/**
 * @deprecated old search
 */
@Deprecated
class RemovePotentialMatchesAction extends Action {

	private IWorkbenchSite fSite;

	public RemovePotentialMatchesAction(IWorkbenchSite site) {
		fSite= site;

		if (usePluralLabel()) {
			setText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatches_text);
			setToolTipText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatches_tooltip);
		}
		else {
			setText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatch_text);
			setToolTipText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatch_tooltip);
		}
	}

	@Override
	public void run() {
		IMarker[] markers= getMarkers();
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message);
			}
		else {
			String title= SearchMessages.RemovePotentialMatchesAction_dialog_title;
			String message= SearchMessages.RemovePotentialMatchesAction_dialog_message;
			MessageDialog.openInformation(fSite.getShell(), title, message);
		}

		// action only makes sense once
		setEnabled(false);
	}

	private IMarker[] getMarkers() {

		ISelection s= fSite.getSelectionProvider().getSelection();
		if (! (s instanceof IStructuredSelection))
			return null;
		IStructuredSelection selection= (IStructuredSelection)s;

		int size= selection.size();
		if (size <= 0)
			return null;

		ArrayList<IMarker> markers= new ArrayList<>(size * 3);
		Iterator<?> iter= selection.iterator();
		while (iter.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator<IMarker> entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= entryIter.next();
				if (marker.getAttribute(SearchUI.POTENTIAL_MATCH, false))
					markers.add(marker);
			}
		}
		return markers.toArray(new IMarker[markers.size()]);
	}

	private boolean usePluralLabel() {
		ISelection s= fSite.getSelectionProvider().getSelection();

		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return false;

		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size <= 0)
			return false;

		int markerCount= 0;
		Iterator<?> iter= selection.iterator();
		while (iter.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator<IMarker> entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= entryIter.next();
				if (marker.getAttribute(SearchUI.POTENTIAL_MATCH, false)) {
					markerCount++;
				}
				if (markerCount > 1)
					return true;
			}
		}
		return false;
	}
}
