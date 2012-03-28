/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IBreakpointImportParticipant;

import com.ibm.icu.text.MessageFormat;

/**
 * The breakpoint manager manages all registered breakpoints for the Debug plug-in. It is
 * instantiated by the Debug plug-in at startup.
 * <p>
 * <strong>Note:</strong> This manager is created while the Debug plug-in is started, but it
 * will not automatically be initialized. Client code that expects markers and breakpoints to be
 * initialized must call {@link #ensureInitialized()}.
 * </p>
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
	 * A collection of breakpoints registered with this manager.
	 */
	private Vector fBreakpoints= null;
	
	/**
	 * Map of breakpoint import participants.
	 * Map has the form:
	 * <pre>Map(String - marker_id, List of {@link IBreakpointImportParticipant})</pre>
	 */
	private HashMap fImportParticipants = null;
	
	/**
	 * A system default import participant that performs legacy comparison support
	 * when no participants are provided for a given type.
	 * 
	 * @since 3.5
	 */
	private IBreakpointImportParticipant fDefaultParticipant = null;
	
	/**
	 * A collection of breakpoint markers that have received a POST_CHANGE notification
	 * that they have changed before a POST_BUILD notification of add. This allows us
	 * to tell if a marker has been created & changed since the breakpoint has been
	 * registered (see bug 138473).
	 */
	private Set fPostChangMarkersChanged = new HashSet();
	
	/**
	 * A collection of breakpoint markers that have received a POST_BUILD notification
	 * of being added.
	 */
	private Set fPostBuildMarkersAdded = new HashSet();
	
	/**
	 * Collection of breakpoints being added currently. Used to 
	 * suppress change notification of "REGISTERED" attribute when
	 * being added.
	 */
	private List fSuppressChange = new ArrayList();
	
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
	private ListenerList fBreakpointListeners= new ListenerList();
		
	/**
	 * Collection of (plural) breakpoint listeners.
	 */
	private ListenerList fBreakpointsListeners= new ListenerList();	
	
	/**
	 * Singleton resource delta visitor which handles marker
	 * additions, changes, and removals.
	 */
	private static BreakpointManagerVisitor fgVisitor;
	
	/**
	 * Collection of breakpoint manager listeners which are
	 * notified when this manager's state changes.
	 */
	private ListenerList fBreakpointManagerListeners= new ListenerList();

	/**
	 * Listens to POST_CHANGE notifications of breakpoint markers to detect when
	 * a breakpoint is added & changed before the POST_BUILD add notification is
	 * sent.
	 */
	class PostChangeListener implements IResourceChangeListener {
		
		private PostChangeVisitor fVisitor = new PostChangeVisitor();

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta= event.getDelta();
			if (delta != null) {
				try {
					delta.accept(fVisitor);
				} catch (CoreException ce) {
					DebugPlugin.log(ce);
				}
			}
		}
		
	}
	
	/**
	 * Default implementation of a breakpoint import participant
	 * 
	 * @since 3.5
	 */
	class DefaultImportParticipant implements IBreakpointImportParticipant {

		public boolean matches(Map attributes, IBreakpoint breakpoint) throws CoreException {
			//perform legacy comparison
			IMarker marker = breakpoint.getMarker();
			String type = (String) attributes.get("type"); //$NON-NLS-1$
			Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
			Object localline = marker.getAttribute(IMarker.LINE_NUMBER);
			String localtype = marker.getType();
			if (type.equals(localtype)) {
				if(line != null && line.equals(localline)) {
					return true;
				}
				else if(line == null) {
					return true;
				}
			}
			return false;
		}
		public void verify(IBreakpoint breakpoint) throws CoreException {}
	}
	
	/**
	 * The listener
	 */
	private PostChangeListener fPostChangeListener = new PostChangeListener();
	
	class PostChangeVisitor implements IResourceDeltaVisitor {

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta == null) {
				return false;
			}
			IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
			for (int i= 0; i < markerDeltas.length; i++) {
				IMarkerDelta markerDelta= markerDeltas[i];
				if (markerDelta.isSubtypeOf(IBreakpoint.BREAKPOINT_MARKER)) {
					switch (markerDelta.getKind()) {
						case IResourceDelta.ADDED :
							break;
						case IResourceDelta.REMOVED :
							break;
						case IResourceDelta.CHANGED :
							IMarker marker = markerDelta.getMarker();
							synchronized (fPostChangMarkersChanged) {
								if (!fPostBuildMarkersAdded.contains(marker)) {
									fPostChangMarkersChanged.add(marker);
								}
							}
							break;
					}
				}
			}
			return true;
		}
		
	}
	
	/**
	 * Constructs a new breakpoint manager.
	 */
	public BreakpointManager() {
		fMarkersToBreakpoints= new HashMap(10);	
		fBreakpointExtensions= new HashMap(15);
	}
	
	/**
	 * Loads all the breakpoints on the given resource.
	 * 
	 * @param resource the resource which contains the breakpoints
	 * @param notify whether to notify of the breakpoint additions
	 * @throws CoreException if a problem is encountered
	 */
	private void loadBreakpoints(IResource resource, boolean notify) throws CoreException {
		initBreakpointExtensions();
		IMarker[] markers= getPersistedMarkers(resource);
		List added = new ArrayList();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker= markers[i];
			try {
				IBreakpoint breakpoint = createBreakpoint(marker);
				synchronized (fPostChangMarkersChanged) {
					fPostBuildMarkersAdded.add(marker);
				}
				if (breakpoint.isRegistered()) {
					added.add(breakpoint);
				}
			} catch (DebugException e) {
				DebugPlugin.log(e);
			}
		}
		addBreakpoints((IBreakpoint[])added.toArray(new IBreakpoint[added.size()]), notify);
	}
	
	/**
	 * Returns the persisted markers associated with the given resource.
	 * 
	 * Delete any invalid breakpoint markers. This is done at startup rather
	 * than shutdown, since the changes made at shutdown are not persisted as
	 * the workspace state has already been saved. See bug 7683.
	 * 
	 * Since the <code>TRANSIENT</code> marker attribute/feature has been added,
	 * we no longer have to manually delete non-persisted markers - the platform
	 * does this for us (at shutdown, transient markers are not saved). However,
	 * the code is still present to delete non-persisted markers from old
	 * workspaces.
	 * @param resource the {@link IResource} to get markers for
	 * @return the complete listing of persisted markers for the given {@link IResource}
	 * @throws CoreException if a problem is encountered
	 */
	protected IMarker[] getPersistedMarkers(IResource resource) throws CoreException {
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		final List delete = new ArrayList();
		List persisted= new ArrayList();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker= markers[i];
			// ensure the marker has a valid model identifier attribute
			// and delete the breakpoint if not
			String modelId = marker.getAttribute(IBreakpoint.ID, null);
			if (modelId == null) {
				// marker with old/invalid format - delete
				delete.add(marker);
			} else if (!marker.getAttribute(IBreakpoint.PERSISTED, true)) {
				// the breakpoint is marked as not to be persisted,
				// schedule for deletion
				delete.add(marker);
			} else {
				persisted.add(marker);
			}
		}
		// delete any markers that are not to be restored
		if (!delete.isEmpty()) {
            final IMarker[] delMarkers = (IMarker[])delete.toArray(new IMarker[delete.size()]);
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
                    for (int i = 0; i < delMarkers.length; i++) {
                        IMarker marker = delMarkers[i];
                        marker.delete();
                    }
				}
			};
			new BreakpointManagerJob(wr).schedule();
		}
		return (IMarker[])persisted.toArray(new IMarker[persisted.size()]);
	}
	
	/**
	 * Removes this manager as a resource change listener
	 * and removes all breakpoint listeners.
	 */
	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
		getWorkspace().removeResourceChangeListener(fPostChangeListener);
		fBreakpointListeners.clear();
        fBreakpointsListeners.clear();
        fBreakpointManagerListeners.clear();
        if(fImportParticipants != null) {
        	fImportParticipants.clear();
        	fImportParticipants = null;
        	fDefaultParticipant = null;
        }
        if(fBreakpoints != null) {
        	fBreakpoints.clear();
        	fBreakpoints = null;
        }
        if(fMarkersToBreakpoints != null) {
        	fMarkersToBreakpoints.clear();
        }
	}

	/**
	 * Find the defined breakpoint extensions and cache them for use in recreating
	 * breakpoints from markers.
	 */
	private void initBreakpointExtensions() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), DebugPlugin.EXTENSION_POINT_BREAKPOINTS);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			String markerType = elements[i].getAttribute(IConfigurationElementConstants.MARKER_TYPE);
			String className = elements[i].getAttribute(IConfigurationElementConstants.CLASS);
			if (markerType == null) {
				DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "Breakpoint extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() + " missing required attribute: markerType", null)); //$NON-NLS-1$ //$NON-NLS-2$ 
			} else if (className == null){
				DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "Breakpoint extension " + elements[i].getDeclaringExtension().getUniqueIdentifier() + " missing required attribute: class", null)); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				fBreakpointExtensions.put(markerType, elements[i]);
			}
		}
	}

	/**
	 * Convenience method to get the workspace
	 * @return the default {@link IWorkspace}
	 */
	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * @see IBreakpointManager#getBreakpoint(IMarker)
	 */
	public IBreakpoint getBreakpoint(IMarker marker) {
		// ensure that breakpoints are initialized
		getBreakpoints0();
		return (IBreakpoint)fMarkersToBreakpoints.get(marker);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		IBreakpoint[] temp = new IBreakpoint[0];
		Vector breakpoints = getBreakpoints0();
		synchronized (breakpoints) {
			temp = new IBreakpoint[breakpoints.size()];
			breakpoints.copyInto(temp);
		}
		return temp;
	}

	/**
	 * Ensures that this manager is initialized.
	 * <p>
	 * This manager is created while the Debug plug-in is started, but it will not automatically
	 * be initialized. Client code that expects markers and breakpoints to be initialized must call
	 * this method.
	 * </p>
	 * 
	 * @since 3.8
	 */
	public void ensureInitialized() {
		getBreakpoints0();
	}

	/**
	 * The BreakpointManager waits to load the breakpoints
	 * of the workspace until a request is made to retrieve the 
	 * breakpoints.
	 * @return the underlying {@link Vector} of breakpoints
	 */
	private synchronized Vector getBreakpoints0() {
		if (fBreakpoints == null) {
			initializeBreakpoints();
		}
		return fBreakpoints;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#getBreakpoints(java.lang.String)
	 */
	public IBreakpoint[] getBreakpoints(String modelIdentifier) {
		Vector allBreakpoints= getBreakpoints0();
		synchronized (allBreakpoints) {
			ArrayList temp = new ArrayList(allBreakpoints.size());
			Iterator breakpoints= allBreakpoints.iterator();
			while (breakpoints.hasNext()) {
				IBreakpoint breakpoint= (IBreakpoint) breakpoints.next();
				String id= breakpoint.getModelIdentifier();
				if (id != null && id.equals(modelIdentifier)) {
					temp.add(breakpoint);
				}
			}
			return (IBreakpoint[]) temp.toArray(new IBreakpoint[temp.size()]);
		}
	}

	/**
	 * Loads the list of breakpoints from the breakpoint markers in the
	 * workspace. Start listening to resource deltas.
	 */
	private void initializeBreakpoints() {
		setBreakpoints(new Vector(10));
		try {
			loadBreakpoints(getWorkspace().getRoot(), false);
			getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
			getWorkspace().addResourceChangeListener(fPostChangeListener, IResourceChangeEvent.POST_CHANGE);
		} catch (CoreException ce) {
			DebugPlugin.log(ce);
			setBreakpoints(new Vector(0));
		} 
	}
	
	/**
	 * @see IBreakpointManager#isRegistered(IBreakpoint)
	 */
	public boolean isRegistered(IBreakpoint breakpoint) {
		return getBreakpoints0().contains(breakpoint);
	}

	
	/**
	 * @see IBreakpointManager#removeBreakpoint(IBreakpoint, boolean)
	 */
	public void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException {
		removeBreakpoints(new IBreakpoint[]{breakpoint}, delete);
	}
	
	/**
	 * @see IBreakpointManager#removeBreakpoints(IBreakpoint[], boolean)
	 */
	public void removeBreakpoints(IBreakpoint[] breakpoints, final boolean delete) throws CoreException {
		final List remove = new ArrayList(breakpoints.length);
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (getBreakpoints0().contains(breakpoint)) {
				remove.add(breakpoint);
			}
		}
		if (!remove.isEmpty()) {
			Iterator iter = remove.iterator();
			while (iter.hasNext()) {
				IBreakpoint breakpoint = (IBreakpoint)iter.next();
				getBreakpoints0().remove(breakpoint);
				fMarkersToBreakpoints.remove(breakpoint.getMarker());
			}
			fireUpdate(remove, null, REMOVED);
			IWorkspaceRunnable r = new IWorkspaceRunnable() {
				public void run(IProgressMonitor montitor) throws CoreException {
					Iterator innerIter = remove.iterator();
					while (innerIter.hasNext()) {
						IBreakpoint breakpoint = (IBreakpoint)innerIter.next();
						if (delete) {
							breakpoint.delete();
						} else {
							// if the breakpoint is being removed from the manager
							// because the project is closing, the breakpoint should
							// remain as registered, otherwise, the breakpoint should
							// be marked as unregistered
							IMarker marker = breakpoint.getMarker();
							if (marker.exists()) {
								IProject project = breakpoint.getMarker().getResource().getProject();
								if (project == null || project.isOpen()) {
									breakpoint.setRegistered(false);
								}
							}
						}
					}					
				}
			};
			getWorkspace().run(r, null, 0, null);
		}
	}	
	
	/**
	 * Create a breakpoint for the given marker. The created breakpoint
	 * is of the type specified in the breakpoint extension associated
	 * with the given marker type.
	 * 
	 * @param marker marker to create a breakpoint for
	 * @return a breakpoint on this marker
	 * @exception DebugException if breakpoint creation fails. Reasons for 
	 *  failure include:
	 * <ol>
	 * <li>The breakpoint manager cannot determine what kind of breakpoint
	 *     to instantiate for the given marker type</li>
	 * <li>A lower level exception occurred while accessing the given marker</li>
	 * </ol>
	 */
	public IBreakpoint createBreakpoint(IMarker marker) throws DebugException {
		IBreakpoint breakpoint= (IBreakpoint) fMarkersToBreakpoints.get(marker);
		if (breakpoint != null) {
			return breakpoint;
		}
		try {
			IConfigurationElement config = (IConfigurationElement)fBreakpointExtensions.get(marker.getType());
			if (config == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
					DebugException.CONFIGURATION_INVALID, MessageFormat.format(DebugCoreMessages.BreakpointManager_Missing_breakpoint_definition, new String[] {marker.getType()}), null)); 
			}
			Object object = config.createExecutableExtension(IConfigurationElementConstants.CLASS);
			if (object instanceof IBreakpoint) {
				breakpoint = (IBreakpoint)object;
				breakpoint.setMarker(marker);
			} else {
				DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "Breakpoint extension " + config.getDeclaringExtension().getUniqueIdentifier() + " missing required attribute: class", null)); //$NON-NLS-1$ //$NON-NLS-2$ 
			}
			return breakpoint;		
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}	

	/**
	 * @see IBreakpointManager#addBreakpoint(IBreakpoint)
	 */
	public void addBreakpoint(IBreakpoint breakpoint) throws CoreException {
		addBreakpoints(new IBreakpoint[]{breakpoint});
	}
	
	/**
	 * @see IBreakpointManager#addBreakpoints(IBreakpoint[])
	 */
	public void addBreakpoints(IBreakpoint[] breakpoints) throws CoreException {
	    addBreakpoints(breakpoints, true);
	}	
	
	/**
	 * Registers the given breakpoints and notifies listeners if specified.
	 * 
	 * @param breakpoints the breakpoints to register
	 * @param notify whether to notify listeners of the add
	 * @throws CoreException if a problem is encountered
	 */
	private void addBreakpoints(IBreakpoint[] breakpoints, boolean notify) throws CoreException {
		List added = new ArrayList(breakpoints.length);
		final List update = new ArrayList();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			if (!getBreakpoints0().contains(breakpoint)) {
				verifyBreakpoint(breakpoint);
				if (breakpoint.isRegistered()) {
				    // If notify == false, the breakpoints are just being added at startup
					added.add(breakpoint);
					getBreakpoints0().add(breakpoint);
					fMarkersToBreakpoints.put(breakpoint.getMarker(), breakpoint);
				} else {
					// need to update the 'registered' and/or 'group' attributes
					update.add(breakpoint);
				}
			}	
		}
		if (notify) {
		    fireUpdate(added, null, ADDED);
		}
		if (!update.isEmpty()) {
			IWorkspaceRunnable r = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					Iterator iter = update.iterator();
					while (iter.hasNext()) {
						IBreakpoint breakpoint = (IBreakpoint)iter.next();
						getBreakpoints0().add(breakpoint);
						breakpoint.setRegistered(true);
						fMarkersToBreakpoints.put(breakpoint.getMarker(), breakpoint);						
					}
				}
			};
			// Need to suppress change notification, since this is really
			// an add notification			
			fSuppressChange.addAll(update);
			getWorkspace().run(r, null, 0, null);
			fSuppressChange.removeAll(update);
			if (notify) {
			    fireUpdate(update, null, ADDED);
			}
		}			
	}	
	
	/**
	 * Returns whether change notification is to be suppressed for the given breakpoint.
	 * Used when adding breakpoints and changing the "REGISTERED" attribute.
	 * 
	 * @param breakpoint the breakpoint
	 * @return boolean whether change notification is suppressed
	 */
	protected boolean isChangeSuppressed(IBreakpoint breakpoint) {
		return fSuppressChange.contains(breakpoint);
	}
	
	/**
	 * @see IBreakpointManager#fireBreakpointChanged(IBreakpoint)
	 */
	public void fireBreakpointChanged(IBreakpoint breakpoint) {
		if (getBreakpoints0().contains(breakpoint)) {
			List changed = new ArrayList();
			changed.add(breakpoint);
			fireUpdate(changed, null, CHANGED);
		}
	}

	/**
	 * Verifies that the breakpoint marker has the minimal required attributes,
	 * and throws a debug exception if not.
	 * @param breakpoint the {@link IBreakpoint} to verify
	 * @throws DebugException if a problem is encountered
	 */
	private void verifyBreakpoint(IBreakpoint breakpoint) throws DebugException {
		try {
			String id= breakpoint.getModelIdentifier();
			if (id == null) {
				throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), 
					DebugException.CONFIGURATION_INVALID, DebugCoreMessages.BreakpointManager_Missing_model_identifier, null)); 
			}
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}

	/**
	 * A resource has changed. Traverses the delta for breakpoint changes.
	 * 
	 * @param event resource change event
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fgVisitor == null) {
					fgVisitor= new BreakpointManagerVisitor();
				}
				delta.accept(fgVisitor);
				fgVisitor.update();
			} catch (CoreException ce) {
				DebugPlugin.log(ce);
			}
		}
	}

	/**
	 * Visitor for handling resource deltas
	 */
	class BreakpointManagerVisitor implements IResourceDeltaVisitor {
		/**
		 * Moved markers
		 */
		private List fMoved = new ArrayList();
		/**
		 * Removed breakpoints
		 */
		private List fRemoved = new ArrayList();
		
		/**
		 * Added breakpoints.
		 * @since 3.7
		 */
		private List fAdded= new ArrayList();

		/**
		 * Changed breakpoints and associated marker deltas
		 */
		private List fChanged = new ArrayList();
		private List fChangedDeltas = new ArrayList();
		
		/**
		 * Resets the visitor for a delta traversal - empties
		 * collections of removed/changed breakpoints.
		 */
		protected void reset() {
			fMoved.clear();
			fRemoved.clear();
			fAdded.clear();
			fChanged.clear();
			fChangedDeltas.clear();
		}
		
		/**
		 * Performs updates on accumulated changes, and fires change notification after
		 * a traversal. Accumulated updates are reset.
		 */
		public void update() {
			if (!fMoved.isEmpty()) {
				// delete moved markers
				IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
                        IMarker[] markers = (IMarker[])fMoved.toArray(new IMarker[fMoved.size()]);
						for (int i = 0; i < markers.length; i++) {
                            markers[i].delete();
                        }
					}
				};
				try {
					getWorkspace().run(wRunnable, null, 0, null);
				} catch (CoreException e) {
				}
			}
			if (!fRemoved.isEmpty()) {
				try {
					removeBreakpoints((IBreakpoint[])fRemoved.toArray(new IBreakpoint[fRemoved.size()]), false);
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
			if (!fAdded.isEmpty()) {
				try {
					IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							Iterator iter= fAdded.iterator();
							while (iter.hasNext()) {
								IBreakpoint breakpoint= (IBreakpoint)iter.next();
								breakpoint.getMarker().setAttribute(DebugPlugin.ATTR_BREAKPOINT_IS_DELETED, false);
								breakpoint.setRegistered(true);
							}
						}
					};
					getWorkspace().run(runnable, null, 0, null);
					addBreakpoints((IBreakpoint[])fAdded.toArray(new IBreakpoint[fAdded.size()]), false);
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
			if (!fChanged.isEmpty()) {
				fireUpdate(fChanged, fChangedDeltas, CHANGED);
			}
			reset();
		} 
				
		/**
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			if (0 != (delta.getFlags() & IResourceDelta.OPEN) && 0 == (delta.getFlags() & IResourceDelta.MOVED_FROM)) {
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
							handleRemoveBreakpoint(markerDelta.getMarker());
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
		 * @param rDelta the {@link IResourceDelta}
		 * @param marker the new {@link IMarker}
		 * @param mDelta the accompanying {@link IMarkerDelta}
		 */
		protected void handleAddBreakpoint(IResourceDelta rDelta, IMarker marker, IMarkerDelta mDelta) {
			if (0 != (rDelta.getFlags() & IResourceDelta.MOVED_FROM)) {
				// This breakpoint has actually been moved - already removed
				// from the Breakpoint manager during the remove callback.
				// Schedule the marker associated with the new resource for deletion.
				if (getBreakpoint(marker) == null) {
					fMoved.add(marker);
				}
			} else {
				// check if the an add & change have be combined into one add notification 
				synchronized (fPostChangMarkersChanged) {
					if (fPostChangMarkersChanged.contains(marker)) {
						handleChangeBreakpoint(marker, mDelta);
						fPostChangMarkersChanged.remove(marker);
					} else if (marker.getAttribute(DebugPlugin.ATTR_BREAKPOINT_IS_DELETED, false) && getBreakpoint(marker) == null) {
						try {
							fAdded.add(createBreakpoint(marker));
						} catch (CoreException e) {
							DebugPlugin.log(e);
						}
					}
					fPostBuildMarkersAdded.add(marker);
				}
			}
		}
		
		/**
		 * Wrapper for handling removes
		 * @param marker the {@link IMarker}
		 */
		protected void handleRemoveBreakpoint(IMarker marker) {
			synchronized (fPostChangMarkersChanged) {
				fPostChangMarkersChanged.remove(marker);
				fPostBuildMarkersAdded.remove(marker);
			}
			IBreakpoint breakpoint= getBreakpoint(marker);
			if (breakpoint != null) {
				fRemoved.add(breakpoint);
			}
		}

		/**
		 * Wrapper for handling changes
		 * @param marker the {@link IMarker} that was changed
		 * @param delta the {@link IMarkerDelta}
		 */
		protected void handleChangeBreakpoint(IMarker marker, IMarkerDelta delta) {
			IBreakpoint breakpoint= getBreakpoint(marker);
			if (breakpoint != null && isRegistered(breakpoint) && !isChangeSuppressed(breakpoint)) {
				fChanged.add(breakpoint);
				fChangedDeltas.add(delta);
			}
		}
		
		/**
		 * A project has been opened or closed.  Updates the breakpoints for
		 * that project
		 * @param project the {@link IProject} that was changed
		 */
		private void handleProjectResourceOpenStateChange(final IResource project) {
			if (!project.isAccessible()) {
				//closed
				Enumeration breakpoints= ((Vector)getBreakpoints0().clone()).elements();
				while (breakpoints.hasMoreElements()) {
					IBreakpoint breakpoint= (IBreakpoint) breakpoints.nextElement();
					IResource markerResource= breakpoint.getMarker().getResource();
					if (project.getFullPath().isPrefixOf(markerResource.getFullPath())) {
						fRemoved.add(breakpoint);
					}
				}
				return;
			} 
			try {
				loadBreakpoints(project, true);
			} catch (CoreException e) {
				DebugPlugin.log(e);
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
	 * Notifies listeners of the adds/removes/changes
	 * 
	 * @param breakpoints associated breakpoints
	 * @param deltas or <code>null</code>
	 * @param update type of change
	 */
	private void fireUpdate(List breakpoints, List deltas, int update) {
		if (breakpoints.isEmpty()) {
			return; 
		}
		IBreakpoint[] bpArray = (IBreakpoint[])breakpoints.toArray(new IBreakpoint[breakpoints.size()]);
		IMarkerDelta[] deltaArray = new IMarkerDelta[bpArray.length];
		if (deltas != null) {
			deltaArray = (IMarkerDelta[])deltas.toArray(deltaArray);
		}
		// single listeners
		getBreakpointNotifier().notify(bpArray, deltaArray, update);
		
		// plural listeners
		getBreakpointsNotifier().notify(bpArray, deltaArray, update);
	}	

	protected void setBreakpoints(Vector breakpoints) {
		fBreakpoints = breakpoints;
	}
	
	/**
	 * @see IBreakpointManager#hasBreakpoints()
	 */
	public boolean hasBreakpoints() {
		return !getBreakpoints0().isEmpty();
	}
	
	/**
	 * @see org.eclipse.debug.core.IBreakpointManager#addBreakpointListener(org.eclipse.debug.core.IBreakpointsListener)
	 */
	public void addBreakpointListener(IBreakpointsListener listener) {
		fBreakpointsListeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.IBreakpointManager#removeBreakpointListener(org.eclipse.debug.core.IBreakpointsListener)
	 */
	public void removeBreakpointListener(IBreakpointsListener listener) {
		fBreakpointsListeners.remove(listener);
	}

	private BreakpointNotifier getBreakpointNotifier() {
		return new BreakpointNotifier();
	}
	
	/**
	 * Notifies breakpoint listener (single breakpoint) in a safe runnable to
	 * handle exceptions.
	 */
	class BreakpointNotifier implements ISafeRunnable {
		
		private IBreakpointListener fListener;
		private int fType;
		private IMarkerDelta fDelta;
		private IBreakpoint fBreakpoint;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during breakpoint change notification.", exception);  //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.breakpointAdded(fBreakpoint);
					break;
				case REMOVED:
					fListener.breakpointRemoved(fBreakpoint, fDelta);
					break;
				case CHANGED:
					fListener.breakpointChanged(fBreakpoint, fDelta);		
					break;
			}			
		}

		/**
		 * Notifies the listeners of the add/change/remove
		 * 
		 * @param breakpoints the breakpoints that changed
		 * @param deltas the deltas associated with the change
		 * @param update the type of change
		 */
		public void notify(IBreakpoint[] breakpoints, IMarkerDelta[] deltas, int update) {
			fType = update;
			Object[] copiedListeners= fBreakpointListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IBreakpointListener)copiedListeners[i];
				for (int j = 0; j < breakpoints.length; j++) {
					fBreakpoint = breakpoints[j];
					fDelta = deltas[j];
                    SafeRunner.run(this);				
				}
			}
			fListener = null;
			fDelta = null;
			fBreakpoint = null;			
		}
	}
	
	private BreakpointsNotifier getBreakpointsNotifier() {
		return new BreakpointsNotifier();
	}
	
	/**
	 * Notifies breakpoint listener (multiple breakpoints) in a safe runnable to
	 * handle exceptions.
	 */
	class BreakpointsNotifier implements ISafeRunnable {
		
		private IBreakpointsListener fListener;
		private int fType;
		private IMarkerDelta[] fDeltas;
		private IBreakpoint[] fNotifierBreakpoints;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during breakpoint change notification.", exception);  //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.breakpointsAdded(fNotifierBreakpoints);
					break;
				case REMOVED:
					fListener.breakpointsRemoved(fNotifierBreakpoints, fDeltas);
					break;
				case CHANGED:
					fListener.breakpointsChanged(fNotifierBreakpoints, fDeltas);		
					break;
			}			
		}

		/**
		 * Notifies the listeners of the adds/changes/removes
		 * 
		 * @param breakpoints the breakpoints that changed
		 * @param deltas the deltas associated with the changed breakpoints
		 * @param update the type of change
		 */
		public void notify(IBreakpoint[] breakpoints, IMarkerDelta[] deltas, int update) {
			fType = update;
			fNotifierBreakpoints = breakpoints;
			fDeltas = deltas;
			Object[] copiedListeners = fBreakpointsListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IBreakpointsListener)copiedListeners[i];
                SafeRunner.run(this);
			}
			fDeltas = null;
			fNotifierBreakpoints = null;
			fListener = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#isEnabled()
	 */
	public boolean isEnabled() {
		return Platform.getPreferencesService().getBoolean(DebugPlugin.getUniqueIdentifier(), IInternalDebugCoreConstants.PREF_BREAKPOINT_MANAGER_ENABLED_STATE, true, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#setEnabled(boolean)
	 */
	public void setEnabled(final boolean enabled) {
        if (isEnabled() != enabled) {
        	Preferences.setBoolean(DebugPlugin.getUniqueIdentifier(), IInternalDebugCoreConstants.PREF_BREAKPOINT_MANAGER_ENABLED_STATE, enabled, null);
            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    IBreakpoint[] breakpoints = getBreakpoints();
                    for (int i = 0; i < breakpoints.length; i++) {
                        IBreakpoint breakpoint = breakpoints[i];
                        // Touch the marker (but don't actually change anything) so that the icon in
                        // the editor ruler will be updated (editors listen to marker changes). 
                        breakpoint.getMarker().setAttribute(IBreakpoint.ENABLED, breakpoint.isEnabled());
                    }
                }
            };
            try {
                ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE ,null);
            } catch (CoreException e) {
                DebugPlugin.log(e);
            }
    		new BreakpointManagerNotifier().notify(enabled);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#addBreakpointManagerListener(org.eclipse.debug.core.IBreakpointManagerListener)
	 */
	public void addBreakpointManagerListener(IBreakpointManagerListener listener) {
		fBreakpointManagerListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#removeBreakpointManagerListener(org.eclipse.debug.core.IBreakpointManagerListener)
	 */
	public void removeBreakpointManagerListener(IBreakpointManagerListener listener) {
		fBreakpointManagerListeners.remove(listener);
	}	
	
	/**
	 * Notifies breakpoint manager listeners in a safe runnable to
	 * handle exceptions.
	 */
	class BreakpointManagerNotifier implements ISafeRunnable {
		
		private IBreakpointManagerListener fListener;
		private boolean fManagerEnabled;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during breakpoint change notification.", exception);  //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fListener.breakpointManagerEnablementChanged(fManagerEnabled);
		}

		/**
		 * Notifies the listeners of the enabled state change
		 * 
		 * @param enabled whether the manager is enabled
		 */
		public void notify(boolean enabled) {
			fManagerEnabled= enabled;
			Object[] copiedListeners = fBreakpointManagerListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IBreakpointManagerListener)copiedListeners[i];
                SafeRunner.run(this);
			}
			fListener = null;
		}
	}
	
	class BreakpointManagerJob extends Job {
		
		private final IWorkspaceRunnable fRunnable;

		public BreakpointManagerJob (IWorkspaceRunnable wRunnable) {
			super("breakpoint manager job"); //$NON-NLS-1$
			fRunnable= wRunnable;
			setSystem(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			try {
				getWorkspace().run(fRunnable, null, 0, null);
			} catch (CoreException ce) {
				DebugPlugin.log(ce);
			}
			return new Status(IStatus.OK, DebugPlugin.getUniqueIdentifier(), IStatus.OK, "", null); //$NON-NLS-1$
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointManager#getTypeName(org.eclipse.debug.core.model.IBreakpoint)
     */
    public String getTypeName(IBreakpoint breakpoint) {
        String typeName= null;
        IMarker marker = breakpoint.getMarker();
        if (marker != null) {
            try {
                IConfigurationElement element = (IConfigurationElement) fBreakpointExtensions.get(marker.getType());
                if (element != null) {
                    typeName= element.getAttribute(IConfigurationElementConstants.NAME);
                }
            } 
            catch (CoreException e) {}
        }
        return typeName;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManager#getImportParticipants(java.lang.String)
	 */
	public IBreakpointImportParticipant[] getImportParticipants(String markertype) throws CoreException {
		initializeImportParticipants();
		ArrayList list = (ArrayList) fImportParticipants.get(markertype);
		if(list == null) {
			return new IBreakpointImportParticipant[] {fDefaultParticipant};
		}
		IBreakpointImportParticipant[] participants = new IBreakpointImportParticipant[list.size()];
		BreakpointImportParticipantDelegate delegate = null;
		for(int i = 0; i < list.size(); i++) {
			delegate = (BreakpointImportParticipantDelegate) list.get(i);
			participants[i] = delegate.getDelegate();
		}
		if(participants.length == 0) {
			return new IBreakpointImportParticipant[] {fDefaultParticipant};
		}
		return participants; 
	}
	
	/**
	 * Initializes the cache of breakpoint import participants. Does no work if the cache 
	 * has already been initialized
	 */
	private synchronized void initializeImportParticipants() {
		if(fImportParticipants == null) {
			fImportParticipants = new HashMap();
			fDefaultParticipant = new DefaultImportParticipant();
			IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), DebugPlugin.EXTENSION_POINT_BREAKPOINT_IMPORT_PARTICIPANTS);
			IConfigurationElement[] elements = ep.getConfigurationElements();
			String type = null;
			ArrayList list = null;
			for(int i = 0; i < elements.length; i++) {
				type = elements[i].getAttribute(IConfigurationElementConstants.TYPE);
				if(type != null) {
					list = (ArrayList) fImportParticipants.get(type);
					if(list == null) {
						list = new ArrayList();
						fImportParticipants.put(type, list);
					}
					list.add(new BreakpointImportParticipantDelegate(elements[i]));
				}
			}
		}
	}
}

