/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

class RemovePotentialMatchesAction extends Action {

	IWorkbenchSite fSite;

	public RemovePotentialMatchesAction(IWorkbenchSite site) {
		super(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatches.text")); //$NON-NLS-1$
		setToolTipText(SearchMessages.getString("RemovePotentialMatchesAction.removePotentialMatches.tooltip")); //$NON-NLS-1$
		fSite= site;
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
		IMarker[] markers;
		try {
			markers= SearchPlugin.getWorkspace().getRoot().findMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.findMarkers.title"), SearchMessages.getString("Search.Error.findMarkers.message")); //$NON-NLS-2$ //$NON-NLS-1$
			return null;
		}
		
		ArrayList potentialMatches= new ArrayList(markers.length);
		for (int i= 0; i < markers.length; i++) {
			if (markers[i].getAttribute(SearchUI.POTENTIAL_MATCH, false))
				potentialMatches.add(markers[i]);
		}
		if (potentialMatches.size() == 0)
			return null;
		
		return (IMarker[])potentialMatches.toArray(new IMarker[potentialMatches.size()]);
	}
}
