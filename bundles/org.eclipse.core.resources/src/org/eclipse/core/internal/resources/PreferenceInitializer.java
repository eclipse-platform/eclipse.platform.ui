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
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.*;

/**
 * @since 3.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode(ResourcesPlugin.PI_RESOURCES);

		// auto-refresh default
		node.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, false);

		// linked resources default
		node.putBoolean(ResourcesPlugin.PREF_DISABLE_LINKING, false);

		// build manager defaults
//		node.putBoolean(ResourcesPlugin.PREF_AUTO_BUILDING, true);
//		node.put(ResourcesPlugin.PREF_BUILD_ORDER, ""); //$NON-NLS-1$
//		node.put(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS, ""); //$NON-NLS-1$
//		node.put(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, ""); //$NON-NLS-1$

		// history store defaults
//		node.putLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY, 7 * 24 * 3600 * 1000l); // 7 days
//		node.putLong(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE, 1024 * 1024l); // 1 MB
//		node.putInt(ResourcesPlugin.PREF_MAX_FILE_STATES, 50);

		// save manager defaults
//		node.put(ResourcesPlugin.PREF_ENCODING, ""); //$NON-NLS-1$
//		node.putLong(ResourcesPlugin.PREF_MAX_NOTIFICATION_DELAY, 10000l); // 10 seconds

		// encoding defaults
//		node.putLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, 5 * 60 * 1000l); // 5 min
	}

}
