package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * The breakpoint manager manages all registered breakpoints
 * for the debug plugin. It is instantiated by the debug plugin.
 *
 * @see IBreakpointManager
 */
public class BreakpointManager implements IBreakpointManager, IResourceChangeListener {
	
	/**
	 * Constants for breakpoint add/remove/change updates
	 */
	private final static int ADDED = 0;
	private final static int REMOVED = 1;
	private final static int CHANGED = 2;

	/**
	 * A collection of breakpoint registered with this manager.
	 *
	 */
	protected Vector fBreakpoints;
	
	/**
	 * A table of breakpoint extension points, keyed by
	 * marker type
	 */
	protected HashMap fBreakpointExtensions;
	
	/**
	 * Collection of markers that associates markers to breakpoints
	 */	
	protected HashMap fMarkers;

	/**
	 * Collection of breakpoint listeners.
	 */
	protected ListenerList fBreakpointListeners= new ListenerList(6);

	/**
	 * Singleton resource delta visitor
	 */
	protected static BreakpointManagerVisitor fgVisitor;

	/**
	 * Constructs a new breakpoint manager.
	 */
	public BreakpointManager() {
		fBreakpoints= new Vector(15);
		fMarkers= new HashMap(15);	
		fBreakpointExtensions = new HashMap(15);	
	}

	/**
	 * Registers this manager as a resource change listener.
	 * Loads the list of breakpoints from the breakpoint markers in the
	 * workspace.  This method should only be called on initial startup of 
	 * the debug plugin.
	 *
	 * @exception CoreException if an error occurrs retreiving breakpoint markers
	 */
	public void startup() throws CoreException {
		initBreakpointExtensions();
		getWorkspace().addResourceChangeListener(this);
		
		IMarker[] breakpoints= null;
		IWorkspaceRoot root= getWorkspace().getRoot();
		breakpoints= root.findMarkers(IDebugConstants.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		
		for (int i = 0; i < breakpoints.length; i++) {
			IMarker marker= breakpoints[i];
			try {
				createBreakpoint(marker);
			} catch (DebugException e) {
				logError(e);
			}
		}		
	}
	
	/**
	 * Removes this manager as a resource change listener
	 */
	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
	}

