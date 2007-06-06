/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.scheduler.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.update.internal.scheduler.UpdateSchedulerPlugin;
import org.osgi.service.prefs.Preferences;


/**
 * @since 3.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences node = new DefaultScope().getNode("org.eclipse.update.scheduler"); //$NON-NLS-1$
		node.putBoolean(UpdateSchedulerPlugin.P_ENABLED, false);
		node.put(UpdateSchedulerPlugin.P_SCHEDULE, UpdateSchedulerPlugin.VALUE_ON_STARTUP);
		node.putBoolean(UpdateSchedulerPlugin.P_DOWNLOAD, false);
	}

}
