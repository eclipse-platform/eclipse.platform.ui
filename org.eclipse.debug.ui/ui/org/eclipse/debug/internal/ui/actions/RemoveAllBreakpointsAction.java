package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IViewPart;

/**
 * Removes all breakpoints from the source (markers) and remove all
 * breakpoints from processes
 */
public class RemoveAllBreakpointsAction extends AbstractRemoveAllActionDelegate implements IBreakpointListener {

	protected void doAction() {
		final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints= breakpointManager.getBreakpoints();
		final MultiStatus ms= new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.REQUEST_FAILED, ActionMessages.getString("RemoveAllBreakpointsAction.Breakpoint(s)_removal_failed_3"), null); //$NON-NLS-1$
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) {
				for (int i= 0; i < breakpoints.length; i++) {
					try {
						breakpointManager.removeBreakpoint(breakpoints[i], true);
					} catch (CoreException e) {
						ms.merge(e.getStatus());
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			ms.merge(e.getStatus());
		}
		if (!ms.isOK()) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), ActionMessages.getString("RemoveAllBreakpointsAction.Removing_all_breakpoints_4"),ActionMessages.getString("RemoveAllBreakpointsAction.Exceptions_occurred_removing_breakpoints._5"), ms); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	protected void update() {
		getAction().setEnabled(
			DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints());
	}	
	
	/**
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (getAction() != null && !getAction().isEnabled()){
			update();
		}
	}

	/**
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/**
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (getAction() != null) {
			update();
		}
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);	
		super.dispose();
	}
}
