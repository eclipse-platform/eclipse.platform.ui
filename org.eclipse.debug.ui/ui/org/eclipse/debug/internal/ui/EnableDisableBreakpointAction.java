package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import java.util.Iterator;

/**
 * Enables or disables a breakpoint
 */
public class EnableDisableBreakpointAction extends SelectionProviderAction implements IBreakpointListener {
	
	private final static String PREFIX= "enable_disable_breakpoint_action.";
	private final static String ENABLE= PREFIX + TEXT + ".enable";
	private final static String DISABLE= PREFIX + TEXT + ".disable";
	private final static String ERROR= PREFIX + "error.";
	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setEnabled(!getStructuredSelection().isEmpty());
	}

	/**
	 * Returns the breakpoint manager
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
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
		MultiStatus ms= new MultiStatus(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.REQUEST_FAILED, "Enable/Disable breakpoint(s) failed", null);
		while (enum.hasNext()) {
			IMarker breakpoint= (IMarker) enum.next();
			boolean enabled= manager.isEnabled(breakpoint);
			try {
				manager.setEnabled(breakpoint, !enabled);
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
			boolean enabled= getBreakpointManager().isEnabled(marker);
			if (enabled) {
				setText(DebugUIUtils.getResourceString(DISABLE));
			} else {
				setText(DebugUIUtils.getResourceString(ENABLE));
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
	public void breakpointAdded(IMarker breakpoint) {
	}

	/** 
	 * @see IBreakpointListener
	 */
	public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta) {
	}

	/** 
	 * @see IBreakpointListener
	 */
	public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ISelection s= getSelectionProvider().getSelection();
				if (s instanceof IStructuredSelection) {
					selectionChanged((IStructuredSelection) s);
				}
			}
		});
	}

}

