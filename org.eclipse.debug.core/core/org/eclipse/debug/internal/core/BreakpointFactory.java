package org.eclipse.debug.internal.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IBreakpointFactoryDelegate;

public class BreakpointFactory implements IBreakpointFactory {

	/**
	 * The configuration element that defines this breakpoint factory handle
	 */
	protected IConfigurationElement fConfigElement = null;
	
	/**
	 * The type of markers that this breakpoint factory creates
	 * breakpoints for
	 */
	private String fType= null;
	
	/**
	 * The underlying breakpoint factory, which is <code>null</code> until the
	 * it needs to be instantiated.
	 */
	protected IBreakpointFactoryDelegate fDelegate = null;

	/**
	 * Constructs a handle for a breakpoint factory extension.
	 */
	public BreakpointFactory(IConfigurationElement element) {
		fConfigElement = element;
	}
	
	/**
	 * Returns the marker type specified in the configuration data.
	 */
	public String getMarkerType() {
		if (fType == null) {
			fType= fConfigElement.getAttribute("type");
		}
		return fType;
	}	
	
	/**
	 * Returns the breakpoint factory for this handle, instantiating it if required.
	 */
	public IBreakpointFactoryDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (IBreakpointFactoryDelegate)fConfigElement.createExecutableExtension("class");
			} catch (CoreException e) {
				//status logged in the #createExecutableExtension code
			}
		}	
		return fDelegate;
	}	
	
	public boolean canCreateBreakpointsFor(IMarker marker) throws CoreException {
		return (marker.isSubtypeOf(getMarkerType()));
	}

	/**
	 * @see IBreakpointFactory#createBreakpointFor(IMarker)
	 */
	public IBreakpoint createBreakpointFor(IMarker marker) throws DebugException {
		try {
			if (canCreateBreakpointsFor(marker)) {
				return getDelegate().createBreakpointFor(marker);
			}
		} catch (CoreException ce) {
			throw new DebugException(ce.getStatus());
		}
		return null;
	}

}

