/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		DialogsTestCase.class, PreferencesTestCase.class, ToolbarBrowserTestCase.class, WebBrowserUtilTestCase.class
})
public class AllTests {
}