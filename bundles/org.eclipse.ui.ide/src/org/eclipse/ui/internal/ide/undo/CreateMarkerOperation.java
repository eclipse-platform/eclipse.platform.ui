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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @since 3.2
 * 
 */
public class CreateMarkerOperation extends AbstractMarkersOperation {

	/**
	 * Create one of these
	 * 
	 * @param type
	 * @param attributes
	 * @param resource
	 * @param name
	 * @param errorTitle
	 */
	public CreateMarkerOperation(String type, Map attributes, IResource resource, String name,
			String errorTitle) {
		super(null, new MarkerDescription [] {new MarkerDescription(type, attributes, resource)}, null, name, errorTitle);
	}

	protected void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		createMarkers(100, monitor);
		monitor.done();
	}

	protected void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100); //$NON-NLS-1$
		deleteMarkers(100, monitor);
		monitor.done();
	}

}
