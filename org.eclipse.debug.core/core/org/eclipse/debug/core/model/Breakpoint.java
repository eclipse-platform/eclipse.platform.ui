package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.DebugCoreMessages;

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
			setAttribute(ENABLED, enabled);
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
			setAttribute(REGISTERED, registered);
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
			setAttribute(PERSISTED, persisted);
		}
	}
	
	protected void setAttribute(final String attributeName, final boolean value) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ensureMarker().setAttribute(attributeName, value);
				}
			};
			
		workspace.run(runnable, null);
	}
	
	protected void setAttribute(final String attributeName, final int value) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ensureMarker().setAttribute(attributeName, value);
				}
			};
			
		workspace.run(runnable, null);
	}

	protected void setAttribute(final String attributeName, final Object value) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ensureMarker().setAttribute(attributeName, value);
				}
			};
			
		workspace.run(runnable, null);
	}

	protected void setAttributes(final String[] attributeNames, final Object[] values) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ensureMarker().setAttributes(attributeNames, values);
				}
			};
			
		workspace.run(runnable, null);
	}

	protected void setAttributes(final Map attributes) throws CoreException{
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ensureMarker().setAttributes(attributes);
				}
			};
			
		workspace.run(runnable, null);
	}

	protected IMarker ensureMarker() throws DebugException {
		IMarker m = getMarker();
		if (m == null || !m.exists()) {
			throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.REQUEST_FAILED,
				DebugCoreMessages.getString("Breakpoint.no_associated_marker"), null)); //$NON-NLS-1$
		}
		return m;
	}
}

