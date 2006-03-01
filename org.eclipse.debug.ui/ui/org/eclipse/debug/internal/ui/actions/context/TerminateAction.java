/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.resource.ImageDescriptor;

public class TerminateAction extends AbstractDebugContextAction {

    protected void doAction(Object element) throws DebugException {
        if (element instanceof ITerminate) {
            if (element instanceof IProcess) {
                killTargets((IProcess) element);
            }
            ((ITerminate) element).terminate();
        }
    }

    private void killTargets(IProcess process) throws DebugException {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunch[] launches = launchManager.getLaunches();

        for (int i = 0; i < launches.length; i++) {
            ILaunch launch = launches[i];
            IProcess[] processes = launch.getProcesses();
            for (int j = 0; j < processes.length; j++) {
                IProcess process2 = processes[j];
                if (process2.equals(process)) {
                    IDebugTarget[] debugTargets = launch.getDebugTargets();
                    for (int k = 0; k < debugTargets.length; k++) {
                        IDebugTarget target = debugTargets[k];
                        if (target.canTerminate()) {
                            target.terminate();
                        }
                    }
                    return; // all possible targets have been terminated for the
                            // launch.
                }
            }
        }
    }

    protected boolean isEnabledFor(Object element) {
        return element instanceof ITerminate && ((ITerminate) element).canTerminate();
    }

    protected String getStatusMessage() {
        return ActionMessages.TerminateActionDelegate_Exceptions_occurred_attempting_to_terminate__2;
    }

    protected String getErrorDialogMessage() {
        return ActionMessages.TerminateActionDelegate_Terminate_failed__1;
    }

    protected Object getTarget(Object selectee) {
        if (selectee instanceof ITerminate) {
            return selectee;
        }
        if (selectee instanceof IAdaptable) {
            return ((IAdaptable) selectee).getAdapter(ITerminate.class);
        }
        return null;
    }

    public String getText() {
        return ActionMessages.TerminateAction_0;
    }

    public String getHelpContextId() {
        return "terminate_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.terminate"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE);
    }
}
