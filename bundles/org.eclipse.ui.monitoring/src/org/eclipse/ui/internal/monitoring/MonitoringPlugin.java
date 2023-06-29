/*******************************************************************************
 * Copyright (C) 2014 , 2019 Google Inc and others.
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
 *     Christoph Läubrich - remove dependency to UI Activator
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.monitoring.PreferenceConstants;

/**
 * The activator class that controls the plug-in life cycle.
 */
public class MonitoringPlugin {

	private static ILog logger = ILog.of(MonitoringPlugin.class);
	private static IPreferenceStore store;

	public static void logError(String message, Throwable e) {
		logger.log(new Status(IStatus.ERROR, PreferenceConstants.PLUGIN_ID, message, e));
	}

	public static void logWarning(String message) {
		logger.log(new Status(IStatus.WARNING, PreferenceConstants.PLUGIN_ID, message));
	}



	public static IPreferenceStore getPreferenceStore() {
		if (store == null) {
			store = PlatformUI.createPreferenceStore(MonitoringPlugin.class);
		}
		return store;
	}

}
