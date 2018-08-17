/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.3
 *
 */
public class TestPreferenceInitializer extends AbstractPreferenceInitializer {

	public static String TEST_LISTENER_KEY = "TEST_LISTENER";

	public static String TEST_SET_VALUE = "TEST_SET_VALUE";

	public static String TEST_DEFAULT_VALUE = "TEST_DEFAULT_VALUE";

	@Override
	public void initializeDefaultPreferences() {

		IScopeContext context = DefaultScope.INSTANCE;
		IEclipsePreferences node = context.getNode(TestPlugin.getDefault()
				.getBundle().getSymbolicName());
		node.put(TEST_LISTENER_KEY, TEST_DEFAULT_VALUE);

	}

}
