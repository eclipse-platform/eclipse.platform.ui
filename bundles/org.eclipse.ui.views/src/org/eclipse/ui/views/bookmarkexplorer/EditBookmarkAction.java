/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class EditBookmarkAction implements IViewActionDelegate {
	
	private IViewPart view;
	private IMarker marker;

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		this.view = view;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (marker != null)
			editBookmark();
	}
	
	public void setMarker(IMarker marker) {
		this.marker = marker;
	}
	

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		marker = null;
		if (!(selection instanceof IStructuredSelection)) 
			return;
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object o = structuredSelection.getFirstElement(); 
		if (!(o instanceof IMarker) || structuredSelection.size() > 1)
			return;
			
		IMarker selectedMarker = (IMarker) o;
		IResource resource = selectedMarker.getResource();
		if (resource instanceof IFile)
			marker = selectedMarker;
	}

	private void editBookmark() {
		IFile file = (IFile) marker.getResource();
		
		try {
			file.getWorkspace().run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						BookmarkPropertiesDialog dialog = new BookmarkPropertiesDialog(view.getSite().getShell());
						dialog.setMarker(marker);	
						dialog.open();
					}
				},
				null);
		} catch (CoreException e) {
			WorkbenchPlugin.log(null, e.getStatus()); // We don't care
		}
	}

}
