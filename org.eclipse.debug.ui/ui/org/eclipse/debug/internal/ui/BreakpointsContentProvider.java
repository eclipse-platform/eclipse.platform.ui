package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
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
		super.dispose();
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (breakpoint.getMarker().exists()) {		
			asyncExec(new Runnable() {
				public void run() {
					if (!isDisposed()) {
						((TableViewer)fViewer).add(breakpoint);
					}
				}
			});
		}
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {
		asyncExec(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					((TableViewer)fViewer).remove(breakpoint);
				}
			}
		});
	}

	/**
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(final IBreakpoint breakpoint, IMarkerDelta delta) {
		if (breakpoint.getMarker().exists()) {
			asyncExec(new Runnable() {
				public void run() {
					if (!isDisposed()) {
						refresh(breakpoint);
					}
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

