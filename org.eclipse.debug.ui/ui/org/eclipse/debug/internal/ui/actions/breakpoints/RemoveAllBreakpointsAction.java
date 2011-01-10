/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

 
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IMarkerDelta;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;

import org.eclipse.debug.ui.DebugUITools;

/**
 * Removes all breakpoints from the source (markers) and remove all
 * breakpoints from processes
 */
public class RemoveAllBreakpointsAction extends AbstractRemoveAllActionDelegate implements IBreakpointsListener {

	private Shell fShell;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#isEnabled()
	 */
	protected boolean isEnabled() {
		return DebugPlugin.getDefault().getBreakpointManager().hasBreakpoints();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#initialize()
	 */
	protected void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		final IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		final IBreakpoint[] breakpoints = breakpointManager.getBreakpoints();
		if (breakpoints.length < 1) {
			return;
		}
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean prompt = store.getBoolean(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_ALL_BREAKPOINTS);
		boolean proceed = true;
		if(prompt) {
			MessageDialogWithToggle mdwt = MessageDialogWithToggle.openYesNoQuestion(window.getShell(), ActionMessages.RemoveAllBreakpointsAction_0, 
					ActionMessages.RemoveAllBreakpointsAction_1, ActionMessages.RemoveAllBreakpointsAction_3, !prompt, null, null);
			if(mdwt.getReturnCode() !=  IDialogConstants.YES_ID){
				proceed = false;
			}
			else {
				store.setValue(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_ALL_BREAKPOINTS, !mdwt.getToggleState());
			}
		}  
		if (proceed) {
			new Job(ActionMessages.RemoveAllBreakpointsAction_2) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						DebugUITools.deleteBreakpoints(breakpoints, fShell, monitor);
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/*
	 * @see org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate#init(org.eclipse.ui.IViewPart)
	 * @since 3.7
	 */
	public void init(IViewPart view) {
		super.init(view);
		fShell= view.getSite().getShell();
	}

	/*
	 * @see org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 * @since 3.7
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		fShell= window.getShell();
	}
}
