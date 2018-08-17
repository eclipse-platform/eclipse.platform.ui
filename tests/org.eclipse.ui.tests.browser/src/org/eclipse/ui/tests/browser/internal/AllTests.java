/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Tomasz Zarna (Tasktop Technologies) - [429546] External Browser with parameters
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		ExistenceTestCase.class, InternalBrowserViewTestCase.class, InternalBrowserEditorTestCase.class,
		// ExternalBrowserTestCase.class);
		DialogsTestCase.class, PreferencesTestCase.class, TestInput.class, ToolbarBrowserTestCase.class,
		WebBrowserUtilTestCase.class
})
public class AllTests {
}