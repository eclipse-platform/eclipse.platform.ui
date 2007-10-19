/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for Bug 73612  
 *        	[Markers] "Open All" does not work with multi-select in the bookmarks view
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Action to open an editor on the selected bookmarks.
 */
public class ActionOpenMarker extends MarkerSelectionProviderAction {

	private final String IMAGE_PATH = "elcl16/gotoobj_tsk.gif"; //$NON-NLS-1$

	private final String DISABLED_IMAGE_PATH = "dlcl16/gotoobj_tsk.gif"; //$NON-NLS-1$

	protected IWorkbenchPart part;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param part
	 * @param provider
	 */
	public ActionOpenMarker(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, MarkerMessages.openAction_title);
		this.part = part;
		setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor(IMAGE_PATH));
		setDisabledImageDescriptor(IDEWorkbenchPlugin
				.getIDEImageDescriptor(DISABLED_IMAGE_PATH));
		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IMarker[] markers = getSelectedMarkers();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];

			// optimization: if the active editor has the same input as the
			// selected marker then
			// RevealMarkerAction would have been run and we only need to
			// activate the editor
			IEditorPart editor = part.getSite().getPage().getActiveEditor();
			if (editor != null) {
				IEditorInput input = editor.getEditorInput();
				IFile file = ResourceUtil.getFile(input);
				if (file != null) {
					if (marker.getResource().equals(file)) {
						part.getSite().getPage().activate(editor);
					}
				}
			}

			if (marker.getResource() instanceof IFile) {
				try {
					IFile file = (IFile) marker.getResource();
					if (file.getLocation() == null
							|| file.getLocationURI() == null)
						return; // Abort if it cannot be opened
					IDE.openEditor(part.getSite().getPage(), marker,
							OpenStrategy.activateOnOpen());
				} catch (PartInitException e) {
					// Open an error style dialog for PartInitException by
					// including any extra information from the nested
					// CoreException if present.

					// Check for a nested CoreException
					CoreException nestedException = null;
					IStatus status = e.getStatus();
					if (status != null
							&& status.getException() instanceof CoreException) {
						nestedException = (CoreException) status.getException();
					}

					if (nestedException != null) {
						// Open an error dialog and include the extra
						// status information from the nested CoreException
						reportStatus(nestedException.getStatus());
					} else {
						// Open a regular error dialog since there is no
						// extra information to display
						reportError(e.getLocalizedMessage());
					}
				}
			}
		}
	}

	/**
	 * Report an error message
	 * 
	 * @param message
	 */
	private void reportError(String message) {
		IStatus status = new Status(IStatus.ERROR,
				IDEWorkbenchPlugin.IDE_WORKBENCH, message);
		reportStatus(status);
	}

	/**
	 * Report the status
	 * 
	 * @param status
	 */
	private void reportStatus(IStatus status) {
		StatusAdapter adapter = new StatusAdapter(status);
		adapter.setProperty(StatusAdapter.TITLE_PROPERTY,
				MarkerMessages.OpenMarker_errorTitle);
		StatusManager.getManager().handle(adapter, StatusManager.SHOW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		if (Util.allConcreteSelection(selection)) {
			Iterator nodes = selection.iterator();
			while (nodes.hasNext()) {
				ConcreteMarker marker = ((MarkerNode) nodes.next()).getConcreteRepresentative();
				if (marker.getResource().getType() == IResource.FILE) {
					setEnabled(true);
					return;
				}
			}
		}
		setEnabled(false);
	}
}
