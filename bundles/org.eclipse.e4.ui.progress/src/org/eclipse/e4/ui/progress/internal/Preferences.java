/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;


public class Preferences {

	private static Map<String, String> preferences;

	@Inject
	private static synchronized void updatePreferences(MApplication application) {
		preferences = application.getPersistedState();
	}

	public static synchronized boolean getBoolean(String key) {
		return Boolean.parseBoolean(preferences.get(key));
	}

	public static synchronized void set(String key, boolean value) {
		preferences.put(key, Boolean.toString(value));
	}

	public static synchronized void set(String key, String value) {
		preferences.put(key, value);
	}

}
