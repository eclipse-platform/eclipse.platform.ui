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
package org.eclipse.ui.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Test;

public class ScopedPreferenceStoreTestCase {

	final String DEFAULT_DEFAULT_STRING = "";

	@Test
	public void testNeedsSaving() throws IOException {
		IScopeContext context = InstanceScope.INSTANCE;
		String qualifier = "org.eclipse.ui.tests.preferences";
		ScopedPreferenceStore store = new ScopedPreferenceStore(context,
				qualifier);
		String key = "key1";
		String value = "value1";

		// nothing there
		assertFalse("0.1", store.needsSaving());
		assertFalse("0.2", store.contains(key));
		assertEquals("0.3", DEFAULT_DEFAULT_STRING, store.getString(key));

		// set the value
		store.setValue(key, value);
		assertTrue("1.0", store.needsSaving());
		assertTrue("1.1", store.contains(key));
		assertEquals("1.2", value, store.getString(key));

		// flush
		store.save();

		// do the test
		assertFalse("3.0", store.needsSaving());

		// change the node outside of the scoped store
		String key2 = "key2";
		String value2 = "value2";
		IEclipsePreferences node = context.getNode(qualifier);
		node.put(key2, value2);
		assertEquals("4.0", value2, node.get(key2, null));
		assertFalse("4.1", store.needsSaving());
	}

	@Test
	public void testRestoreDefaults() {
		IScopeContext context = InstanceScope.INSTANCE;
		String qualifier = "org.eclipse.ui.tests.preferences#testRestoreDefaults";
		ScopedPreferenceStore store = new ScopedPreferenceStore(context, qualifier);
		final String key = "key";
		final String value = "value";

		// setup and initial assertions
		assertFalse("0.1", store.contains(key));
		assertEquals("0.2", DEFAULT_DEFAULT_STRING, store.getString(key));

		// set the value
		store.setValue(key, value);
		assertTrue("1.0", store.contains(key));
		assertEquals("1.1", value, store.getString(key));

		final boolean[] found = new boolean[1];
		IPropertyChangeListener listener= event -> {
			if (key.equals(event.getProperty()) && value.equals(event.getOldValue())) {
				found[0] = true;
			}
		};
		store.addPropertyChangeListener(listener);

		// restore the default
		store.setToDefault(key);
		assertFalse("2.0", store.contains(key));
		assertEquals("2.1", DEFAULT_DEFAULT_STRING, store.getString(key));

		// check it
		assertTrue("3.0", found[0]);
}

}
