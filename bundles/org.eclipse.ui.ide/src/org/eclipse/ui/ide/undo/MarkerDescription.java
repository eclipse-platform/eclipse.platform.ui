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

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * MarkerDescription is a lightweight description of a marker that can be used
 * to describe a marker to be created or updated.
 * 
 * This class is not intended to be instantiated or used by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
class MarkerDescription {
	String type;

	Map attributes;

	IResource resource;

	/*
	 * Create a marker description from the specified marker.
	 */
	MarkerDescription(IMarker marker) throws CoreException {
		this.type = marker.getType();
		this.attributes = marker.getAttributes();
		this.resource = marker.getResource();

	}

	/*
	 * Create a marker description from the specified marker type, attributes,
	 * and resource.
	 */
	MarkerDescription(String type, Map attributes, IResource resource) {
		this.type = type;
		this.attributes = attributes;
		this.resource = resource;
	}

	/*
	 * Create a marker from the marker description.
	 */
	protected IMarker createMarker() throws CoreException {
		IMarker marker = resource.createMarker(type);
		marker.setAttributes(attributes);
		return marker;
	}

	/*
	 * Update an existing marker using the attributes in the marker description.
	 */
	protected void updateMarker(IMarker marker) throws CoreException {
		marker.setAttributes(attributes);
	}
}