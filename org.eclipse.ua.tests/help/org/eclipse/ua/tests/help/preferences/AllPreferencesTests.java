/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.preferences;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests help preferences functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ProductPreferencesTest.class,
	HelpDataTest.class,
	CssPreferences.class,
	BookmarksTest.class
})
public class AllPreferencesTests {
}
