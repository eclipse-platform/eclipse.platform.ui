/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 485843
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.util.*;

/**
 * Manages the log file providers.
 * One adds log file provider to let Log View know where to find log files.
 */
public class LogFilesManager {

	private static List<ILogFileProvider> logFileProviders = new ArrayList<>();

	/**
	 * Adds log file provider.
	 * Has no effect if an identical provider is already registered.
	 */
	public static void addLogFileProvider(ILogFileProvider provider) {
		if (!logFileProviders.contains(provider)) {
			logFileProviders.add(provider);
		}
	}

	/**
	 * Removes log file provider.
	 * Has no effect if an identical provider is already removed.
	 */
	public static void removeLogFileProvider(ILogFileProvider provider) {
		logFileProviders.remove(provider);
	}

	/**
	 * Returns the list of logs.
	 */
	static Map<String, String> getLogSources() {
		ILogFileProvider[] providers = logFileProviders.toArray(new ILogFileProvider[logFileProviders.size()]);
		Map<String, String> result = new HashMap<>(providers.length);

		for (ILogFileProvider provider : providers) {
			Map<String, String> sources = provider.getLogSources();
			result.putAll(sources);
		}

		return result;
	}
}
