/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.tests.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ScopedPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.junit.jupiter.api.Test;

public class ScopedPreferenceStoreTest {

	final String DEFAULT_DEFAULT_STRING = "";

	@Test
	public void testNeedsSaving() throws IOException {
		IScopeContext context = InstanceScope.INSTANCE;
		String qualifier = "org.eclipse.ui.tests.preferences";
		ScopedPreferenceStore store = new ScopedPreferenceStore(context, qualifier);
		String key = "key1";
		String value = "value1";

		// nothing there
		assertFalse(store.needsSaving(), "0.1");
		assertFalse(store.contains(key), "0.2");
		assertEquals(DEFAULT_DEFAULT_STRING, store.getString(key), "0.3");

		// set the value
		store.setValue(key, value);
		assertTrue(store.needsSaving(), "1.0");
		assertTrue(store.contains(key), "1.1");
		assertEquals(value, store.getString(key), "1.2");

		// flush
		store.save();

		// do the test
		assertFalse(store.needsSaving(), "3.0");

		// change the node outside of the scoped store
		String key2 = "key2";
		String value2 = "value2";
		IEclipsePreferences node = context.getNode(qualifier);
		node.put(key2, value2);
		assertEquals(value2, node.get(key2, null), "4.0");
		assertFalse(store.needsSaving(), "4.1");
	}

	@Test
	public void testRestoreDefaults() {
		IScopeContext context = InstanceScope.INSTANCE;
		String qualifier = "org.eclipse.ui.tests.preferences#testRestoreDefaults";
		ScopedPreferenceStore store = new ScopedPreferenceStore(context, qualifier);
		final String key = "key";
		final String value = "value";

		// setup and initial assertions
		assertFalse(store.contains(key), "0.1");
		assertEquals(DEFAULT_DEFAULT_STRING, store.getString(key), "0.2");

		// set the value
		store.setValue(key, value);
		assertTrue(store.contains(key), "1.0");
		assertEquals(value, store.getString(key), "1.1");

		final boolean[] found = new boolean[1];
		IPropertyChangeListener listener = event -> {
			if (key.equals(event.getProperty()) && value.equals(event.getOldValue())) {
				found[0] = true;
			}
		};
		store.addPropertyChangeListener(listener);

		// restore the default
		store.setToDefault(key);
		assertFalse(store.contains(key), "2.0");
		assertEquals(DEFAULT_DEFAULT_STRING, store.getString(key), "2.1");

		// check it
		assertTrue(found[0], "3.0");
	}

}
