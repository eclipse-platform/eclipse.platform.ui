/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class DebugModePromptStatusHandler implements IStatusHandler {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if (source instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration)source;
			boolean privateConfig = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
			if (privateConfig) {
				return new Boolean(false);
			}	
		}
		
		Shell activeShell = DebugUIPlugin.getShell();
		String title = LaunchConfigurationsMessages.getString("DebugModePromptStatusHandler.0"); //$NON-NLS-1$
		String message = LaunchConfigurationsMessages.getString("DebugModePromptStatusHandler.1"); //$NON-NLS-1$
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		
		ILaunchConfiguration configuration = (ILaunchConfiguration)source;
		
		String pref = store.getString(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE); 
		if (pref != null) {
			if (pref.equals(AlwaysNeverDialog.NEVER)) {
				return new Boolean(false);
			} else if (pref.equals(AlwaysNeverDialog.ALWAYS)) { 
				relaunchInDebugMode(configuration);
				return new Boolean(true);
			}
		}
		
		boolean switchToDebug = AlwaysNeverDialog.openQuestion(activeShell, title, message, IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, store); 
		if (switchToDebug) {
			relaunchInDebugMode(configuration);
		}
		return new Boolean(switchToDebug);
	}
	/**
	 * @param configuration
	 */
	private void relaunchInDebugMode(ILaunchConfiguration configuration) {
		DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
	}
}
