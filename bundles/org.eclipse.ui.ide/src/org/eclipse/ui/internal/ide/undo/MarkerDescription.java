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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A lightweight description of a marker that can be used to describe 
 * a marker to be created or updated.
 * 
 * @since 3.2
 *
 */
class MarkerDescription {
	String type;
	Map attributes;
	IResource resource;
	
	MarkerDescription(IMarker marker) throws CoreException {
		this.type = marker.getType();
		this.attributes = marker.getAttributes();
		this.resource = marker.getResource();
		
	}
	
	MarkerDescription(String type, Map attributes, IResource resource)  {
		this.type = type;
		this.attributes = attributes;
		this.resource = resource;
	}
	
	protected IMarker createMarker() throws CoreException {
		IMarker marker = resource.createMarker(type);
		marker.setAttributes(attributes);
		return marker;
	}
	
	protected void updateMarker(IMarker marker) throws CoreException {
		marker.setAttributes(attributes);
	}
}