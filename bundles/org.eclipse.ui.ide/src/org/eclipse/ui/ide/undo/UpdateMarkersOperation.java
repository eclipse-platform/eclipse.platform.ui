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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An UpdateMarkersOperation represents an undoable operation for updating one
 * or more markers in the workspace with one or more sets of attributes. Clients
 * may call the public API from a background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * @since 3.3
 * 
 */
public class UpdateMarkersOperation extends AbstractMarkersOperation {

	// Whether attributes should be merged with existing attributes when
	// updated, or considered to be complete replacements.
	private boolean mergeAttributes;

	/**
	 * Create an undoable operation that can update the specified marker with
	 * the specified attributes.
	 * 
	 * @param marker
	 *            the marker to be updated
	 * @param attributes
	 *            the map of attributes to be assigned to the marker. This map
	 *            does not replace the attribute map of the marker, but instead,
	 *            each attribute in the map is added or updated with the current
	 *            value in the map. In other words
	 * @param name
	 *            the name used to describe this operation
	 * @param mergeAttributes
	 *            <code>true</code> if the specified map of attributes for the
	 *            marker is to be merged with the attributes already specified
	 *            for the marker, or <code>false</code> if the specified map
	 *            of attributes is to be considered a complete replacement of
	 *            all attributes of the marker
	 */
	public UpdateMarkersOperation(IMarker marker, Map attributes, String name,
			boolean mergeAttributes) {
		super(new IMarker[] { marker }, null, attributes, name);
		this.mergeAttributes = mergeAttributes;
	}

	/**
	 * Create an undoable operation that updates many markers to have the same
	 * set of attributes.
	 * 
	 * @param markers
	 *            the markers to be updated
	 * @param attributes
	 *            the map of attributes to be assigned to each marker
	 * @param name
	 *            the name used to describe this operation
	 * @param mergeAttributes
	 *            <code>true</code> if the specified map of attributes for
	 *            each marker is to be merged with the attributes already
	 *            specified for that marker, or <code>false</code> if the
	 *            specified map of attributes is to be considered a complete
	 *            replacement of all attributes for each marker
	 */
	public UpdateMarkersOperation(IMarker[] markers, Map attributes,
			String name, boolean mergeAttributes) {
		super(markers, null, attributes, name);
		this.mergeAttributes = mergeAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map execution to updating the markers.
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
		monitor.setTaskName(UndoMessages.MarkerOperation_UpdateProgress);
		updateMarkers(100, monitor, mergeAttributes);
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map undo to execute (since both operations update the markers).
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		// doExecute simply swaps the current and remembered attributes,
		// so it can also be used for undo
		doExecute(monitor, info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map undo status to marker update status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicUndoStatus()
	 */
	protected IStatus getBasicUndoStatus() {
		return getMarkerUpdateStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map redo status to marker update status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicRedoStatus()
	 */
	protected IStatus getBasicRedoStatus() {
		return getMarkerUpdateStatus();
	}
}
