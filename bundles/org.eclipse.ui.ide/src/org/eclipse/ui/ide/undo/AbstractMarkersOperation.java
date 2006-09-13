/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractMarkersOperation represents an undoable operation that affects
 * markers on a resource. It provides implementations for marker creation,
 * deletion, and updating.
 * 
 * This class is not intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
abstract class AbstractMarkersOperation extends AbstractWorkspaceOperation {

	MarkerDescription[] markerDescriptions;

	IMarker[] markers;

	Map[] attributes;

	/**
	 * Create an AbstractMarkersOperation by specifying a combination of markers
	 * and attributes or marker descriptions.
	 * 
	 * @param markers
	 *            the markers used in the operation or <code>null</code> if no
	 *            markers yet exist
	 * @param markerDescriptions
	 *            the marker descriptions that should be used to create markers,
	 *            or <code>null</code> if the markers already exist
	 * @param attributes
	 *            The map of attributes that should be assigned to any existing
	 *            markers when the markers are updated. Ignored if the markers
	 *            parameter is <code>null</code>.
	 * @param name
	 *            the name used to describe the operation
	 */
	AbstractMarkersOperation(IMarker[] markers,
			MarkerDescription[] markerDescriptions, Map attributes, String name) {
		super(name);
		this.markers = markers;
		this.attributes = null;
		// If there is more than one marker, create an array with a copy
		// of the attributes map for each marker. Keeping a unique map
		// per marker allows us to support the scenario where attributes
		// are merged when updated. In this case, each marker's attributes
		// may differ since their original attributes may have differed.
		if (attributes != null && markers != null) {
			if (markers.length > 1) {
				this.attributes = new Map[markers.length];
				for (int i = 0; i < markers.length; i++) {
					Map copiedAttributes = new HashMap();
					copiedAttributes.putAll(attributes);
					this.attributes[i] = copiedAttributes;
				}
			} else {
				this.attributes = new Map[] { attributes };
			}
		}
		setMarkerDescriptions(markerDescriptions);
	}

