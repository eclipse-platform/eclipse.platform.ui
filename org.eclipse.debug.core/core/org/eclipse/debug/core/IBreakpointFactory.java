package org.eclipse.debug.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * Creates breakpoints from markers
 */
public interface IBreakpointFactory {

	/**
	 * Create a breakpoint for the given marker based on the marker type
	 */
	IBreakpoint createBreakpointFor(IMarker marker) throws DebugException;
	
	/**
	 * Returns whether this breakpoint factory knows how to create
	 * breakpoints for the given marker.
	 */
	boolean canCreateBreakpointsFor(IMarker marker) throws CoreException;

}

