/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.MarkerResolutionSelectionDialog;
import org.eclipse.ui.ide.IDE;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 2.0
 */
public class ActionResolveMarker extends SelectionProviderAction {

	private IWorkbenchPart part;

	/**
	 * Create a new instance of the receiver.
	 * @param part
	 * @param provider
	 */
	public ActionResolveMarker(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, MarkerMessages.resolveMarkerAction_title);
		this.part = part;
		setEnabled(false);
	}

	/**
	 * Displays a list of resolutions and performs the selection.
	 */
	public void run() {
		if (!isEnabled()) {
			return;
		}
		Iterator markers = getStructuredSelection().toList().iterator();

		if (!markers.hasNext()) {
			return;
		}
		while (markers.hasNext()) {
			IMarker marker = (IMarker) markers.next();
			IMarkerResolution[] resolutions = getResolutions(marker);
			if (resolutions.length == 0) {
				MessageDialog.openInformation(part.getSite().getShell(),
						MarkerMessages.resolveMarkerAction_dialogTitle,
						MarkerMessages.resolveMarkerAction_noResolutionsLabel);
				return;
			}
			IMarkerResolution resolution = resolutions[0];
			if (resolutions.length > 1) {
				MarkerResolutionSelectionDialog d = new MarkerResolutionSelectionDialog(
						part.getSite().getShell(), resolutions);
				d.open();
				
				Object[] result = d.getResult();
				if (result == null || result.length == 0)
					return;
				resolution = (IMarkerResolution) result[0];
			}
			resolution.run(marker);
		}

	}

	/**
	 * Returns the resolutions for the given marker.
	 * 
	 * @param marker the marker for which to obtain resolutions
	 * @return IMarkerResolution[] the resolutions for the selected marker
	 */
	private IMarkerResolution[] getResolutions(IMarker marker) {
		return IDE.getMarkerHelpRegistry().getResolutions(marker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(false);
		if (selection.size() == 0) {
			return;
		}
		Iterator markers = selection.iterator();

		while (markers.hasNext()) {
			if (IDE.getMarkerHelpRegistry().hasResolutions(
					(IMarker) markers.next())) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}
}
