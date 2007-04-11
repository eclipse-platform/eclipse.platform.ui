/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.ide.undo.MarkerDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A CreateMarkersOperation represents an undoable operation for creating one or
 * more markers on one or more resources in the workspace. Clients may call the
 * public API from a background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * @since 3.3
 * 
 */
public class CreateMarkersOperation extends AbstractMarkersOperation {

	/**
	 * Create an undoable operation that can create a marker of a specific type
	 * on a resource.
	 * 
	 * @param type
	 *            the type of marker to be created
	 * @param attributes
	 *            the map of attributes that should be assigned to the marker
	 * @param resource
	 *            the resource on which the marker should be created
	 * @param name
	 *            the name used to describe the operation that creates the
	 *            marker
	 * 
	 * @see org.eclipse.core.resources.IMarker
	 */
	public CreateMarkersOperation(String type, Map attributes,
			IResource resource, String name) {
		super(null, new MarkerDescription[] { new MarkerDescription(type,
				attributes, resource) }, null, name);
	}

	/**
	 * Create an undoable operation that can create multiple markers of various
	 * types on multiple resources.
	 * 
	 * @param types
	 *            an array describing the types of markers to be created
	 * @param attributes
	 *            an array of maps of attributes that should be assigned to each
	 *            created marker, corresponding to each marker type described
	 * @param resources
	 *            an array of resources describing the resource on which the
	 *            corresponding marker type should be created
	 * @param name
	 *            the name used to describe the operation that creates the
	 *            markers
	 */
	public CreateMarkersOperation(String[] types, Map[] attributes,
			IResource[] resources, String name) {
		super(null, null, null, name);
		MarkerDescription[] markersToCreate = new MarkerDescription[attributes.length];
		for (int i = 0; i < markersToCreate.length; i++) {
			markersToCreate[i] = new MarkerDescription(types[i], attributes[i],
					resources[i]);
		}
		setMarkerDescriptions(markersToCreate);
	}

	/**
	 * Create an undoable operation that can create multiple markers of a single
	 * type on multiple resources.
	 * 
	 * @param type
	 *            the type of markers to be created
	 * @param attributes
	 *            an array of maps of attributes that should be assigned to each
	 *            created marker
	 * @param resources
	 *            an array of resources describing the resource on which the
	 *            marker with the corresponding attributes should be created
	 * @param name
	 *            the name used to describe the operation that creates the
	 *            markers
	 */
	public CreateMarkersOperation(String type, Map[] attributes,
			IResource[] resources, String name) {
		super(null, null, null, name);
		MarkerDescription[] markersToCreate = new MarkerDescription[attributes.length];
		for (int i = 0; i < markersToCreate.length; i++) {
			markersToCreate[i] = new MarkerDescription(type, attributes[i],
					resources[i]);
		}
		setMarkerDescriptions(markersToCreate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation creates markers
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		monitor.setTaskName(UndoMessages.MarkerOperation_CreateProgress);
		createMarkers(100, monitor);
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation deletes markers
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		monitor.setTaskName(UndoMessages.MarkerOperation_DeleteProgress);
		deleteMarkers(100, monitor);
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation maps the undo status to the deletion status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicUndoStatus()
	 */
	protected IStatus getBasicUndoStatus() {
		return getMarkerDeletionStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation maps the redo status to the creation status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicRedoStatus()
	 */
	protected IStatus getBasicRedoStatus() {
		return getMarkerCreationStatus();
	}
}
