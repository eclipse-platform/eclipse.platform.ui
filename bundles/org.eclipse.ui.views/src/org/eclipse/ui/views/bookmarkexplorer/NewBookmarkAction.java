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

import java.util.HashMap;
import java.util.Map;

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

/**
 * Creates a new bookmark from an existing marker. Opens a properties dialog 
 * to allow the user to customize the bookmark's message.
 */
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
	 * Sets marker to the current selection if the selection is an instance of 
	 * <code>org.eclipse.core.resources.IMarker<code> and the selected marker's 
	 * resource is an instance of <code>org.eclipse.core.resources.IFile<code>.
	 * Otherwise sets marker to null.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		marker = null;
		
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() != 1)
			return;
			
		Object o = structuredSelection.getFirstElement();
		if (!(o instanceof IMarker))
			return;		
		
		IMarker selectedMarker = (IMarker) o;
		IResource resource = selectedMarker.getResource();
		if (!(resource instanceof IFile))
			return;
			
		marker = selectedMarker;
	}

	/**
	 * Creates a new bookmark from the given marker.
	 *
	 * @param marker the marker
	 */
	private void createBookmark(final IMarker marker) {
		final IFile file = (IFile) marker.getResource();
		
		try {
			file.getWorkspace().run(
				new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						Map initialAttrs = new HashMap();
						initialAttrs.put(IMarker.CHAR_START, new Integer(MarkerUtil.getCharStart(marker)));
						initialAttrs.put(IMarker.CHAR_END, new Integer(MarkerUtil.getCharEnd(marker)));
						initialAttrs.put(IMarker.LINE_NUMBER, new Integer(MarkerUtil.getLineNumber(marker)));
						initialAttrs.put(IMarker.MESSAGE, MarkerUtil.getMessage(marker));
						BookmarkPropertiesDialog dialog = new BookmarkPropertiesDialog(view.getSite().getShell(), BookmarkMessages.getString("NewBookmarkDialogTitle.text")); //$NON-NLS-1$
						dialog.setResource(marker.getResource());
						dialog.setInitialAttributes(initialAttrs);	
						dialog.open();
					}
				},
				null);
		} catch (CoreException e) {
			WorkbenchPlugin.log(null, e.getStatus()); // We don't care
		}
	}	
	
}
