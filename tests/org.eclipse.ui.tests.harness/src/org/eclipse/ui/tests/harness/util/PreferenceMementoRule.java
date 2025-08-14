/*******************************************************************************
 * Copyright (c) 2025 Vector Informatik and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceMemento;
import org.junit.rules.ExternalResource;

/**
 * Preference helper to restore changed preference values after test run.
 */
public class PreferenceMementoRule extends ExternalResource {

	private PreferenceMemento prefMemento;

	@Override
	protected void before() {
		prefMemento = new PreferenceMemento();
	}

	@Override
	protected void after() {
		prefMemento.resetPreferences();
	}

	/**
	 * Change a preference value for the associated test run. The preference will
	 * automatically be reset to the value it had before starting when executing
	 * {@link #after()}.
	 *
	 * @param <T>   preference value type. The type must have a corresponding
	 *              {@link IPreferenceStore} setter.
	 * @param store preference store to manipulate (must not be <code>null</code>)
	 * @param name  preference to change
	 * @param value new preference value
	 * @throws IllegalArgumentException when setting a type which is not supported
	 *                                  by {@link IPreferenceStore}
	 *
	 * @see IPreferenceStore#setValue(String, double)
	 * @see IPreferenceStore#setValue(String, float)
	 * @see IPreferenceStore#setValue(String, int)
	 * @see IPreferenceStore#setValue(String, long)
	 * @see IPreferenceStore#setValue(String, boolean)
	 * @see IPreferenceStore#setValue(String, String)
	 */
	public <T> void setPreference(IPreferenceStore store, String name, T value) {
		prefMemento.setValue(store, name, value);
	}
}
