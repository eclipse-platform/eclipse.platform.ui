/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.Map;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

/**
 * An abstract marker implementation.
 * Subclasses must implement the <code>clone</code> method, and
 * are free to declare additional field and method members.
 * <p>
 * Note: Marker objects do not store whether they are "standalone"
 * vs. "attached" to the workspace. This information is maintained
 * by the workspace.
 * </p>
 *
 * @see IMarker
 */
public class Marker extends PlatformObject implements IMarker {

	/** Marker identifier. */
	protected long id;

	/** Resource with which this marker is associated. */
	protected IResource resource;

	/**
	 * Constructs a new marker object. 
	 */
	Marker(IResource resource, long id) {
		Assert.isLegal(resource != null);
		this.resource = resource;
		this.id = id;
	}

	/**
	 * Checks the given marker info to ensure that it is not null.
	 * Throws an exception if it is.
	 */
	private void checkInfo(MarkerInfo info) throws CoreException {
		if (info == null) {
			String message = NLS.bind(Messages.resources_markerNotFound, Long.toString(id));
			throw new ResourceException(new ResourceStatus(IResourceStatus.MARKER_NOT_FOUND, resource.getFullPath(), message));
		}
	}

	/**
	 * @see IMarker#delete()
	 */
	public void delete() throws CoreException {
		final ISchedulingRule rule = getWorkspace().getRuleFactory().markerRule(resource);
		try {
			getWorkspace().prepareOperation(rule, null);
			getWorkspace().beginOperation(true);
			getWorkspace().getMarkerManager().removeMarker(getResource(), getId());
		} finally {
			getWorkspace().endOperation(rule, false, null);
		}
	}

	/**
	 * @see IMarker#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof IMarker))
			return false;
		IMarker other = (IMarker) object;
		return (id == other.getId() && resource.equals(other.getResource()));
	}

	/**
	 * @see IMarker#exists()
	 */
	public boolean exists() {
		return getInfo() != null;
	}

	/**
	 * @see IMarker#getAttribute(String)
	 */
	public Object getAttribute(String attributeName) throws CoreException {
		Assert.isNotNull(attributeName);
		MarkerInfo info = getInfo();
		checkInfo(info);
		return info.getAttribute(attributeName);
	}

	/**
	 * @see IMarker#getAttribute(String, int)
	 */
	public int getAttribute(String attributeName, int defaultValue) {
		Assert.isNotNull(attributeName);
		MarkerInfo info = getInfo();
		if (info == null)
			return defaultValue;
		Object value = info.getAttribute(attributeName);
		if (value instanceof Integer)
			return ((Integer) value).intValue();
		return defaultValue;
	}

	/**
	 * @see IMarker#getAttribute(String, String)
	 */
	public String getAttribute(String attributeName, String defaultValue) {
		Assert.isNotNull(attributeName);
		MarkerInfo info = getInfo();
		if (info == null)
			return defaultValue;
		Object value = info.getAttribute(attributeName);
		if (value instanceof String)
			return (String) value;
		return defaultValue;
	}

	/**
	 * @see IMarker#getAttribute(String, boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		Assert.isNotNull(attributeName);
		MarkerInfo info = getInfo();
		if (info == null)
			return defaultValue;
		Object value = info.getAttribute(attributeName);
		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue();
		return defaultValue;
	}

	/**
	 * @see IMarker#getAttributes()
	 */
	public Map getAttributes() throws CoreException {
		MarkerInfo info = getInfo();
		checkInfo(info);
		return info.getAttributes();
	}

	/**
	 * @see IMarker#getAttributes(String[])
	 */
	public Object[] getAttributes(String[] attributeNames) throws CoreException {
		Assert.isNotNull(attributeNames);
		MarkerInfo info = getInfo();
		checkInfo(info);
		return info.getAttributes(attributeNames);
	}

	/**
	 * @see IMarker#getCreationTime()
	 */
	public long getCreationTime() throws CoreException {
		MarkerInfo info = getInfo();
		checkInfo(info);
		return info.getCreationTime();
	}

	/**
	 * @see IMarker#getId()
	 */
	public long getId() {
		return id;
	}

	protected MarkerInfo getInfo() {
		return getWorkspace().getMarkerManager().findMarkerInfo(resource, id);
	}

