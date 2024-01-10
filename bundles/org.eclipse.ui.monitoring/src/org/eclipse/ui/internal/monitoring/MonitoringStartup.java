/*******************************************************************************
 * Copyright (C) 2014, 2023 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *     Sergey Prigogin (Google)
 *     Christoph Läubrich - change to new preference store API
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.monitoring.preferences.MonitoringPreferenceListener;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;

/**
 * Starts the event loop monitoring thread. Initializes preferences from {@link IPreferenceStore}.
 */
@Component(service = EventHandler.class)
@EventTopics(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class MonitoringStartup implements EventHandler {
	private EventLoopMonitorThread monitoringThread;

	@Override
	public void handleEvent(Event event) {
		if (monitoringThread != null) {
			return;
		}

		IPreferenceStore preferences = MonitoringPlugin.getPreferenceStore();
		if (preferences.getBoolean(PreferenceConstants.MONITORING_ENABLED) && !Platform.inDevelopmentMode()) {
			monitoringThread = createAndStartMonitorThread();
		}

		preferences.addPropertyChangeListener(new MonitoringPreferenceListener(monitoringThread));
	}

	/**
	 * Creates and starts a new monitoring thread.
	 */
	public static EventLoopMonitorThread createAndStartMonitorThread() {
		EventLoopMonitorThread.Parameters args = loadPreferences();
		EventLoopMonitorThread temporaryThread = null;

		try {
			temporaryThread = new EventLoopMonitorThread(args);
		} catch (IllegalArgumentException e) {
			MonitoringPlugin.logError(Messages.MonitoringStartup_initialization_error, e);
			return null;
		}

		final EventLoopMonitorThread thread = temporaryThread;
		final Display display = PlatformUI.getWorkbench().getDisplay();
		// Final setup and start asynchronously on the display thread.
		display.asyncExec(() -> {
			// If we're still running when display gets disposed, shutdown the thread.
			display.disposeExec(thread::shutdown);
			thread.start();
		});

		return thread;
	}

	private static EventLoopMonitorThread.Parameters loadPreferences() {
		IPreferenceStore preferences = MonitoringPlugin.getPreferenceStore();
		EventLoopMonitorThread.Parameters args = new EventLoopMonitorThread.Parameters();

		args.longEventWarningThreshold =
				preferences.getInt(PreferenceConstants.LONG_EVENT_WARNING_THRESHOLD_MILLIS);
		args.longEventErrorThreshold =
				preferences.getInt(PreferenceConstants.LONG_EVENT_ERROR_THRESHOLD_MILLIS);
		args.deadlockThreshold =
				preferences.getInt(PreferenceConstants.DEADLOCK_REPORTING_THRESHOLD_MILLIS);
		args.maxStackSamples = preferences.getInt(PreferenceConstants.MAX_STACK_SAMPLES);
		args.uiThreadFilter = preferences.getString(PreferenceConstants.UI_THREAD_FILTER);
		args.noninterestingThreadFilter =
				preferences.getString(PreferenceConstants.NONINTERESTING_THREAD_FILTER);
		args.logToErrorLog = preferences.getBoolean(PreferenceConstants.LOG_TO_ERROR_LOG);

		return args;
	}
}
