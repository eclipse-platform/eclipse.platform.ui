/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class RelaunchActionDelegate extends AbstractDebugActionDelegate {
	
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object object) {
		ILaunch launch= DebugUIPlugin.getLaunch(object);
        if (launch != null) {
            relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
        }
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
		ILaunch launch= DebugUIPlugin.getLaunch(element);
		return launch != null && launch.getLaunchConfiguration() != null && LaunchConfigurationManager.isVisible(launch.getLaunchConfiguration());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#getTargetSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected IStructuredSelection getTargetSelection(IStructuredSelection s) {
		if (s.isEmpty()) {
			return s;
		}
		Set dups = new LinkedHashSet();
		Iterator iterator = s.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			ILaunch launch = DebugUIPlugin.getLaunch(object);
			if (launch == null) {
				return s;
			}
			dups.add(launch);
		}
		return new StructuredSelection(dups.toArray());
	}
			
	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.RelaunchActionDelegate_Launch_Failed_1; 
	}
	
	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.RelaunchActionDelegate_An_exception_occurred_while_launching_2; 
	}
}
