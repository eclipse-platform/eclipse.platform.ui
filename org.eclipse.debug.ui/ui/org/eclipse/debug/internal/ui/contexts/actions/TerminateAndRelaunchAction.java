/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.RelaunchActionDelegate;

/**
 * Action which terminates a launch and then relaunches it.
 * This is equivalent to the Terminate action followed by
 * Relaunch, but is provided to the user as a convenience.
 */
public class TerminateAndRelaunchAction extends AbstractDebugContextActionDelegate {

    protected void doAction(Object element) throws DebugException {
        final ILaunch launch= RelaunchActionDelegate.getLaunch(element);
        if (launch == null || !(element instanceof ITerminate)) {
            // Shouldn't happen because of enablement check.
            return;
        }
        
        ((ITerminate)element).terminate();
        DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                // Must be run in the UI thread since the launch can require prompting to proceed
                RelaunchActionDelegate.relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());        
            }
        });
    }
    
    /**
     * @see AbstractDebugActionDelegate#isRunInBackground()
     */
    protected boolean isRunInBackground() {
        return true;
    }

    /**
     * @see AbstractDebugActionDelegate#isEnabledFor(Object)
     */
    protected boolean isEnabledFor(Object element) {
        return element instanceof ITerminate && ((ITerminate)element).canTerminate() &&
            RelaunchActionDelegate.getLaunch(element) != null;
    }
}
