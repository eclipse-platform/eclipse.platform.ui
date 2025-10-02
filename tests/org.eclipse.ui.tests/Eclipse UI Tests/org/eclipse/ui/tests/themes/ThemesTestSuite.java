/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.tests.themes;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @since 3.0
 */
@Suite
@SelectClasses({
	 ThemeAPITest.class,
	 JFaceThemeTest.class,
	 WorkbenchThemeChangedHandlerTest.class,
	 ThemeRegistryModifiedHandlerTest.class,
	 StylingPreferencesHandlerTest.class
})
public class ThemesTestSuite {

}
