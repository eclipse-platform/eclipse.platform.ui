package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * Abstract implementation of a breakpoint. This class is
 * intended to be subclassed by implementations
 * of breakpoints.
 */

public abstract class Breakpoint implements IBreakpoint {
				
	/**
	 * Underlying marker.
	 */
	private IMarker fMarker= null;
	
	/**
	 * @see IBreakpoint#setMarker(IMarker)
	 */
	public void setMarker(IMarker marker) throws CoreException {
		fMarker= marker;
	}
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object item) {
		if (item instanceof IBreakpoint) {
			return getMarker().equals(((IBreakpoint)item).getMarker());
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getMarker().hashCode();
	}
		
	/**
	 * @see IBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) throws CoreException {
		if (enabled != isEnabled()) {
			getMarker().setAttribute(ENABLED, enabled);
		}
	}
	
	/**
	 * @see IBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CoreException {
		return getMarker().getAttribute(ENABLED, false);
	}
	
	/**
	 * @see IBreakpoint#isRegistered()
	 */
	public boolean isRegistered() throws CoreException {
		return getMarker().getAttribute(REGISTERED, true);
	}	
	
	/**
	 * @see IBreakpoint#setRegistered(boolean)
	 */
	public void setRegistered(boolean registered) throws CoreException {
		if (isRegistered() != registered) {
			getMarker().setAttribute(REGISTERED, registered);
		}
	}	

	/**
	 * @see IBreakpoint#delete()
	 */
	public void delete() throws CoreException {
		getMarker().delete();
	}

	/**
	 * @see IBreakpoint#getMarker()
	 */
	public IMarker getMarker() {
		return fMarker;
	}
	
	/**
	 * @see IAdaptable#getAdapter(Class adapter)
	 */
	public Object getAdapter(Class adapter) {		
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * @see IBreakpoint#isPersisted()
	 */
	public boolean isPersisted() throws CoreException {
		return getMarker().getAttribute(PERSISTED, true);
	}

	/**
	 * @see IBreakpoint#setPersisted(boolean)
	 */
	public void setPersisted(boolean persisted) throws CoreException {
		if (isPersisted() != persisted) {
			getMarker().setAttribute(PERSISTED, persisted);
		}
	}

}

