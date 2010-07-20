/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

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
		// Step filter preferences
		Preferences.setDefaultBoolean(DebugPlugin.getUniqueIdentifier(), StepFilterManager.PREF_USE_STEP_FILTERS, false);
		//launch configurations preferences
		Preferences.setDefaultBoolean(DebugPlugin.getUniqueIdentifier(), DebugPlugin.PREF_DELETE_CONFIGS_ON_PROJECT_DELETE, false);
		Preferences.setDefaultBoolean(DebugPlugin.getUniqueIdentifier(), IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, true);
		Preferences.setDefaultBoolean(DebugPlugin.getUniqueIdentifier(), IInternalDebugCoreConstants.PREF_BREAKPOINT_MANAGER_ENABLED_STATE, true);
		Preferences.savePreferences(DebugPlugin.getUniqueIdentifier());
	}

}
