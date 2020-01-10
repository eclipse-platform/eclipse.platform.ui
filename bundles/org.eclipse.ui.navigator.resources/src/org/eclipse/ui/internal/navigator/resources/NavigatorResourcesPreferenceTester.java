/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc., and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

public class NavigatorResourcesPreferenceTester extends PropertyTester {

	private static final String NAVIGATOR_RESOURCES_PREFERENCES = "navigatorResourcePreference"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!NAVIGATOR_RESOURCES_PREFERENCES.equals(property) || args.length != 1) {
			return false;
		}
		String preferenceName = (String) args[0];
		IPreferenceStore preferenceStore = WorkbenchNavigatorPlugin.getDefault().getPreferenceStore();
		if (expectedValue == null) {
			return preferenceStore.getBoolean(preferenceName);
		}
		return expectedValue.toString().equals(preferenceStore.getString(preferenceName));
	}

}
