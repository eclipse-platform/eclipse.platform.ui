package org.eclipse.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpoint;

public interface IBreakpointFactoryDelegate {
	/**
	 * Notifies this breakpoint factory delegate to create a breakpoint 
	 * based on the type of the marker.
	 * Returns the breakpoint if successful, otherwise <code>null</code>.
	 *
	 * @param elements an array of objects providing a context for the launch
	 * @param mode run or debug (as defined by <code>ILaunchManager.RUN_MODE</code>,
	 *    <code>ILaunchManager.DEBUG_MODE</code>)
	 * @param launcher the proxy to this lazily instantiated extension which needs
	 *    to be supplied in the resulting launch object
	 * @return whether the launch succeeded
	 *
	 * @see org.eclipse.debug.core.ILaunch
	 * @see org.eclipse.debug.core.Launch
	 * @see IDebugTarget
	 * @see IProcess
	 * @see org.eclipse.debug.core.ILaunchManager#registerLaunch
	 */
	IBreakpoint createBreakpointFor(IMarker marker) throws DebugException;

}

