/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Initializes preferences for debug.core
 * 
 * @since 3.3
 */
public class DebugPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = DebugPlugin.getDefault().getPluginPreferences();
		// Step filter preferences
		prefs.setDefault(StepFilterManager.PREF_USE_STEP_FILTERS, false);
		prefs.setDefault(LaunchManager.PREF_DELETE_CONFIGS_ON_PROJECT_DELETE, true);
	}

}
