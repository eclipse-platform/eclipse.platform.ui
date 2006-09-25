/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {

		IScopeContext context = new DefaultScope();
		IEclipsePreferences node = context.getNode(TestPlugin.getDefault()
				.getBundle().getSymbolicName());
		node.put(TEST_LISTENER_KEY, TEST_DEFAULT_VALUE);

	}

}
