/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

class RemovePotentialMatchesAction extends Action {

	private IWorkbenchSite fSite;

	public RemovePotentialMatchesAction(IWorkbenchSite site) {
		fSite= site;

		if (usePluralLabel()) {
			setText(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatches.text")); //$NON-NLS-1$
			setToolTipText(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatches.tooltip")); //$NON-NLS-1$
		}
		else {
			setText(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatch.text")); //$NON-NLS-1$
			setToolTipText(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatch.tooltip")); //$NON-NLS-1$
		}
	}
	
	public void run() {
		IMarker[] markers= getMarkers();
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.deleteMarkers.title"), SearchMessages.getString("Search.Error.deleteMarkers.message")); //$NON-NLS-2$ //$NON-NLS-1$
			}
		else {
			String title= SearchMessages.getString("RemovePotentialMatchesAction.dialog.title"); //$NON-NLS-1$
			String message= SearchMessages.getString("RemovePotentialMatchesAction.dialog.message"); //$NON-NLS-1$
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

		ArrayList markers= new ArrayList(size * 3);
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= (IMarker)entryIter.next();
				if (marker.getAttribute(SearchUI.POTENTIAL_MATCH, false))
					markers.add(marker);
			}
		}
		return (IMarker[])markers.toArray(new IMarker[markers.size()]);
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
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= (IMarker)entryIter.next();
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
