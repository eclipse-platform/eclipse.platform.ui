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
package org.eclipse.update.internal.scheduler.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.update.internal.scheduler.UpdateScheduler;
import org.osgi.service.prefs.Preferences;


/**
 * @since 3.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences node = new DefaultScope().getNode("org.eclipse.update.scheduler");
		node.putBoolean(UpdateScheduler.P_ENABLED, false);
		node.put(UpdateScheduler.P_SCHEDULE, UpdateScheduler.VALUE_ON_STARTUP);
		node.putBoolean(UpdateScheduler.P_DOWNLOAD, false);
	}

}