package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Enables or disables a breakpoint
 */
public class EnableDisableBreakpointAction extends SelectionProviderAction implements IBreakpointListener {
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, DebugUIMessages.getString("EnableDisableBreakpointAction.&Enable/Disable_1")); //$NON-NLS-1$
		setEnabled(!getStructuredSelection().isEmpty());
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.ENABLE_DISABLE_BREAKPOINT_ACTION });
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		IStructuredSelection selection= (IStructuredSelection) getStructuredSelection();
		//Get the selected marker
		Iterator enum= selection.iterator();
		if (!enum.hasNext()) {
			return;
		}

		MultiStatus ms= new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.REQUEST_FAILED, DebugUIMessages.getString("EnableDisableBreakpointAction.Enable/Disable_breakpoint(s)_failed_2"), null); //$NON-NLS-1$
		while (enum.hasNext()) {
			IBreakpoint breakpoint = (IBreakpoint) enum.next();
			try {
				breakpoint.setEnabled(!breakpoint.isEnabled());
			} catch (CoreException e) {
				ms.merge(e.getStatus());
			}
		}
		if (!ms.isOK()) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("EnableDisableBreakpointAction.Enabling/disabling_breakpoints_3"), DebugUIMessages.getString("EnableDisableBreakpointAction.Exceptions_occurred_enabling/disabling_the_breakpoint(s)._4"), ms); //$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * @see SelectionProviderAction#selectionChanged(IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		Iterator enum= sel.iterator();
		if (!enum.hasNext()) {
			//No selection
			setEnabled(false);
			return;
		}
		IBreakpoint bp= (IBreakpoint)enum.next();
		if (!enum.hasNext()) {
			//single selection
			try {
				if (bp.isEnabled()) {
					setText(DebugUIMessages.getString("EnableDisableBreakpointAction.&Disable_5")); //$NON-NLS-1$
				} else {
					setText(DebugUIMessages.getString("EnableDisableBreakpointAction.&Enable_6")); //$NON-NLS-1$
				}
			} catch (CoreException ce) {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("EnableDisableBreakpointAction.Enabling/disabling_breakpoints_7"), DebugUIMessages.getString("EnableDisableBreakpointAction.Exceptions_occurred_enabling/disabling_the_breakpoint(s)._8"), ce.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
			}
		} else {
			// multi- selection
			setText(DebugUIMessages.getString("EnableDisableBreakpointAction.&Enable/Disable_9")); //$NON-NLS-1$
		}
		setEnabled(true);
	}

	/** 
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	/** 
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/** 
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
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
}

