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

package org.eclipse.ui.views.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.MarkerResolutionSelectionDialog;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 2.0
 */
class ActionResolveMarker extends SelectionProviderAction {
	
	private IWorkbenchPart part;

	/**
	 * @param provider
	 * @param text
	 */
	protected ActionResolveMarker(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, Messages.getString("resolveMarkerAction.title")); //$NON-NLS-1$
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
		IMarker marker = getMarker();
		if (marker == null) {
			return;
		}
		IMarkerResolution[] resolutions = getResolutions(marker);
		if (resolutions.length == 0) {
			MessageDialog.openInformation(
				part.getSite().getShell(),
				Messages.getString("resolveMarkerAction.dialogTitle"),  //$NON-NLS-1$
				Messages.getString("resolveMarkerAction.noResolutionsLabel")); //$NON-NLS-1$
			return;
		}	 
		MarkerResolutionSelectionDialog d = new MarkerResolutionSelectionDialog(part.getSite().getShell(), resolutions);
		if (d.open() != Dialog.OK)
			return;
		Object[] result = d.getResult();
		if (result != null && result.length > 0)
			((IMarkerResolution)result[0]).run(marker);			
	}
	
	/**
	 * Returns the resolutions for the given marker.
	 *
	 * @param the marker for which to obtain resolutions
	 * @return the resolutions for the selected marker	
	 */
	private IMarkerResolution[] getResolutions(IMarker marker) {
		IWorkbench workbench = part.getSite().getWorkbenchWindow().getWorkbench();
		return workbench.getMarkerHelpRegistry().getResolutions(marker);
	}

	/**
	 * Returns the selected marker (may be <code>null</code>).
	 * 
	 * @return the selected marker
	 */
	private IMarker getMarker() {
		IStructuredSelection selection = getStructuredSelection();
		// only enable for single selection
		if (selection.size() != 1)
			return null;
		return (IMarker) selection.getFirstElement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(false);
		if (selection.size() != 1) {
			return;
		}
		IMarker marker = (IMarker) selection.getFirstElement();
		if (marker == null) {
			return;
		}
		IWorkbench workbench = part.getSite().getWorkbenchWindow().getWorkbench();
		setEnabled(workbench.getMarkerHelpRegistry().hasResolutions(marker));
	}
}
