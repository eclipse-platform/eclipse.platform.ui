package org.eclipse.debug.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

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
 * Returns whether this breakpoint is enabled
 */
public boolean isEnabled() throws CoreException;
/**
 * Set the enabled state of this breakpoint.
 * 
 * @param enabled  whether this breakpoint should be enabled
 */
public void setEnabled(boolean enabled) throws CoreException;

}

