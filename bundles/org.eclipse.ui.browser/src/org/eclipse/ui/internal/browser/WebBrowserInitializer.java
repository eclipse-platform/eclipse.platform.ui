/*******************************************************************************
 * Copyright (c) 2021 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.browser;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class WebBrowserInitializer extends AbstractPreferenceInitializer {

	public WebBrowserInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(WebBrowserUIPlugin.PLUGIN_ID);
		if (node != null) {
			node.putInt(WebBrowserPreference.PREF_BROWSER_CHOICE, 2);
			try {
				node.flush();
			} catch (BackingStoreException e) {
				// do nothing
			}
		}
	}

}
