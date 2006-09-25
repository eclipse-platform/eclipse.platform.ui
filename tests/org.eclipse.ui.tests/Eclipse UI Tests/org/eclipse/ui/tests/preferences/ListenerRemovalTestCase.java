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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class ListenerRemovalTestCase extends UITestCase {

	class TestPropertyListener implements IPropertyChangeListener {
		boolean listened = false;

		/**
		 * 
		 */
		public TestPropertyListener() {
			super();
		}

		public void propertyChange(PropertyChangeEvent event) {
			listened = true;
		}
	}

	/**
	 * @param testName
	 */
	public ListenerRemovalTestCase(String testName) {
		super(testName);
	}

	public void testRemoveLastListener() {

		TestPropertyListener testListener = new TestPropertyListener();
		IPreferenceStore preferenceStore = TestPlugin.getDefault()
				.getPreferenceStore();

		// Check it is found when added
		preferenceStore.addPropertyChangeListener(testListener);
		testListener.listened = false;
		preferenceStore.setValue(TestPreferenceInitializer.TEST_LISTENER_KEY,
				TestPreferenceInitializer.TEST_SET_VALUE);
		assertTrue("Listener not hit on set value", testListener.listened);
		testListener.listened = false;

		// Check it is found when set to default
		preferenceStore
				.setToDefault(TestPreferenceInitializer.TEST_LISTENER_KEY);
		assertTrue("Listener not hit on default value", testListener.listened);
		testListener.listened = false;

		// Check that the listener is removed
		preferenceStore.removePropertyChangeListener(testListener);
		preferenceStore.setValue(TestPreferenceInitializer.TEST_LISTENER_KEY,
				TestPreferenceInitializer.TEST_SET_VALUE);
		assertFalse("Listener hit when removed", testListener.listened);
		
//		 Check it is found when set to default
		preferenceStore
				.setToDefault(TestPreferenceInitializer.TEST_LISTENER_KEY);

		// Check that you can add it again
		preferenceStore.addPropertyChangeListener(testListener);
		testListener.listened = false;
		preferenceStore.setValue(TestPreferenceInitializer.TEST_LISTENER_KEY,
				TestPreferenceInitializer.TEST_SET_VALUE);
		assertTrue("Listener not hit on second set value",
				testListener.listened);
		testListener.listened = false;

		preferenceStore.removePropertyChangeListener(testListener);

	}

}
