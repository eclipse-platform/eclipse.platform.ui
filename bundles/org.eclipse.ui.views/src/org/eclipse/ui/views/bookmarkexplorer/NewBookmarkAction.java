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

public class NewBookmarkAction implements IViewActionDelegate {
	
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
			createBookmark(marker);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		marker = null;
		
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object o = structuredSelection.getFirstElement();
		if (!(o instanceof IMarker) || structuredSelection.size() > 1)
			return;		
		
		IMarker selectedMarker = (IMarker) o;
		IResource resource = selectedMarker.getResource();
		if (!(resource instanceof IFile))
			return;
			
		marker = selectedMarker;
	}

	/**
	 * Creates a marker of the given type on the given file resource.
	 *
	 * @param file the file resource
	 * @param markerType the marker type
	 */
	private void createBookmark(final IMarker marker) {
		final IFile file = (IFile) marker.getResource();
		
		try {
			file.getWorkspace().run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						IMarker newMarker = file.createMarker(IMarker.BOOKMARK);
						newMarker.setAttribute(IMarker.CHAR_START, marker.getAttribute(IMarker.CHAR_START, 0));
						newMarker.setAttribute(IMarker.CHAR_END, marker.getAttribute(IMarker.CHAR_END, 0));
						newMarker.setAttribute(IMarker.LINE_NUMBER, marker.getAttribute(IMarker.LINE_NUMBER, -1));
						newMarker.setAttribute(IMarker.MESSAGE, marker.getAttribute(IMarker.MESSAGE, "")); //$NON-NLS-1$
						BookmarkPropertiesDialog dialog = new BookmarkPropertiesDialog(view.getSite().getShell(), BookmarkMessages.getString("NewBookmarkDialogTitle.text")); //$NON-NLS-1$
						dialog.setMarker(newMarker);	
						int returnCode = dialog.open();
						if (returnCode == BookmarkPropertiesDialog.CANCEL)
							newMarker.delete();
					}
				},
				null);
		} catch (CoreException e) {
			WorkbenchPlugin.log(null, e.getStatus()); // We don't care
		}
	}	
	
}
