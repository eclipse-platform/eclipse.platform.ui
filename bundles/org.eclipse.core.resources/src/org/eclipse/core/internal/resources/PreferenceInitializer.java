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

	// auto-refresh
	public static final boolean PREF_AUTO_REFRESH_DEFAULT = false;

	// linked resources
	public static final boolean PREF_DISABLE_LINKING_DEFAULT = false;

	// file encoding
	public static final String PREF_ENCODING_DEFAULT = ""; //$NON-NLS-1$

	// build manager
	public static final boolean PREF_AUTO_BUILDING_DEFAULT = true;
	public static final String PREF_BUILD_ORDER_DEFAULT = ""; //$NON-NLS-1$
	public static final String PREF_MAX_BUILD_ITERATIONS_DEFAULT = ""; //$NON-NLS-1$
	public static final String PREF_DEFAULT_BUILD_ORDER = ""; //$NON-NLS-1$

	// save manager
	public final static long PREF_SNAPSHOT_INTERVAL_DEFAULT = 5 * 60 * 1000l; // 5 min
	public final static long PREF_MAX_NOTIFICATION_DELAY = 10000l; // 10 seconds

	// history store
	public static final long PREF_FILE_STATE_LONGEVITY_DEFAULT = 7 * 24 * 3600 * 1000l; // 7 days
	public static final long PREF_MAX_FILE_STATE_SIZE_DEFAULT = 1024 * 1024l; // 1 MB
	public static final int PREF_MAX_FILE_STATES_DEFAULT = 50;

	public PreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope().getNode(ResourcesPlugin.PI_RESOURCES);

		// auto-refresh default
		node.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, PREF_AUTO_REFRESH_DEFAULT);

		// linked resources default
		node.putBoolean(ResourcesPlugin.PREF_DISABLE_LINKING, PREF_DISABLE_LINKING_DEFAULT);

		// build manager defaults
		//		node.putBoolean(ResourcesPlugin.PREF_AUTO_BUILDING, PREF_AUTO_BUILDING_DEFAULT);
		//		node.put(ResourcesPlugin.PREF_BUILD_ORDER, PREF_BUILD_ORDER_DEFAULT);
		//		node.put(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS, PREF_MAX_BUILD_ITERATIONS_DEFAULT);
		//		node.put(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, PREF_DEFAULT_BUILD_ORDER_DEFAULT);

		// history store defaults
		//		node.putLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY, PREF_FILE_STATE_LONGEVITY_DEFAULT);
		//		node.putLong(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE, PREF_MAX_FILE_STATE_SIZE_DEFAULT);
		//		node.putInt(ResourcesPlugin.PREF_MAX_FILE_STATES, PREF_MAX_FILE_STATES_DEFAULT);

		// save manager defaults
		//		node.putLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, PREF_SNAPSHOT_INTERVAL_DEFAULT);
		//		node.putLong(ResourcesPlugin.PREF_MAX_NOTIFICATION_DELAY, PREF_MAX_NOTIFICATION_DELAY_DEFAULT);

		// encoding defaults
		node.put(ResourcesPlugin.PREF_ENCODING, PREF_ENCODING_DEFAULT);
	}

}
