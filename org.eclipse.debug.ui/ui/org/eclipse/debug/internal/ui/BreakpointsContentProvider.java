package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.*;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Provides the contents for a breakpoints viewer
 */
public class BreakpointsContentProvider extends BasicContentProvider implements IBreakpointListener {

	/**
	 * Creates this content provider
	 */
	public BreakpointsContentProvider() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	/**
	 * Returns all the breakpoint markers in the current open workspace
	 */
	public Object[] getElements(Object parent) {
		return ((IBreakpointManager) parent).getBreakpoints();
	}
	
	/**
	 * @see IContentProvider
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointAdded(final IMarker breakpoint) {
		if (breakpoint.exists()) {		
			asyncExec(new Runnable() {
				public void run() {
					((TableViewer)fViewer).add(breakpoint);
				}
			});
		}
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(final IMarker breakpoint, IMarkerDelta delta) {
		asyncExec(new Runnable() {
			public void run() {
				((TableViewer)fViewer).remove(breakpoint);
			}
		});
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(final IMarker breakpoint, IMarkerDelta delta) {
		if (breakpoint.exists()) {
			asyncExec(new Runnable() {
				public void run() {
					refresh(breakpoint);
				}
			});
		}
	}
	
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		//not a registered debug event listener
	}
	
	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		//not a tree content provider
		return null;
	}
}

