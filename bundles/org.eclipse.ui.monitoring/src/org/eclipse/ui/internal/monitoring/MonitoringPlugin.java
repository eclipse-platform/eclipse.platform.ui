/*******************************************************************************
 * Copyright (C) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class that controls the plug-in life cycle.
 */
public class MonitoringPlugin extends AbstractUIPlugin {
	private static MonitoringPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static MonitoringPlugin getDefault() {
		return plugin;
	}

	public static void logError(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PreferenceConstants.PLUGIN_ID, message, e));
	}

	public static void logWarning(String message) {
		log(new Status(IStatus.WARNING, PreferenceConstants.PLUGIN_ID, message));
	}

	private static void log(IStatus status) {
		plugin.getLog().log(status);
	}
}
