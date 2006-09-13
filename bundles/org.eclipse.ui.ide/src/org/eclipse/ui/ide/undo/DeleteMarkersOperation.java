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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * A DeleteMarkersOperation represents an undoable operation for deleting one or
 * more markers in the workspace.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class DeleteMarkersOperation extends AbstractMarkersOperation {

	/**
	 * Create an undoable operation that can delete the specified markers.
	 * 
	 * @param markers
	 *            the markers to be deleted
	 * @param name
	 *            the name used to describe the operation that deletes the
	 *            markers
	 */
	public DeleteMarkersOperation(IMarker[] markers, String name) {
		super(markers, null, null, name);
	}

	/*
	 * Execute the operation by deleting the markers.
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		deleteMarkers(100, monitor);
		monitor.done();
	}

	/*
	 * Undo the operation by recreating the markers.
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		createMarkers(100, monitor);
		monitor.done();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicUndoStatus()
	 */
	protected IStatus getBasicUndoStatus() {
		 return getMarkerCreationStatus();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ide.undo.AbstractMarkersOperation#getBasicRedoStatus()
	 */
	protected IStatus getBasicRedoStatus() {
		return getMarkerDeletionStatus();
	}
}