	/**
	 * @see IMarker#getResource()
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * @see IMarker#getType()
	 */
	public String getType() throws CoreException {
		MarkerInfo info = getInfo();
		checkInfo(info);
		return info.getType();
	}

	/**
	 * Returns the workspace which manages this marker.  Returns
	 * <code>null</code> if this resource does not have an associated
	 * resource.
	 */
	private Workspace getWorkspace() {
		return resource == null ? null : (Workspace) resource.getWorkspace();
	}

	public int hashCode() {
		return (int) id + resource.hashCode();
	}

	/**
	 * @see IMarker#isSubtypeOf(String)
	 */
	public boolean isSubtypeOf(String type) throws CoreException {
		return getWorkspace().getMarkerManager().isSubtype(getType(), type);
	}

	/**
	 * @see IMarker#setAttribute(String, int)
	 */
	public void setAttribute(String attributeName, int value) throws CoreException {
		setAttribute(attributeName, new Integer(value));
	}

	/**
	 * @see IMarker#setAttribute(String, Object)
	 */
	public void setAttribute(String attributeName, Object value) throws CoreException {
		Assert.isNotNull(attributeName);
		Workspace workspace = getWorkspace();
		MarkerManager manager = workspace.getMarkerManager();
		try {
			workspace.prepareOperation(null, null);
			workspace.beginOperation(true);
			MarkerInfo markerInfo = getInfo();
			checkInfo(markerInfo);

			//only need to generate delta info if none already
			boolean needDelta = !manager.hasDelta(resource.getFullPath(), id);
			MarkerInfo oldInfo = needDelta ? (MarkerInfo) markerInfo.clone() : null;
			markerInfo.setAttribute(attributeName, value);
			if (manager.isPersistent(markerInfo))
				((Resource) resource).getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
			if (needDelta) {
				MarkerDelta delta = new MarkerDelta(IResourceDelta.CHANGED, resource, oldInfo);
				manager.changedMarkers(resource, new MarkerDelta[] {delta});
			}
		} finally {
			workspace.endOperation(null, false, null);
		}
	}

	/**
	 * @see IMarker#setAttribute(String, boolean)
	 */
	public void setAttribute(String attributeName, boolean value) throws CoreException {
		setAttribute(attributeName, value ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * @see IMarker#setAttributes(String[], Object[])
	 */
	public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
		Assert.isNotNull(attributeNames);
		Assert.isNotNull(values);
		Workspace workspace = getWorkspace();
		MarkerManager manager = workspace.getMarkerManager();
		try {
			workspace.prepareOperation(null, null);
			workspace.beginOperation(true);
			MarkerInfo markerInfo = getInfo();
			checkInfo(markerInfo);

			//only need to generate delta info if none already
			boolean needDelta = !manager.hasDelta(resource.getFullPath(), id);
			MarkerInfo oldInfo = needDelta ? (MarkerInfo) markerInfo.clone() : null;
			markerInfo.setAttributes(attributeNames, values);
			if (manager.isPersistent(markerInfo))
				((Resource) resource).getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
			if (needDelta) {
				MarkerDelta delta = new MarkerDelta(IResourceDelta.CHANGED, resource, oldInfo);
				manager.changedMarkers(resource, new MarkerDelta[] {delta});
			}
		} finally {
			workspace.endOperation(null, false, null);
		}
	}

	/**
	 * @see IMarker#setAttributes(Map)
	 */
	public void setAttributes(Map values) throws CoreException {
		Workspace workspace = getWorkspace();
		MarkerManager manager = workspace.getMarkerManager();
		try {
			workspace.prepareOperation(null, null);
			workspace.beginOperation(true);
			MarkerInfo markerInfo = getInfo();
			checkInfo(markerInfo);

			//only need to generate delta info if none already
			boolean needDelta = !manager.hasDelta(resource.getFullPath(), id);
			MarkerInfo oldInfo = needDelta ? (MarkerInfo) markerInfo.clone() : null;
			markerInfo.setAttributes(values);
			if (manager.isPersistent(markerInfo))
				((Resource) resource).getResourceInfo(false, true).set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
			if (needDelta) {
				MarkerDelta delta = new MarkerDelta(IResourceDelta.CHANGED, resource, oldInfo);
				manager.changedMarkers(resource, new MarkerDelta[] {delta});
			}
		} finally {
			workspace.endOperation(null, false, null);
		}
	}
}
