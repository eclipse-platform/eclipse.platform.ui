package org.eclipse.debug.core;

import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;

public interface IBreakpoint {

/**
 * Deletes this marker from its associated resource.  This method has no
 * effect if this marker does not exist.
 *
 * @exception CoreException if this marker could not be deleted. Reasons include:
 * <ul>
 * <li> Resource changes are disallowed during resource change event notification.</li>
 * </ul>
 */
public void delete() throws CoreException;
/**
 * Tests this breakpoint for equality with the given object.
 * Two breakpoints are equal if their markers have the same id.
 * Markers are assigned an id when created on a resource.
 *
 * @param object the other object
 * @return an indication of whether the objects are equal
 */
public boolean equals(Object object);
/**
 * Returns whether this marker exists in the workspace.  A marker
 * exists if its resource exists and has a marker with the marker's id.
 *
 * @return <code>true</code> if this marker exists, otherwise
 *    <code>false</code>
 */
public boolean exists();
/**
 * Enable this breakpoint
 */
public void enable() throws CoreException;
/**
 * Disable this breakpoint
 */
public void disable() throws CoreException;
/**
 * Returns the marker associated with the breakpoint.
 * 
 * @return the marker, or <code>null</code> if the marker does not exist.
 */
public IMarker getMarker();
/**
 * Sets the marker associated with this breakpoint to the given marker
 */
public void setMarker(IMarker marker);
/**
 * Returns the model identifier for this breakpoint.
 */
public String getModelIdentifier();
/**
 * Returns the id of the marker.  The id of a marker is unique
 * relative to the resource with which the marker is associated.
 * Marker ids are not globally unique.
 *
 * @return the id of the marker
 * @see IResource#findMarker
 */
public long getId();
/**
 * Returns the resource with which this marker is associated. 
 *
 * @return the resource with which this marker is associated
 */
public IResource getResource();
/**
 * Returns the type of this breakpoint.
 *
 * @return the type of this marker
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This marker does not exist.</li>
 * </ul>
 */
public String getType() throws CoreException;
/**
 * Returns whether this breakpoint is enabled
 */
public boolean isEnabled() throws CoreException;
/**
 * Returns whether this breakpoint is disabled
 */
public boolean isDisabled() throws CoreException;
/**
 * Sets the enabled state of this breakpoint to the opposite of its
 * current state.
 */
public void toggleEnabled() throws CoreException;

/**
 * Install a breakpoint request for this breakpoint in the given target.
 * 
 * @param target the debug target into which the request should be added.
 */
public abstract void addToTarget(IDebugTarget target);
/**
 * Update the breakpoint request for this breakpoint in the given target.
 * 
 * @param target the debug target for which the request should be updated.
 */
public abstract void changeForTarget(IDebugTarget target);
/**
 * Remove the breakpoint request for this breakpoint from the given target.
 * 
 * @param target the debug target from which the request should be removed.
 */
public abstract void removeFromTarget(IDebugTarget target);

}

