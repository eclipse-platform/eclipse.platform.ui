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

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

/**
 * Action to remove the selected bookmarks.
 */
public class ActionRemoveMarker extends MarkerSelectionProviderAction {

	private IWorkbenchPart part;
	
	private String markerName;

	/**
	 * Creates the action.
	 * 
	 * @param part
	 * @param provider
	 * @param markerName
	 *            the name describing the specific kind of marker being removed
	 */
	public ActionRemoveMarker(IWorkbenchPart part, ISelectionProvider provider,
			String markerName) {
		super(provider, MarkerMessages.deleteAction_title);
		this.part = part;
		this.markerName = markerName;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setToolTipText(MarkerMessages.deleteAction_tooltip);
		setEnabled(false);
	}

	/**
	 * Delete the marker selection.
	 */
	public void run() {
		String operationTitle = NLS.bind(MarkerMessages.qualifiedMarkerCommand_title,
				MarkerMessages.deleteAction_title, markerName);
		DeleteMarkersOperation op = new DeleteMarkersOperation(
				getSelectedMarkers(), operationTitle);
		execute(op, MarkerMessages.RemoveMarker_errorTitle, null,
				WorkspaceUndoUtil.getUIInfoAdapter(part.getSite().getShell()));
	}

	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(false);
		if (selection == null || selection.isEmpty()) {
			return;
		}
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Object obj = iterator.next();
			if (!(obj instanceof ConcreteMarker)) {
				return;
			}

			if (!Util.isEditable(((ConcreteMarker) obj).getMarker())) {
				return;
			}
		}
		setEnabled(true);
	}
}
