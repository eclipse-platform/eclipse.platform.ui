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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * ActionRevealMarker is the action for opening the editor on
 * a marker.
 *
 */
public class ActionRevealMarker extends MarkerSelectionProviderAction {

	protected IWorkbenchPart part;

	/**
	 * Create a new instance of the receiver.
	 * @param part
	 * @param provider
	 */
	public ActionRevealMarker(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, Util.EMPTY_STRING); 
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		
		IEditorPart editor = part.getSite().getPage().getActiveEditor();
		if (editor == null) {
			return;
		}
		IFile file = ResourceUtil.getFile(editor.getEditorInput());
		if (file != null) {
			IMarker marker = getSelectedMarker();
			if (marker.getResource().equals(file)) {
				try {
					IDE.openEditor(part.getSite().getPage(),
							marker, false);
				} catch (CoreException e) {
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(Util.isSingleConcreteSelection(selection));
	}
}
