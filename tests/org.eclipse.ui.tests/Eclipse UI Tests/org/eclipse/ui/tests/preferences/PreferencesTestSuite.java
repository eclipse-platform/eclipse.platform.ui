/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import org.eclipse.ui.tests.propertyPages.PropertyPageEnablementTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for preferences.
 */
public class PreferencesTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new PreferencesTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public PreferencesTestSuite() {
		addTest(new TestSuite(FontPreferenceTestCase.class));
		addTest(new TestSuite(DeprecatedFontPreferenceTestCase.class));
		addTest(new TestSuite(ScopedPreferenceStoreTestCase.class));
		addTest(new TestSuite(WorkingCopyPreferencesTestCase.class));
		addTest(new TestSuite(PropertyPageEnablementTest.class));
		addTest(new TestSuite(ListenerRemovalTestCase.class));
		addTest(new TestSuite(PreferencesDialogTest.class));
	}
}
