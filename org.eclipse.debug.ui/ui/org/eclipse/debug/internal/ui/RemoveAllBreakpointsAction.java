package org.eclipse.debug.internal.ui;

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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Removes all breakpoints from the source (markers) and remove all
 * breakpoints from processes
 */
public class RemoveAllBreakpointsAction extends Action implements IBreakpointListener {
	
	public RemoveAllBreakpointsAction() {
		super(DebugUIMessages.getString("RemoveAllBreakpointsAction.Remove_&All_1")); //$NON-NLS-1$
		setToolTipText(DebugUIMessages.getString("RemoveAllBreakpointsAction.Remove_All_Breakpoints_2")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE_ALL));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ALL_ACTION });
	}

	/**
	 * @see IAction
	 */
	public void run() {
		final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints= breakpointManager.getBreakpoints();
		final MultiStatus ms= new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, DebugUIMessages.getString("RemoveAllBreakpointsAction.Breakpoint(s)_removal_failed_3"), null); //$NON-NLS-1$
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
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("RemoveAllBreakpointsAction.Removing_all_breakpoints_4"),DebugUIMessages.getString("RemoveAllBreakpointsAction.Exceptions_occurred_removing_breakpoints._5"), ms); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		breakpointAltered();
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		breakpointAltered();
	}
	
	protected void breakpointAltered() {
		boolean enable= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true;
		setEnabled(enable);
	}
}
