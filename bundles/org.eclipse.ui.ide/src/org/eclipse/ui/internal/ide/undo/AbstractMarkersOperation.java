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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 3.2
 * 
 */
public abstract class AbstractMarkersOperation extends
		AbstractWorkspaceOperation {

	MarkerDescription[] markerDescriptions;

	IMarker[] markers;

	Map[] attributes;

	AbstractMarkersOperation(IMarker[] markers,
			MarkerDescription[] markerDescriptions, Map[] attributes,
			String name, String errorTitle) {
		super(name, errorTitle);
		this.markers = markers;
		this.attributes = attributes;
		this.markerDescriptions = markerDescriptions;
		updateTargetResources(markers);
	}

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

	protected void updateMarkers(int work, IProgressMonitor monitor)
			throws CoreException {
		if (attributes == null || markers == null
				|| attributes.length != markers.length || markers.length == 0) {
			monitor.worked(work);
			return;
		}
		int markerWork = work / markers.length;
		for (int i = 0; i < markers.length; i++) {
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

		}
	}

	private void updateTargetResources(IMarker[] markers) {
		if (markers != null) {
			IResource[] resources = new IResource[markers.length];
			for (int i = 0; i < markers.length; i++) {
				resources[i] = markers[i].getResource();
			}
		}
		setTargetResources(resources);
	}
}
