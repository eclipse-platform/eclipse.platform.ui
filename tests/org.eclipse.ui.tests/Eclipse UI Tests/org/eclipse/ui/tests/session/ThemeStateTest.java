/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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

package org.eclipse.ui.tests.session;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests various persistent theme properties.
 *
 * @since 3.4
 */
public class ThemeStateTest extends TestCase {
	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.ThemeStateTest");
		ts.addTest(new ThemeStateTest("testBadPreference"));
		return ts;
	}

	public ThemeStateTest(final String name) {
		super(name);
	}

	/**
	 * Tests to ensure that the workbench still comes up if there's a bad theme
	 * ID in the preference store.
	 */
	public void testBadPreference() {
		String themeId = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme().getId();
		assertNotNull(themeId);
		assertEquals(IThemeManager.DEFAULT_THEME, themeId);
	}
}
