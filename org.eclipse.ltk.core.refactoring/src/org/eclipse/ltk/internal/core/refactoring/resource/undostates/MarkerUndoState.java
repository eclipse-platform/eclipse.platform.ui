/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * {@link MarkerUndoState} is a lightweight description of a marker that can be used
 * to describe a marker to be created or updated.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
public class MarkerUndoState {

	protected IResource resource;

	private String type;
	private Map<String, Object> attributes;

	/**
	 *
	 * Create a {@link MarkerUndoState} from the specified marker.
	 *
	 * @param marker
	 *            the marker to be described
	 * @throws CoreException if the marker is invalid
	 */
	public MarkerUndoState(IMarker marker) throws CoreException {
		this.type = marker.getType();
		this.attributes = marker.getAttributes();
		this.resource = marker.getResource();

	}

	/**
	 * Create a marker from the marker description.
	 *
	 * @return the created marker
	 * @throws CoreException if the marker could not be created
	 */
	public IMarker createMarker() throws CoreException {
		IMarker marker = resource.createMarker(type, attributes);
		return marker;
	}

	/**
	 * Update an existing marker using the attributes in the marker description.
	 *
	 * @param marker
	 *            the marker to be updated
	 * @throws CoreException if the marker could not be updated
	 */
	public void updateMarker(IMarker marker) throws CoreException {
		marker.setAttributes(attributes);
	}

	/**
	 * Return the resource associated with this marker.
	 *
	 * @return the resource associated with this marker
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Return the marker type associated with this marker.
	 *
	 * @return the string marker type of this marker
	 */
	public String getType() {
		return type;
	}
}
