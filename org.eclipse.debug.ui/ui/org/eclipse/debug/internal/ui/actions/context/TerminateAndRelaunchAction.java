/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.RelaunchActionDelegate;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousTerminateAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Action which terminates a launch and then relaunches it. This is equivalent
 * to the Terminate action followed by Relaunch, but is provided to the user as
 * a convenience.
 */
public class TerminateAndRelaunchAction extends AbstractDebugContextAction {

    protected void doAction(Object element) {
        final ILaunch launch = RelaunchActionDelegate.getLaunch(element);
        if (launch == null || !(element instanceof ITerminate)) {
            // Shouldn't happen because of enablement check.
            return;
        }
        
        if (element instanceof IAdaptable) {
            IAsynchronousTerminateAdapter adapter = (IAsynchronousTerminateAdapter) ((IAdaptable)element).getAdapter(IAsynchronousTerminateAdapter.class);
            if (adapter != null)
                adapter.terminate(element, new ActionRequestMonitor());
        }


        DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                // Must be run in the UI thread since the launch can require
                // prompting to proceed
                RelaunchActionDelegate.relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
            }
        });
    }


    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAsynchronousTerminateAdapter adapter = (IAsynchronousTerminateAdapter) ((IAdaptable)element).getAdapter(IAsynchronousTerminateAdapter.class);
            if (adapter != null)
                adapter.canTerminate(element, monitor);
        }
        
    }


    public String getActionDefinitionId() {
        return ActionMessages.TerminateAndRelaunchAction_0;
    }

    public String getHelpContextId() {
        return "terminate_and_relaunch_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.popupMenu.TerminateAndRelaunch"; //$NON-NLS-1$
    }

    public String getText() {
        return ActionMessages.TerminateAndRelaunchAction_3;
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAndRelaunchAction_4;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_RELAUNCH);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_RELAUNCH);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_RELAUNCH);
    }
}
