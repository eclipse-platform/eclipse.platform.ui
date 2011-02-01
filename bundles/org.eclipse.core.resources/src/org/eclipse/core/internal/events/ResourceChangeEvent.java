/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;

public class ResourceChangeEvent extends EventObject implements IResourceChangeEvent {

	private static final IMarkerDelta[] NO_MARKER_DELTAS = new IMarkerDelta[0];
	private static final long serialVersionUID = 1L;
	IResourceDelta delta;
	IResource resource;

	/**
	 * The build trigger for this event, or 0 if not applicable.
	 */
	private int trigger = 0;
	int type;

	protected ResourceChangeEvent(Object source, int type, IResource resource) {
		super(source);
		this.resource = resource;
		this.type = type;
	}

	public ResourceChangeEvent(Object source, int type, int buildKind, IResourceDelta delta) {
		super(source);
		this.delta = delta;
		this.trigger = buildKind;
		this.type = type;
	}

	/**
	 * @see IResourceChangeEvent#findMarkerDeltas(String, boolean)
	 */
	public IMarkerDelta[] findMarkerDeltas(String findType, boolean includeSubtypes) {
		if (delta == null)
			return NO_MARKER_DELTAS;
		ResourceDeltaInfo info = ((ResourceDelta) delta).getDeltaInfo();
		if (info == null)
			return NO_MARKER_DELTAS;
		//Map of IPath -> MarkerSet containing MarkerDelta objects
		Map<IPath, MarkerSet> markerDeltas = info.getMarkerDeltas();
		if (markerDeltas == null || markerDeltas.size() == 0)
			return NO_MARKER_DELTAS;
		ArrayList<IMarkerDelta> matching = new ArrayList<IMarkerDelta>();
		Iterator<MarkerSet> deltaSets = markerDeltas.values().iterator();
		while (deltaSets.hasNext()) {
			MarkerSet deltas = deltaSets.next();
			IMarkerSetElement[] elements = deltas.elements();
			for (int i = 0; i < elements.length; i++) {
				MarkerDelta markerDelta = (MarkerDelta) elements[i];
				//our inclusion test depends on whether we are considering subtypes
				if (findType == null || (includeSubtypes ? markerDelta.isSubtypeOf(findType) : markerDelta.getType().equals(findType)))
					matching.add(markerDelta);
			}
		}
		return matching.toArray(new IMarkerDelta[matching.size()]);
	}

	/**
	 * @see IResourceChangeEvent#getBuildKind()
	 */
	public int getBuildKind() {
		return trigger;
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
