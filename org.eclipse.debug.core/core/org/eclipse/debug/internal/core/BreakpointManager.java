package org.eclipse.debug.internal.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IBreakpointSupport;
import org.eclipse.debug.core.model.IDebugTarget;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * The breakpoint manager manages all registered breakpoints
 * for the debug plugin. It is instantiated by the debug plugin.
 *
 * @see IBreakpointManager
 */
public class BreakpointManager implements IBreakpointManager, IResourceChangeListener, ILaunchListener {

	private final static String PREFIX= "breakpoint_manager.";
	private final static String ERROR= PREFIX + "error.";
	private final static String OLD_MARKER= ERROR + "old_marker";
	private final static String ADAPTER= ERROR + "adapter";
	private final static String LABEL_PROPERTY= ERROR + "label_property";
	private final static String EXTENSION= ERROR + "extension";
	private final static String ASSOCIATED_RESOURCE= ERROR + "associated_resource";
	private final static String REQUIRED_ATTRIBUTES= ERROR + "required_attributes";
	private final static String LINE_NUMBER= ERROR + "line_number";
	private final static String CREATION_FAILED= ERROR + "creation_failed";
	private final static String UNSUPPORTED_EXTENSION= ERROR + "unsupported_extension";
	private final static String INSTALLATION_FAILED= ERROR + "installation_failed";
	private final static String REMOVE_FAILED= ERROR + "remove_failed";
	private final static String ENABLE= ERROR + "enable";
	private final static String HIT_COUNT= ERROR + "hit_count";
	private final static String TARGET= ERROR + "target";
	
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
	 * Collection of breakpoint listeners.
	 */
	protected ListenerList fBreakpointListeners= new ListenerList(6);

	/**
	 * Singleton resource delta visitor
	 */
	protected static BreakpointManagerVisitor fgVisitor;
	
	/**
	 * The set of attributes used to configure a breakpoint
	 */
	protected static final String[] fgBreakpointAttributes= new String[]{IDebugConstants.MODEL_IDENTIFIER, IDebugConstants.ENABLED};
	
	/**
	 * The set of attributes used to configure a line breakpoint
	 */
	protected static final String[] fgLineBreakpointAttributes= new String[]{IDebugConstants.MODEL_IDENTIFIER, IDebugConstants.ENABLED, IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END};

	/**
	 * Constructs a new breakpoint manager.
	 */
	public BreakpointManager() {
		fBreakpoints= new Vector(15);
	}

