package org.eclipse.debug.core.model;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IDebugConstants;

/**
 * Abstract implementation of a breakpoint. This class is
 * intended to be subclassed by implementations
 * of breakpoints.
 */

public abstract class Breakpoint implements IBreakpoint {
		
	/**
	 * Underlying marker.
	 */
	protected IMarker fMarker= null;
	
	/**
	 * Constructor for Breakpoint
	 */
	public Breakpoint() {
	}
	
	public void setMarker(IMarker marker) throws CoreException {
		fMarker= marker;
	}
	
	public boolean equals(Object item) {
		if (item instanceof IBreakpoint) {
			return getMarker().equals(((IBreakpoint)item).getMarker());
		}
		return false;
	}
	
	public int hashCode() {
		return getMarker().hashCode();
	}
		
	/**
	 * @see IBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		if (enabled != isEnabled()) {
			fMarker.setAttribute(IDebugConstants.ENABLED, enabled);
		}
	}
	
	/**
	 * Returns whether the breakpoint is enabled
	 */
	public boolean isEnabled() throws CoreException {
		return fMarker.getAttribute(IDebugConstants.ENABLED, false);
	}

	/**
	 * @see IBreakpoint#delete()
	 */
	public void delete() throws CoreException {
		fMarker.delete();
	}

	/**
	 * @see IBreakpoint#getMarker()
	 */
	public IMarker getMarker() {
		return fMarker;
	}

}

