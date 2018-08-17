/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import org.eclipse.ui.tests.propertyPages.PropertyPageEnablementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for preferences.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FontPreferenceTestCase.class,
	DeprecatedFontPreferenceTestCase.class,
	ScopedPreferenceStoreTestCase.class,
	WorkingCopyPreferencesTestCase.class,
	PropertyPageEnablementTest.class,
	ListenerRemovalTestCase.class,
	PreferencesDialogTest.class,
	ZoomAndPreferencesFontTest.class,
	DialogSettingsCustomizationTest.class})
public class PreferencesTestSuite {
}
