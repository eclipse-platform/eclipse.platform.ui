/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;

/**
 * Assigns some default preference values using initializer extension that
 * are later verified by tests.
 */
public class TestInitializer extends AbstractPreferenceInitializer {
	public static final String DEFAULT_PREF_KEY = "SomePreference";
	public static final String DEFAULT_PREF_VALUE = "Hello";

	@Override
	public void initializeDefaultPreferences() {

		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(RuntimeTestsPlugin.PI_RUNTIME_TESTS);
		node.put(DEFAULT_PREF_KEY, DEFAULT_PREF_VALUE);
	}
}
