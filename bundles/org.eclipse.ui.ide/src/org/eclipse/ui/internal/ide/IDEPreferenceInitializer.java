/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.ide.IDE;

/**
 * The IDEPreferenceInitializer is the preference initializer for 
 * the IDE preferences.
 */
public class IDEPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		
		IEclipsePreferences node = new DefaultScope().getNode(IDEWorkbenchPlugin.getDefault().getBundle().getSymbolicName());
		node.putBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD, false);
		node.putInt(IDEInternalPreferences.SAVE_INTERVAL, 5); //5 minutes
		node.putBoolean(IDEInternalPreferences.WELCOME_DIALOG, true);
		node.putBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP,
                false);
		node.putBoolean(
                IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, true);
		node.put(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE,
                IDEInternalPreferences.PSPM_PROMPT);
		node.put(IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE,
                IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
    

	}
}
