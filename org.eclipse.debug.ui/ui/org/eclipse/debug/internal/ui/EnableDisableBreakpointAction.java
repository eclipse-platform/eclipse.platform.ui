package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Enables or disables a breakpoint
 */
public class EnableDisableBreakpointAction extends SelectionProviderAction implements IBreakpointListener {
	
	private final static String PREFIX= "enable_disable_breakpoint_action.";
	private final static String ENABLE= PREFIX + TEXT + ".enable";
	private final static String DISABLE= PREFIX + TEXT + ".disable";
	private final static String ERROR= PREFIX + "error.";
	private final static String STATUS= PREFIX + "status.message";
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setEnabled(!getStructuredSelection().isEmpty());
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.ENABLE_DISABLE_BREAKPOINT_ACTION });
	}

	/**
	 * @see Action
	 */
	public void run() {
		IStructuredSelection selection= (IStructuredSelection) getStructuredSelection();
		//Get the selected marker
		Iterator enum= selection.iterator();
		if (!enum.hasNext()) {
			return;
		}

		IBreakpointManager manager= getBreakpointManager();
		MultiStatus ms= new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(STATUS), null);
		while (enum.hasNext()) {
			IMarker marker= (IMarker) enum.next();
			try {
				getBreakpoint(marker).toggleEnabled();
			} catch (CoreException e) {
				ms.merge(e.getStatus());
			}
		}
		if (!ms.isOK()) {
			DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), ERROR, ms);
		}
	}

	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		Iterator enum= sel.iterator();
		if (!enum.hasNext()) {
			//No selection
			setEnabled(false);
			return;
		}
		IMarker marker= (IMarker)enum.next();
		if (!enum.hasNext()) {
			//single selection
			try {
				if (getBreakpoint(marker).isEnabled()) {
					setText(DebugUIUtils.getResourceString(DISABLE));
				} else {
					setText(DebugUIUtils.getResourceString(ENABLE));
				}
			} catch (CoreException ce) {
				DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), ERROR, ce.getStatus());
			}
		} else {
			// multi- selection
			setText(DebugUIUtils.getResourceString(PREFIX + TEXT));
		}
		setEnabled(true);
	}

	/** 
	 * @see IBreakpointListener
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	/** 
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/** 
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		Display display= Display.getDefault();
		if (display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				ISelection s= getSelectionProvider().getSelection();
				if (s instanceof IStructuredSelection) {
					selectionChanged((IStructuredSelection) s);
				}
			}
		});
	}
	
	/**
	 * Returns the breakpoint manager
	 */
	private IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	/**
	 * Returns the breakpoint associated with marker
	 */
	private IBreakpoint getBreakpoint(IMarker marker) {
		return getBreakpointManager().getBreakpoint(marker);
	}	
}

