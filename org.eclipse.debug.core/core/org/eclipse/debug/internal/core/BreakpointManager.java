package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * The breakpoint manager manages all registered breakpoints
 * for the debug plugin. It is instantiated by the debug plugin at startup.
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
	 * String constants corresponding to XML extension keys
	 */
	private final static String CLASS = "class"; //$NON-NLS-1$

	/**
	 * A collection of breakpoint registered with this manager.
	 *
	 */
	private Vector fBreakpoints;
	
	/**
	 * A table of breakpoint extension points, keyed by
	 * marker type
	 * key: a marker type
	 * value: the breakpoint extension which corresponds to that marker type
	 */
	private HashMap fBreakpointExtensions;
	
	/**
	 * Collection of markers that associates markers to breakpoints
	 * key: a marker
	 * value: the breakpoint which contains that marker
	 */	
	private HashMap fMarkersToBreakpoints;

	/**
	 * Collection of breakpoint listeners.
	 */
	private ListenerList fBreakpointListeners= new ListenerList(6);

	/**
	 * Singleton resource delta visitor which handles marker
	 * additions, changes, and removals.
	 */
	private static BreakpointManagerVisitor fgVisitor;

	/**
	 * Constructs a new breakpoint manager.
	 */
	public BreakpointManager() {
		fBreakpoints= new Vector(15);
		fMarkersToBreakpoints= new HashMap(15);	
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
		
		IWorkspaceRoot root= getWorkspace().getRoot();
		loadBreakpoints(root);	
	}
	
	/**
	 * Loads all the breakpoints on the given resource.
	 * 
	 * @param resource the resource which contains the breakpoints
	 */
	private void loadBreakpoints(IResource resource) throws CoreException {
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker= markers[i];
			try {
				createBreakpoint(marker);
			} catch (DebugException e) {
				logError(e);
			}
		}	
	}
	
	/**
	 * Removes this manager as a resource change listener
	 * and removes all breakpoint listeners.
	 */
	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
		fBreakpointListeners.removeAll();
	}

	/**
	 * Find the defined breakpoint extensions and cache them for use in recreating
	 * breakpoints from markers.
	 */
	private void initBreakpointExtensions() {
		IExtensionPoint ep= DebugPlugin.getDefault().getDescriptor().getExtensionPoint(DebugPlugin.EXTENSION_POINT_BREAKPOINTS);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			fBreakpointExtensions.put(elements[i].getAttribute(IBreakpoint.MARKER_TYPE), elements[i]);
		}
		
	}

	/**
	 * Convenience method to get the workspace
	 */
	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * @see IBreakpointManager#getBreakpoint(IMarker)
	 */
	public IBreakpoint getBreakpoint(IMarker marker) {
		return (IBreakpoint)fMarkersToBreakpoints.get(marker);
	}

	/**
	 * @see IBreakpointManager#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		Breakpoint[] temp= new Breakpoint[fBreakpoints.size()];
		fBreakpoints.copyInto(temp);
		return temp;
	}
	
	/**
	 * @see IBreakpointManager#getBreakpoints(String)
	 */
	public IBreakpoint[] getBreakpoints(String modelIdentifier) {
		ArrayList temp= new ArrayList(fBreakpoints.size());
		Iterator breakpoints= fBreakpoints.iterator();
		while (breakpoints.hasNext()) {
			IBreakpoint breakpoint= (IBreakpoint) breakpoints.next();
			String id= breakpoint.getModelIdentifier();
			if (id != null && id.equals(modelIdentifier)) {
				temp.add(breakpoint);
			}
		}
		return (IBreakpoint[]) temp.toArray(new IBreakpoint[temp.size()]);
	}

	/**
	 * @see IBreakpointManager#isRegistered(IBreakpoint)
	 */
	public boolean isRegistered(IBreakpoint breakpoint) {
		return fBreakpoints.contains(breakpoint);
	}

	
	/**
	 * @see IBreakpointManager#removeBreakpoint(IBreakpoint, boolean)
	 */
	public void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
		if (fBreakpoints.remove(breakpoint)) {
			fMarkersToBreakpoints.remove(breakpoint.getMarker());
			fireUpdate(breakpoint, null, REMOVED);
			if (delete) {
				breakpoint.delete();
			}
		}
	}
	
	/**
	 * Create a breakpoint for the given marker. The created breakpoint
	 * is of the type specified in the breakpoint extension associated
	 * with the given marker type.
	 * 
	 * @return a breakpoint on this marker
	 * @exception DebugException if breakpoint creation fails. Reasons for 
	 *  failure include:
	 * <ol>
	 * <li>The breakpoint manager cannot determine what kind of breakpoint
	 *     to instantiate for the given marker type</li>
	 * <li>A lower level exception occurred while accessing the given marker</li>
	 * </ol>
	 */
	private IBreakpoint createBreakpoint(IMarker marker) throws DebugException {
		IBreakpoint breakpoint= (IBreakpoint) fMarkersToBreakpoints.get(marker);
		if (breakpoint != null) {
			return breakpoint;
		}
		try {
			IConfigurationElement config = (IConfigurationElement)fBreakpointExtensions.get(marker.getType());
			if (config == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
					DebugException.CONFIGURATION_INVALID, MessageFormat.format(DebugCoreMessages.getString("BreakpointManager.Missing_breakpoint_definition"), new String[] {marker.getType()}), null)); //$NON-NLS-1$
			}
			breakpoint = (IBreakpoint)config.createExecutableExtension(CLASS);
			breakpoint.setMarker(marker);
			addBreakpoint(breakpoint);
			return breakpoint;		
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}	

	/**
	 * @see IBreakpointManager#addBreakpoint(IBreakpoint)
	 */
	public void addBreakpoint(IBreakpoint breakpoint) throws DebugException {
		if (!fBreakpoints.contains(breakpoint)) {
			verifyBreakpoint(breakpoint);
			fBreakpoints.add(breakpoint);
			fMarkersToBreakpoints.put(breakpoint.getMarker(), breakpoint);
			fireUpdate(breakpoint, null, ADDED);
		}			
	}

	/**
	 * Verifies that the breakpoint marker has the minimal required attributes,
	 * and throws a debug exception if not.
	 */
	private void verifyBreakpoint(IBreakpoint breakpoint) throws DebugException {
		//see bug 6084
	/*	try {
			String id= breakpoint.getModelIdentifier();
			if (id == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
					DebugException.CONFIGURATION_INVALID, DebugCoreMessages.getString("BreakpointManager.Missing_model_identifier"), null)); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}		*/
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
	private void handleProjectResourceOpenStateChange(IResource project) {
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
			try {
				loadBreakpoints(project);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * An exception has occurred. Make a note of it in the log file.
	 */
	private void logError(Exception e) {
		DebugCoreUtils.logError(e);
	}

	/**
	 * Visitor for handling resource deltas
	 */
	class BreakpointManagerVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
				handleProjectResourceOpenStateChange(delta.getResource());
				return false;
			}
			IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
			for (int i= 0; i < markerDeltas.length; i++) {
				IMarkerDelta markerDelta= markerDeltas[i];
				if (markerDelta.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
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
				// this breakpoint has actually been moved - remove from the Breakpoint manager and delete
				IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
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
	 * @see IBreakpointManager#addBreakpointListener(IBreakpointListener)
	 */
	public void addBreakpointListener(IBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}

	/**
	 * @see IBreakpointManager#removeBreakpointListener(IBreakpointListener)
	 */
	public void removeBreakpointListener(IBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}
	
	/**
	 * Notifies listeners of the add/remove/change
	 */
	private void fireUpdate(IBreakpoint breakpoint, IMarkerDelta delta, int update) {
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

