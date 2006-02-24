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

package org.eclipse.ui.internal.ide.undo;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @since 3.2
 * 
 */
public class UpdateMarkerOperation extends AbstractMarkersOperation {

	/**
	 * Create one of these
	 * 
	 * @param marker
	 * @param attributes
	 * @param name
	 * @param errorTitle
	 */
	public UpdateMarkerOperation(IMarker marker, Map attributes, String name,
			String errorTitle) {
		super(new IMarker[] { marker }, null, new Map [] {attributes}, name, errorTitle);
	}
	
	/**
	 * Create an update that updates many markers to have the same set of attributes.
	 * 
	 * @param markers
	 * @param attributes
	 * @param name
	 * @param errorTitle
	 */
	public UpdateMarkerOperation(IMarker [] markers, Map attributes, String name,
			String errorTitle) {
		super(markers, null, new Map [] {attributes}, name, errorTitle);
	}

	protected void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		updateMarkers(100, monitor);
		monitor.done();
	}

	protected void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		// doExecute swaps the attributes so it can be used for undo
		doExecute(monitor, info);
	}

}
