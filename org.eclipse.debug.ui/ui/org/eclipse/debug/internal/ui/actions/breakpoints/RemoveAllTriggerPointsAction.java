/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;


import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractRemoveAllActionDelegate;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Removes all triggerpoints from the source (markers)
 */
public class RemoveAllTriggerPointsAction extends AbstractRemoveAllActionDelegate implements IBreakpointsListener {


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#isEnabled()
	 */
	@Override
	protected boolean isEnabled() {
		return DebugPlugin.getDefault().getBreakpointManager().getTriggerPoints().length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	@Override
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#initialize()
	 */
	@Override
	protected void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean prompt = store.getBoolean(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_ALL_TRIGGER_BREAKPOINTS);
		boolean proceed = true;
		if (prompt) {
			MessageDialogWithToggle mdwt = MessageDialogWithToggle.openYesNoQuestion(window.getShell(), ActionMessages.RemoveAllTriggerPointsAction_0, ActionMessages.RemoveAllTriggerPointsAction_1, ActionMessages.RemoveAllBreakpointsAction_3, !prompt, null, null);
			if (mdwt.getReturnCode() != IDialogConstants.YES_ID) {
				proceed = false;
			} else {
				store.setValue(IDebugPreferenceConstants.PREF_PROMPT_REMOVE_ALL_TRIGGER_BREAKPOINTS, !mdwt.getToggleState());
			}
		}
		if (proceed) {
			new Job(ActionMessages.RemoveAllTriggerPointsAction_1) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						DebugPlugin.getDefault().getBreakpointManager().removeAllTriggerPoints();
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
					}
			}.schedule();
		}
	}

}