	protected void initBreakpointExtensions() {
		IExtensionPoint ep= DebugPlugin.getDefault().getDescriptor().getExtensionPoint(IDebugConstants.EXTENSION_POINT_BREAKPOINTS);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			fBreakpointExtensions.put(elements[i].getAttribute(IDebugConstants.MARKER_TYPE), elements[i]);
		}
		
	}

	/**
	 * Convenience method to get the launch manager.
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Convenience method to get the workspace
	 */
	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * @see IBreakpointManager
	 */
	public IBreakpoint getBreakpoint(IMarker marker) {
		return (IBreakpoint)fMarkers.get(marker);
	}

	/**
	 * @see IBreakpointManager
	 */
	public IBreakpoint[] getBreakpoints() {
		Breakpoint[] temp= new Breakpoint[fBreakpoints.size()];
		fBreakpoints.copyInto(temp);
		return temp;
	}
	
	/**
	 * @see IBreakpointManager
	 */
	public IBreakpoint[] getBreakpoints(String modelIdentifier) {
		Vector temp= new Vector(fBreakpoints.size());
		if (!fBreakpoints.isEmpty()) {
			Iterator breakpoints= fBreakpoints.iterator();
			while (breakpoints.hasNext()) {
				IBreakpoint breakpoint= (IBreakpoint) breakpoints.next();
				String id= breakpoint.getModelIdentifier();
				if (id != null && id.equals(modelIdentifier)) {
					temp.add(breakpoint);
				}
			}
		}
		Breakpoint[] m= new Breakpoint[temp.size()];
		temp.copyInto(m);
		return m;
	}	

	/**
	 * @see IBreakpointManager
	 */
	public boolean isRegistered(IBreakpoint breakpoint) {
		return fBreakpoints.contains(breakpoint);
	}

	
	/**
	 * Remove the given breakpoint
	 */
	public void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
		if (fBreakpoints.remove(breakpoint)) {
			fMarkers.remove(breakpoint.getMarker());
			fireUpdate(breakpoint, null, REMOVED);
			if (delete) {
				breakpoint.delete();
			}
		}
	}
	
	/**
	 * @see IBreakpointManager
	 */
	public IBreakpoint createBreakpoint(IMarker marker) throws DebugException {
		if (fMarkers.containsKey(marker)) {
			return (IBreakpoint) fMarkers.get(marker);
		}	
		try {
			IConfigurationElement config = (IConfigurationElement)fBreakpointExtensions.get(marker.getType());
			if (config == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
					IDebugStatusConstants.CONFIGURATION_INVALID, MessageFormat.format(DebugCoreMessages.getString("BreakpointManager.Missing_breakpoint_definition"), new String[] {marker.getType()}), null)); //$NON-NLS-1$
			}
			IBreakpoint bp = (IBreakpoint)config.createExecutableExtension("class"); //$NON-NLS-1$
			bp.setMarker(marker);
			addBreakpoint(bp);
			return bp;		
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}	

	/**
	 * @see IBreakpointManager
	 */
	public void addBreakpoint(IBreakpoint breakpoint) throws DebugException {
		if (!fBreakpoints.contains(breakpoint)) {
			verifyBreakpoint(breakpoint);
			fBreakpoints.add(breakpoint);
			fMarkers.put(breakpoint.getMarker(), breakpoint);
			fireUpdate(breakpoint, null, ADDED);
		}			
	}

	/**
	 * Verifies that the breakpoint marker has the minimal required attributes,
	 * and throws a debug exception if not.
	 */
	protected void verifyBreakpoint(IBreakpoint breakpoint) throws DebugException {
		try {
			String id= breakpoint.getModelIdentifier();
			if (id == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
					IDebugStatusConstants.CONFIGURATION_INVALID, DebugCoreMessages.getString("BreakpointManager.Missing_model_identifier"), null)); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}		
	}

	/**
	 * A resource has changed. Traverses the delta for breakpoint changes.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fgVisitor == null) {
					fgVisitor= new BreakpointManagerVisitor();
				}
				delta.accept(fgVisitor);
			} catch (CoreException ce) {
				logError(ce);
			}
		}
	}

	/**
	 * A project has been opened or closed.  Updates the breakpoints for
	 * that project
	 */
	protected void handleProjectResourceOpenStateChange(IResource project) {
		if (!project.isAccessible()) {
			//closed
			Enumeration breakpoints= fBreakpoints.elements();
			while (breakpoints.hasMoreElements()) {
				IBreakpoint breakpoint= (IBreakpoint) breakpoints.nextElement();
				IResource markerResource= breakpoint.getMarker().getResource();
				if (project.getFullPath().isPrefixOf(markerResource.getFullPath())) {
					try {
						removeBreakpoint(breakpoint, false);
					} catch (CoreException e) {
						logError(e);
					}
				}
			}
			return;
		} else {
			IMarker[] markers= null;
			try {
				markers= project.findMarkers(IDebugConstants.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				logError(e);
				return;
			}

			if (markers != null) {
				for (int i= 0; i < markers.length; i++) {
					try {
						createBreakpoint(markers[i]);
					} catch (DebugException e) {
						logError(e);
					}
				}
			}
		}
	}

	/**
	 * Logs errors
	 */
	protected void logError(Exception e) {
		DebugCoreUtils.logError(e);
	}

	/**
	 * Visitor for handling resource deltas
	 */
	class BreakpointManagerVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor
		 */
		public boolean visit(IResourceDelta delta) {
			if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
				handleProjectResourceOpenStateChange(delta.getResource());
				return true;
			}
			IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
			for (int i= 0; i < markerDeltas.length; i++) {
				IMarkerDelta markerDelta= markerDeltas[i];
				if (markerDelta.isSubtypeOf(IDebugConstants.BREAKPOINT_MARKER)) {
					switch (markerDelta.getKind()) {
						case IResourceDelta.ADDED :
							handleAddBreakpoint(delta, markerDelta.getMarker(), markerDelta);
							break;
						case IResourceDelta.REMOVED :
							handleRemoveBreakpoint(markerDelta.getMarker(), markerDelta);
							break;
						case IResourceDelta.CHANGED :
							handleChangeBreakpoint(markerDelta.getMarker(), markerDelta);
							break;
					}
				}
			}

			return true;
		}		

		/**
		 * Wrapper for handling adds
		 */
		protected void handleAddBreakpoint(IResourceDelta rDelta, final IMarker marker, IMarkerDelta mDelta) {
			if (0 != (rDelta.getFlags() & IResourceDelta.MOVED_FROM)) {
				// this breakpoint has actually been moved - removed from the Breakpoint manager and deleted
				final IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) {
						try {
							IBreakpoint breakpoint= getBreakpoint(marker);
							if (breakpoint != null) {
								breakpoint.delete();
							}
						} catch (CoreException ce) {
							logError(ce);
						}
					}
				};
				fork(wRunnable);
			} else {
				// do nothing - we do not add until explicitly added
			}
		}

		protected void fork(final IWorkspaceRunnable wRunnable) {
			Runnable runnable= new Runnable() {
				public void run() {
					try {
						getWorkspace().run(wRunnable, null);
					} catch (CoreException ce) {
						logError(ce);
					}
				}
			};
			new Thread(runnable).start();
		}
		
		/**
		 * Wrapper for handling removes
		 */
		protected void handleRemoveBreakpoint(IMarker marker, IMarkerDelta delta) {
			IBreakpoint breakpoint= getBreakpoint(marker);
			if (breakpoint != null) {
				try {
					removeBreakpoint(breakpoint, false);
				} catch (CoreException e) {
					logError(e);
				}
			}
		}

		/**
		 * Wrapper for handling changes
		 */
		protected void handleChangeBreakpoint(IMarker marker, IMarkerDelta delta) {
			final IBreakpoint breakpoint= getBreakpoint(marker);
			if (isRegistered(breakpoint)) {
				fireUpdate(breakpoint, delta, CHANGED);
			}
		}
	}

	/**
	 * @see IBreakpointManager
	 */
	public void addBreakpointListener(IBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}

	/**
	 * @see IBreakpointManager
	 */
	public void removeBreakpointListener(IBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}
	
	/**
	 * Notifies listeners of the add/remove/change
	 */
	protected void fireUpdate(IBreakpoint breakpoint, IMarkerDelta delta, int update) {
		Object[] copiedListeners= fBreakpointListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			IBreakpointListener listener = (IBreakpointListener)copiedListeners[i];
			switch (update) {
				case ADDED:
					listener.breakpointAdded(breakpoint);
					break;
				case REMOVED:
					listener.breakpointRemoved(breakpoint, delta);
					break;
				case CHANGED:
					listener.breakpointChanged(breakpoint, delta);		
					break;
			}
		}
	}

}

