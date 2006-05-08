/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import java.io.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class ScopedPreferenceStoreTestCase extends UITestCase {

	final String DEFAULT_DEFAULT_STRING = "";

	public ScopedPreferenceStoreTestCase(String name) {
		super(name);
	}

	public void testNeedsSaving() {
		IScopeContext context = new InstanceScope();
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
		try {
			store.save();
		} catch (IOException e) {
			fail("2.99", e);
		}

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
	
	public void testRestoreDefaults() {
		IScopeContext context = new InstanceScope();
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
		IPropertyChangeListener listener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (key.equals(event.getProperty()) && value.equals(event.getOldValue()))
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
