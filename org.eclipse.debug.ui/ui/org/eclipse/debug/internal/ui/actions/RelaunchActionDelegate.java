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
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.ui.DebugUITools;

public class RelaunchActionDelegate extends AbstractDebugActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) {
		ILaunch launch= getLaunch(object);
        if (launch != null) {
            relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
        }
	}
    
    public static ILaunch getLaunch(Object element) {
        ILaunch launch= null;
        if (element instanceof IDebugElement) {
            launch= ((IDebugElement)element).getLaunch();
        } else if (element instanceof ILaunch) {
            launch= ((ILaunch)element);
        } else if (element instanceof IProcess) {
            launch= ((IProcess)element).getLaunch();
        }
        return launch;
    }
	
	/**
	 * Re-launches the given configuration in the specified mode.
	 */
	public static void relaunch(ILaunchConfiguration config, String mode) {
		DebugUITools.launch(config, mode);		
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		ILaunch launch= null;
		if (element instanceof ILaunch) {
			launch= (ILaunch)element;
		} else if (element instanceof IDebugElement) {
			launch= ((IDebugElement)element).getLaunch();
		} else if (element instanceof IProcess) {
			launch= ((IProcess)element).getLaunch();
		}
		
		return launch != null && launch.getLaunchConfiguration() != null && LaunchConfigurationManager.isVisible(launch.getLaunchConfiguration());
	}
			
	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.RelaunchActionDelegate_Launch_Failed_1; //$NON-NLS-1$
	}
	
	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.RelaunchActionDelegate_An_exception_occurred_while_launching_2; //$NON-NLS-1$
	}
}
