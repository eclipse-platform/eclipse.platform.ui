package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.*;import org.eclipse.core.runtime.*;import org.eclipse.debug.core.*;import org.eclipse.jface.action.Action;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Removes all breakpoints from the source (markers) and remove all
 * breakpoints from processes
 */
public class RemoveAllBreakpointsAction extends Action implements IBreakpointListener {
	
	private static final String PREFIX= "remove_all_breakpoints_action.";
	private static final String ERROR= "error.";
	private static final String STATUS= PREFIX + "status";

	public RemoveAllBreakpointsAction() {
		super(DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.REMOVE_ALL_ACTION });
	}

	/**
	 * @see IAction
	 */
	public void run() {
		final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		final IMarker[] markers= breakpointManager.getBreakpoints();
		final MultiStatus ms= new MultiStatus(DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(STATUS), null);
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor pm) {
				for (int i= 0; i < markers.length; i++) {
					try {
						breakpointManager.removeBreakpoint(markers[i], true);
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
			DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), PREFIX + ERROR, ms);
		}
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointAdded(IMarker breakpoint) {
		breakpointAltered();
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta) {
	}
	/**
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta) {
		breakpointAltered();
	}
	
	protected void breakpointAltered() {
		boolean enable= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length == 0 ? false : true;
		setEnabled(enable);
	}
}
