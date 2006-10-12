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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Pastes one or more bookmark(s) from the clipboard into the bookmark
 * navigator.
 */
public class ActionPasteMarker extends MarkerSelectionProviderAction {

	private IWorkbenchPart part;

	private Clipboard clipboard;

	private String[] pastableTypes;

	private String markerName;

	/**
	 * Creates the action.
	 * 
	 * @param part
	 * @param provider
	 * @param markerName
	 *            the name used to describe the specific kind of marker being
	 *            pasted.
	 */
	public ActionPasteMarker(IWorkbenchPart part, ISelectionProvider provider,
			String markerName) {
		super(provider, MarkerMessages.pasteAction_title);
		this.part = part;
		this.pastableTypes = new String[0];
		this.markerName = markerName;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setEnabled(false);
	}

	void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}

	/**
	 * Copies the marker(s) from the clipboard to the bookmark navigator view.
	 */
	public void run() {
		// Get the markers from the clipboard
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
		paste(markerData);
	}

	void paste(final IMarker[] markers) {
		if (markers == null) {
			return;
		}

		final ArrayList newMarkerTypes = new ArrayList();
		final ArrayList newMarkerAttributes = new ArrayList();
		final ArrayList newMarkerResources = new ArrayList();

		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					for (int i = 0; i < markers.length; i++) {
						// Collect info about the markers to be pasted.
						newMarkerTypes.add(markers[i].getType());
						newMarkerResources.add(markers[i].getResource());
						newMarkerAttributes.add(markers[i].getAttributes());

					}
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(part.getSite().getShell(),
					MarkerMessages.PasteMarker_errorTitle, null, e.getStatus());
			return;
		}

		final String[] types = (String[]) newMarkerTypes
				.toArray(new String[newMarkerTypes.size()]);
		final Map[] attrs = (Map[]) newMarkerAttributes
				.toArray(new Map[newMarkerAttributes.size()]);
		final IResource[] resources = (IResource[]) newMarkerResources
				.toArray(new IResource[newMarkerResources.size()]);
		String operationTitle = NLS.bind(MarkerMessages.qualifiedMarkerCommand_title,
				MarkerMessages.pasteAction_title, markerName);
		final CreateMarkersOperation op = new CreateMarkersOperation(types,
				attrs, resources, operationTitle);
		execute(op, MarkerMessages.PasteMarker_errorTitle, null,
				WorkspaceUndoUtil.getUIInfoAdapter(part.getSite().getShell()));

		// Need to do this in an asyncExec, even though we're in the UI thread
		// here,
		// since the marker view updates itself with the addition in an
		// asyncExec,
		// which hasn't been processed yet.
		// Must be done outside the create marker operation above since
		// notification for add is
		// sent after the operation is executed.
		if (getSelectionProvider() != null && op.getMarkers() != null) {
			part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getSelectionProvider().setSelection(
							new StructuredSelection(op.getMarkers()));
				}
			});
		}
	}

	void updateEnablement() {
		setEnabled(false);
		if (clipboard == null) {
			return;
		}

		// Paste if clipboard contains pastable markers
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
		if (markerData == null || markerData.length < 1
				|| pastableTypes == null) {
			return;
		}
		for (int i = 0; i < markerData.length; i++) {
			try {
				IMarker marker = markerData[i];
				if (!marker.exists()) {
					break;
				}
				boolean pastable = false;
				for (int j = 0; j < pastableTypes.length; j++) {
					if (marker.isSubtypeOf(pastableTypes[j])) {
						pastable = true;
						break;
					}
				}
				if (!pastable) {
					return;
				}
				if (!Util.isEditable(marker)) {
					return;
				}
			} catch (CoreException e) {
				return;
			}
		}
		setEnabled(true);
	}

	/**
	 * @param strings
	 */
	void setPastableTypes(String[] strings) {
		pastableTypes = strings;
	}
}