	/**
	 * Registers this manager as a resource change listener and launch
	 * listener. Loads the list of breakpoints from the breakpoint markers in the
	 * workspace.  This method should only be called on initial startup of 
	 * the debug plugin.
	 *
	 * @exception CoreException if an error occurrs retreiving breakpoint markers
	 */
	public void startup() throws CoreException {
		getWorkspace().addResourceChangeListener(this);
		getLaunchManager().addLaunchListener(this);
		IMarker[] breakpoints= null;
		IWorkspaceRoot root= getWorkspace().getRoot();
		breakpoints= root.findMarkers(IDebugConstants.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		
		for (int i = 0; i < breakpoints.length; i++) {
			IMarker marker= breakpoints[i];
			try {
				addBreakpoint(marker);
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
		getLaunchManager().removeLaunchListener(this);
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
	public IMarker[] getBreakpoints() {
		IMarker[] temp= new IMarker[fBreakpoints.size()];
		fBreakpoints.copyInto(temp);
		return temp;
	}

	/**
	 * @see IBreakpointManager
	 */
	public IMarker[] getBreakpoints(String modelIdentifier) {
		Vector temp= new Vector(fBreakpoints.size());
		if (!fBreakpoints.isEmpty()) {
			Iterator bps= fBreakpoints.iterator();
			while (bps.hasNext()) {
				IMarker bp= (IMarker) bps.next();
				String id= getModelIdentifier(bp);
				if (id != null && id.equals(modelIdentifier)) {
					temp.add(bp);
				}
			}
		}
		IMarker[] m= new IMarker[temp.size()];
		temp.copyInto(m);
		return m;
	}

	/**
	 * @see IBreakpointManager
	 */
	public int getLineNumber(IMarker breakpoint) {
		return breakpoint.getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/**
	 * @see IBreakpointManager
	 */
	public int getCharStart(IMarker breakpoint) {
		return breakpoint.getAttribute(IMarker.CHAR_START, -1);
	}

	/**
	 * @see IBreakpointManager
	 */
	public int getCharEnd(IMarker breakpoint) {
		return breakpoint.getAttribute(IMarker.CHAR_END, -1);
	}

	/**
	 * Returns the model identifier for the given breakpoint.
	 */
	public String getModelIdentifier(IMarker breakpoint) {
		return (String) breakpoint.getAttribute(IDebugConstants.MODEL_IDENTIFIER, (String)null);
	}

	/**
	 * @see IBreakpointManager
	 */
	public boolean isEnabled(IMarker marker) {
		return marker.getAttribute(IDebugConstants.ENABLED, true);
	}
	
	/**
	 * @see IBreakpointManager
	 */
	public boolean isRegistered(IMarker marker) {
		return fBreakpoints.contains(marker);
	}

	/**
	 * @see IBreakpointManager
	 */
	public void removeBreakpoint(IMarker breakpoint, boolean delete) throws CoreException {
		if (fBreakpoints.remove(breakpoint)) {
			fireUpdate(breakpoint, null, REMOVED);
			if (delete) {
				breakpoint.delete();
			}
		}
	}

	/**
	 * @see IBreakpointManager
	 */
	public void addBreakpoint(IMarker breakpoint) throws DebugException {
		if (!fBreakpoints.contains(breakpoint)) {
			verifyBreakpoint(breakpoint);
			fBreakpoints.add(breakpoint);
			fireUpdate(breakpoint, null, ADDED);
		}
	}

	/**
	 * Verifies that the breakpoint marker has the minimal required attributes,
	 * and throws a debug exception if not.
	 */
	protected void verifyBreakpoint(IMarker breakpoint) throws DebugException {
		try {
			String id= (String) breakpoint.getAttribute(IDebugConstants.MODEL_IDENTIFIER);
			if (id == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 
					IDebugStatusConstants.CONFIGURATION_INVALID, DebugCoreUtils.getResourceString(REQUIRED_ATTRIBUTES), null));
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
				IMarker breakpoint= (IMarker) breakpoints.nextElement();
				IResource breakpointResource= breakpoint.getResource();
				if (project.getFullPath().isPrefixOf(breakpointResource.getFullPath())) {
					try {
						removeBreakpoint(breakpoint, false);
					} catch (CoreException e) {
						logError(e);
					}
				}
			}
			return;
		} else {
			IMarker[] breakpoints= null;
			try {
				breakpoints= project.findMarkers(IDebugConstants.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				logError(e);
				return;
			}
			if (breakpoints != null) {
				for (int i= 0; i < breakpoints.length; i++) {
					try {
						addBreakpoint(breakpoints[i]);
					} catch (DebugException e) {
						logError(e);
					}
				}
			}
		}
	}

	/**
	 * @see IBreakpointManager.
	 */
	public void setEnabled(IMarker marker, boolean enabled) throws CoreException {
		marker.setAttribute(IDebugConstants.ENABLED, enabled);
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
				final IWorkspace workspace= getWorkspace();
				final IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) {
						try {
							marker.delete();
						} catch (CoreException ce) {
							logError(ce);
						}
					}
				};
					Runnable runnable= new Runnable() {
						public void run() {
							try {
								workspace.run(wRunnable, null);
							} catch (CoreException ce) {
								logError(ce);
							}
						}
					};
					new Thread(runnable).start();
			} else {
				// do nothing - we do not add until explicitly added
			}
		}

		/**
		 * Wrapper for handling removes
		 */
		protected void handleRemoveBreakpoint(IMarker marker, IMarkerDelta delta) {
			if (isRegistered(marker)) {
				fBreakpoints.remove(marker);
				fireUpdate(marker, delta, REMOVED);
			}
		}

		/**
		 * Wrapper for handling changes
		 */
		protected void handleChangeBreakpoint(IMarker marker, IMarkerDelta delta) {
			if (isRegistered(marker)) {
				fireUpdate(marker, delta, CHANGED);
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
	protected void fireUpdate(IMarker marker, IMarkerDelta delta, int update) {
		Object[] copiedListeners= fBreakpointListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			IBreakpointListener listener = (IBreakpointListener)copiedListeners[i];
			if (supportsBreakpoint(listener, marker)) {
				switch (update) {
					case ADDED:
						listener.breakpointAdded(marker);
						break;
					case REMOVED:
						listener.breakpointRemoved(marker, delta);
						break;
					case CHANGED:
						listener.breakpointChanged(marker, delta);		
						break;
				}
				
			}
		}
	}
	
	/**
	 * Returns whether the given listener supports breakpoints. If
	 * the listener is a debug target, we check if the specific
	 * breakpoint is supported by the target.
	 */
	protected boolean supportsBreakpoint(IBreakpointListener listener, IMarker breakpoint) {
		if (listener instanceof IBreakpointSupport) {
			return ((IBreakpointSupport)listener).supportsBreakpoint(breakpoint);
		} else {
			return true;
		}
	}

	/**
	 * @see IBreakpointManager
	 */
	public void configureBreakpoint(IMarker breakpoint, String modelIdentifier, boolean enabled) throws CoreException {
		breakpoint.setAttributes(fgBreakpointAttributes, new Object[]{modelIdentifier, new Boolean(enabled)});
	}

	/**
	 * @see IBreakpointManager
	 */
	public void configureLineBreakpoint(IMarker breakpoint, String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) throws CoreException {
		Object[] values= new Object[]{modelIdentifier, new Boolean(enabled), new Integer(lineNumber), new Integer(charStart), new Integer(charEnd)};
		breakpoint.setAttributes(fgLineBreakpointAttributes, values);
	}

	/**
	 * Adds any debug targets as listeners
	 *
	 * @see ILaunchListener
	 */
	public void launchRegistered(ILaunch launch) {
		IDebugTarget dt= launch.getDebugTarget();
		if (dt != null) {
			addBreakpointListener(dt);
		}
	}

	/**
	 * Removes the debug target associated with
	 * this launch as a breakpoint listener.
	 *
	 * @see ILaunchListener
	 */
	public void launchDeregistered(ILaunch launch) {
		IDebugTarget dt= launch.getDebugTarget();
		if (dt != null) {
			removeBreakpointListener(dt);
		}
	}
}

