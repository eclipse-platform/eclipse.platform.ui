/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;

public class ResourceChangeEvent extends EventObject implements IResourceChangeEvent {
	private static final long serialVersionUID = 1L;
	int type;
	IResource resource;
	IResourceDelta delta;

	private static final IMarkerDelta[] NO_MARKER_DELTAS = new IMarkerDelta[0];

	protected ResourceChangeEvent(Object source, int type, IResource resource) {
		super(source);
		this.resource = resource;
		this.type = type;
	}

	protected ResourceChangeEvent(Object source, int type, IResourceDelta delta) {
		super(source);
		this.delta = delta;
		this.type = type;
	}

	/**
	 * @see IResourceChangeEvent#findMarkerDeltas(String, boolean)
	 */
	public IMarkerDelta[] findMarkerDeltas(String type, boolean includeSubtypes) {
		if (delta == null)
			return NO_MARKER_DELTAS;
		ResourceDeltaInfo info = ((ResourceDelta) delta).getDeltaInfo();
		if (info == null)
			return NO_MARKER_DELTAS;
		//Map of IPath -> MarkerSet containing MarkerDelta objects
		Map markerDeltas = info.getMarkerDeltas();
		if (markerDeltas == null || markerDeltas.size() == 0)
			return NO_MARKER_DELTAS;
		ArrayList matching = new ArrayList();
		Iterator deltaSets = markerDeltas.values().iterator();
		while (deltaSets.hasNext()) {
			MarkerSet deltas = (MarkerSet) deltaSets.next();
			IMarkerSetElement[] elements = deltas.elements();
			for (int i = 0; i < elements.length; i++) {
				MarkerDelta delta = (MarkerDelta) elements[i];
				//our inclusion test depends on whether we are considering subtypes
				if (type == null || (includeSubtypes ? delta.isSubtypeOf(type) : delta.getType().equals(type)))
					matching.add(delta);
			}
		}
		return (IMarkerDelta[]) matching.toArray(new IMarkerDelta[matching.size()]);
	}

	/**
	 * @see IResourceChangeEvent#getDelta()
	 */
	public IResourceDelta getDelta() {
		return delta;
	}

	/**
	 * @see IResourceChangeEvent#getResource()
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * @see IResourceChangeEvent#getType()
	 */
	public int getType() {
		return type;
	}

	public void setDelta(IResourceDelta value) {
		delta = value;
	}
}