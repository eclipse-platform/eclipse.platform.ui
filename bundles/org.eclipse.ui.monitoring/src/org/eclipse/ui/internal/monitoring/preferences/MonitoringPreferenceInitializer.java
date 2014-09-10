/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.monitoring.MonitoringPlugin;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * Initializes the default values for monitoring plug-in preferences.
 */
public class MonitoringPreferenceInitializer extends AbstractPreferenceInitializer {
	/** Force a logged event for a possible deadlock when an event hangs for longer than this */
	private static final int DEFAULT_FORCE_DEADLOCK_LOG_TIME_MILLIS = 10 * 60 * 1000; // == 10 minutes
	private static final String DEFAULT_FILTER_TRACES =
			"org.eclipse.swt.internal.gtk.OS.gtk_dialog_run," //$NON-NLS-1$
			+ "org.eclipse.swt.internal.win32.OS.TrackPopupMenu," //$NON-NLS-1$
			+ "org.eclipse.e4.ui.workbench.addons.dndaddon.DnDManager.startDrag"; //$NON-NLS-1$

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MonitoringPlugin.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.MONITORING_ENABLED, false);
		store.setDefault(PreferenceConstants.LONG_EVENT_THRESHOLD_MILLIS, 500);
		store.setDefault(PreferenceConstants.MAX_STACK_SAMPLES, 3);
		store.setDefault(PreferenceConstants.SAMPLE_INTERVAL_MILLIS, 300);
		store.setDefault(PreferenceConstants.INITIAL_SAMPLE_DELAY_MILLIS, 300);
		store.setDefault(PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS,
				DEFAULT_FORCE_DEADLOCK_LOG_TIME_MILLIS);
		store.setDefault(PreferenceConstants.DUMP_ALL_THREADS, false);
		store.setDefault(PreferenceConstants.LOG_TO_ERROR_LOG, true);
		store.setDefault(PreferenceConstants.FILTER_TRACES, DEFAULT_FILTER_TRACES);
	}
}