	/*
	 * Delete any currently known markers and save their information in marker
	 * descriptions so that they can be restored.
	 */
	protected void deleteMarkers(int work, IProgressMonitor monitor)
			throws CoreException {
		if (markers == null || markers.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markers.length;
		markerDescriptions = new MarkerDescription[markers.length];
		for (int i = 0; i < markers.length; i++) {
			markerDescriptions[i] = new MarkerDescription(markers[i]);
			markers[i].delete();
			monitor.worked(markerWork);
		}
		markers = new IMarker[0];
	}

	/*
	 * Create markers from the currently known marker descriptions.
	 */
	protected void createMarkers(int work, IProgressMonitor monitor)
			throws CoreException {
		if (markerDescriptions == null || markerDescriptions.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markerDescriptions.length;
		markers = new IMarker[markerDescriptions.length];

		// Recreate the markers from the descriptions
		for (int i = 0; i < markerDescriptions.length; i++) {
			markers[i] = markerDescriptions[i].createMarker();
			monitor.worked(markerWork);
		}
	}

	/*
	 * Update the currently known markers with the corresponding array of marker
	 * descriptions. The boolean specifies whether the attributes are merged or
	 * considered to be a replacement of the previous attributes.
	 */
	protected void updateMarkers(int work, IProgressMonitor monitor,
			boolean mergeAttributes) throws CoreException {
		if (attributes == null || markers == null
				|| attributes.length != markers.length || markers.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markers.length;
		for (int i = 0; i < markers.length; i++) {
			if (mergeAttributes) {
				Map oldAttributes = markers[i].getAttributes();
				int increment = markerWork / attributes[i].size();
				Map replacedAttributes = new HashMap();

				for (Iterator iter = attributes[i].keySet().iterator(); iter
						.hasNext();) {
					String key = (String) iter.next();
					Object val = attributes[i].get(key);
					markers[i].setAttribute(key, val);
					replacedAttributes.put(key, oldAttributes.get(key));
					monitor.worked(increment);
				}
				attributes[i] = replacedAttributes;
			} else {
				// replace all of the attributes
				Map oldAttributes = markers[i].getAttributes();
				markers[i].setAttributes(attributes[i]);
				attributes[i] = oldAttributes;
			}
		}
	}

	/*
	 * Set the marker descriptions that describe markers that can be created.
	 */
	protected void setMarkerDescriptions(MarkerDescription[] descriptions) {
		markerDescriptions = descriptions;
		addUndoContexts();
		updateTargetResources();
	}

	/*
	 * Update the target resources by traversing the currently known markers or
	 * marker descriptions and getting their resources.
	 */

	private void updateTargetResources() {
		IResource[] resources = null;
		if (markers == null) {
			if (markerDescriptions != null) {
				resources = new IResource[markerDescriptions.length];
				for (int i = 0; i < markerDescriptions.length; i++) {
					resources[i] = markerDescriptions[i].resource;
				}
			}
		} else {
			resources = new IResource[markers.length];
			for (int i = 0; i < markers.length; i++) {
				resources[i] = markers[i].getResource();
			}
		}
		setTargetResources(resources);
	}

	/*
	 * Add undo contexts according to marker types. Any unknown marker types
	 * will cause the workspace undo context to be added.
	 * 
	 * This is an optimization that allows us to add specific undo contexts for
	 * tasks and bookmarks, without also adding the workspace undo context. Note
	 * that clients with different marker types may still assign their own
	 * specific undo context using AbstractOperation.addContext(IUndoContext) in
	 * addition to the workspace context assigned by this method.
	 */

	private void addUndoContexts() {
		String[] types = null;
		if (markers == null) {
			if (markerDescriptions != null) {
				types = new String[markerDescriptions.length];
				for (int i = 0; i < markerDescriptions.length; i++) {
					types[i] = markerDescriptions[i].type;
				}
			}
		} else {
			types = new String[markers.length];
			for (int i = 0; i < markers.length; i++) {
				try {
					types[i] = markers[i].getType();
				} catch (CoreException e) {
				}

			}
		}
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				if (types[i].equals(IMarker.BOOKMARK)) {
					addContext(WorkspaceUndoSupport.getBookmarksUndoContext());
				} else if (types[i].equals(IMarker.TASK)) {
					addContext(WorkspaceUndoSupport.getTasksUndoContext());
				} else if (types[i] != null) {
					// type is not known, use the workspace undo context
					addContext(WorkspaceUndoSupport.getWorkspaceUndoContext());
				}
			}
		}
	}

	/**
	 * Return the array of markers that has been updated or created.
	 * 
	 * @return the array of markers that have been updated or created, or
	 *         <code>null</code> if no markers have been created or updated.
	 * 
	 * @since 3.3
	 */
	public IMarker[] getMarkers() {
		return markers;
	}

	/**
	 * Return whether the markers known by this operation currently exist.
	 * 
	 * @return <code>true</code> if there are existing markers and
	 *         <code>false</code> if there are no known markers or any one of
	 *         them does not exist
	 */
	protected boolean markersExist() {
		if (markers == null || markers.length == 0) {
			return false;
		}
		for (int i = 0; i < markers.length; i++) {
			if (!markers[i].exists()) {
				return false;
			}
		}
		return true;

	}
	
	/**
	 * Return a status indicating the projected outcome of undoing the marker
	 * operation. The receiver is not responsible for remembering the result of
	 * this computation.
	 * 
	 * @return the status indicating whether the operation can be undone
	 */
	protected abstract IStatus getBasicUndoStatus();

	/**
	 * Return a status indicating the projected outcome of redoing the marker
	 * operation. The receiver is not responsible for remembering the result of
	 * this computation.
	 * 
	 * @return the status indicating whether the operation can be undone
	 */
	protected abstract IStatus getBasicRedoStatus();

	/*
	 * Return a status indicating the projected outcome of executing the
	 * receiver. 
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeExecutionStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = getBasicRedoStatus();
		if (status.isOK()) {
			return super.computeExecutionStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/*
	 * Return a status indicating the projected outcome of undoing the receiver.
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = getBasicUndoStatus();
		if (status.isOK()) {
			return super.computeUndoableStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/*
	 * Return a status indicating the projected outcome of redoing the receiver.
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeRedoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = getBasicRedoStatus();
		if (status.isOK()) {
			return super.computeRedoableStatus(monitor);
		}
		if (status.getSeverity() == IStatus.ERROR) {
			markInvalid();
		}
		return status;
	}

	/*
	 * Return a status that indicates whether markers can be deleted.
	 */
	protected IStatus getMarkerDeletionStatus() {
		if (markersExist()) {
			return Status.OK_STATUS;
		}
		return getErrorStatus(UndoMessages.MarkerOperation_MarkerDoesNotExist);
	}

	/*
	 * Return a status that indicates whether markers can be created.
	 */
	protected IStatus getMarkerCreationStatus() {
		if (!resourcesExist()) {
			return getErrorStatus(UndoMessages.MarkerOperation_ResourceDoesNotExist);
		} else if (markerDescriptions == null) {
			return getErrorStatus(UndoMessages.MarkerOperation_NotEnoughInfo);
		}
		return Status.OK_STATUS;
	}

	/*
	 * Return a status that indicates whether markers can be updated.
	 */
	protected IStatus getMarkerUpdateStatus() {
		if (!markersExist()) {
			return getErrorStatus(UndoMessages.MarkerOperation_MarkerDoesNotExist);
		} else if (attributes == null) {
			return getErrorStatus(UndoMessages.MarkerOperation_NotEnoughInfo);
		}
		return Status.OK_STATUS;
	}
}
