/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * ConsoleTerminateAction
 */
public class ConsoleTerminateAction extends Action implements IUpdate {

	private ProcessConsole fConsole;

	/**
	 * Creates a terminate action for the console 
	 */
	public ConsoleTerminateAction(ProcessConsole console) {
		super(ConsoleMessages.ConsoleTerminateAction_0); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		fConsole = console;
		setToolTipText(ConsoleMessages.ConsoleTerminateAction_1); 
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE));
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IProcess process = fConsole.getProcess(); 
		setEnabled(process.canTerminate());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		try {
			IProcess process = fConsole.getProcess();
            killTargets(process);
            process.terminate();
		} catch (DebugException e) {
			// TODO: report exception
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
                    return; // all possible targets have been terminated for the launch.
                }
            }
        }
    }

    public void dispose() {
	    fConsole = null;
	}

}
