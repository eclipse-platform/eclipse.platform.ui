/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * This is a concrete class that stores the same type of information as the IMarkers
 * used by the IDE. This class exists as an optimization. The various get* methods
 * on IMarker are extremely slow, which makes it very slow to sort markers (for example,
 * in the problems view). This marker class stores the fields in the most efficient form
 * for sorting and display, but necessarily removes some generality from IMarker.
 */
public class ConcreteMarker {

	private String description;
	private String resourceName;
	private String inFolder;
	private int line;
	private long creationTime;
	private String type;
	
	private IMarker marker;

	public ConcreteMarker(IMarker toCopy) {
		marker = toCopy;
		refresh();
	}
	
	
	/**
	 * Refresh the properties of this marker from the underlying IMarker instance
	 */
	public void refresh() {
		description = Util.getProperty(IMarker.MESSAGE, marker);
		resourceName = marker.getResource().getName();
		inFolder = Util.getContainerName(marker);
		line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		try {
			creationTime = marker.getCreationTime();
		} catch (CoreException e) {
			creationTime = 0;
		}
		
		try {
			type = marker.getType();
		} catch (CoreException e1) {
			type = ""; //$NON-NLS-1$
		}		
	}
	
	public IResource getResource() {
		return marker.getResource();
	}
	
	public String getType() {
		return type;
	}
		
	public String getDescription() {
		return description;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public int getLine() {
		return line;
	}
	
	public String getFolder() {
		return inFolder;
	}
	
	public long getCreationTime() {
		return creationTime;
	}

	public IMarker getMarker() {
		return marker;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ConcreteMarker)) {
			return false;
		}
		
		ConcreteMarker other = (ConcreteMarker)object;
		
		return other.getMarker().equals(getMarker());
	}
	
	public int hashCode() {
		return getMarker().hashCode();
	}
}
